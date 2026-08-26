package com.myplaces.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.myplaces.data.local.PlaceEntity
import com.myplaces.data.local.PlacesDatabase
import com.myplaces.data.repository.PlacesRepository
import com.myplaces.utils.ImportExportManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlacesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PlacesRepository by lazy {
        PlacesRepository(PlacesDatabase.getInstance(application).placeDao())
    }

    private val importExportManager = ImportExportManager(application)

    val places: StateFlow<List<PlaceEntity>> = repository.allPlaces
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addPlace(title: String, description: String, emoji: String, lat: Double, lon: Double, photoPath: String?) =
        viewModelScope.launch {
            // Insertion immédiate, puis mise à jour de l'adresse une fois le geocoding terminé
            val id = repository.insert(
                PlaceEntity(
                    title = title, description = description, emoji = emoji,
                    latitude = lat, longitude = lon,
                    address = "Récupération en cours…",
                    photoPath = photoPath, timestamp = System.currentTimeMillis()
                )
            )
            val address = repository.reverseGeocode(lat, lon) ?: "$lat, $lon"
            repository.update(
                PlaceEntity(
                    id = id, title = title, description = description, emoji = emoji,
                    latitude = lat, longitude = lon, address = address,
                    photoPath = photoPath, timestamp = System.currentTimeMillis()
                )
            )
        }

    fun deletePlace(place: PlaceEntity) = viewModelScope.launch {
        repository.delete(place)
    }

    fun exportJournal(onResult: (Uri?) -> Unit) = viewModelScope.launch {
        val uri = importExportManager.export(repository.getOwnPlaces())
        onResult(uri)
    }

    fun importFromUri(uri: Uri, authorName: String, onResult: (Int) -> Unit) = viewModelScope.launch {
        val imported = importExportManager.import(uri, authorName)
        repository.insertAll(imported)
        onResult(imported.size)
    }
}
