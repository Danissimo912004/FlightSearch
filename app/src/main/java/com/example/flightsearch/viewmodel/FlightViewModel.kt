package com.example.flightsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.*
import com.example.flightsearch.repository.FlightRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FlightViewModel(
    private val repository: FlightRepository
) : ViewModel() {

    private val _selectedAirport = MutableStateFlow<String?>(null)
    val selectedAirport: StateFlow<String?> = _selectedAirport.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _clearSearchEvent = Channel<Unit>(Channel.BUFFERED)
    val clearSearchEvent = _clearSearchEvent.receiveAsFlow()

    // Зависимые флоу
    val destinationsFromSelectedAirport: StateFlow<List<Airport>> = _selectedAirport
        .filterNotNull()
        .flatMapLatest { iataCode ->
            repository.getDestinationsFromAirport(iataCode)
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val routesFromSelectedAirport: StateFlow<List<Route>> = _selectedAirport
        .filterNotNull()
        .flatMapLatest { iataCode ->
            repository.getDestinationsFromAirport(iataCode)
                .flatMapLatest { destinations ->
                    flow {
                        val fromAirport = repository.getAirportByIata(iataCode)
                        if (fromAirport != null) {
                            val routes = destinations.map { toAirport ->
                                Route(from = fromAirport, to = toAirport)
                            }
                            emit(routes)
                        } else {
                            emit(emptyList())
                        }
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val favorites: StateFlow<List<Favorite>> = repository.getAllFavorites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteRoutes: StateFlow<List<Route>> = favorites
        .flatMapLatest { favList ->
            flow {
                val routes = repository.getRoutesFromFavorites(favList)
                emit(routes)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val airports: StateFlow<List<Airport>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchAirports(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredRoutesFromSelectedAirport = combine(
        routesFromSelectedAirport,
        searchQuery
    ) { routes, query ->
        if (query.isBlank()) {
            routes
        } else {
            routes.filter { route ->
                route.to.name.contains(query, ignoreCase = true) ||
                        route.to.iata_code.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    // Обновляем поисковый запрос
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    // Обновляем выбранный аэропорт
    fun updateSelectedAirport(iata: String?) {
        _selectedAirport.value = iata
    }

    // Вставляем начальный список аэропортов
    fun insertInitialAirports(airports: List<Airport>) {
        viewModelScope.launch {
            repository.insertAirports(airports)
        }
    }

    // Переключаем избранное: добавляем или удаляем
    fun toggleFavorite(route: Route) {
        viewModelScope.launch {
            val favorite = Favorite(
                departure_code = route.from.iata_code,
                destination_code = route.to.iata_code
            )
            val exists = favorites.value.any {
                it.departure_code == favorite.departure_code &&
                        it.destination_code == favorite.destination_code
            }
            if (exists) {
                repository.deleteFavoriteByCodes(favorite.departure_code, favorite.destination_code)
            } else {
                repository.insertFavorite(favorite)
            }
            // После изменения избранного отправляем событие очистки поиска
            triggerClearSearch()
        }
    }

    // Триггерим событие очистки поиска
    fun triggerClearSearch() {
        viewModelScope.launch {
            _clearSearchEvent.send(Unit)
        }
    }
}
