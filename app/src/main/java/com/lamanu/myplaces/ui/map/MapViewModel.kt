package com.lamanu.myplaces.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lamanu.myplaces.core.location.Coordinates
import com.lamanu.myplaces.core.location.LocationProvider
import com.lamanu.myplaces.data.repository.PlaceRepository
import com.lamanu.myplaces.domain.model.Place
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Filtre du bandeau superieur : tout / mes lieux / lieux importes. */
enum class PlaceFilter(val label: String) {
    ALL("Tous"),
    MINE("Les miens"),
    IMPORTED("Importes"),
}

data class MapUiState(
    val places: List<Place> = emptyList(),
    val filter: PlaceFilter = PlaceFilter.ALL,
    val selectedPlace: Place? = null,
    val userLocation: Coordinates? = null,
    val hasLocationPermission: Boolean = false,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val placeRepository: PlaceRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val filter = MutableStateFlow(PlaceFilter.ALL)
    private val selectedPlaceId = MutableStateFlow<String?>(null)
    private val userLocation = MutableStateFlow<Coordinates?>(null)
    private val locationPermission = MutableStateFlow(false)

    val uiState: StateFlow<MapUiState> = combine(
        placeRepository.observePlaces(),
        filter,
        selectedPlaceId,
        userLocation,
        locationPermission,
    ) { places, currentFilter, selectedId, location, hasPermission ->
        val visible = when (currentFilter) {
            PlaceFilter.ALL -> places
            PlaceFilter.MINE -> places.filter { it.isOwn }
            PlaceFilter.IMPORTED -> places.filterNot { it.isOwn }
        }
        MapUiState(
            places = visible,
            filter = currentFilter,
            selectedPlace = places.firstOrNull { it.id == selectedId },
            userLocation = location,
            hasLocationPermission = hasPermission,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = MapUiState(),
    )

    private val _events = MutableStateFlow<String?>(null)
    val events: StateFlow<String?> = _events.asStateFlow()

    fun onFilterChange(newFilter: PlaceFilter) {
        filter.value = newFilter
    }

    fun onMarkerClick(place: Place) {
        selectedPlaceId.value = place.id
    }

    fun onSheetDismiss() {
        selectedPlaceId.value = null
    }

    /** Appele des que la permission de localisation est accordee. */
    fun onLocationPermissionGranted() {
        locationPermission.value = true
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            userLocation.value = locationProvider.currentLocation()
        }
    }

    fun deletePlace(id: String) {
        viewModelScope.launch {
            placeRepository.deletePlace(id)
            if (selectedPlaceId.value == id) selectedPlaceId.value = null
        }
    }

    /** Relance le geocodage inverse pour un lieu cree hors ligne. */
    fun retryAddressLookup(id: String) {
        viewModelScope.launch {
            val ok = placeRepository.refreshAddress(id)
            _events.value = if (ok) "Adresse mise a jour." else "Adresse toujours introuvable."
        }
    }

    fun consumeEvent() {
        _events.value = null
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
