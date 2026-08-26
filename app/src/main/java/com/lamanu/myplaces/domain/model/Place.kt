package com.lamanu.myplaces.domain.model

/**
 * Un lieu du journal.
 *
 * L'[id] est un UUID genere a la creation et **jamais** reattribue : c'est lui qui rend
 * l'import idempotent (reimporter deux fois le meme fichier ne duplique rien).
 *
 * La [photoPath] ne contient qu'un nom de fichier relatif au repertoire prive de photos.
 * Aucune image n'est stockee en base (cf. cahier des charges).
 */
data class Place(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val photoFileName: String?,
    val createdAt: Long,
    val author: Author,
    val origin: PlaceOrigin,
    val importedAt: Long?,
) {
    val isOwn: Boolean get() = origin == PlaceOrigin.LOCAL
}

/** Auteur d'un lieu : soi-meme, ou l'ami dont on a importe le journal. */
data class Author(
    val id: String,
    val name: String,
)

/** Distingue les lieux crees ici de ceux venus d'un fichier d'echange. */
enum class PlaceOrigin {
    LOCAL,
    IMPORTED,
}
