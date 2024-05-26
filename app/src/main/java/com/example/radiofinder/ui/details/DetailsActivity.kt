package com.example.radiofinder.ui.details

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation
import com.squareup.picasso.Picasso

class DetailsActivity : AppCompatActivity() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var playButton: Button
    private lateinit var volumeControl: SeekBar
    private lateinit var stationImage: ImageView
    private lateinit var stationName: TextView
    private lateinit var stationDescription: TextView
    private lateinit var stationTags: TextView
    private lateinit var stationBitrate: TextView
    private lateinit var stationLanguage: TextView
    private lateinit var stationVotes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val station = intent.getParcelableExtra<RadioStation>("station") ?: run {
            showErrorAndClose("Station data is missing")
            return
        }

        initViews()
        bindDataToViews(station)
        setupExoPlayer(station)
    }

    private fun initViews() {
        stationImage = findViewById(R.id.stationImage)
        stationName = findViewById(R.id.stationName)
        stationDescription = findViewById(R.id.stationDescription)
        stationTags = findViewById(R.id.stationTags)
        stationBitrate = findViewById(R.id.stationBitrate)
        stationLanguage = findViewById(R.id.stationLanguage)
        stationVotes = findViewById(R.id.stationVotes)
        playButton = findViewById(R.id.playButton)
        volumeControl = findViewById(R.id.volumeControl)
    }

    private fun bindDataToViews(station: RadioStation) {
        stationName.text = station.name
        stationDescription.text = station.country
        stationTags.text = "Tags: ${station.tags ?: "N/A"}"
        stationBitrate.text = "Bitrate: ${station.bitrate ?: "N/A"} kbps"
        stationLanguage.text = "Language: ${station.language ?: "N/A"}"
        stationVotes.text = "Votes: ${station.votes ?: "N/A"}"
        if (!station.favicon.isNullOrBlank()) {
            Picasso.get().load(station.favicon).into(stationImage)
        }
    }

    private fun setupExoPlayer(station: RadioStation) {
        if (station.resolvedUrl.isNullOrBlank()) {
            showErrorAndClose("Resolved URL is empty or invalid")
            return
        }

        try {
            exoPlayer = ExoPlayer.Builder(this)
                .setMediaSourceFactory(DefaultMediaSourceFactory(this))
                .build()

            val mediaItem = MediaItem.fromUri(station.resolvedUrl)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
        } catch (e: Exception) {
            showErrorAndClose("Failed to load media: ${e.message}")
            return
        }

        playButton.setOnClickListener {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
                playButton.text = "Play"
            } else {
                exoPlayer.play()
                playButton.text = "Pause"
            }
        }

        volumeControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                exoPlayer.volume = progress / 100f
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish() // Close the activity if the data is invalid
    }
}
