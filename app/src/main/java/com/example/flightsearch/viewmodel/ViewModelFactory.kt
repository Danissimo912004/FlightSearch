package com.example.flightsearch.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flightsearch.repository.FlightRepository

class FlightViewModelFactory(
    private val repository: FlightRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlightViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlightViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}