package app.codeitralf.radiofinder.data.api.request

data class SearchStationsRequest(
    val name: String,
    val limit: Int,
    val offset: Int,
    val hidebroken: Boolean = true
) : Map<String, String> by mapOf(
    "name" to name,
    "limit" to limit.toString(),
    "offset" to offset.toString(),
    "hidebroken" to hidebroken.toString()
)