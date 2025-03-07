package fr.gouv.monprojetsup.metier.infrastructure.repository

import fr.gouv.monprojetsup.metier.domain.entity.Metier
import fr.gouv.monprojetsup.metier.domain.port.MetierRepository
import org.slf4j.Logger
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class MetierBDDRepository(
    val metierJPARepository: MetierJPARepository,
    val logger: Logger,
) : MetierRepository {
    @Transactional(readOnly = true)
    override fun recupererLesMetiersDetailles(ids: List<String>): List<Metier> {
        val metiers = metierJPARepository.findAllByIdIn(ids)
        return ids.mapNotNull { idMetier ->
            val metier = metiers.firstOrNull { it.id == idMetier }
            if (metier == null) {
                logger.error("Le métier $idMetier n'est pas présent en base")
            }
            metier?.toMetierDetaille()
        }
    }
}
