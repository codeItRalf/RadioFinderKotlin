package com.example.radiofinder.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.data.repository.RadioRepository

import kotlinx.coroutines.launch

class RadioViewModel : ViewModel() {
    private val repository = RadioRepository.getInstance()
    val stations: MutableLiveData<List<RadioStation>> = MutableLiveData()

    fun fetchStations(tag: String, limit: Int, offset: Int) {
        Log.d("RadioViewModel", "Fetching stations by tag: $tag")
        viewModelScope.launch {
            try {
                val result = repository.getStationsByTag(tag, limit, offset)
                Log.d("RadioViewModel", "Fetched ${result.size} stations")
                stations.value = result
            } catch (e: Exception) {
                Log.e("RadioViewModel", "Failed to fetch stations", e)
            }
        }
    }
}
