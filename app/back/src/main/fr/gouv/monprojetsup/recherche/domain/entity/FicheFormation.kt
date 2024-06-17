package fr.gouv.monprojetsup.recherche.domain.entity

sealed class FicheFormation(
    open val formation: FormationDetaillee,
    open val communes: List<String>,
    open val moyenneGeneraleDesAdmis: MoyenneGeneraleDesAdmis,
    open val tauxAffinite: Int?,
) {
    data class FicheFormationPourProfil(
        override val formation: FormationDetaillee,
        override val tauxAffinite: Int?,
        override val moyenneGeneraleDesAdmis: MoyenneGeneraleDesAdmis,
        val explications: ExplicationsSuggestion?,
        val formationsSimilaires: List<Formation>?,
        val interets: List<Interet>?,
        val domaines: List<Domaine>?,
        val explicationAutoEvaluationMoyenne: ExplicationAutoEvaluationMoyenne?,
        val explicationTypeBaccalaureat: ExplicationTypeBaccalaureat?,
        val metiersTriesParAffinites: List<MetierDetaille>,
        val communesTrieesParAffinites: List<String>,
    ) : FicheFormation(
            formation = formation.copy(metiers = metiersTriesParAffinites),
            tauxAffinite = tauxAffinite,
            communes = communesTrieesParAffinites,
            moyenneGeneraleDesAdmis = moyenneGeneraleDesAdmis,
        )

    data class FicheFormationSansProfil(
        override val formation: FormationDetaillee,
        override val communes: List<String>,
        override val moyenneGeneraleDesAdmis: MoyenneGeneraleDesAdmis,
    ) : FicheFormation(
            formation = formation,
            tauxAffinite = null,
            communes = communes,
            moyenneGeneraleDesAdmis = moyenneGeneraleDesAdmis,
        )

    data class Interet(
        val id: String,
        val nom: String,
    )

    data class Domaine(
        val id: String,
        val nom: String,
    )

    data class ExplicationAutoEvaluationMoyenne(
        val baccalaureat: Baccalaureat,
        val moyenneAutoEvalue: Float,
        val basIntervalleNotes: Float,
        val hautIntervalleNotes: Float,
    )

    data class ExplicationTypeBaccalaureat(
        val baccalaureat: Baccalaureat,
        val pourcentage: Int,
    )

    data class Baccalaureat(
        val id: String,
        val idExterne: String,
        val nom: String,
    )

    data class MoyenneGeneraleDesAdmis(
        val idBaccalaureat: String?,
        val nomBaccalaureat: String?,
        val centilles: List<Centille>,
    ) {
        data class Centille(
            val centille: Int,
            val note: Float,
        )
    }
}
