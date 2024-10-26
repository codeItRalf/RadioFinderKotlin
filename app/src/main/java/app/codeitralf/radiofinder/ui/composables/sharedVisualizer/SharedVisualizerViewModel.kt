package app.codeitralf.radiofinder.ui.composables.sharedVisualizer

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.services.FFTAudioProcessor
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class SharedVisualizerViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val serviceConnectionManager: ServiceConnectionManager
) : ViewModel() {
    private val _processor = MutableStateFlow<FFTAudioProcessor?>(null)
    val processor = _processor.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation = _currentStation.asStateFlow()


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

}