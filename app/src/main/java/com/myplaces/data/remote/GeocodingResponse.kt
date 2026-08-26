package com.myplaces.data.remote

import com.google.gson.annotations.SerializedName

data class GeocodingResponse(val features: List<Feature>)

data class Feature(val properties: Properties)

data class Properties(
    val label: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("postcode") val postcode: String?
)
