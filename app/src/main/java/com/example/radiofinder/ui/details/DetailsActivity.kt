package com.example.radiofinder.ui.details

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.example.radiofinder.R
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.data.model.StationCheck
import com.example.radiofinder.services.PlayerService
import com.example.radiofinder.services.ServiceConnectionManager
import com.example.radiofinder.views.ExoVisualizer
import com.squareup.picasso.Picasso

@UnstableApi
class DetailsActivity : AppCompatActivity() {

    private lateinit var serviceConnectionManager: ServiceConnectionManager
    private lateinit var playButton: ImageButton
    private lateinit var stationImage: ImageView
    private lateinit var stationName: TextView
    private lateinit var stationDescription: TextView
    private lateinit var stationTags: TextView
    private lateinit var stationBitrate: TextView
    private lateinit var stationLanguage: TextView
    private lateinit var stationVotes: TextView
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var station: RadioStation
    private lateinit var viewModel: DetailsViewModel
    private lateinit var playButtonLoadingIndicator: ProgressBar
    private var visualizer: ExoVisualizer? = null
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        serviceConnectionManager = ServiceConnectionManager(this)

        station = intent.getParcelableExtra<RadioStation>("station") ?: run {
            showErrorAndClose("Station data is missing")
            return
        }

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initViews()
        bindDataToViews(station)
        setupViewModel()
        setupObservers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[DetailsViewModel::class.java]
        viewModel.getStationCheck(station.stationUuid)
    }

    private fun setupObservers() {
        viewModel.stationChecks.observe(this, Observer { stationChecks ->
            if (stationChecks.isNotEmpty()) {
                val stationCheck = stationChecks[0]
                bindStationCheckToViews(stationCheck)
            }
        })
        viewModel.isLoading.observe(this, Observer { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun bindStationCheckToViews(stationCheck: StationCheck) {
        stationTags.text = "Tags: ${stationCheck.tags ?: "N/A"}"
        stationBitrate.text = "Bitrate: ${stationCheck.bitrate} kbps"
        stationLanguage.text = "Language: ${stationCheck.languageCodes ?: "N/A"}"
        // Update this to bind other relevant stationCheck fields to views
    }


    override fun onStart() {
        super.onStart()
        serviceConnectionManager.bindService { playerService ->
            if (playerService != null) {
                setupPlayerControls(station)
            }
            playerService?.isPlaying?.observe(this, Observer { playing ->
                if (playing && playerService.getStation() == station) {
                    playButton.setImageResource(android.R.drawable.ic_media_pause)
                    connectToAudioVisualization(playerService)


                } else {
                    playButton.setImageResource(android.R.drawable.ic_media_play)
                }
            })

            playerService?.isLoading?.observe(this, Observer { loading ->
                if (loading) {
                    playButton.visibility = View.GONE
                    playButtonLoadingIndicator.visibility = View.VISIBLE
                } else {
                    playButton.visibility = View.VISIBLE
                    playButtonLoadingIndicator.visibility = View.GONE
                }
            })
        }
    }

    private fun connectToAudioVisualization(playerService: PlayerService?) {
        if(visualizer != null) {
            return;
        }
        visualizer = findViewById<ExoVisualizer>(R.id.visualizer)
        visualizer?.processor = playerService?.getAudioProcessor()
        visualizer?.updateProcessorListenerState(true)
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
        loadingIndicator = findViewById(R.id.loadingIndicator)
        playButtonLoadingIndicator = findViewById(R.id.playButtonLoadingIndicator)
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
        playButton.setOnClickListener {
            serviceConnectionManager.getService()?.let {
                connectToAudioVisualization(it)
                it.playPause(station)
            }

        }
    }

    private fun showErrorAndClose(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        finish() // Close the activity if the data is invalid
    }



    override fun onDestroy() {
        super.onDestroy()
        serviceConnectionManager.unbindService()
    }
}
