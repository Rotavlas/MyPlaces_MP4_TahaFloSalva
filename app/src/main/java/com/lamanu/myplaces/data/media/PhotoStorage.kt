package com.lamanu.myplaces.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.lamanu.myplaces.di.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Toutes les photos vivent dans `filesDir/place_photos/`, c'est-a-dire le stockage **interne
 * prive** de l'application. La base ne connait que le nom de fichier (cf. cahier des charges :
 * aucune image en Base64 dans Room).
 */
@Singleton
class PhotoStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val photosDir: File
        get() = directoryIn(context)

    fun fileFor(fileName: String): File = File(photosDir, fileName)

    /** Uri consommable par Coil / l'appareil photo pour un fichier deja enregistre. */
    fun uriFor(fileName: String): Uri = FileProvider.getUriForFile(context, authority, fileFor(fileName))

    /**
     * Prepare une destination pour `ActivityResultContracts.TakePicture` : le nom retourne
     * n'est a persister qu'une fois la capture confirmee (sinon appeler [delete]).
     */
    fun newCaptureTarget(): CaptureTarget {
        val fileName = "${UUID.randomUUID()}.jpg"
        val file = fileFor(fileName)
        file.parentFile?.mkdirs()
        return CaptureTarget(fileName = fileName, uri = FileProvider.getUriForFile(context, authority, file))
    }

    /** Copie une image choisie dans la galerie vers le stockage prive, redimensionnee. */
    suspend fun importFromUri(source: Uri): String? = withContext(ioDispatcher) {
        runCatching {
            val fileName = "${UUID.randomUUID()}.jpg"
            val target = fileFor(fileName)
            val bitmap = context.contentResolver.openInputStream(source).use { input ->
                requireNotNull(input) { "Flux introuvable pour $source" }
                decodeDownscaled(input.readBytes())
            }
            target.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            bitmap.recycle()
            fileName
        }.getOrNull()
    }

    /** Recompresse une photo capturee par l'appareil pour eviter des fichiers de 8 Mo. */
    suspend fun compressInPlace(fileName: String) = withContext(ioDispatcher) {
        runCatching {
            val file = fileFor(fileName)
            if (!file.exists()) return@runCatching
            val orientation = ExifInterface(file.absolutePath)
                .getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            val bitmap = decodeDownscaled(file.readBytes())
            file.outputStream().use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, output)
            }
            bitmap.recycle()
            // La recompression efface l'EXIF : on remet l'orientation pour un affichage correct.
            ExifInterface(file.absolutePath).apply {
                setAttribute(ExifInterface.TAG_ORIENTATION, orientation.toString())
                saveAttributes()
            }
        }
        Unit
    }

    suspend fun delete(fileName: String?) = withContext(ioDispatcher) {
        if (fileName.isNullOrBlank()) return@withContext
        runCatching { fileFor(fileName).delete() }
        Unit
    }

    /** Supprime les fichiers orphelins (captures abandonnees, lieux effaces). */
    suspend fun pruneOrphans(referenced: Set<String>) = withContext(ioDispatcher) {
        photosDir.listFiles()?.forEach { file ->
            if (file.name !in referenced) file.delete()
        }
        Unit
    }

    private fun decodeDownscaled(bytes: ByteArray): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
        var sample = 1
        while (bounds.outWidth / sample > MAX_DIMENSION || bounds.outHeight / sample > MAX_DIMENSION) {
            sample *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            ?: error("Image illisible")
    }

    private val authority: String get() = "${context.packageName}.fileprovider"

    data class CaptureTarget(val fileName: String, val uri: Uri)

    companion object {
        private const val DIRECTORY = "place_photos"
        private const val MAX_DIMENSION = 1600
        private const val JPEG_QUALITY = 85

        /** Seul endroit ou le chemin des photos est construit ; l'UI passe aussi par ici. */
        fun directoryIn(context: Context): File =
            File(context.filesDir, DIRECTORY).apply { if (!exists()) mkdirs() }

        fun fileIn(context: Context, fileName: String): File = File(directoryIn(context), fileName)
    }
}
