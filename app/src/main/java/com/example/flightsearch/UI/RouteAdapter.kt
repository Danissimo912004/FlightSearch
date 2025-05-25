package com.example.flightsearch.UI

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.flightsearch.data.Favorite

import com.example.flightsearch.data.Route
import com.example.flightsearch.databinding.ItemRouteBinding

class RouteAdapter(
    private val onAddFavoriteClick: (Route) -> Unit
) : ListAdapter<Route, RouteAdapter.RouteViewHolder>(DiffCallback()) {

    var favorites: List<Favorite> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemRouteBinding.inflate(inflater, parent, false)
        return RouteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RouteViewHolder(private val binding: ItemRouteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(route: Route) {
            binding.departAirport.text = "${route.from.name} (${route.from.iata_code})"
            binding.arriveAirport.text = "${route.to.name} (${route.to.iata_code})"

            val isFavorite = favorites.any {
                it.departure_code == route.from.iata_code && it.destination_code == route.to.iata_code
            }

            val colorRes = if (isFavorite) {
                android.R.color.holo_red_light
            } else {
                android.R.color.black
            }
            binding.favoriteButton.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))

            binding.favoriteButton.setOnClickListener {
                onAddFavoriteClick(route)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Route>() {
        override fun areItemsTheSame(oldItem: Route, newItem: Route) =
            oldItem.from.iata_code == newItem.from.iata_code &&
                    oldItem.to.iata_code == newItem.to.iata_code

        override fun areContentsTheSame(oldItem: Route, newItem: Route) = oldItem == newItem
    }
}
