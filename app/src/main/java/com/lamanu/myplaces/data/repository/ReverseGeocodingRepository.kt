package com.lamanu.myplaces.data.repository

import android.util.Log
import com.lamanu.myplaces.data.remote.GeocodingApi
import com.lamanu.myplaces.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Traduit des coordonnees GPS en adresse postale via un appel REST.
 *
 * L'echec n'est jamais bloquant : un lieu sans reseau se cree quand meme, avec `address = null`.
 * C'est un journal intime hors-ligne d'abord.
 */
@Singleton
class ReverseGeocodingRepository @Inject constructor(
    private val api: GeocodingApi,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun reverseGeocode(latitude: Double, longitude: Double): String? =
        withContext(ioDispatcher) {
            runCatching {
                api.reverse(latitude = latitude, longitude = longitude)
                    .features
                    .firstOrNull()
                    ?.properties
                    ?.label
            }.onFailure { error ->
                Log.w(TAG, "Reverse geocoding indisponible pour $latitude/$longitude", error)
            }.getOrNull()
        }

    private companion object {
        const val TAG = "ReverseGeocoding"
    }
}
