package app.codeitralf.radiofinder.ui.main

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val radioRepository: RadioRepository,
) : ViewModel() {

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    private val stations = _stations.asStateFlow()

    val filteredStations = stations.map { list ->
        list.filter {
            !it.name.isNullOrBlank() && !it.resolvedUrl.isNullOrBlank()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _endReached = MutableStateFlow(false)
    val endReached = _endReached.asStateFlow()

    private var searchJob: Job? = null
    private var searchTerm = ""
    private val limit = 30 // Preserved from old ViewModel

    init {
        searchStations("")
    }

    @OptIn(UnstableApi::class)
    fun searchStations(query: String) {
        searchJob?.cancel()
        searchTerm = query // Save search term for loadNextPage
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _endReached.value = false
            try {
                val result = radioRepository.searchStationsByName(query, limit, 0)
                _stations.value = result.filter {
                    _stations.value.contains(it).not()
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to fetch stations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun loadNextPage() {
        Log.i("MainViewModel", "Loading next page")
        if (_isLoading.value || endReached.value) return

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
                }else{
                    _endReached.value = true
                }


            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to load next page", e)
            } finally {
                _isLoading.value = false
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
    }
}