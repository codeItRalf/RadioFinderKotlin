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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        val station = intent.getParcelableExtra<RadioStation>("station") ?: run {
            showErrorAndClose("Station data is missing")
            return
        }

        stationImage = findViewById(R.id.stationImage)
        stationName = findViewById(R.id.stationName)
        stationDescription = findViewById(R.id.stationDescription)
        playButton = findViewById(R.id.playButton)
        volumeControl = findViewById(R.id.volumeControl)

        stationName.text = station.name
        stationDescription.text = station.country
        Picasso.get().load(station.favicon).into(stationImage)

        if (station.resolvedUrl.isNullOrBlank()) {
            showErrorAndClose("Resolved URL is empty or invalid")
            return
        }

        exoPlayer = ExoPlayer.Builder(this).build()
        val mediaItem = MediaItem.fromUri(station.resolvedUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

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
