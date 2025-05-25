package com.example.flightsearch.data


import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface AirportDao {
    @Query("SELECT * FROM airport WHERE iata_code LIKE :query OR name LIKE :query")
    fun searchAirports(query: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport WHERE iata_code = :iataCode LIMIT 1")
    suspend fun getAirportByIata(iataCode: String): Airport?

    @Query("SELECT * FROM airport WHERE iata_code != :iataCode ORDER BY passengers DESC")
    fun getDestinationsFromAirport(iataCode: String): Flow<List<Airport>>

    @Query("SELECT * FROM airport")
    fun getAllAirports(): Flow<List<Airport>>

    @Insert
    suspend fun insertAll(airports: List<Airport>)
}
