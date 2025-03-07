package fr.gouv.monprojetsup.referentiel.infrastructure.entity

import fr.gouv.monprojetsup.referentiel.domain.entity.InteretSousCategorie
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "interet_sous_categorie")
class InteretSousCategorieEntity {
    @Id
    @Column(name = "id", nullable = false)
    lateinit var id: String

    @Column(name = "nom", nullable = false)
    lateinit var nom: String

    @Column(name = "emoji", nullable = false)
    lateinit var emoji: String

    @Column(name = "id_categorie", nullable = false)
    lateinit var idCategorie: String

    fun toInteretSousCategorie() =
        InteretSousCategorie(
            id = id,
            nom = nom,
            emoji = emoji,
        )
}
