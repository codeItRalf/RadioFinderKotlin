package app.codeitralf.radiofinder.data.api.response

import android.os.Parcelable
import app.codeitralf.radiofinder.data.model.RadioStation
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class RadioStationResponse(
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
    fun toRadioStation(): RadioStation = RadioStation(
        changeUuid = changeUuid,
        stationUuid = stationUuid,
        state = state,
        language = language,
        languageCodes = languageCodes,
        votes = votes,
        lastChangeTime = lastChangeTime,
        lastChangeTimeIso8601 = lastChangeTimeIso8601,
        codec = codec,
        bitrate = bitrate,
        hls = hls,
        lastCheckOk = lastCheckOk,
        lastCheckTime = lastCheckTime,
        lastCheckTimeIso8601 = lastCheckTimeIso8601,
        lastCheckOkTime = lastCheckOkTime,
        lastCheckOkTimeIso8601 = lastCheckOkTimeIso8601,
        lastLocalCheckTime = lastLocalCheckTime,
        lastLocalCheckTimeIso8601 = lastLocalCheckTimeIso8601,
        clickTimestamp = clickTimestamp,
        clickTimestampIso8601 = clickTimestampIso8601,
        clickCount = clickCount,
        clickTrend = clickTrend,
        sslError = sslError,
        geoLat = geoLat,
        geoLong = geoLong,
        hasExtendedInfo = hasExtendedInfo,
        name = name,
        url = url,
        resolvedUrl = resolvedUrl,
        homepage = homepage,
        favicon = favicon,
        tags = tags,
        country = country,
        countryCode = countryCode,
        iso31662 = iso31662

    )
}