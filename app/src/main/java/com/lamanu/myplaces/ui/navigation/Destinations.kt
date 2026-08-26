package com.lamanu.myplaces.ui.navigation

/** Routes de l'application. La fiche detaillee est une bottom sheet portee par la carte. */
object Destinations {

    const val MAP = "map"
    const val SETTINGS = "settings"

    const val ARG_LATITUDE = "lat"
    const val ARG_LONGITUDE = "lng"
    const val ADD_PLACE = "add_place/{$ARG_LATITUDE}/{$ARG_LONGITUDE}"

    fun addPlace(latitude: Double, longitude: Double): String = "add_place/$latitude/$longitude"
}
