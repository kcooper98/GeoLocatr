package com.csci442.geolocatr.api

import com.google.gson.annotations.SerializedName

class OpenWeatherResponse {
    @SerializedName("main")
    lateinit var main: MainItem

    @SerializedName("weather")
    lateinit var weatherItems: List<WeatherItem>
}