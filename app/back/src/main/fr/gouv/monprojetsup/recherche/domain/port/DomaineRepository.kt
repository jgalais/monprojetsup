package fr.gouv.monprojetsup.recherche.domain.port

import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.Domaine

interface DomaineRepository {
    fun recupererLesDomaines(ids: List<String>): List<Domaine>
}
