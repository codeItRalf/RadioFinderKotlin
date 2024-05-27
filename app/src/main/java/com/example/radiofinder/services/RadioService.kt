package com.example.radiofinder.services

import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface RadioService {
    @GET("stations/search")
    suspend fun searchByName(
        @QueryMap options: Map<String, String>
    ): List<Map<String, Any>>

    @POST("url/{stationUuid}")
    suspend fun clickCounter(
        @QueryMap options: Map<String, String>
    ): Map<String, Any>

    @GET("checks/{stationUuid}")
    suspend fun getStationCheck(
        @QueryMap options: Map<String, String>
    ): List<Map<String, Any>>
}