package app.codeitralf.radiofinder.data.api.request

data class StationCheckRequest(
    val stationuuid: String,
    val limit: Int = 1
) : Map<String, String> by mapOf(
    "stationuuid" to stationuuid,
    "limit" to limit.toString()
)
