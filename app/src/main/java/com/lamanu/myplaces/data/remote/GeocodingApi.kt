package com.lamanu.myplaces.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Base Adresse Nationale (BAN) : API publique, gratuite, sans cle.
 * Couverture : France. Hors de France l'API repond une collection vide, ce que
 * [com.lamanu.myplaces.data.repository.ReverseGeocodingRepository] traduit en adresse absente.
 */
interface GeocodingApi {

    @GET("reverse/")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
    ): ReverseGeocodingResponse

    companion object {
        const val BASE_URL = "https://api-adresse.data.gouv.fr/"
    }
}
