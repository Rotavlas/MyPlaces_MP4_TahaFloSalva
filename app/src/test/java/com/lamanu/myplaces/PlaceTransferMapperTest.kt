package com.lamanu.myplaces

import com.lamanu.myplaces.data.transfer.ExportedAuthor
import com.lamanu.myplaces.data.transfer.ExportedPlace
import com.lamanu.myplaces.data.transfer.PlacesExportFile
import com.lamanu.myplaces.data.transfer.isValid
import com.lamanu.myplaces.data.transfer.toEntity
import com.lamanu.myplaces.domain.model.PlaceOrigin
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaceTransferMapperTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; explicitNulls = false }

    private val fileAuthor = ExportedAuthor(id = "author-taha", name = "Taha")

    private fun place(
        id: String = "place-1",
        title: String = "Plage du Crotoy",
        latitude: Double = 50.2166,
        longitude: Double = 1.6220,
        author: ExportedAuthor? = null,
    ) = ExportedPlace(
        id = id,
        title = title,
        description = "Coucher de soleil en baie",
        emoji = "🌊",
        latitude = latitude,
        longitude = longitude,
        address = "Le Crotoy",
        createdAt = 1_700_000_000_000,
        author = author,
    )

    @Test
    fun `un lieu importe est toujours marque IMPORTED`() {
        val entity = place().toEntity(fileAuthor = fileAuthor, importedAt = 42L)

        assertEquals(PlaceOrigin.IMPORTED, entity.origin)
        assertEquals(42L, entity.importedAt)
    }

    @Test
    fun `l identite du lieu survit a l aller-retour`() {
        val entity = place(id = "uuid-stable").toEntity(fileAuthor = fileAuthor, importedAt = 0L)

        // C'est cette stabilite qui permet a INSERT OR IGNORE de dedupliquer les reimports.
        assertEquals("uuid-stable", entity.id)
    }

    @Test
    fun `l auteur du fichier sert de repli quand le lieu n en declare pas`() {
        val withoutAuthor = place().toEntity(fileAuthor = fileAuthor, importedAt = 0L)
        val withAuthor = place(author = ExportedAuthor("author-flo", "Flo"))
            .toEntity(fileAuthor = fileAuthor, importedAt = 0L)

        assertEquals("author-taha", withoutAuthor.authorId)
        assertEquals("Flo", withAuthor.authorName)
    }

    @Test
    fun `aucune photo ne transite par le JSON`() {
        assertNull(place().toEntity(fileAuthor = fileAuthor, importedAt = 0L).photoFileName)
    }

    @Test
    fun `les coordonnees hors bornes sont rejetees`() {
        assertTrue(place().isValid())
        assertFalse(place(latitude = 95.0).isValid())
        assertFalse(place(longitude = -200.0).isValid())
        assertFalse(place(id = "").isValid())
        assertFalse(place(title = "  ").isValid())
    }

    @Test
    fun `le fichier d echange se relit tel quel`() {
        val payload = PlacesExportFile(
            exportedAt = 1_700_000_000_000,
            author = fileAuthor,
            places = listOf(place(), place(id = "place-2", title = "Cafe du coin")),
        )

        val decoded = json.decodeFromString<PlacesExportFile>(json.encodeToString(payload))

        assertEquals(PlacesExportFile.CURRENT_FORMAT_VERSION, decoded.formatVersion)
        assertEquals(payload.author, decoded.author)
        assertEquals(listOf("place-1", "place-2"), decoded.places.map { it.id })
    }

    @Test
    fun `un fichier venu d une version plus riche reste lisible`() {
        val raw = """
            {
              "formatVersion": 1,
              "exportedAt": 1700000000000,
              "author": { "id": "author-salva", "name": "Salva", "avatar": "inconnu" },
              "places": [
                {
                  "id": "p1", "title": "Test", "emoji": "☕",
                  "latitude": 49.0, "longitude": 2.0, "createdAt": 1700000000000,
                  "weather": "champ futur non gere"
                }
              ]
            }
        """.trimIndent()

        val decoded = json.decodeFromString<PlacesExportFile>(raw)

        assertEquals(1, decoded.places.size)
        assertEquals("", decoded.places.first().description)
    }
}
