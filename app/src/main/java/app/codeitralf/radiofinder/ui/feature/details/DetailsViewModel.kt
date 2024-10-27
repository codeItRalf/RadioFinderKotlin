package app.codeitralf.radiofinder.ui.feature.details

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import app.codeitralf.radiofinder.data.model.StationCheck
import app.codeitralf.radiofinder.data.repository.RadioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DetailsViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    private val repository: RadioRepository,
) : ViewModel() {

    private val _stationChecks = MutableStateFlow<List<StationCheck>>(emptyList())
    val stationChecks = _stationChecks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()



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
