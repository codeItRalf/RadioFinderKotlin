package app.codeitralf.radiofinder.ui.details

import android.util.Log
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.model.StationCheck
import app.codeitralf.radiofinder.data.repository.RadioRepository
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailsViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val repository: RadioRepository,
    private val serviceConnectionManager: ServiceConnectionManager
) : ViewModel() {

    private val _stationChecks = MutableStateFlow<List<StationCheck>>(emptyList())
    val stationChecks = _stationChecks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation = _currentStation.asStateFlow()


    init {
        observePlayerService()
    }

    @OptIn(UnstableApi::class)
    private fun observePlayerService() {
        Log.d("DetailsViewModel", "Starting service observation")

        serviceConnectionManager.bindService { service ->
            Log.d("DetailsViewModel", "Service callback received: ${service != null}")

            service?.let { playerService ->
                // Observe current station
                viewModelScope.launch {
                    try {
                        playerService.currentStation.collect { station ->
                            Log.d("DetailsViewModel", "Station updated: ${station?.name}")
                            _currentStation.value = station
                        }
                    } catch (e: Exception) {
                        Log.e("DetailsViewModel", "Error collecting station", e)
                    }
                }

                // Observe playing state
                viewModelScope.launch {
                    try {
                        playerService.isPlaying.collect { playing ->
                            Log.d("DetailsViewModel", "Playing state updated: $playing")
                            _isPlaying.value = playing
                        }
                    } catch (e: Exception) {
                        Log.e("DetailsViewModel", "Error collecting playing state", e)
                    }
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun playPause(station: RadioStation?) {
        serviceConnectionManager.getService()?.playPause(station)
    }

    fun getStationCheck(stationUuid: String) {
        if (_isLoading.value) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.getStationCheck(stationUuid)
                _stationChecks.value = result
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }


}
