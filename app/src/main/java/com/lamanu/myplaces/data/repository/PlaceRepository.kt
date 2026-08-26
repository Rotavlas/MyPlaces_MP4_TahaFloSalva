package com.lamanu.myplaces.data.repository

import com.lamanu.myplaces.data.local.PlaceDao
import com.lamanu.myplaces.data.local.toDomain
import com.lamanu.myplaces.data.local.toEntity
import com.lamanu.myplaces.data.media.PhotoStorage
import com.lamanu.myplaces.data.prefs.UserPreferencesRepository
import com.lamanu.myplaces.domain.model.Author
import com.lamanu.myplaces.domain.model.Place
import com.lamanu.myplaces.domain.model.PlaceOrigin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Point d'entree unique sur les lieux. L'UI ne parle jamais au DAO directement.
 */
@Singleton
class PlaceRepository @Inject constructor(
    private val placeDao: PlaceDao,
    private val photoStorage: PhotoStorage,
    private val geocoding: ReverseGeocodingRepository,
    private val userPreferences: UserPreferencesRepository,
) {

    fun observePlaces(): Flow<List<Place>> =
        placeDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    fun observePlace(id: String): Flow<Place?> =
        placeDao.observeById(id).map { it?.toDomain() }

    /**
     * Cree un lieu. L'adresse est resolue par le web service *avant* insertion quand le reseau
     * repond ; sinon le lieu est enregistre sans adresse et pourra etre complete plus tard
     * via [refreshAddress].
     */
    suspend fun createPlace(
        title: String,
        description: String,
        emoji: String,
        latitude: Double,
        longitude: Double,
        photoFileName: String?,
    ): Place {
        val author: Author = userPreferences.currentAuthor()
        val address = geocoding.reverseGeocode(latitude, longitude)
        val place = Place(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            description = description.trim(),
            emoji = emoji,
            latitude = latitude,
            longitude = longitude,
            address = address,
            photoFileName = photoFileName,
            createdAt = System.currentTimeMillis(),
            author = author,
            origin = PlaceOrigin.LOCAL,
            importedAt = null,
        )
        placeDao.upsert(place.toEntity())
        return place
    }

    suspend fun updatePlace(place: Place) {
        placeDao.update(place.toEntity())
    }

    /** Deuxieme chance pour les lieux crees hors ligne. */
    suspend fun refreshAddress(id: String): Boolean {
        val entity = placeDao.findById(id) ?: return false
        val address = geocoding.reverseGeocode(entity.latitude, entity.longitude) ?: return false
        placeDao.update(entity.copy(address = address))
        return true
    }

    /** Supprime le lieu **et** sa photo : pas de fichier orphelin dans le stockage prive. */
    suspend fun deletePlace(id: String) {
        val entity = placeDao.findById(id) ?: return
        placeDao.deleteById(id)
        photoStorage.delete(entity.photoFileName)
    }

    /** Retire d'un coup tous les lieux importes d'un ami. */
    suspend fun deleteImportedFrom(authorId: String) {
        placeDao.deleteImportedFrom(authorId)
    }

    suspend fun countOwnPlaces(): Int = placeDao.countByOrigin(PlaceOrigin.LOCAL)

    suspend fun countImportedPlaces(): Int = placeDao.countByOrigin(PlaceOrigin.IMPORTED)
}
