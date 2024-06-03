package com.example.radiofinder.utils

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.media3.session.SessionToken
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.services.PlayerService

@UnstableApi
object PlayerInitializer {
    fun initializePlayer(
        context: Context,
        service: PlayerService,
        currentStation: () -> RadioStation?,
        onPlayerInitialized: (ExoPlayer, MediaSession) -> Unit
    ) {
        val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
        val renderersFactory = RenderersFactoryUtil.createRenderersFactory(context, service.getAudioProcessor())
        val exoPlayer = ExoPlayer.Builder(context, renderersFactory).build()
        val mediaSession = MediaSession.Builder(context, exoPlayer).build()

        exoPlayer.addListener(service.createPlayerListener())

        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            val mediaItem = MediaItem.Builder()
                .setMediaId(currentStation()?.stationUuid ?: "")
                .setMediaMetadata(MediaMetadata.Builder().setArtist(currentStation()?.name ?: "").build())
                .build()
            mediaController.setMediaItem(mediaItem)
        }, ContextCompat.getMainExecutor(context))

        onPlayerInitialized(exoPlayer, mediaSession)
    }
}
