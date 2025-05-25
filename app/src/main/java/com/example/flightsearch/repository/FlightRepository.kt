package com.example.flightsearch.repository

import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.AirportDao
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FavoriteDao
import com.example.flightsearch.data.Route
import kotlinx.coroutines.flow.Flow

class FlightRepository(
    private val airportDao: AirportDao,
    private val favoriteDao: FavoriteDao
) {
    fun searchAirports(query: String): Flow<List<Airport>> =
        airportDao.searchAirports("%$query%")

    suspend fun getAirportByIata(iataCode: String): Airport? {
        return airportDao.getAirportByIata(iataCode)
    }

    suspend fun deleteFavoriteByCodes(departure: String, destination: String) {
        favoriteDao.deleteByCodes(departure, destination)
    }

    suspend fun getRoutesFromFavorites(favorites: List<Favorite>): List<Route> {
        return favorites.mapNotNull { favorite ->
            val fromAirport = airportDao.getAirportByIata(favorite.departure_code)
            val toAirport = airportDao.getAirportByIata(favorite.destination_code)
            if (fromAirport != null && toAirport != null) {
                Route(from = fromAirport, to = toAirport)
            } else {
                null
            }
        }
    }


    fun getDestinationsFromAirport(iataCode: String): Flow<List<Airport>> =
        airportDao.getDestinationsFromAirport(iataCode)

    suspend fun insertFavorite(favorite: Favorite) = favoriteDao.insert(favorite)
    fun getAllFavorites() = favoriteDao.getAllFavorites()

    suspend fun insertAirports(airports: List<Airport>) =
        airportDao.insertAll(airports)
}
