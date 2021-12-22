package com.example.stalkr.services

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*

class LocationService: Service() {
    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    private val binder = LocalBinder()
    private lateinit var context: Context

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    inner class LocalBinder : Binder() {
        fun getService(): LocationService = this@LocationService
    }

    override fun startService(service: Intent?): ComponentName? {
        return super.startService(service)
    }

    fun setupLocationService(context: Context, locationCallback: LocationCallback) {
        Log.d(TAG,"on setupLocationService");
        this.context = context

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        this.locationCallback = locationCallback

        //createLocationRequest() // do this after mapAsync
    }

    private fun startLocationListener() {
        Log.d(TAG,"on startLocationListener");
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    fun lastLocation(callback: (location: Location) -> Unit) {
        Log.d(TAG,"on lastLocation");
        if (checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener(context as Activity) { location ->
                callback(location)
            }
        }
    }

    fun createLocationRequest() {
        Log.d(TAG,"on createLocationRequest");
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            startLocationListener()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
//                    e.startResolutionForResult(
//                        context,
//                        REQUEST_CHECK_SETTINGS
//                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG,"on LocationService onBind");
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        return super.onUnbind(intent)
        Log.d(TAG,"on LocationService onUnbind");
    }
}