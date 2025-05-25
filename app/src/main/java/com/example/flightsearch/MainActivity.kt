package com.example.flightsearch

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flightsearch.UI.FlightAdapter
import com.example.flightsearch.UI.RouteAdapter
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.DatabaseProvider
import com.example.flightsearch.data.Route
import com.example.flightsearch.databinding.ActivityMainBinding
import com.example.flightsearch.repository.FlightRepository
import com.example.flightsearch.viewmodel.FlightViewModel
import com.example.flightsearch.viewmodel.FlightViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var airportAdapter: FlightAdapter
    private lateinit var routeAdapter: RouteAdapter
    private lateinit var viewModel: FlightViewModel

    private fun clearSearch() {
        binding.airportSearchInput.setText("")
        viewModel.updateSearchQuery("")
        viewModel.updateSelectedAirport(null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        window.decorView.systemUiVisibility = 0

        // Инициализация ViewModel
        val db = DatabaseProvider.getDatabase(applicationContext)
        val repository = FlightRepository(db.airportDao(), db.favoriteDao())
        val viewModelFactory = FlightViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[FlightViewModel::class.java]

        val clearButton = binding.clearButton
        val searchInput = binding.airportSearchInput
        clearButton.visibility = View.GONE

        // Отслеживаем изменения текста, показываем кнопку очистки если выбран аэропорт
        searchInput.doOnTextChanged { text, _, _, _ ->
            clearButton.visibility =
                if (viewModel.selectedAirport.value != null) View.VISIBLE else View.GONE
            viewModel.updateSearchQuery(text.toString())
        }

        // Отслеживаем изменения выбранного аэропорта, чтобы обновлять видимость кнопки
        lifecycleScope.launch {
            viewModel.selectedAirport.collectLatest { selectedIata ->
                clearButton.visibility = if (selectedIata != null) View.VISIBLE else View.GONE
            }
        }

        clearButton.setOnClickListener {
            clearSearch()
        }

        // Подписываемся на событие очистки из ViewModel
        lifecycleScope.launch {
            viewModel.clearSearchEvent.collectLatest {
                clearSearch()
            }
        }

        // Адаптер для списка аэропортов (поисковая выдача)
        airportAdapter = FlightAdapter { airport ->
            viewModel.updateSelectedAirport(airport.iata_code)
            binding.airportSearchInput.setText("") // очищаем строку поиска
            binding.flightListRecyclerView.adapter = routeAdapter
        }

        // Адаптер для маршрутов
        routeAdapter = RouteAdapter { route ->
            viewModel.toggleFavorite(route)
            // clearSearch() здесь не нужен, очистка по событию из ViewModel
        }

        // Настройка RecyclerView
        binding.flightListRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.flightListRecyclerView.adapter = routeAdapter

        // Обновление списка избранных в адаптере
        lifecycleScope.launch {
            viewModel.favorites.collectLatest { favorites ->
                routeAdapter.favorites = favorites
                routeAdapter.notifyDataSetChanged()
            }
        }

        // Подписка на все необходимые данные и переключение адаптеров/списков
        lifecycleScope.launch {
            combine(
                listOf(
                    viewModel.searchQuery,
                    viewModel.selectedAirport,
                    viewModel.favoriteRoutes,
                    viewModel.routesFromSelectedAirport,
                    viewModel.airports,
                    viewModel.filteredRoutesFromSelectedAirport
                )
            ) { arrayOfValues ->
                val query = arrayOfValues[0] as String
                val selectedIata = arrayOfValues[1] as String?
                val favoriteRoutes = arrayOfValues[2] as List<Route>
                val routesFromAirport = arrayOfValues[3] as List<Route>
                val airports = arrayOfValues[4] as List<Airport>
                val filteredRoutes = arrayOfValues[5] as List<Route>

                QuerySelectedData(
                    query,
                    selectedIata,
                    favoriteRoutes,
                    routesFromAirport,
                    airports,
                    filteredRoutes
                )
            }.collectLatest { data ->
                when {
                    data.query.isBlank() && data.selectedIata == null -> {
                        binding.flightListRecyclerView.adapter = routeAdapter
                        routeAdapter.submitList(data.favoriteRoutes)
                    }

                    data.query.isBlank() && data.selectedIata != null -> {
                        binding.flightListRecyclerView.adapter = routeAdapter
                        routeAdapter.submitList(data.routesFromAirport)
                    }

                    data.query.isNotBlank() && data.selectedIata == null -> {
                        binding.flightListRecyclerView.adapter = airportAdapter
                        airportAdapter.submitList(data.airports)
                    }

                    data.query.isNotBlank() && data.selectedIata != null -> {
                        binding.flightListRecyclerView.adapter = routeAdapter
                        routeAdapter.submitList(data.filteredRoutes)
                    }
                }
            }
        }
    }
}

// Вспомогательный класс для удобства объединения данных
data class QuerySelectedData(
    val query: String,
    val selectedIata: String?,
    val favoriteRoutes: List<Route>,
    val routesFromAirport: List<Route>,
    val airports: List<Airport>,
    val filteredRoutes: List<Route>
)
