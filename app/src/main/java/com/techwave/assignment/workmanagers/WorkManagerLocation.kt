package com.techwave.assignment.workmanagers

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.*
import com.techwave.assignment.interfaces.Delay
import com.techwave.assignment.models.LocationCoords
import com.techwave.assignment.utils.Globals.delayMainThread
import com.techwave.assignment.utils.Globals.initWork
import com.techwave.assignment.utils.Globals.saveLoc
import com.techwave.assignment.utils.Globals.tS
import com.techwave.assignment.utils.Preferences

class WorkManagerLocation(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequest: LocationRequest? = null
    override fun doWork(): Result {
        wmExecutionDone()
        if (locationRequest == null) {
            locationRequest = LocationRequest.create()
            locationRequest!!.priority =
                LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest!!.numUpdates = 1
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            || ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
            delayMainThread(0, object : Delay {
                @SuppressLint("MissingPermission")
                override fun done() {
                    mFusedLocationClient!!.requestLocationUpdates(
                        locationRequest!!,
                        locationCallback,
                        Looper.myLooper()!!
                    )
                }
            })
        initWork()
        return Result.success()
    }

    private fun wmExecutionDone() {
        Preferences.saveLongPreferences(Preferences.PREF_LAST_WM_TS, tS)
    }

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = locationResult.lastLocation
            Log.d("Work updServ lat", currentLocation.latitude.toString() + "z")
            Log.d("Work updServ long", currentLocation.longitude.toString() + "z")
            saveLoc(LocationCoords(currentLocation.latitude, currentLocation.longitude))
        }
    }
}