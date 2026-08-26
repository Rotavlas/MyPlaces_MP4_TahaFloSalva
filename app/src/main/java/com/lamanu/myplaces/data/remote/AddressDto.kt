package com.lamanu.myplaces.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Reponse GeoJSON de https://api-adresse.data.gouv.fr/reverse/
 * Seuls les champs reellement utilises sont declares : le Json du module reseau est
 * configure avec `ignoreUnknownKeys = true`.
 */
@Serializable
data class ReverseGeocodingResponse(
    @SerialName("features") val features: List<AddressFeature> = emptyList(),
)

@Serializable
data class AddressFeature(
    @SerialName("properties") val properties: AddressProperties,
)

@Serializable
data class AddressProperties(
    /** Adresse complete formatee, ex. "8 Boulevard du Port 80000 Amiens". */
    @SerialName("label") val label: String? = null,
    @SerialName("housenumber") val houseNumber: String? = null,
    @SerialName("street") val street: String? = null,
    @SerialName("postcode") val postcode: String? = null,
    @SerialName("city") val city: String? = null,
    @SerialName("context") val context: String? = null,
)
