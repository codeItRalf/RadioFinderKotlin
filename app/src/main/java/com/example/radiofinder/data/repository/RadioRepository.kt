package com.example.radiofinder.data.repository

import android.util.Log
import com.example.radiofinder.data.model.RadioStation
import com.example.radiofinder.data.model.StationCheck
import com.example.radiofinder.services.RadioService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RadioRepository private constructor() {

    private val radioService: RadioService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://at1.api.radio-browser.info/json/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        radioService = retrofit.create(RadioService::class.java)
    }

    suspend fun searchStationsByName(searchTerm: String, limit: Int, offset: Int): List<RadioStation> {
        val queryParams = mapOf(
            "name" to searchTerm,
            "limit" to limit.toString(),
            "offset" to offset.toString(),
            "hidebroken" to "true"
        )

        return try {
            val result = withContext(Dispatchers.IO) {
                radioService.searchByName(queryParams)
            }
            Log.d("RadioRepository", "Result: ${result.size}")
            result.map {
                RadioStation.fromJson(it) }

        } catch (e: Exception) {
            // Log the error
            e.printStackTrace()
            throw e
        }
    }


    suspend fun clickCounter(stationUuid: String): Map<String, Any> {
        val queryParams = mapOf("stationUuid" to stationUuid)
        return try {
            withContext(Dispatchers.IO) {
                radioService.clickCounter(queryParams)
            }
        } catch (e: Exception) {
            // Log the error
            e.printStackTrace()
            throw e
        }
    }

    suspend fun getStationCheck(stationUuid: String): List<StationCheck> {
        val queryParams = mapOf("stationUuid" to stationUuid)
        return try {
          val result =   withContext(Dispatchers.IO) {
                radioService.getStationCheck(queryParams)
            }

            result.map {
                StationCheck.fromJson(it)
            }

        } catch (e: Exception) {
            // Log the error
            e.printStackTrace()
            throw e
        }
    }

    companion object {
        @Volatile private var instance: RadioRepository? = null

        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: RadioRepository().also { instance = it }
            }
    }
}
