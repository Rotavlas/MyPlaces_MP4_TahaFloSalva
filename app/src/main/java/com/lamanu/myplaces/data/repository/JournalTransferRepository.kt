package com.lamanu.myplaces.data.repository

import android.content.Context
import android.net.Uri
import com.lamanu.myplaces.data.local.PlaceDao
import com.lamanu.myplaces.data.prefs.UserPreferencesRepository
import com.lamanu.myplaces.data.transfer.ExportedAuthor
import com.lamanu.myplaces.data.transfer.ImportError
import com.lamanu.myplaces.data.transfer.ImportReport
import com.lamanu.myplaces.data.transfer.PlacesExportFile
import com.lamanu.myplaces.data.transfer.isValid
import com.lamanu.myplaces.data.transfer.toEntity
import com.lamanu.myplaces.data.transfer.toExported
import com.lamanu.myplaces.di.IoDispatcher
import com.lamanu.myplaces.domain.model.PlaceOrigin
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Import / export du journal au format `places_export.json`.
 *
 * Regles de fusion (le point delicat du sujet) :
 *  1. on n'exporte que **ses propres** lieux (`origin = LOCAL`), jamais ceux deja importes,
 *     pour eviter qu'un journal se propage en boucle entre amis ;
 *  2. a l'import, tout lieu est force en `origin = IMPORTED` ;
 *  3. la cle primaire etant l'UUID du lieu, un `INSERT OR IGNORE` suffit a rendre l'operation
 *     idempotente : reimporter le meme fichier ne cree aucun doublon et n'ecrase rien.
 */
@Singleton
class JournalTransferRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val placeDao: PlaceDao,
    private val userPreferences: UserPreferencesRepository,
    private val json: Json,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    /** Serialise mes lieux vers le document choisi par l'utilisateur (Storage Access Framework). */
    suspend fun exportTo(destination: Uri): Result<Int> = withContext(ioDispatcher) {
        runCatching {
            val author = userPreferences.currentAuthor()
            val places = placeDao.listByOrigin(PlaceOrigin.LOCAL)
            val payload = PlacesExportFile(
                exportedAt = System.currentTimeMillis(),
                author = ExportedAuthor(id = author.id, name = author.name),
                places = places.map { it.toExported() },
            )
            context.contentResolver.openOutputStream(destination, "wt").use { output ->
                requireNotNull(output) { "Destination non inscriptible" }
                output.write(json.encodeToString(payload).toByteArray())
            }
            places.size
        }
    }

    /** Lit un fichier d'ami et fusionne son contenu. */
    suspend fun importFrom(source: Uri): Result<ImportReport> = withContext(ioDispatcher) {
        runCatching {
            val raw = context.contentResolver.openInputStream(source)?.use { it.readBytes() }
                ?.toString(Charsets.UTF_8)
                ?: throw ImportError.NotReadable

            val payload = runCatching { json.decodeFromString<PlacesExportFile>(raw) }
                .getOrElse { throw ImportError.Malformed }

            if (payload.formatVersion > PlacesExportFile.CURRENT_FORMAT_VERSION) {
                throw ImportError.UnsupportedVersion(payload.formatVersion)
            }

            val me = userPreferences.currentAuthor()
            if (payload.author.id == me.id) throw ImportError.OwnFile

            val now = System.currentTimeMillis()
            val (valid, invalid) = payload.places.partition { it.isValid() }

            // Un fichier ne peut pas reintroduire mes propres lieux comme s'ils venaient d'un ami.
            val (foreign, mine) = valid.partition { (it.author?.id ?: payload.author.id) != me.id }

            val entities = foreign.map { it.toEntity(fileAuthor = payload.author, importedAt = now) }
            val insertedRows = placeDao.insertIgnoringDuplicates(entities)
            val inserted = insertedRows.count { it != -1L }

            ImportReport(
                fileAuthor = payload.author,
                imported = inserted,
                duplicatesSkipped = entities.size - inserted,
                invalidSkipped = invalid.size,
                ownPlacesSkipped = mine.size,
            )
        }
    }

    fun suggestedFileName(): String = PlacesExportFile.FILE_NAME
}
