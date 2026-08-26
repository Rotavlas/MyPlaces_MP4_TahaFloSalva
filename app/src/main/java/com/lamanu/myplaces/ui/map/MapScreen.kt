package com.lamanu.myplaces.ui.map

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.lamanu.myplaces.R
import com.lamanu.myplaces.core.location.LocationProvider
import com.lamanu.myplaces.ui.common.EmojiMarker
import com.lamanu.myplaces.ui.detail.PlaceDetailSheet

/**
 * Ecran principal : carte plein ecran, marqueurs emoji, appui long pour creer un lieu.
 */
@OptIn(
    ExperimentalPermissionsApi::class,
    ExperimentalMaterial3Api::class,
    MapsComposeExperimentalApi::class,
)
@Composable
fun MapScreen(
    onAddPlace: (latitude: Double, longitude: Double) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val event by viewModel.events.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)

    // La permission peut etre accordee depuis les reglages systeme : on resynchronise a chaque
    // recomposition declenchee par un changement d'etat de la permission.
    LaunchedEffect(locationPermission.status.isGranted) {
        if (locationPermission.status.isGranted) {
            viewModel.onLocationPermissionGranted()
        } else {
            locationPermission.launchPermissionRequest()
        }
    }

    LaunchedEffect(event) {
        event?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeEvent()
        }
    }

    val start = uiState.userLocation ?: LocationProvider.FALLBACK
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(start.latitude, start.longitude), DEFAULT_ZOOM)
    }

    // Premier fix GPS : on recentre une seule fois, sans confisquer la camera ensuite.
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { location ->
            cameraPositionState.position =
                CameraPosition.fromLatLngZoom(LatLng(location.latitude, location.longitude), DEFAULT_ZOOM)
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val target = cameraPositionState.position.target
                    onAddPlace(target.latitude, target.longitude)
                },
            ) {
                Text(text = "+", style = MaterialTheme.typography.headlineSmall)
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermission.status.isGranted),
                uiSettings = MapUiSettings(myLocationButtonEnabled = false, zoomControlsEnabled = false),
                onMapLongClick = { latLng -> onAddPlace(latLng.latitude, latLng.longitude) },
            ) {
                uiState.places.forEach { place ->
                    // `key` evite que Compose recycle un marqueur d'un lieu sur un autre.
                    MarkerComposable(
                        keys = arrayOf(place.id, place.emoji),
                        state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                        title = place.title,
                        onClick = {
                            viewModel.onMarkerClick(place)
                            true
                        },
                    ) {
                        EmojiMarker(emoji = place.emoji, isOwn = place.isOwn)
                    }
                }
            }

            FilterBar(
                selected = uiState.filter,
                onSelect = viewModel::onFilterChange,
                modifier = Modifier.align(Alignment.TopCenter).padding(12.dp),
            )

            if (uiState.places.isEmpty()) {
                Surface(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 3.dp,
                ) {
                    Text(
                        text = stringResource(R.string.map_empty),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            IconButton(
                onClick = viewModel::refreshLocation,
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp),
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = stringResource(R.string.map_my_location))
            }
        }
    }

    uiState.selectedPlace?.let { place ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = viewModel::onSheetDismiss,
            sheetState = sheetState,
        ) {
            PlaceDetailSheet(
                place = place,
                onDelete = { viewModel.deletePlace(place.id) },
                onRetryAddress = { viewModel.retryAddressLookup(place.id) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBar(
    selected: PlaceFilter,
    onSelect: (PlaceFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 3.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PlaceFilter.entries.forEach { filter ->
                FilterChip(
                    selected = filter == selected,
                    onClick = { onSelect(filter) },
                    label = { Text(filter.label) },
                )
            }
        }
    }
}

private const val DEFAULT_ZOOM = 13f
