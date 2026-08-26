package com.myplaces.data.repository

import com.myplaces.data.local.PlaceDao
import com.myplaces.data.local.PlaceEntity
import com.myplaces.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.Flow

class PlacesRepository(private val dao: PlaceDao) {

    val allPlaces: Flow<List<PlaceEntity>> = dao.observeAll()

    suspend fun insert(place: PlaceEntity): Long = dao.insert(place)
    suspend fun update(place: PlaceEntity) = dao.update(place)
    suspend fun delete(place: PlaceEntity) = dao.delete(place)
    suspend fun deleteById(id: Long) = dao.deleteById(id)
    suspend fun getOwnPlaces(): List<PlaceEntity> = dao.getOwnPlaces()
    suspend fun insertAll(places: List<PlaceEntity>) = dao.insertAll(places)

    // Appel à api-adresse.data.gouv.fr pour convertir lat/lon en adresse postale
    suspend fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            val response = RetrofitInstance.geocodingApi.reverseGeocode(latitude = lat, longitude = lon)
            response.features.firstOrNull()?.properties?.label
        } catch (e: Exception) {
            null
        }
    }
}
