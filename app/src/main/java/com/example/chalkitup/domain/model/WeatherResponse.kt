package com.example.chalkitup.domain.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("current") val current: Current
)

data class Current(
    @SerializedName("temp_c") val tempC: Double,
    @SerializedName("condition") val condition: Condition
)

data class Condition(
    @SerializedName("text") val text: String
)
