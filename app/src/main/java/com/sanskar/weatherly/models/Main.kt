package com.sanskar.weatherly.models

import java.io.Serializable

data class Main(
    val temp:Double,
    val pressure: Double,
    val humidity: Int,
    var temp_min:Double,
    var temp_max:Double,
    val sea_level:Double,
    val ground_level:Double
):Serializable
