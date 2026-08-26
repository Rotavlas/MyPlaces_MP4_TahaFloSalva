package com.lamanu.myplaces.core.location

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

data class Coordinates(val latitude: Double, val longitude: Double)

/**
 * Position courante via les Play Services.
 *
 * L'appelant est responsable d'avoir obtenu ACCESS_FINE_LOCATION : la methode est annotee
 * [SuppressLint] et renvoie simplement `null` si la permission manque.
 */
@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val client by lazy { LocationServices.getFusedLocationProviderClient(context) }

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): Coordinates? = suspendCancellableCoroutine { continuation ->
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .setMaxUpdateAgeMillis(MAX_AGE_MILLIS)
            .build()

        runCatching {
            client.getCurrentLocation(request, null)
                .addOnSuccessListener { location ->
                    continuation.resume(location?.let { Coordinates(it.latitude, it.longitude) })
                }
                .addOnFailureListener { continuation.resume(null) }
        }.onFailure {
            // SecurityException si la permission a ete revoquee entre-temps.
            continuation.resume(null)
        }
    }

    companion object {
        private const val MAX_AGE_MILLIS = 60_000L

        /** Repli quand la position est inconnue : centre de la France metropolitaine. */
        val FALLBACK = Coordinates(latitude = 46.6034, longitude = 1.8883)
    }
}
