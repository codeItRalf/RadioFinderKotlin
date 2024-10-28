package app.codeitralf.radiofinder.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.repository.RadioRepository
import app.codeitralf.radiofinder.services.FFTAudioProcessor
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class SharedPlayerViewModel @Inject constructor(
    private val serviceConnectionManager: ServiceConnectionManager,
    private val radioRepository: RadioRepository
) : ViewModel() {

    data class PlayerState(
        val currentStation: RadioStation? = null,
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val processor: FFTAudioProcessor? = null
    )

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    init {
        initializePlayerService()
    }

    private fun initializePlayerService() {
        serviceConnectionManager.bindService { service ->
            service?.let { playerService ->
                // Set audio processor
                updatePlayerState {
                    it.copy(processor = playerService.getAudioProcessor())
                }

                // Observe station changes
                viewModelScope.launch {
                    playerService.currentStation.collect { station ->
                        updatePlayerState { it.copy(currentStation = station) }
                        updateClickCounter(station)
                    }
                }

                // Observe loading state
                viewModelScope.launch {
                    playerService.isLoading.collect { loading ->
                        updatePlayerState { it.copy(isLoading = loading) }
                    }
                }

                // Observe playing state
                viewModelScope.launch {
                    playerService.isPlaying.collect { playing ->
                        updatePlayerState { it.copy(isPlaying = playing) }
                    }
                }
            }
        }
    }

    private fun updatePlayerState(update: (PlayerState) -> PlayerState) {
        _playerState.value = update(_playerState.value)
    }

    private suspend fun updateClickCounter(station: RadioStation?) {
        station?.let {
            try {
                radioRepository.clickCounter(it.stationUuid)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update click counter", e)
            }
        }
    }

    fun playPause(station: RadioStation?) {
        serviceConnectionManager.getService()?.playPause(station)
    }

    companion object {
        private const val TAG = "SharedPlayerViewModel"
    }
}

