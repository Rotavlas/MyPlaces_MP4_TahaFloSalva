package com.myplaces.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingApi {

    @GET("reverse/")
    suspend fun reverseGeocode(
        @Query("lon") longitude: Double,
        @Query("lat") latitude: Double
    ): GeocodingResponse
}
