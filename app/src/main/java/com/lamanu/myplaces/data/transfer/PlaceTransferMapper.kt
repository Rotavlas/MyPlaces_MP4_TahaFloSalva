package com.lamanu.myplaces.data.transfer

import com.lamanu.myplaces.data.local.PlaceEntity
import com.lamanu.myplaces.domain.model.PlaceOrigin

/** Mes lieux -> fichier d'echange. */
fun PlaceEntity.toExported(): ExportedPlace = ExportedPlace(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    latitude = latitude,
    longitude = longitude,
    address = address,
    createdAt = createdAt,
    author = ExportedAuthor(id = authorId, name = authorName),
)

/**
 * Fichier d'echange -> ma base. L'origine est **toujours** forcee a [PlaceOrigin.IMPORTED] :
 * un fichier d'ami ne peut pas se faire passer pour un lieu local.
 */
fun ExportedPlace.toEntity(
    fileAuthor: ExportedAuthor,
    importedAt: Long,
): PlaceEntity {
    val effectiveAuthor = author ?: fileAuthor
    return PlaceEntity(
        id = id,
        title = title,
        description = description,
        emoji = emoji,
        latitude = latitude,
        longitude = longitude,
        address = address,
        photoFileName = null, // les photos ne transitent pas dans le JSON v1
        createdAt = createdAt,
        authorId = effectiveAuthor.id,
        authorName = effectiveAuthor.name,
        origin = PlaceOrigin.IMPORTED,
        importedAt = importedAt,
    )
}

/** Un lieu importe doit etre exploitable : id non vide et coordonnees plausibles. */
fun ExportedPlace.isValid(): Boolean =
    id.isNotBlank() &&
        title.isNotBlank() &&
        latitude in -90.0..90.0 &&
        longitude in -180.0..180.0
