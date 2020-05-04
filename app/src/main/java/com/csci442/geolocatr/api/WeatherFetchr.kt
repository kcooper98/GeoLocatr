package com.csci442.geolocatr.api

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherFetchr {
    val weatherApi: WeatherApi

    private val logTag = "448.WeatherFetchr"

    init {
        // Set up weather api
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        weatherApi = retrofit.create(WeatherApi::class.java)
    }

    fun fetchWeather(lat: Double, lon: Double): LiveData<OpenWeatherResponse> {
        val responseLiveData: MutableLiveData<OpenWeatherResponse> = MutableLiveData()
        val weatherRequest: Call<OpenWeatherResponse> = weatherApi.fetchWeather(
            lat,
            lon,
            "4120291d548d88e3201b37be29a00791"
        )

        weatherRequest.enqueue(object : Callback<OpenWeatherResponse> {
            override fun onFailure(call: Call<OpenWeatherResponse>, t: Throwable) {
                Log.d(logTag, t.localizedMessage)
            }

            override fun onResponse(
                call: Call<OpenWeatherResponse>,
                response: Response<OpenWeatherResponse>
            ) {
                responseLiveData.value = response.body()
                Log.d(logTag, "onresponse: ${responseLiveData?.value?.main?.temp.toString()}")
            }
        })

        return responseLiveData
    }
}