package com.lamanu.myplaces.data.local

import androidx.room.TypeConverter
import com.lamanu.myplaces.domain.model.PlaceOrigin

class Converters {

    @TypeConverter
    fun fromOrigin(origin: PlaceOrigin): String = origin.name

    @TypeConverter
    fun toOrigin(value: String): PlaceOrigin =
        runCatching { PlaceOrigin.valueOf(value) }.getOrDefault(PlaceOrigin.LOCAL)
}
