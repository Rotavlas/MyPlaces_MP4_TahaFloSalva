package com.myplaces.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// authorId = "me" pour l'utilisateur local, sinon le nom de l'ami importé
// isOwn permet de distinguer ses propres lieux des lieux importés
@Entity(tableName = "places")
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val emoji: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val photoPath: String?,
    val timestamp: Long,
    val authorId: String = "me",
    val isOwn: Boolean = true
)
