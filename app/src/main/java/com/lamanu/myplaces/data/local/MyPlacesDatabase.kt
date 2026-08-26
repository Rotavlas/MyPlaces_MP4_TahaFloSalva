package com.lamanu.myplaces.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PlaceEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class MyPlacesDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao

    companion object {
        const val NAME = "myplaces.db"
    }
}
