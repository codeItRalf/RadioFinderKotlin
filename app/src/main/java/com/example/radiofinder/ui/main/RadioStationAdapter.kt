package com.example.radiofinder.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation

class RadioStationAdapter(private val clickListener: (RadioStation) -> Unit) :
    RecyclerView.Adapter<RadioStationAdapter.ViewHolder>() {

    private var stations: List<RadioStation> = listOf()

    class ViewHolder(view: View, private val clickListener: (RadioStation) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.station_name)

        fun bind(station: RadioStation) {
            nameTextView.text = station.name
            itemView.setOnClickListener {
                clickListener(station)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_station, parent, false)
        return ViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stations[position])
    }

    override fun getItemCount(): Int = stations.size

    fun submitList(newStations: List<RadioStation>) {
        stations = newStations
        notifyDataSetChanged()
    }
}
