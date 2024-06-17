package fr.gouv.monprojetsup.recherche.domain.port

import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.Baccalaureat

interface BaccalaureatRepository {
    fun recupererUnBaccalaureatParIdExterne(idExterneBaccalaureat: String): Baccalaureat?

    fun recupererUnBaccalaureat(id: String): Baccalaureat?
}
