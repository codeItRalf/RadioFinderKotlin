package com.example.radiofinder.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class StationCheck(
    @SerializedName("stationuuid") val stationUuid: String,
    @SerializedName("checkuuid") val checkUuid: String,
    val source: String,
    val codec: String,
    val bitrate: Int,
    val hls: Int,
    val ok: Int,
    val timestamp: String,
    @SerializedName("timestamp_iso8601") val timestampIso8601: String,
    @SerializedName("urlcache")val urlCache: String?,
    @SerializedName("metainfo_overrides_database") val metaInfoOverridesDatabase: Int,
    val public: String?,
    val name: String?,
    val description: String?,
    val tags: String?,
    @SerializedName("countrycode")val countryCode: String?,
    @SerializedName("countrysubdivisioncode") val countrySubdivisionCode: String?,
    val homepage: String?,
    val favicon: String?,
    @SerializedName("loadbalancer")val loadBalancer: String?,
    @SerializedName("server_software") val serverSoftware: String?,
    val sampling: Int?,
    @SerializedName("timing_ms") val timingMs: Int,
    @SerializedName("languagecodes")val languageCodes: String?,
    @SerializedName("ssl_error") val sslError: Int,
    @SerializedName("geo_lat") val geoLat: Double?,
    @SerializedName("geo_long") val geoLong: Double?
){
    companion object {
        fun fromJson(json: Map<String, Any>): StationCheck {
            return Gson().fromJson(Gson().toJson(json), StationCheck::class.java)
        }
    }
}