package app.codeitralf.radiofinder.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.codeitralf.radiofinder.data.model.StationCheck
import app.codeitralf.radiofinder.data.repository.RadioRepository
import kotlinx.coroutines.launch

class DetailsViewModel : ViewModel() {
 private  val repository = RadioRepository.getInstance()
    private  val _stationChecks = MutableLiveData<List<StationCheck>>()
    val stationChecks : LiveData<List<StationCheck>> get() = _stationChecks

    private  val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> get() = _isLoading


    fun getStationCheck(stationUuid: String) {
        if (_isLoading.value == true) return
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