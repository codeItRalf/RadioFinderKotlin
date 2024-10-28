package app.codeitralf.radiofinder.data.model

data class StationCheck(
    val stationUuid: String,
    val checkUuid: String,
    val source: String,
    val codec: String,
    val bitrate: Int,
    val hls: Int,
    val ok: Int,
    val timestamp: String,
    val timestampIso8601: String,
    val urlCache: String?,
    val metaInfoOverridesDatabase: Int,
    val public: String?,
    val name: String?,
    val description: String?,
    val tags: String?,
    val countryCode: String?,
    val countrySubdivisionCode: String?,
    val homepage: String?,
    val favicon: String?,
    val loadBalancer: String?,
    val serverSoftware: String?,
    val sampling: Int?,
    val timingMs: Int,
    val languageCodes: String?,
    val sslError: Int,
    val geoLat: Double?,
    val geoLong: Double?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StationCheck

        return stationUuid == other.stationUuid
    }

    override fun hashCode(): Int {
        return stationUuid.hashCode()
    }
}