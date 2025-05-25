package com.example.flightsearch.UI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.ListAdapter
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.flightsearch.R
import com.example.flightsearch.data.Airport
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.databinding.ItemFlightBinding

class FlightAdapter(
    private val onItemClick: (Airport) -> Unit
) : ListAdapter<Airport, FlightAdapter.FlightViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFlightBinding.inflate(inflater, parent, false)
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FlightViewHolder(private val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(airport: Airport) {
            binding.airportName.text = airport.name
            binding.airportCode.text = airport.iata_code
            binding.root.setOnClickListener { onItemClick(airport) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Airport>() {
        override fun areItemsTheSame(oldItem: Airport, newItem: Airport) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Airport, newItem: Airport) = oldItem == newItem
    }
}
