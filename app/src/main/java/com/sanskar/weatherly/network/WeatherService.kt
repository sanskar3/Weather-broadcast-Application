package com.sanskar.weatherly.network



import com.sanskar.weatherly.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {

    @GET("2.5/weather")
    fun getWeather(
        @Query("lat")lat:Double,
        @Query("lon")lon:Double,
        @Query("appid")appid:String?,
        @Query("metric_unit") metric_unit:String?

    ):retrofit2.Call<WeatherResponse>
}