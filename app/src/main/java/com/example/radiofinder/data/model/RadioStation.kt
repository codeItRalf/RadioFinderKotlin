package com.example.radiofinder.data.model
import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RadioStation(
    @SerializedName("changeuuid") val changeUuid: String,
    @SerializedName("stationuuid") val stationUuid: String,
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("url_resolved") val resolvedUrl: String?,
    @SerializedName("homepage") val homepage: String?,
    @SerializedName("favicon") val favicon: String?,
    @SerializedName("tags") val tags: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("countrycode") val countryCode: String?,
    @SerializedName("iso_3166_2") val iso31662: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("language") val language: String?,
    @SerializedName("languagecodes") val languageCodes: String?,
    @SerializedName("votes") val votes: Int?,
    @SerializedName("lastchangetime") val lastChangeTime: String?,
    @SerializedName("lastchangetime_iso8601") val lastChangeTimeIso8601: String?,
    @SerializedName("codec") val codec: String?,
    @SerializedName("bitrate") val bitrate: Int?,
    @SerializedName("hls") val hls: Int?,
    @SerializedName("lastcheckok") val lastCheckOk: Int?,
    @SerializedName("lastchecktime") val lastCheckTime: String?,
    @SerializedName("lastchecktime_iso8601") val lastCheckTimeIso8601: String?,
    @SerializedName("lastcheckoktime") val lastCheckOkTime: String?,
    @SerializedName("lastcheckoktime_iso8601") val lastCheckOkTimeIso8601: String?,
    @SerializedName("lastlocalchecktime") val lastLocalCheckTime: String?,
    @SerializedName("lastlocalchecktime_iso8601") val lastLocalCheckTimeIso8601: String?,
    @SerializedName("clicktimestamp") val clickTimestamp: String?,
    @SerializedName("clicktimestamp_iso8601") val clickTimestampIso8601: String?,
    @SerializedName("clickcount") val clickCount: Int?,
    @SerializedName("clicktrend") val clickTrend: Int?,
    @SerializedName("ssl_error") val sslError: Int?,
    @SerializedName("geo_lat") val geoLat: Double?,
    @SerializedName("geo_long") val geoLong: Double?,
    @SerializedName("has_extended_info") val hasExtendedInfo: Boolean?
) : Parcelable {
    companion object {
        fun fromJson(json: Map<String, Any>): RadioStation {
            return RadioStation(
                changeUuid = json["changeuuid"] as String,
                stationUuid = json["stationuuid"] as String,
                name = json["name"]  as? String,
                url = json["url"] as? String,
                resolvedUrl = json["url_resolved"] as? String,
                homepage = json["homepage"] as?String,
                favicon = json["favicon"] as?String,
                tags = json["tags"] as?String,
                country = json["country"] as?String,
                countryCode = json["countrycode"] as?String,
                iso31662 = json["iso_3166_2"] as?String,
                state = json["state"] as?String,
                language = json["language"] as?String,
                languageCodes = json["languagecodes"] as?String,
                votes = json["votes"] as?Int,
                lastChangeTime = json["lastchangetime"] as?String,
                lastChangeTimeIso8601 = json["lastchangetime_iso8601"] as?String,
                codec = json["codec"] as?String,
                bitrate = json["bitrate"] as?Int,
                hls = json["hls"] as?Int,
                lastCheckOk = json["lastcheckok"] as?Int,
                lastCheckTime = json["lastchecktime"] as?String,
                lastCheckTimeIso8601 = json["lastchecktime_iso8601"] as?String,
                lastCheckOkTime = json["lastcheckoktime"] as?String,
                lastCheckOkTimeIso8601 = json["lastcheckoktime_iso8601"] as?String,
                lastLocalCheckTime = json["lastlocalchecktime"] as?String,
                lastLocalCheckTimeIso8601 = json["lastlocalchecktime_iso8601"] as?String,
                clickTimestamp = json["clicktimestamp"] as?String?,
                clickTimestampIso8601 = json["clicktimestamp_iso8601"] as?String?,
                clickCount = json["clickcount"] as?Int,
                clickTrend = json["clicktrend"] as?Int,
                sslError = json["ssl_error"] as?Int,
                geoLat = json["geo_lat"] as?Double,
                geoLong = json["geo_long"] as?Double,
                hasExtendedInfo = json["has_extended_info"] as?Boolean
            )
        }
    }
}