package fr.gouv.monprojetsup.referentiel.infrastructure.repository

import fr.gouv.monprojetsup.referentiel.domain.entity.InteretCategorie
import fr.gouv.monprojetsup.referentiel.domain.entity.InteretSousCategorie
import fr.gouv.monprojetsup.referentiel.domain.port.InteretRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class InteretBDDRepository(
    val interetJPARepository: InteretJPARepository,
    val interetCategorieJPARepository: InteretCategorieJPARepository,
) : InteretRepository {
    @Transactional(readOnly = true)
    override fun recupererLesSousCategoriesDInterets(idsInterets: List<String>): Map<String, InteretSousCategorie> {
        return interetJPARepository.findAllByIdIn(idsInterets).associate { it.id to it.sousCategorie.toInteretSousCategorie() }
    }

    override fun recupererToutesLesCategoriesEtLeursSousCategoriesDInterets(): Map<InteretCategorie, List<InteretSousCategorie>> {
        return interetCategorieJPARepository.findAll().associate {
            it.toInteretCategorie() to it.sousCategories.map { it.toInteretSousCategorie() }
        }
    }
}
