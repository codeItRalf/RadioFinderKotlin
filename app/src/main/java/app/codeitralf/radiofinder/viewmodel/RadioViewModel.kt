package app.codeitralf.radiofinder.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.repository.RadioRepository
import kotlinx.coroutines.launch

class RadioViewModel : ViewModel() {
    private val repository = RadioRepository.getInstance()
    private val _stations = MutableLiveData<List<RadioStation>>()
    val stations: LiveData<List<RadioStation>> get() = _stations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var searchTerm = ""
    private val limit = 30

    fun loadNextPage() {
        if (_isLoading.value == true) return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val result = repository.searchStationsByName(searchTerm, limit, _stations.value?.size ?: 0)
                if (result.isNotEmpty()) {
                    val currentList = _stations.value ?: listOf()
                    val updatedList = currentList + result
                    _stations.value = updatedList
                }
            } catch (e: Exception) {
                Log.d("RadioViewModel", "Failed to fetch stations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchStations(searchTerm: String) {
        if (_isLoading.value == true) return
        _isLoading.value = true
        this.searchTerm = searchTerm

        viewModelScope.launch {
            try {
                val result = repository.searchStationsByName(searchTerm, limit, 0)
                _stations.value = result

            } catch (e: Exception) {
                Log.e("RadioViewModel", "Failed to fetch stations", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
