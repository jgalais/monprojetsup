package fr.gouv.monprojetsup.data;

import fr.gouv.monprojetsup.data.model.cities.CitiesBack;
import fr.gouv.monprojetsup.data.model.eds.Attendus;
import fr.gouv.monprojetsup.data.model.eds.EDSAggAnalysis;
import fr.gouv.monprojetsup.data.model.eds.EDSAnalysis;
import fr.gouv.monprojetsup.data.model.formations.Formation;
import fr.gouv.monprojetsup.data.model.specialites.Specialites;
import fr.gouv.monprojetsup.data.model.specialites.SpecialitesLoader;
import fr.gouv.monprojetsup.data.model.stats.PsupStatistiques;
import fr.gouv.monprojetsup.data.model.stats.StatsContainers;
import fr.gouv.monprojetsup.data.update.BackEndData;
import fr.gouv.monprojetsup.data.update.onisep.DomainePro;
import fr.gouv.monprojetsup.data.update.onisep.OnisepData;
import fr.gouv.monprojetsup.data.update.psup.PsupData;
import fr.gouv.monprojetsup.data.tools.Serialisation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static fr.gouv.monprojetsup.data.Constants.FILIERE_PREFIX;
import static fr.gouv.monprojetsup.data.Constants.FORMATION_PREFIX;
import static fr.parcoursup.carte.algos.Filiere.LAS_CONSTANT;


@Slf4j
public class ServerData {


    /***************************************************************************
     ******************* DATAS ***********************************
     ****************************************************************************/

    public static PsupData backPsupData;
    public static PsupStatistiques statistiques;
    public static OnisepData onisepData;

    public static Map<String, Set<String>> liensSecteursMetiers;
    public static Map<DomainePro, Set<String>> liensDomainesMetiers;


    public static final Map<String, Set<String>> reverseFlGroups = new HashMap<>();

    public static Specialites specialites;
    public static Map<String, Integer> codesSpecialites = new HashMap<>();

    //regroupement des filieres
    public static Map<String, String> flGroups = null;

    protected static final Map<String, List<Formation>> filToFormations = new HashMap<>();

    public static CitiesBack cities = null;
    /*
    ************************************************************************
    **************************** LOADERS ***********************************
    ************************************************************************
     */

    private static boolean dataLoaded = false;


    /**
     * Load data into server
     * @throws IOException unlucky
     */
    public static synchronized void load() throws IOException {

        if(dataLoaded) return;

        log.info("Loading server data...");

        loadBackEndData();

        flGroups = new HashMap<>(backPsupData.getCorrespondances());
        flGroups.forEach((s, s2) -> reverseFlGroups.computeIfAbsent(s2, z -> new HashSet<>()).add(s));

        ServerData.specialites = SpecialitesLoader.load();
        ServerData.specialites.specialites().forEach((iMtCod, s) -> ServerData.codesSpecialites.put(s, iMtCod));

        ServerData.statistiques = Serialisation.fromZippedJson(DataSources.getSourceDataFilePath(DataSources.STATS_BACK_SRC_FILENAME), PsupStatistiques.class);
        ServerData.statistiques.removeSmallPopulations();

        ServerData.statistiques =
                Serialisation.fromZippedJson(
                        DataSources.getSourceDataFilePath(DataSources.STATS_BACK_SRC_FILENAME),
                        PsupStatistiques.class
                );
        /* can be deleted afte rnext data update */
        ServerData.statistiques.rebuildMiddle50();
        ServerData.statistiques.createGroupAdmisStatistique(reverseFlGroups);
        ServerData.statistiques.createGroupAdmisStatistique(getLasToGtaMapping());

        ServerData.updateLabelsForDebug();

        liensSecteursMetiers  = OnisepData.getSecteursVersMetiers(
                onisepData.fichesMetiers(),
                onisepData.formations().getFormationsDuSup()
        );
        liensDomainesMetiers  = OnisepData.getDomainesVersMetiers(onisepData.metiers());

        dataLoaded = true;
    }

    private static Map<String, Set<String>> getLasToGtaMapping() {
        //fl1002033
        Set<String> lasCodes = ServerData.statistiques.getLASCorrespondance().lasToGeneric().keySet();
        return
                lasCodes
                        .stream()
                        .collect(Collectors.toMap(
                                        las -> las,
                                        las -> filToFormations.getOrDefault(las, List.of())
                                                .stream()
                                                .map(f ->  FORMATION_PREFIX + f.gTaCod)
                                                .collect(Collectors.toSet())
                                )
                        );
    }

    public static String getGroupOfFiliere(String fl) {
        return flGroups.getOrDefault(fl,fl);
    }
    public static Set<String> getFilieresOfGroup(String fl) {
        return  reverseFlGroups.getOrDefault(fl, Set.of(fl));
    }

    private static void updateLabelsForDebug() {
        statistiques.updateLabels(onisepData, backPsupData, statistiques.getLASCorrespondance().lasToGeneric());
        Map<String, String> suffixes =
                reverseFlGroups.entrySet().stream()
                        .filter(e -> !e.getValue().isEmpty())
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        s -> s.getValue().toString())
                                );
        suffixes.forEach((key, suffix) -> statistiques.labels.put(key, statistiques.labels.get(key) + " groupe " + suffix) );
        try {
            Serialisation.toJsonFile("labelsDebug.json", statistiques.labels, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static boolean isFiliere(@NotNull String key) {
        return key.startsWith(FILIERE_PREFIX)
                || key.startsWith(Constants.TYPE_FORMATION_PREFIX);
    }
    public static boolean isMetier(@NotNull String key) {
        return key.startsWith(Constants.MET_PREFIX);
    }

    public static boolean isTheme(@NotNull String key) {
        return key.startsWith(Constants.THEME_PREFIX);
    }

    public static boolean isInteret(@NotNull String key) {
        return key.startsWith(Constants.CENTRE_INTERETS_ONISEP)
                || key.startsWith(Constants.CENTRE_INTERETS_ROME);
    }

    static void loadBackEndData() throws IOException {

        BackEndData backendData = Serialisation.fromZippedJson(DataSources.getBackDataFilePath(), BackEndData.class);

        ServerData.onisepData = backendData.onisepData();

        backPsupData = backendData.psupData();
        backPsupData.cleanup();//should be useless but it does not harm...

        backPsupData.formations().formations.values()
                .forEach(f -> {
                    int gFlCod = (f.isLAS() && f.gFlCod < LAS_CONSTANT) ? f.gFlCod + LAS_CONSTANT: f.gFlCod;
                    filToFormations
                            .computeIfAbsent(Constants.gFlCodToFrontId(gFlCod), z -> new ArrayList<>())
                            .add(f);
                });

        ServerData.cities = new CitiesBack(backendData.cities().cities());

    }

    /*
    ***********************************************
    ************* STATS HELPERS *******************
    ************************************************/

    /**
     * utilisé pour l'envoi des stats aux profs
     * @param bac le bac
     * @param groups les groupes
     * @return les stats
     */
    public static Map<String, StatsContainers.DetailFiliere> getGroupStats(@Nullable String bac, @Nullable Collection<String> groups) {
        if(groups == null) return Collections.emptyMap();
        if(bac == null) bac = PsupStatistiques.TOUS_BACS_CODE;
        @Nullable String finalBac = bac;
        return groups.stream().collect(Collectors.toMap(
                g -> g,
                g -> getDetailedGroupStats(finalBac, g)
        ));
    }

    /**
    Utilisé pour aire des stats
     */



    /**
     * utilisé pour l'envoi des stats aux élèves
     *
     * @param bac le bac
     * @param g le groupe
     * @return les détails
     */
    public static @NotNull StatsContainers.DetailFiliere getSimpleGroupStats(@Nullable String bac, String g) {
        if(bac == null) bac = PsupStatistiques.TOUS_BACS_CODE;
        return getDetailedGroupStats(bac, g, false);
    }

    /**
     * utilisé pour l'envoi des stats aux profs
     * @param bac le bac
     * @param g le groupe
     * @return les stats
     */
    private static StatsContainers.DetailFiliere getDetailedGroupStats(@NotNull String bac, String g) {
        return getDetailedGroupStats(bac, g, true);
    }
    private static StatsContainers.DetailFiliere getDetailedGroupStats(@NotNull String bac, String g, boolean includeProfDetails) {
        StatsContainers.SimpleStatGroupParBac statFil
                = new StatsContainers.SimpleStatGroupParBac(
                        statistiques.getGroupStats(
                                g,
                                bac,
                                !includeProfDetails
                        )
        );
        //statFil.stats().entrySet().removeIf(e -> e.getValue().nbAdmis() == null);

        if(includeProfDetails) {
            Map<String, StatsContainers.DetailFormation> statsFormations = new HashMap<>();
            try {
                List<Formation> fors = getFormationsFromFil(g);
                fors.forEach(f -> {
                    try {
                        String fr = FORMATION_PREFIX + f.gTaCod;
                        StatsContainers.SimpleStatGroupParBac statFor = new StatsContainers.SimpleStatGroupParBac(
                                statistiques.getGroupStats(
                                        fr,
                                        bac,
                                        !includeProfDetails)
                        );
                        statFor.stats().entrySet().removeIf(e -> e.getValue().statsScol().isEmpty());
                        statsFormations.put(fr, new StatsContainers.DetailFormation(
                                f.libelle,
                                fr,
                                statFor
                        ));
                    } catch (Exception ignored) {
                        //ignored
                    }
                });
            } catch (Exception ignored) {
                //ignore
            }
            return new StatsContainers.DetailFiliere(
                    g,
                    statFil,
                    statsFormations
            );
        } else {
            return new StatsContainers.DetailFiliere(
                    g,
                    statFil,
                    null
            );
        }
    }

    public static List<Formation> getFormationsFromFil(String fl) {
        return filToFormations
                .getOrDefault(fl, Collections.emptyList());
    }

    private ServerData() {

    }


    public static EDSAggAnalysis getEDS(PsupData backPsupData, PsupStatistiques statistiques, Specialites specialites, boolean specifiques, boolean prettyPrint) {

        EDSAggAnalysis analyses = new EDSAggAnalysis();

        backPsupData.filActives().forEach(gFlCod -> {
            String key = Constants.gFlCodToFrontId(gFlCod);
            String gFlLib = statistiques.nomsFilieres.get(key);
            if(gFlLib != null) {
                String ppkey = prettyPrint ? ServerData.getDebugLabel(key) : key;
                //les nbAdmisEDS
                EDSAnalysis analysis = analyses.analyses().computeIfAbsent(ppkey, z -> new EDSAnalysis(
                        gFlCod,
                        gFlLib,
                        backPsupData.getAttendus(gFlCod),
                        backPsupData.getRecoPremGeneriques(gFlCod),
                        backPsupData.getRecoTermGeneriques(gFlCod)
                ));
                Map<Integer, Integer> statsEds = statistiques.getStatsSpec(key);
                if(statsEds != null) {
                    statsEds.forEach((iMtCod, pct) -> {
                        String name = specialites.specialites().get(iMtCod);
                        if(name != null) {
                            analysis.nbAdmisEDS().put(pct, name);
                        }
                    });
                }
                Map<Integer, Integer> statsEds2 = statistiques.getStatsSpecCandidats(key);
                if(statsEds2 != null) {
                    statsEds2.forEach((iMtCod, pct) -> {
                        String name = specialites.specialites().get(iMtCod);
                        if(name != null) {
                            analysis.nbCandidatsEDS().put(pct, name);
                        }
                    });
                }

                if(specifiques) {
                    //les messages
                    //on aggrege tous les codes jurys de la filiere
                    List<Integer> gTaCods = getFormationsFromFil(key).stream().map(f -> f.gTaCod).toList();
                    Set<String> juryCodes = backPsupData.getJuryCodesFromGTaCods(gTaCods);
                    analysis.recosScoPremSpecifiques().putAll(backPsupData.getRecoScoPremiere(juryCodes));
                    analysis.recosScoTermSpecifiques().putAll(backPsupData.getRecoScoTerminale(juryCodes));
                }
            }
        });
        return analyses;
    }

    public static Map<String, Attendus> getEDSSimple(PsupData psupData, PsupStatistiques data, Specialites specs, boolean specifiques) {
        EDSAggAnalysis eds = getEDS(psupData, data, specs, specifiques, false);
        return eds.getFrontData();
    }


    public static String getLabel(String key) {
        return ServerData.statistiques.labels.getOrDefault(
                key,
                ServerData.statistiques.nomsFilieres.get(key)
        );
    }

    public static String getDebugLabel(String key) {
        return getLabel(key) + " (" + key  + ")";
    }

    public static String getLabel(String key, String defaultValue) {
        return ServerData.statistiques.labels.getOrDefault(key, defaultValue);
    }

    private static final LevenshteinDistance levenAlgo = new LevenshteinDistance(10);
    public static @Nullable String getFlCodFromLabel(String expectation) {
        return
                statistiques.labels.entrySet().stream()
                        .map(e -> Pair.of(e.getKey(), levenAlgo.apply(e.getValue(), expectation)))
                        .filter(e -> e.getRight() >= 0)
                        .min(Comparator.comparingInt(Pair::getRight))
                        .map(Pair::getLeft)
                        .orElse(null);
    }

}

