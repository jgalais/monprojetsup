package fr.gouv.monprojetsup.eleve.domain.entity

import fr.gouv.monprojetsup.referentiel.domain.entity.ChoixAlternance
import fr.gouv.monprojetsup.referentiel.domain.entity.ChoixDureeEtudesPrevue
import fr.gouv.monprojetsup.referentiel.domain.entity.ChoixNiveau
import fr.gouv.monprojetsup.referentiel.domain.entity.SituationAvanceeProjetSup

data class ProfilEleve(
    val id: String,
    val situation: SituationAvanceeProjetSup,
    val classe: ChoixNiveau,
    val bac: String?,
    val specialites: List<String>?,
    val domainesInterets: List<String>?,
    val centresInterets: List<String>?,
    val metiersChoisis: List<String>?,
    val dureeEtudesPrevue: ChoixDureeEtudesPrevue,
    val alternance: ChoixAlternance,
    val communesPreferees: List<Commune>?,
    val formationsChoisies: List<String>?,
    val moyenneGenerale: Float?,
)
