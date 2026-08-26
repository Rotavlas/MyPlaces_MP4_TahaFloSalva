package com.lamanu.myplaces.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.lamanu.myplaces.domain.model.Author
import com.lamanu.myplaces.domain.model.Place
import com.lamanu.myplaces.domain.model.PlaceOrigin

/**
 * Table `places`.
 *
 * Choix de conception impose par l'import/export :
 *  - la cle primaire est l'UUID **du lieu**, pas un autoincrement. Un lieu garde donc la meme
 *    identite d'un telephone a l'autre, ce qui permet d'ignorer les doublons a l'import
 *    (`OnConflictStrategy.IGNORE`) au lieu de les empiler ;
 *  - `authorId` / `authorName` portent l'auteur du souvenir et `origin` distingue mes lieux
 *    de ceux importes. Les deux sont necessaires : `origin` filtre l'export (on n'exporte que
 *    ses propres lieux) tandis que `authorId` sert a regrouper/afficher les journaux d'amis.
 */
@Entity(
    tableName = "places",
    indices = [
        Index(value = ["origin"]),
        Index(value = ["author_id"]),
        Index(value = ["created_at"]),
    ],
)
data class PlaceEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "emoji")
    val emoji: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    /** Adresse postale resolue par le web service de geocodage inverse, null si indisponible. */
    @ColumnInfo(name = "address")
    val address: String?,

    /** Nom de fichier relatif dans le stockage interne prive. Jamais de Base64 ici. */
    @ColumnInfo(name = "photo_file_name")
    val photoFileName: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "author_id")
    val authorId: String,

    @ColumnInfo(name = "author_name")
    val authorName: String,

    @ColumnInfo(name = "origin")
    val origin: PlaceOrigin,

    /** Date d'integration dans MA base ; null pour mes propres lieux. */
    @ColumnInfo(name = "imported_at")
    val importedAt: Long?,
)

fun PlaceEntity.toDomain(): Place = Place(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    latitude = latitude,
    longitude = longitude,
    address = address,
    photoFileName = photoFileName,
    createdAt = createdAt,
    author = Author(id = authorId, name = authorName),
    origin = origin,
    importedAt = importedAt,
)

fun Place.toEntity(): PlaceEntity = PlaceEntity(
    id = id,
    title = title,
    description = description,
    emoji = emoji,
    latitude = latitude,
    longitude = longitude,
    address = address,
    photoFileName = photoFileName,
    createdAt = createdAt,
    authorId = author.id,
    authorName = author.name,
    origin = origin,
    importedAt = importedAt,
)
