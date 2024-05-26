package com.example.radiofinder.ui.details

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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
import com.example.radiofinder.services.PlayerService
import com.squareup.picasso.Picasso

class DetailsActivity : AppCompatActivity() {

    private var playerService: PlayerService? = null
    private var isBound = false
    private lateinit var playButton: Button
    private lateinit var volumeControl: SeekBar
    private lateinit var stationImage: ImageView
    private lateinit var stationName: TextView
    private lateinit var stationDescription: TextView
    private lateinit var stationTags: TextView
    private lateinit var stationBitrate: TextView
    private lateinit var stationLanguage: TextView
    private lateinit var stationVotes: TextView
    private lateinit var station: RadioStation

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PlayerService.PlayerBinder
            playerService = binder.service
            isBound = true
            setupPlayerControls(station)
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            playerService = null
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        station = intent.getParcelableExtra<RadioStation>("station") ?: run {
            showErrorAndClose("Station data is missing")
            return
        }

        initViews()
        bindDataToViews(station)

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

    private fun setupPlayerControls(station: RadioStation) {
        if (!isBound) return

        playButton.setOnClickListener {
            playerService?.let {
                if (it.isPlaying()) {
                    it.pause()
                    playButton.text = "Play"
                } else {
                    it.play(station.resolvedUrl!!)
                    playButton.text = "Pause"
                }
            }
        }

        volumeControl.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                playerService?.setVolume(progress / 100f)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    override fun onStart() {
        super.onStart()
        Intent(this, PlayerService::class.java).also { intent ->
            bindService(intent, connection, BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()
        unbindService(connection)
        isBound = false
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish() // Close the activity if the data is invalid
    }
}
