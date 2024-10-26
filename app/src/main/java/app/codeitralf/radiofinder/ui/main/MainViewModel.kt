package app.codeitralf.radiofinder.ui.main

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.repository.RadioRepository
import app.codeitralf.radiofinder.services.ServiceConnectionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val radioRepository: RadioRepository,
    private val serviceConnectionManager: ServiceConnectionManager
) : ViewModel() {

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations = _stations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation = _currentStation.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private var searchJob: Job? = null
    private var searchTerm = ""
    private val limit = 30 // Preserved from old ViewModel

    init {
        observePlayerService()
        searchStations("")
    }

    @OptIn(UnstableApi::class)
    fun searchStations(query: String) {
        searchJob?.cancel()
        searchTerm = query // Save search term for loadNextPage
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = radioRepository.searchStationsByName(query, limit, 0)
                _stations.value = result.filter {
                    !it.name.isNullOrBlank() && !it.resolvedUrl.isNullOrBlank()
                }
            } catch (e: Exception) {
                Log.e("app.codeitralf.radiofinder.ui.main.MainViewModel", "Failed to fetch stations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun loadNextPage() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val currentSize = _stations.value.size
                val result = radioRepository.searchStationsByName(
                    searchTerm,
                    limit,
                    currentSize
                )
                if (result.isNotEmpty()) {
                    _stations.value += result.filter {
                        !it.name.isNullOrBlank() && !it.resolvedUrl.isNullOrBlank()
                    }
                }
            } catch (e: Exception) {
                Log.e("app.codeitralf.radiofinder.ui.main.MainViewModel", "Failed to load next page", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun playPause(station: RadioStation?) {
        serviceConnectionManager.getService()?.playPause(station)
    }

    @OptIn(UnstableApi::class)
    private fun observePlayerService() {
        serviceConnectionManager.bindService { service ->
            service?.let { playerService ->
                viewModelScope.launch {
                    playerService.currentStation.collect { station ->
                        _currentStation.value = station
                        station?.let {
                            try {
                                radioRepository.clickCounter(it.stationUuid)
                            } catch (e: Exception) {
                                Log.e("app.codeitralf.radiofinder.ui.main.MainViewModel", "Failed to update click counter", e)
                            }
                        }
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

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}