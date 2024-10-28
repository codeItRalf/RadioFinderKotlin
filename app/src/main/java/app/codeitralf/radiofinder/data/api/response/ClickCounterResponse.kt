package app.codeitralf.radiofinder.data.api.response

import com.google.gson.annotations.SerializedName

data class ClickCounterResponse(
    @SerializedName("ok") val success: Boolean
)