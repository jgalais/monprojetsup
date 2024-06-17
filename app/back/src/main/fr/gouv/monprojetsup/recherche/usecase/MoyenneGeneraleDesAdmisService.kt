package fr.gouv.monprojetsup.recherche.usecase

import fr.gouv.monprojetsup.commun.Constantes.NOTE_MAXIMAL
import fr.gouv.monprojetsup.commun.Constantes.TAILLE_ECHELLON_NOTES
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.Baccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.MoyenneGeneraleDesAdmis
import fr.gouv.monprojetsup.recherche.domain.port.MoyenneGeneraleAdmisRepository
import org.springframework.stereotype.Service

@Service
class MoyenneGeneraleDesAdmisService(
    val moyenneGeneraleAdmisRepository: MoyenneGeneraleAdmisRepository,
) {
    fun recupererMoyenneGeneraleDesAdmisDUneFormation(
        baccalaureat: Baccalaureat?,
        idFormation: String,
    ): MoyenneGeneraleDesAdmis {
        val frequencesCumulees = moyenneGeneraleAdmisRepository.recupererFrequencesCumuleesDeToutLesBacs(idFormation)
        return if (baccalaureat?.id != null && frequencesCumulees.containsKey(baccalaureat.id)) {
            MoyenneGeneraleDesAdmis(
                idBaccalaureat = baccalaureat.id,
                nomBaccalaureat = baccalaureat.nom,
                centilles = frequencesCumulees[baccalaureat.id]?.let { transformerFrequencesCumuleesEnCentilles(it) } ?: emptyList(),
            )
        } else {
            MoyenneGeneraleDesAdmis(
                idBaccalaureat = null,
                nomBaccalaureat = null,
                centilles =
                    transformerFrequencesCumuleesEnCentilles(
                        calculerLesFrequencesCumuleesDeTousLesBaccalaureatsConfondus(frequencesCumulees),
                    ),
            )
        }
    }

    private fun calculerLesFrequencesCumuleesDeTousLesBaccalaureatsConfondus(
        listeDesFrequencesCumulees: Map<String, List<Int>>,
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

    private fun transformerFrequencesCumuleesEnCentilles(frequencesCumulees: List<Int>): List<MoyenneGeneraleDesAdmis.Centille> {
        return frequencesCumulees.takeUnless { it.isEmpty() }?.last()?.let { total ->
            val centilles = listOf(CENTILLE_5EME, CENTILLE_25EME, CENTILLE_75EME, CENTILLE_95EME)
            centilles.map { centille ->
                val rang = (centille.toFloat() / CENTILLE_100EME.toFloat()) * total
                val index = frequencesCumulees.indexOfFirst { it >= rang }
                MoyenneGeneraleDesAdmis.Centille(
                    centille = centille,
                    note = index * TAILLE_ECHELLON_NOTES,
                )
            }
        } ?: emptyList()
    }

    companion object {
        private const val CENTILLE_5EME = 5
        private const val CENTILLE_25EME = 25
        private const val CENTILLE_75EME = 75
        private const val CENTILLE_95EME = 95
        private const val CENTILLE_100EME = 100
    }
}
