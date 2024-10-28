package app.codeitralf.radiofinder.data.repository

import android.util.Log
import app.codeitralf.radiofinder.data.api.RadioApi
import app.codeitralf.radiofinder.data.api.request.SearchStationsRequest
import app.codeitralf.radiofinder.data.api.request.StationCheckRequest
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.model.StationCheck
import app.codeitralf.radiofinder.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepository @Inject constructor(
    private val radioApi: RadioApi,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    suspend fun searchStationsByName(
        searchTerm: String,
        limit: Int,
        offset: Int
    ): List<RadioStation> = withContext(dispatcher) {
        try {
            val request = SearchStationsRequest(
                name = searchTerm,
                limit = limit,
                offset = offset
            )
            radioApi.searchByName(request).map { it.toRadioStation() }
        } catch (e: Exception) {
            Log.e("RadioRepository", "Search failed: ${e.message}")
            throw e
        }
    }

    suspend fun clickCounter(stationUuid: String): Boolean = withContext(dispatcher) {
        try {
            radioApi.clickCounter(stationUuid).success
        } catch (e: Exception) {
            Log.e("RadioRepository", "Click counter failed: ${e.message}")
            false
        }
    }

    suspend fun getStationCheck(stationUuid: String): List<StationCheck> = withContext(dispatcher) {
        try {
            val request = StationCheckRequest(stationuuid = stationUuid)
            radioApi.getStationCheck(request).map { it.toStationCheck() }
        } catch (e: Exception) {
            Log.e("RadioRepository", "Station check failed: ${e.message}")
            throw e
        }
    }
}
