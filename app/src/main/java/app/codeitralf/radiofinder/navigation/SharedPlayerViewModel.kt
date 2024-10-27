package app.codeitralf.radiofinder.navigation

import androidx.annotation.OptIn
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
class SharedPlayerViewModel
@Inject constructor(
    private val serviceConnectionManager: ServiceConnectionManager,
    private val radioRepository: RadioRepository
) : ViewModel() {


    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation: StateFlow<RadioStation?> = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Visualizer State
    private val _processor = MutableStateFlow<FFTAudioProcessor?>(null)
    val processor: StateFlow<FFTAudioProcessor?> = _processor.asStateFlow()

    init {
        observePlayerService()
    }

    @OptIn(UnstableApi::class)
    private fun observePlayerService() {
        serviceConnectionManager.bindService { service ->
            service?.let { playerService ->
                _processor.value = playerService.getAudioProcessor()

                viewModelScope.launch {
                    playerService.currentStation.collect { station ->
                        _currentStation.value = station
                        station?.let {
                            try {
                                radioRepository.clickCounter(it.stationUuid)
                            } catch (e: Exception) {
                                Log.e("MainViewModel", "Failed to update click counter", e)
                            }
                        }
                    }
                }

                viewModelScope.launch {
                    playerService.isLoading.collect { loading ->
                        _isLoading.value = loading
                    }
                }

                viewModelScope.launch {
                    playerService.isPlaying.collect { playing ->
                        _isPlaying.value = playing
                    }
                }
            }
        }
    }



    @OptIn(UnstableApi::class)
    fun playPause(station: RadioStation?) {
        serviceConnectionManager.getService()?.playPause(station)
    }


}

