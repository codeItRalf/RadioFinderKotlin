package app.codeitralf.radiofinder.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RadioStation(
val changeUuid: String,
val stationUuid: String,
 val name: String?,
 val url: String?,
 val resolvedUrl: String?,
  val homepage: String?,
  val favicon: String?,
     val tags: String?,
     val country: String?,
     val countryCode: String?,
     val iso31662: String?,
     val state: String?,
     val language: String?,
     val languageCodes: String?,
     val votes: Int?,
     val lastChangeTime: String?,
     val lastChangeTimeIso8601: String?,
     val codec: String?,
     val bitrate: Int?,
     val hls: Int?,
     val lastCheckOk: Int?,
     val lastCheckTime: String?,
     val lastCheckTimeIso8601: String?,
     val lastCheckOkTime: String?,
     val lastCheckOkTimeIso8601: String?,
     val lastLocalCheckTime: String?,
     val lastLocalCheckTimeIso8601: String?,
     val clickTimestamp: String?,
     val clickTimestampIso8601: String?,
     val clickCount: Int?,
     val clickTrend: Int?,
     val sslError: Int?,
     val geoLat: Double?,
     val geoLong: Double?,
     val hasExtendedInfo: Boolean?
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RadioStation

        return stationUuid == other.stationUuid
    }

    override fun hashCode(): Int {
        return stationUuid.hashCode()
    }
}