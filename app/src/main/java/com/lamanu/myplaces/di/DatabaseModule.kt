package com.lamanu.myplaces.di

import android.content.Context
import androidx.room.Room
import com.lamanu.myplaces.data.local.MyPlacesDatabase
import com.lamanu.myplaces.data.local.PlaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MyPlacesDatabase =
        Room.databaseBuilder(context, MyPlacesDatabase::class.java, MyPlacesDatabase.NAME)
            // Pas de fallbackToDestructiveMigration : une migration ratee ne doit pas
            // effacer le journal de l'utilisateur. On ecrira les Migration a la v2.
            .build()

    @Provides
    fun providePlaceDao(database: MyPlacesDatabase): PlaceDao = database.placeDao()
}
