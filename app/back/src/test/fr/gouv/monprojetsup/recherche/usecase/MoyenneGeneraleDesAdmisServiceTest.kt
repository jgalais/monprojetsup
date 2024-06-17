package fr.gouv.monprojetsup.recherche.usecase

import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.Baccalaureat
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.MoyenneGeneraleDesAdmis
import fr.gouv.monprojetsup.recherche.domain.entity.FicheFormation.MoyenneGeneraleDesAdmis.Centille
import fr.gouv.monprojetsup.recherche.domain.port.MoyenneGeneraleAdmisRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class MoyenneGeneraleDesAdmisServiceTest {
    @Mock
    lateinit var moyenneGeneraleAdmisRepository: MoyenneGeneraleAdmisRepository

    @InjectMocks
    lateinit var moyenneGeneraleDesAdmisService: MoyenneGeneraleDesAdmisService

    private val frequencesCumulees =
        mapOf(
            "Général" to
                listOf(
                    0, // 0,5
                    0, // 1
                    0, // 1,5
                    0, // 2
                    0, // 2,5
                    0, // 3
                    0, // 3,5
                    0, // 4
                    0, // 4,5
                    0, // 5
                    0, // 5,5
                    0, // 6
                    0, // 6,5
                    0, // 7
                    0, // 7,5
                    0, // 8
                    0, // 8,5
                    0, // 9
                    0, // 9,5
                    0, // 10
                    6, // 10,5
                    24, // 11
                    49, // 11,5
                    77, // 12
                    174, // 12,5
                    292, // 13
                    500, // 13,5
                    685, // 14
                    1206, // 14,5
                    1700, // 15
                    2375, // 15,5
                    2845, // 16
                    3924, // 16,5
                    4755, // 17
                    5479, // 17,5
                    5893, // 18
                    6401, // 18,5
                    6612, // 19
                    6670, // 19,5
                    6677, // 20
                ),
            "STMG" to
                listOf(
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    1,
                    1,
                    1,
                    1,
                    1,
                    1,
                    2,
                    2,
                    4,
                    4,
                    8,
                    8,
                    11,
                    12,
                    13,
                    14,
                    15,
                    15,
                    15,
                    15,
                    15,
                    15,
                    15,
                    15,
                    15,
                ),
            "STI2D" to
                listOf(
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    0,
                    2,
                    2,
                    2,
                    5,
                    11,
                    17,
                    27,
                    38,
                    50,
                    61,
                    76,
                    103,
                    128,
                    150,
                    163,
                    173,
                    187,
                    198,
                    201,
                    210,
                    216,
                    221,
                    223,
                    223,
                    223,
                    223,
                ),
        )

    private val centillesTousBacConfondus =
        listOf(
            Centille(5, 12.5f),
            Centille(25, 14.5f),
            Centille(75, 17f),
            Centille(95, 18f),
        )

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        given(moyenneGeneraleAdmisRepository.recupererFrequencesCumuleesDeToutLesBacs("fl0001")).willReturn(
            frequencesCumulees,
        )
    }

    @Test
    fun `doit retourner la moyenne générale pour le baccalauréat donné`() {
        // Given
        val baccalaureat = Baccalaureat(id = "Général", idExterne = "Générale", nom = "Série Générale")

        // When
        val resultat =
            moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(
                baccalaureat = baccalaureat,
                idFormation = "fl0001",
            )

        // Then
        val attendu =
            MoyenneGeneraleDesAdmis(
                idBaccalaureat = "Général",
                nomBaccalaureat = "Série Générale",
                centilles =
                    listOf(
                        Centille(centille = 5, note = 13f),
                        Centille(centille = 25, note = 14.5f),
                        Centille(centille = 75, note = 17f),
                        Centille(centille = 95, note = 18f),
                    ),
            )
        assertThat(resultat).usingRecursiveComparison().isEqualTo(attendu)
    }

    @Test
    fun `si le baccalaureat est null, doit retourner pour tous les baccalauréats confondus`() {
        // When
        val resultat =
            moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(baccalaureat = null, idFormation = "fl0001")

        // Then
        val attendu = MoyenneGeneraleDesAdmis(idBaccalaureat = null, nomBaccalaureat = null, centilles = centillesTousBacConfondus)
        assertThat(resultat).usingRecursiveComparison().isEqualTo(attendu)
    }

    @Test
    fun `si l'id du baccalaureat n'est pas dans la liste retournée, doit retourner pour tous les baccalauréats confondus`() {
        // Given
        val baccalaureat = Baccalaureat(id = "Pro", idExterne = "P", nom = "Bac Pro")

        // When
        val resultat =
            moyenneGeneraleDesAdmisService.recupererMoyenneGeneraleDesAdmisDUneFormation(
                baccalaureat = baccalaureat,
                idFormation = "fl0001",
            )

        // Then
        val attendu = MoyenneGeneraleDesAdmis(idBaccalaureat = null, nomBaccalaureat = null, centilles = centillesTousBacConfondus)
        assertThat(resultat).usingRecursiveComparison().isEqualTo(attendu)
    }

    @Test
    fun `si moins de 30 admis pour le baccalaureat dans la formation, doit retourner pour tous les baccalauréats confondus`() {
        // TODO ce ticket
    }

    @Test
    fun `si moins de 30 admis pour tous bac confondus, doit TODO`() {
        // TODO
    }
}
