package fr.gouv.monprojetsup.recherche.application.dto

import fr.gouv.monprojetsup.recherche.domain.entity.Domaine
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationAutoEvaluationMoyenne
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationTypeBaccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.ExplicationsSuggestion
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation
import fr.gouv.monprojetsup.recherche.domain.entity.Formation
import fr.gouv.monprojetsup.recherche.domain.entity.InteretSousCategorie
import fr.gouv.monprojetsup.recherche.domain.entity.MetierDetaille

data class RecupererFormationReponseDTO(
    val formation: FormationDetailleDTO,
    val explications: ExplicationsDTO?,
) {
    data class FormationDetailleDTO(
        val id: String,
        val nom: String,
        val idsFormationsAssociees: List<String>,
        val descriptifFormation: String?,
        val descriptifDiplome: String?,
        val descriptifConseils: String?,
        val descriptifAttendus: String?,
        val moyenneGeneraleDesAdmis: MoyenneGeneraleDesAdmisDTO?,
        val criteresAnalyseCandidature: CriteresAnalyseCandidatureDTO?,
        val repartitionAdmisAnneePrecedente: RepartitionAdmisAnneePrecedenteDTO?,
        val liens: List<LiensDTO>,
        val villes: List<String>,
        val metiers: List<MetierDetailleDTO>,
        val tauxAffinite: Int?,
    ) {
        companion object {
            fun fromFicheFormation(ficheFormation: FicheFormation): FormationDetailleDTO {
                val formation = ficheFormation.formation
                return FormationDetailleDTO(
                    id = formation.id,
                    nom = formation.nom,
                    idsFormationsAssociees = formation.formationsAssociees ?: emptyList(),
                    descriptifFormation = formation.descriptifGeneral,
                    descriptifDiplome = formation.descriptifDiplome,
                    descriptifAttendus = formation.descriptifAttendus,
                    moyenneGeneraleDesAdmis =
                        MoyenneGeneraleDesAdmisDTO(
                            idBaccalaureat = "",
                            nomBaccalaureat = "",
                            centilles = listOf(),
                        ),
                    // TODO #64
                    descriptifConseils = formation.descriptifConseils,
                    liens = emptyList(), // TODO #66
                    villes =
                        when (ficheFormation) {
                            is FicheFormation.FicheFormationPourProfil -> ficheFormation.communesTrieesParAffinites
                            is FicheFormation.FicheFormationSansProfil -> ficheFormation.communes
                        },
                    metiers =
                        when (ficheFormation) {
                            is FicheFormation.FicheFormationPourProfil -> ficheFormation.metiersTriesParAffinites
                            is FicheFormation.FicheFormationSansProfil -> ficheFormation.formation.metiers
                        }.map { metier ->
                            MetierDetailleDTO.fromMetierDetaille(metier)
                        },
                    tauxAffinite = ficheFormation.tauxAffinite,
                    repartitionAdmisAnneePrecedente =
                        RepartitionAdmisAnneePrecedenteDTO(
                            total = 12,
                            parBaccalaureat = listOf(),
                        ),
                    // TODO #72
                    criteresAnalyseCandidature =
                        CriteresAnalyseCandidatureDTO(
                            nom = "",
                            pourcentage = 12,
                        ),
                    // TODO #71
                )
            }
        }

        data class LiensDTO(
            val nom: String,
            val url: String,
        )

        data class CriteresAnalyseCandidatureDTO(
            val nom: String,
            val pourcentage: Int,
        )

        data class RepartitionAdmisAnneePrecedenteDTO(
            val total: Int,
            val parBaccalaureat: List<AdmisBaccalaureat>,
        ) {
            data class AdmisBaccalaureat(
                val idBaccalaureat: String,
                val nomBaccalaureat: String,
                val nombreAdmis: Int,
            )
        }

        data class MetierDetailleDTO(
            val id: String,
            val nom: String,
            val descriptif: String?,
            val liens: List<LiensDTO>,
        ) {
            companion object {
                fun fromMetierDetaille(metier: MetierDetaille) =
                    MetierDetailleDTO(
                        id = metier.id,
                        nom = metier.nom,
                        descriptif = metier.descriptif,
                        liens = emptyList(), // TODO #66
                    )
            }
        }

        data class MoyenneGeneraleDesAdmisDTO(
            val idBaccalaureat: String,
            val nomBaccalaureat: String?,
            val centilles: List<CentilleDTO>,
        ) {
            data class CentilleDTO(
                val centille: Int,
                val note: Float,
            )
        }
    }

    data class ExplicationsDTO(
        val geographique: List<ExplicationGeographiqueDTO>,
        val formationsSimilaires: List<FormationSimilaireDTO>,
        val dureeEtudesPrevue: String?,
        val alternance: String?,
        val interetsEtDomainesChoisis: InteretsEtDomainesDTO?,
        val specialitesChoisies: List<AffiniteSpecialiteDTO>,
        val typeBaccalaureat: TypeBaccalaureatDTO?,
        val autoEvaluationMoyenne: AutoEvaluationMoyenneDTO?,
    ) {
        companion object {
            fun fromFicheFormation(ficheFormation: FicheFormation.FicheFormationPourProfil): ExplicationsDTO? {
                return ficheFormation.explications?.let { explications ->
                    ExplicationsDTO(
                        geographique =
                            explications.geographique.map {
                                ExplicationGeographiqueDTO.fromExplicationGeographique(
                                    it,
                                )
                            },
                        formationsSimilaires =
                            ficheFormation.formationsSimilaires?.map {
                                FormationSimilaireDTO.fromFormation(it)
                            } ?: emptyList(),
                        dureeEtudesPrevue = explications.dureeEtudesPrevue?.jsonValeur,
                        alternance = explications.alternance?.jsonValeur,
                        interetsEtDomainesChoisis =
                            InteretsEtDomainesDTO(
                                interets = ficheFormation.interets?.map { InteretDTO.fromInteretSousCategorie(it) } ?: emptyList(),
                                domaines = ficheFormation.domaines?.map { DomaineDTO.fromDomaine(it) } ?: emptyList(),
                            ),
                        specialitesChoisies =
                            explications.specialitesChoisies.map {
                                AffiniteSpecialiteDTO.fromAffiniteSpecialite(
                                    it,
                                )
                            },
                        typeBaccalaureat =
                            ficheFormation.explicationTypeBaccalaureat?.let {
                                TypeBaccalaureatDTO.fromExplicationTypeBaccalaureat(it)
                            },
                        autoEvaluationMoyenne =
                            ficheFormation.explicationAutoEvaluationMoyenne?.let {
                                AutoEvaluationMoyenneDTO.fromAutoEvaluationMoyenne(it)
                            },
                    )
                }
            }
        }

        data class InteretsEtDomainesDTO(
            val interets: List<InteretDTO>,
            val domaines: List<DomaineDTO>,
        )

        data class InteretDTO(
            val id: String,
            val nom: String,
        ) {
            companion object {
                fun fromInteretSousCategorie(interet: InteretSousCategorie) = InteretDTO(id = interet.id, nom = interet.nom)
            }
        }

        data class DomaineDTO(
            val id: String,
            val nom: String,
        ) {
            companion object {
                fun fromDomaine(domaine: Domaine) = DomaineDTO(id = domaine.id, nom = domaine.nom)
            }
        }

        data class FormationSimilaireDTO(
            val id: String,
            val nom: String,
        ) {
            companion object {
                fun fromFormation(formation: Formation) = FormationSimilaireDTO(id = formation.id, nom = formation.nom)
            }
        }

        data class AffiniteSpecialiteDTO(
            val nomSpecialite: String,
            val pourcentage: Int,
        ) {
            companion object {
                fun fromAffiniteSpecialite(affiniteSpecialite: ExplicationsSuggestion.AffiniteSpecialite): AffiniteSpecialiteDTO {
                    return AffiniteSpecialiteDTO(
                        nomSpecialite = affiniteSpecialite.nomSpecialite,
                        pourcentage = affiniteSpecialite.pourcentage,
                    )
                }
            }
        }

        data class ExplicationGeographiqueDTO(
            val nomVille: String,
            val distanceKm: Int,
        ) {
            companion object {
                fun fromExplicationGeographique(
                    explicationGeographique: ExplicationsSuggestion.ExplicationGeographique,
                ): ExplicationGeographiqueDTO {
                    return ExplicationGeographiqueDTO(
                        nomVille = explicationGeographique.ville,
                        distanceKm = explicationGeographique.distanceKm,
                    )
                }
            }
        }

        data class AutoEvaluationMoyenneDTO(
            val moyenne: Float,
            val basIntervalleNotes: Float,
            val hautIntervalleNotes: Float,
            val idBaccalaureatUtilise: String,
            val nomBaccalaureatUtilise: String,
        ) {
            companion object {
                fun fromAutoEvaluationMoyenne(autoEvaluationMoyenne: ExplicationAutoEvaluationMoyenne): AutoEvaluationMoyenneDTO {
                    return AutoEvaluationMoyenneDTO(
                        moyenne = autoEvaluationMoyenne.moyenneAutoEvalue,
                        basIntervalleNotes = autoEvaluationMoyenne.basIntervalleNotes,
                        hautIntervalleNotes = autoEvaluationMoyenne.hautIntervalleNotes,
                        idBaccalaureatUtilise = autoEvaluationMoyenne.baccalaureat.id,
                        nomBaccalaureatUtilise = autoEvaluationMoyenne.baccalaureat.nom,
                    )
                }
            }
        }

        data class TypeBaccalaureatDTO(
            val idBaccalaureat: String?,
            val nomBaccalaureat: String?,
            val pourcentage: Int?,
        ) {
            companion object {
                fun fromExplicationTypeBaccalaureat(typeBaccalaureat: ExplicationTypeBaccalaureat): TypeBaccalaureatDTO {
                    return TypeBaccalaureatDTO(
                        idBaccalaureat = typeBaccalaureat.baccalaureat.id,
                        nomBaccalaureat = typeBaccalaureat.baccalaureat.nom,
                        pourcentage = typeBaccalaureat.pourcentage,
                    )
                }
            }
        }
    }
}
