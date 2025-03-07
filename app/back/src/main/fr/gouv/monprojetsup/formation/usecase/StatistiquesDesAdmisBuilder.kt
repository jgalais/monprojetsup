package fr.gouv.monprojetsup.formation.usecase

import fr.gouv.monprojetsup.commun.Constantes.NOTE_MAXIMAL
import fr.gouv.monprojetsup.commun.Constantes.TAILLE_ECHELLON_NOTES
import fr.gouv.monprojetsup.formation.domain.entity.StatistiquesDesAdmis
import fr.gouv.monprojetsup.formation.domain.entity.StatistiquesDesAdmis.MoyenneGeneraleDesAdmis
import fr.gouv.monprojetsup.formation.domain.entity.StatistiquesDesAdmis.RepartitionAdmis
import fr.gouv.monprojetsup.formation.domain.entity.StatistiquesDesAdmis.RepartitionAdmis.TotalAdmisPourUnBaccalaureat
import fr.gouv.monprojetsup.referentiel.domain.entity.Baccalaureat
import fr.gouv.monprojetsup.referentiel.domain.entity.ChoixNiveau
import org.springframework.stereotype.Service

@Service
class StatistiquesDesAdmisBuilder {
    fun creerStatistiquesDesAdmis(
        frequencesCumulees: Map<Baccalaureat, List<Int>>,
        idBaccalaureat: String?,
        classe: ChoixNiveau?,
    ) = StatistiquesDesAdmis(
        repartitionAdmis = recupererRepartitionAdmisDUneFormation(frequencesCumulees),
        moyenneGeneraleDesAdmis = recupererMoyenneGeneraleDesAdmisDUneFormation(idBaccalaureat, frequencesCumulees, classe),
    )

    private fun recupererRepartitionAdmisDUneFormation(frequencesCumulees: Map<Baccalaureat, List<Int>>): RepartitionAdmis {
        var totalAdmis = 0
        val repartitionsParBaccalaureats =
            frequencesCumulees.map {
                val totalAdmisDuBaccalaureat = it.value.lastOrNull() ?: 0
                totalAdmis += totalAdmisDuBaccalaureat
                TotalAdmisPourUnBaccalaureat(
                    baccalaureat = it.key,
                    nombreAdmis = totalAdmisDuBaccalaureat,
                )
            }
        return RepartitionAdmis(
            total = totalAdmis,
            parBaccalaureat = repartitionsParBaccalaureats,
        )
    }

    private fun recupererMoyenneGeneraleDesAdmisDUneFormation(
        idBaccalaureat: String?,
        frequencesCumulees: Map<Baccalaureat, List<Int>>,
        classe: ChoixNiveau?,
    ): MoyenneGeneraleDesAdmis? {
        return when (classe) {
            ChoixNiveau.SECONDE, ChoixNiveau.NON_RENSEIGNE, null -> null
            ChoixNiveau.PREMIERE, ChoixNiveau.TERMINALE -> recupererStatistiquesAdmisDUneFormation(idBaccalaureat, frequencesCumulees)
        }
    }

    private fun recupererStatistiquesAdmisDUneFormation(
        idBaccalaureat: String?,
        frequencesCumulees: Map<Baccalaureat, List<Int>>,
    ): MoyenneGeneraleDesAdmis? {
        return if (lesDonnesSontVides(frequencesCumulees)) {
            null
        } else {
            val frequenceBaccalaureat =
                idBaccalaureat?.let { idBaccaLaureat ->
                    try {
                        frequencesCumulees.firstNotNullOf { if (it.key.id == idBaccaLaureat) it else null }
                    } catch (e: Exception) {
                        null
                    }
                }
            return if (frequenceBaccalaureat != null && assezDAdmisPourLeBaccalaureat(frequenceBaccalaureat)) {
                MoyenneGeneraleDesAdmis(
                    baccalaureat = frequenceBaccalaureat.key,
                    centiles = transformerFrequencesCumuleesEnCentiles(frequenceBaccalaureat.value),
                )
            } else {
                creerMoyenneGeneraleAdmisPourToutBacConfondu(frequencesCumulees)
            }
        }
    }

    private fun creerMoyenneGeneraleAdmisPourToutBacConfondu(frequencesCumulees: Map<Baccalaureat, List<Int>>) =
        MoyenneGeneraleDesAdmis(
            baccalaureat = null,
            centiles =
                transformerFrequencesCumuleesEnCentiles(
                    frequencesCumulees = calculerLesFrequencesCumuleesDeTousLesBaccalaureatsConfondus(frequencesCumulees),
                ),
        )

    private fun assezDAdmisPourLeBaccalaureat(frequencesCumulees: Map.Entry<Baccalaureat, List<Int>>) =
        frequencesCumulees.value.last() > MINIMUM_ADMIS

    private fun lesDonnesSontVides(frequencesCumulees: Map<Baccalaureat, List<Int>>) =
        frequencesCumulees.isEmpty() || frequencesCumulees.values.flatten().isEmpty()

    private fun calculerLesFrequencesCumuleesDeTousLesBaccalaureatsConfondus(
        listeDesFrequencesCumulees: Map<Baccalaureat, List<Int>>,
    ): List<Int> {
        val tailleTableauNotes = (NOTE_MAXIMAL / TAILLE_ECHELLON_NOTES).toInt()
        var frequencesCumulees = List(tailleTableauNotes) { 0 }
        listeDesFrequencesCumulees.forEach {
            frequencesCumulees =
                frequencesCumulees.zip(it.value) { valeurSommeFrequence, valeurFrequence ->
                    valeurSommeFrequence + valeurFrequence
                }
        }
        return frequencesCumulees
    }

    private fun transformerFrequencesCumuleesEnCentiles(frequencesCumulees: List<Int>): List<MoyenneGeneraleDesAdmis.Centile> {
        val totalEleves = frequencesCumulees.last()
        val centiles = listOf(CENTILLE_5EME, CENTILLE_25EME, CENTILLE_75EME, CENTILLE_95EME)
        val moitieDesCentiles = centiles.size / 2
        return centiles.mapIndexed { index: Int, centile: Int ->
            val indexDeLaFrequenceDuCentile =
                frequencesCumulees.indexOfFirst {
                    it >= (centile * totalEleves / CENTILLE_100EME.toFloat())
                }
            MoyenneGeneraleDesAdmis.Centile(
                centile = centile,
                note = recupererLaNoteSelonLeCoteDeLIntervalle(index, moitieDesCentiles, indexDeLaFrequenceDuCentile),
            )
        }
    }

    private fun recupererLaNoteSelonLeCoteDeLIntervalle(
        indexDuCentileEnCours: Int,
        moitieDesCentiles: Int,
        indexDeLaFrequenceDuCentile: Int,
    ): Float {
        val note =
            if (indexDuCentileEnCours < moitieDesCentiles) {
                indexDeLaFrequenceDuCentile * TAILLE_ECHELLON_NOTES
            } else {
                indexDeLaFrequenceDuCentile * TAILLE_ECHELLON_NOTES + TAILLE_ECHELLON_NOTES
            }
        return note
    }

    companion object {
        private const val CENTILLE_5EME = 5
        private const val CENTILLE_25EME = 25
        private const val CENTILLE_75EME = 75
        private const val CENTILLE_95EME = 95
        private const val CENTILLE_100EME = 100

        private const val MINIMUM_ADMIS = 30
    }
}
