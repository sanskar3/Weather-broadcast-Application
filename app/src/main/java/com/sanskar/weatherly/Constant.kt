package com.sanskar.weatherly

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build

object Constant {

    const val APP_ID:String = "de5f98759b04b0247c5d6ca9ab11f879"
    const val BASE_URL:String="http://api.openweathermap.org/data/"
    const val METRIC_UNIT : String = "metric"


    fun isNetworkAvailable(context: Context): Boolean {
        val connetivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
                ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connetivityManager.activeNetwork ?: return false
            val activeNetwork =  connetivityManager.getNetworkCapabilities(network)?:return false

            return when{
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->  true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }


        } else {

            val networkInfo = connetivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting

        }
    }
}