package fr.gouv.monprojetsup.data.model.stats;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static fr.gouv.monprojetsup.data.model.stats.PsupStatistiques.TOUS_GROUPES_CODE;

public record StatistiquesAdmisParGroupe(
        //indexé par groupes
        Map<String, StatistiquesAdmisParBac> parGroupe
) {
    public StatistiquesAdmisParGroupe() { this(new HashMap<>()); }

    public void clear() {
        parGroupe.clear();
    }

    public void minimize() {
        parGroupe.keySet().removeIf(k -> !k.equals(TOUS_GROUPES_CODE));
        parGroupe.values().forEach(StatistiquesAdmisParBac::minimize);
        parGroupe.values().removeIf(v -> v.parBac().isEmpty());
    }

    public @NotNull String toString() {
        return String.valueOf(parGroupe.size());
    }
}
