package com.example.flightsearch.data


import com.example.flightsearch.data.AirportDao
import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [Airport::class, Favorite::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun airportDao(): AirportDao
    abstract fun favoriteDao(): FavoriteDao
}
