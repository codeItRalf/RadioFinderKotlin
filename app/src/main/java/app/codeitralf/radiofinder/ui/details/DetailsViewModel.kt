package app.codeitralf.radiofinder.ui.details

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
        serviceConnectionManager.bindService { service ->
            service?.let { playerService ->
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
