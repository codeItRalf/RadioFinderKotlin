package app.codeitralf.radiofinder.ui.main

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.codeitralf.radiofinder.R
import app.codeitralf.radiofinder.data.model.RadioStation
import com.squareup.picasso.Picasso

class RadioStationAdapter(
    private val clickListener: (RadioStation) -> Unit,
    private val onPlayClick: (RadioStation) -> Unit
) :
    RecyclerView.Adapter<RadioStationAdapter.ViewHolder>() {

    private var stations: List<RadioStation> = listOf()
    private var currentStation: RadioStation? = null
    private var isPlaying: Boolean = false
    private var currentStationPosition: Int? = null
    private  var isLoading: Boolean = false



    inner class ViewHolder(view: View, private val clickListener: (RadioStation) -> Unit) :
        RecyclerView.ViewHolder(view) {
        private val nameTextView: TextView = view.findViewById(R.id.station_name)
        private val descriptionTextView: TextView = view.findViewById(R.id.station_description)
        private val tagsTextView: TextView = view.findViewById(R.id.station_tags)
        private val stationImageView: ImageView = view.findViewById(R.id.station_image)
        private val playButton: ImageView = view.findViewById(R.id.play_button)
        private val playButtonLoadingIndicator: ProgressBar = view.findViewById(R.id.playButtonLoadingIndicator)

        fun bind(station: RadioStation) {
            nameTextView.text = station.name
            descriptionTextView.text = station.country
            tagsTextView.text = station.tags
            if (!station.favicon.isNullOrBlank()) {
                try {
                Picasso.get().load(station.favicon).into(stationImageView)

                }catch (e: Exception){
                    Log.d("RadioStationAdapter", "Error loading image: ${e.message}")
                }
            }


            if (station.stationUuid == currentStation?.stationUuid && isLoading) {
                playButton.visibility = View.GONE
                playButtonLoadingIndicator.visibility = View.VISIBLE
            } else {
                playButton.visibility = View.VISIBLE
                playButtonLoadingIndicator.visibility = View.GONE
            }

            if (station.stationUuid == currentStation?.stationUuid && isPlaying) {
                playButton.setImageResource(android.R.drawable.ic_media_pause)
            } else {
                playButton.setImageResource(android.R.drawable.ic_media_play)
            }
            playButton.setOnClickListener {
                Log.d("RadioStationAdapter", "Play button clicked")
                onPlayClick(station)
            }
            itemView.setOnClickListener {
                Log.d("RadioStationAdapter", "Station clicked")
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

        val stationsToAdd = newStations.filter { !it.name.isNullOrBlank() && !it.resolvedUrl.isNullOrBlank() && !it.favicon.isNullOrBlank() }

        // Check if this is a pagination update
        val isPagination = if (stationsToAdd.size > stations.size) {
            var isPagination = true
            for (i in stations.indices) {
                if (stationsToAdd[i] != stations[i]) {
                    isPagination = false
                    break
                }
            }
            isPagination
        } else {
            false
        }

        if (isPagination) {
            val oldSize = stations.size
            stations = stations + stationsToAdd.subList(oldSize, stationsToAdd.size)
            notifyItemRangeInserted(oldSize, stationsToAdd.size - oldSize)
        } else {
            stations = stationsToAdd
            notifyDataSetChanged()
        }
    }

    fun setCurrentStation(station: RadioStation?) {
        val oldPosition = getCurrentPosition()
        currentStation = station
        currentStationPosition = stations.indexOf(station)

        notifyItemChanged(oldPosition)
        currentStationPosition?.let { notifyItemChanged(it) }
    }


    fun setIsPlaying(playing: Boolean) {
        isPlaying = playing
        notifyItemChanged(getCurrentPosition())
    }

    fun setIsLoading(loading: Boolean) {
        isLoading = loading
        notifyItemChanged(getCurrentPosition())
    }

    private fun getCurrentPosition(): Int {
        return stations.indexOf(currentStation)
    }
}
