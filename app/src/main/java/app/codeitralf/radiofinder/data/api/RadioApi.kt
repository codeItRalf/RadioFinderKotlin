package app.codeitralf.radiofinder.data.api

import app.codeitralf.radiofinder.data.api.request.SearchStationsRequest
import app.codeitralf.radiofinder.data.api.request.StationCheckRequest
import app.codeitralf.radiofinder.data.api.response.ClickCounterResponse
import app.codeitralf.radiofinder.data.api.response.RadioStationResponse
import app.codeitralf.radiofinder.data.api.response.StationCheckResponse
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.QueryMap

interface RadioApi {
    @GET("stations/search")
    suspend fun searchByName(
        @QueryMap queryParams: SearchStationsRequest
    ): List<RadioStationResponse>

    @POST("url/{stationUuid}")
    suspend fun clickCounter(
        @Path("stationUuid") stationUuid: String
    ): ClickCounterResponse

    @GET("checks")
    suspend fun getStationCheck(
        @QueryMap queryParams: StationCheckRequest
    ): List<StationCheckResponse>
}