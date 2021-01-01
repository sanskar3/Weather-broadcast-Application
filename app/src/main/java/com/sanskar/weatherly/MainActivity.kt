package com.sanskar.weatherly

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.DexterBuilder
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.sanskar.weatherly.models.WeatherResponse
import com.sanskar.weatherly.network.WeatherService
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    private lateinit var mFUsedLocationClient: FusedLocationProviderClient

    private var mProgressDialog: Dialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mFUsedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //if location provider is off
        if (!isLocationEnabled()) {
            Toast.makeText(
                this,
                "Your location provider is turned OFFf . Please turned it on",
                Toast.LENGTH_LONG
            ).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        } else {
            Dexter.withActivity(this)
                .withPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report!!.areAllPermissionsGranted())
                            requestLocationData()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread()
                .check()


        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData() {

        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        mFUsedLocationClient.requestLocationUpdates(
            mLocationRequest, mLocationCallBack,
            Looper.myLooper()
        )
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            val latitude = mLastLocation.latitude
            Log.i("Current Latitude", "$latitude")

            val longitude = mLastLocation.longitude
            Log.i("Current longitude", "$longitude")
            getLocationWeatherDetails(latitude, longitude)
        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude: Double) {
        if (Constant.isNetworkAvailable(this)) {

            val retrofit: Retrofit = Retrofit.Builder().baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service: WeatherService = retrofit
                .create<WeatherService>(WeatherService::class.java)

            val listCall: Call<WeatherResponse> = service.getWeather(
                latitude, longitude, Constant.METRIC_UNIT, Constant.APP_ID
            )

            showCustomProgressDialog()

            listCall.enqueue(object : Callback<WeatherResponse> {
                override fun onResponse(
                    call: Call<WeatherResponse>,
                    response: Response<WeatherResponse>
                ) {
                    if (response!!.isSuccessful) {

                        hideProgressDialog()

                        val weatherList: WeatherResponse? = response.body()
                        if (weatherList != null) {
                            setupUI(weatherList)
                        }
                        Log.i("Response Result", "$weatherList")
                    } else {
                        val rc = response.code()
                        when (rc) {
                            400 -> {
                                Log.e("Error 400", "Network Problem")
                            }

                            404 -> {
                                Log.e("Error 404", "Not Found")
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<WeatherResponse>, t: Throwable?) {
                    Log.e("Error", t!!.message.toString())
                    hideProgressDialog()
                }

            })

        } else {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show()


        }
    }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("It looks like you have turned off permission required for this feature.")
            .setPositiveButton("Go To Settings") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }.setNegativeButton("cancel") { dialog, _ -> dialog.dismiss() }.show()
    }


    //this provide access to system location service
    private fun isLocationEnabled(): Boolean {

        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun showCustomProgressDialog() {
        mProgressDialog = Dialog(this)

        /*
        Set the screen content from a layout resource.
        the resource will be inflate , adding all top level views to screen
         */
        mProgressDialog!!.setContentView(R.layout.dialog_custom_progress)

        mProgressDialog!!.show()
    }

    private fun hideProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog!!.dismiss()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupUI(weatherList: WeatherResponse) {
        for (i in weatherList.weather.indices) {
            Log.i("Weather name", weatherList.weather.toString())

            tv_main.text = weatherList.weather[i].main
            tv_main_description.text = weatherList.weather[i].description
            tv_temp.text =
                weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())

        }

    }

    private fun getUnit(value: String): String? {
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

}



