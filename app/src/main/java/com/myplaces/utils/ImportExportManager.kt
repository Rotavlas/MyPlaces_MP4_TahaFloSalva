package com.myplaces.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.myplaces.data.local.PlaceEntity
import java.io.File

class ImportExportManager(private val context: Context) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    data class ExportPayload(
        val version: Int = 1,
        val authorId: String = "me",
        val exportedAt: Long = System.currentTimeMillis(),
        val places: List<PlaceExportDto>
    )

    data class PlaceExportDto(
        val title: String, val description: String, val emoji: String,
        val latitude: Double, val longitude: Double, val address: String,
        val photoPath: String?, val timestamp: Long,
        val authorId: String, val isOwn: Boolean
    )

    fun export(places: List<PlaceEntity>): Uri? {
        return try {
            val json = gson.toJson(ExportPayload(places = places.map { it.toDto() }))
            val file = File(File(context.filesDir, "exports").also { it.mkdirs() }, "places_export.json")
            file.writeText(json)
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }

    fun import(uri: Uri, authorName: String): List<PlaceEntity> {
        return try {
            val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.readText()
                ?: return emptyList()
            val payload = gson.fromJson(json, ExportPayload::class.java)
            payload.places.map { it.toEntity(authorId = authorName.ifBlank { "Ami" }, isOwn = false) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun PlaceEntity.toDto() = PlaceExportDto(
        title, description, emoji, latitude, longitude, address, photoPath, timestamp, authorId, isOwn
    )

    private fun PlaceExportDto.toEntity(authorId: String, isOwn: Boolean) = PlaceEntity(
        id = 0L,
        title = title, description = description, emoji = emoji,
        latitude = latitude, longitude = longitude, address = address,
        photoPath = null, // les chemins photo sont locaux, non transférables
        timestamp = timestamp, authorId = authorId, isOwn = isOwn
    )
}
