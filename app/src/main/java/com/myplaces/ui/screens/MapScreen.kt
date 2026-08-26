package com.myplaces.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.location.LocationServices
import com.myplaces.data.local.PlaceEntity
import com.myplaces.ui.viewmodel.PlacesViewModel
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: PlacesViewModel, onAddPlace: (Double, Double) -> Unit) {
    val context = LocalContext.current
    val places by viewModel.places.collectAsState()
    var selectedPlace by remember { mutableStateOf<PlaceEntity?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationGranted by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        permLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    LaunchedEffect(locationGranted) {
        if (locationGranted) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation
                .addOnSuccessListener { loc ->
                    loc?.let { mapViewRef?.controller?.animateTo(GeoPoint(it.latitude, it.longitude)) }
                }
        }
    }

    LaunchedEffect(places, mapViewRef) {
        val mapView = mapViewRef ?: return@LaunchedEffect
        refreshMarkers(mapView, places, context) { place -> selectedPlace = place }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)   // pinch-to-zoom
                        setBuiltInZoomControls(true)  // boutons +/- visibles
                        controller.setZoom(14.0)
                        controller.setCenter(GeoPoint(48.8566, 2.3522))

                        // Point bleu de position
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                        locationOverlay.enableMyLocation()
                        overlays.add(locationOverlay)

                        // Clic long pour ajouter un lieu
                        overlays.add(MapEventsOverlay(object : MapEventsReceiver {
                            override fun singleTapConfirmedHelper(p: GeoPoint?) = false
                            override fun longPressHelper(p: GeoPoint?): Boolean {
                                p?.let { onAddPlace(it.latitude, it.longitude) }
                                return true
                            }
                        }))

                        mapViewRef = this
                    }
                }
        )

        // FAB ajout centré sur la vue
        FloatingActionButton(
            onClick = {
                val center = mapViewRef?.mapCenter
                if (center != null) onAddPlace(center.latitude, center.longitude)
            },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text("＋", fontSize = 24.sp, color = Color.White)
        }
    }

    selectedPlace?.let { place ->
        PlaceDetailSheet(
            place = place,
            onDismiss = { selectedPlace = null },
            onDelete = { viewModel.deletePlace(it) }
        )
    }
}

private fun refreshMarkers(
    mapView: MapView,
    places: List<PlaceEntity>,
    context: Context,
    onMarkerClick: (PlaceEntity) -> Unit
) {
    mapView.overlays.removeAll { it is Marker }
    places.forEach { place ->
        mapView.overlays.add(Marker(mapView).apply {
            position = GeoPoint(place.latitude, place.longitude)
            title = place.title
            icon = createEmojiDrawable(context, place.emoji)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            setOnMarkerClickListener { _, _ -> onMarkerClick(place); true }
        })
    }
    mapView.invalidate()
}

private fun createEmojiDrawable(context: Context, emoji: String): android.graphics.drawable.Drawable {
    val size = 120
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        setShadowLayer(6f, 0f, 3f, android.graphics.Color.argb(80, 0, 0, 0))
    }
    canvas.drawRoundRect(4f, 4f, size - 4f, size - 4f, 20f, 20f, bgPaint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 64f
        textAlign = Paint.Align.CENTER
    }
    val yPos = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(emoji, size / 2f, yPos, textPaint)

    return android.graphics.drawable.BitmapDrawable(context.resources, bitmap)
}
