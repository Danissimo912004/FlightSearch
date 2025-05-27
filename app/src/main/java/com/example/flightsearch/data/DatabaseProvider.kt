package com.example.flightsearch.data

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        val tempInstance = INSTANCE
        if (tempInstance != null) {
            return tempInstance
        }

        synchronized(this) {
            val instance = INSTANCE
            if (instance != null) {
                return instance
            }

            val newInstance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "FlightSearch.db"
            )
                .createFromAsset("databases/FlightSearch.db")
                .build()

            INSTANCE = newInstance
            return newInstance
        }
    }
}