package com.example.radiofinder.services

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface RadioService {
    @GET("stations/bytag/{tag}")
    suspend fun getStationsByTag(
        @Path("tag") tag: String,
        @QueryMap options: Map<String, String>
    ): List<Map<String, Any>>
}