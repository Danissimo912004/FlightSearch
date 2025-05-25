package com.example.flightsearch.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "FlightSearch.db"
            )
                .createFromAsset("databases/FlightSearch.db") // если база из assets
                .build()
            INSTANCE = instance
            instance
        }
    }
}
