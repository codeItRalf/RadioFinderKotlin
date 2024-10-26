package app.codeitralf.radiofinder.data.repository

import android.util.Log
import app.codeitralf.radiofinder.data.di.IoDispatcher
import app.codeitralf.radiofinder.data.model.RadioStation
import app.codeitralf.radiofinder.data.model.StationCheck
import app.codeitralf.radiofinder.services.RadioService
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RadioRepository @Inject constructor(
    private val radioService: RadioService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) {
    // Rest of the repository implementation remains the same
    suspend fun searchStationsByName(
        searchTerm: String,
        limit: Int,
        offset: Int
    ): List<RadioStation> = withContext(dispatcher) {
        try {
            val queryParams = mapOf(
                "name" to searchTerm,
                "limit" to limit.toString(),
                "offset" to offset.toString(),
                "hidebroken" to "true"
            )
            radioService.searchByName(queryParams)
                .map { RadioStation.fromJson(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }


    suspend fun clickCounter(stationUuid: String): Boolean = withContext(dispatcher) {
        try {
            val response = radioService.clickCounter(stationUuid)
            response["ok"] as? Boolean ?: false
        } catch (e: Exception) {
            Log.d("RadioRepository", "Click counter failed", e)
            e.printStackTrace()
            false
        }
    }

    suspend fun getStationCheck(stationUuid: String): List<StationCheck> = withContext(dispatcher) {
        try {
            val queryParams = mapOf(
                "stationuuid" to stationUuid,
                "limit" to "1"
            )
            val result = radioService.getStationCheck(queryParams)
            Log.d("RadioRepository", "getStationCheck: $result")
            result.map { StationCheck.fromJson(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
