package com.lamanu.myplaces.data.transfer

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Format du fichier d'echange `places_export.json` (documente dans le README).
 *
 * [formatVersion] est lu a l'import : un fichier plus recent que [CURRENT_FORMAT_VERSION]
 * est rejete explicitement plutot que parse a moitie.
 *
 * Les photos ne transitent pas dans le JSON (v1) : l'interdiction du Base64 en base vaut
 * aussi comme principe pour le fichier d'echange, qui reste lisible et leger.
 */
@Serializable
data class PlacesExportFile(
    @SerialName("formatVersion") val formatVersion: Int = CURRENT_FORMAT_VERSION,
    @SerialName("generator") val generator: String = GENERATOR,
    @SerialName("exportedAt") val exportedAt: Long,
    @SerialName("author") val author: ExportedAuthor,
    @SerialName("places") val places: List<ExportedPlace>,
) {
    companion object {
        const val CURRENT_FORMAT_VERSION = 1
        const val GENERATOR = "MyPlaces"
        const val FILE_NAME = "places_export.json"
        const val MIME_TYPE = "application/json"
    }
}

@Serializable
data class ExportedAuthor(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
)

@Serializable
data class ExportedPlace(
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("description") val description: String = "",
    @SerialName("emoji") val emoji: String,
    @SerialName("latitude") val latitude: Double,
    @SerialName("longitude") val longitude: Double,
    @SerialName("address") val address: String? = null,
    @SerialName("createdAt") val createdAt: Long,
    /** Auteur du lieu ; absent, on retombe sur l'auteur du fichier. */
    @SerialName("author") val author: ExportedAuthor? = null,
)

/** Bilan rendu a l'utilisateur apres un import. */
data class ImportReport(
    val fileAuthor: ExportedAuthor,
    val imported: Int,
    val duplicatesSkipped: Int,
    val invalidSkipped: Int,
    val ownPlacesSkipped: Int,
) {
    val total: Int get() = imported + duplicatesSkipped + invalidSkipped + ownPlacesSkipped
}

/** Erreurs d'import remontees telles quelles a l'UI. */
sealed class ImportError(message: String) : Exception(message) {
    data object NotReadable : ImportError("Fichier illisible.")
    data object Malformed : ImportError("Ce fichier n'est pas un export My Places valide.")
    data class UnsupportedVersion(val found: Int) :
        ImportError("Format v$found non supporte (max v${PlacesExportFile.CURRENT_FORMAT_VERSION}).")
    data object OwnFile : ImportError("Ce fichier est votre propre export : rien a importer.")
    private fun readResolve(): Any = this
}
