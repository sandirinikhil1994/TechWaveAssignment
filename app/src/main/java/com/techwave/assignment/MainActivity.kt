package com.techwave.assignment

import android.Manifest.permission
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.techwave.assignment.interfaces.BtnClk
import com.techwave.assignment.receivers.CallReceiver
import com.techwave.assignment.utils.Globals.getStr
import com.techwave.assignment.utils.Globals.global_ctx
import com.techwave.assignment.utils.Globals.initWork
import com.techwave.assignment.utils.Globals.showAlertIntface
import com.techwave.assignment.workmanagers.WorkManagerCareTaker
import java.util.*
import java.util.concurrent.TimeUnit

@RequiresApi(api = Build.VERSION_CODES.Q)
class MainActivity : AppCompatActivity() {
    private val permissionsArray = ArrayList(
        Arrays.asList(
            permission.READ_PHONE_STATE,
            permission.ACCESS_COARSE_LOCATION,
            permission.ACCESS_FINE_LOCATION,
            permission.PROCESS_OUTGOING_CALLS,
            permission.READ_CALL_LOG
        )
    )
    private val sensitivePermissionsArray = ArrayList(listOf(permission.ACCESS_BACKGROUND_LOCATION))
    private lateinit var permissionsList: Array<String>
    private lateinit var sensitivePermissionsList: Array<String>

    //Though we are setting runtime permission request limit to 5, the max allowed is 2 only. We are setting it to 5 as the counter keeps increasing even when
    //user touches outside the runtime permission rationale.
    private var permissionDenialLimit = 5
    private var sensitivePermissionDenialLimit = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        global_ctx = this
        setContentView(R.layout.activity_main)
        permissionsList = permissionsArray.toTypedArray()
        sensitivePermissionsList = sensitivePermissionsArray.toTypedArray()
        initPermissions()
        //        saveLoc(new LocationCoords(1.2, 2.4));
//        saveCallData(new CallData("9666633201", "Incoming"));
    }

    private fun initPermissions() {
        Log.d("logtag_MainActivity", "init Permissions")
        if (hasPermissions()) checkSensitivePermission() else checkPermissions()
    }

    private fun hasPermissions(): Boolean {
        Log.d("logtag_MainActivity", "has Permissions")
        for (perm in permissionsList) if (ContextCompat.checkSelfPermission(
                this,
                perm
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }

    private fun checkPermissions() {
        Log.d("logtag_MainActivity", "check Permissions")
        if (--permissionDenialLimit >= 0) {
            if (hasPermissions()) checkSensitivePermission() else getPermissions(permissionsList)
        } else
            showAlertIntface(getStr(R.string.runtime_permission_required), object : BtnClk {
                override fun btnclk(a: Int) {
                    bgLocationPermissionResultLauncher.launch(settingsIntent)
                }
            })
    }

    private val settingsIntent: Intent
        private get() {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", global_ctx!!.packageName, null)
            intent.data = uri
            return intent
        }

    private fun initPeriodicWork() {
        val wrkReq =
            PeriodicWorkRequest.Builder(WorkManagerCareTaker::class.java, 15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
                ).build()
        WorkManager.getInstance(global_ctx!!)
            .enqueueUniquePeriodicWork("pwm", ExistingPeriodicWorkPolicy.KEEP, wrkReq)
    }

    private fun checkSensitivePermission() {
        Log.d("logtag_MainActivity", "check senstive")
        if (--sensitivePermissionDenialLimit >= 0) {
            if (hasSensitivePermissions()) checkLocationPermission() else getPermissions(
                sensitivePermissionsList
            )
        } else
            showAlertIntface(getStr(R.string.background_permission_not_provided), object : BtnClk {
                override fun btnclk(a: Int) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    bgLocationPermissionResultLauncher.launch(intent)
                }
            })
    }

    private fun checkLocationPermission() {
        Log.d("logtag_MainActivity", "check location")
        if (isLocationEnabled) initWorkManagers_And_Receivers() else locationPermission
    }

    private fun initWorkManagers_And_Receivers() {
        Log.d("logtag_MainActivity", "init WMs")
        initWork()
        initPeriodicWork()
        val intentFilters = IntentFilter()
        intentFilters.addAction("android.intent.action.TIME_TICK")
        intentFilters.addAction("android.intent.action.PHONE_STATE")
        intentFilters.addAction(Intent.ACTION_NEW_OUTGOING_CALL)
        intentFilters.addAction(permission.PROCESS_OUTGOING_CALLS)
        intentFilters.addAction(permission.ANSWER_PHONE_CALLS)
        intentFilters.addAction(permission.MANAGE_ONGOING_CALLS)
        intentFilters.addAction(permission.MANAGE_OWN_CALLS)
        intentFilters.addAction(Intent.ACTION_CALL)
        intentFilters.addAction(Intent.ACTION_CALL_BUTTON)
        intentFilters.addAction(Intent.ACTION_USER_PRESENT)
        registerReceiver(CallReceiver(), intentFilters)
    }

    var bgLocationPermissionResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        Log.d("logtag_MainActivity", "Result bgLoc")
        if (!hasSensitivePermissions()) checkSensitivePermission() else checkLocationPermission()
    }
    private val locationPermission: Unit
        get() {
            Log.d("logtag_MainActivity", "getLoc Perm")
            showAlertIntface(getStr(R.string.please_turn_on_gps), object : BtnClk {
                override fun btnclk(a: Int) {
                    gpsResultLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    var gpsResultLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result: ActivityResult? ->
        Log.d("logtag_MainActivity", "Result GPS")
        checkLocationPermission()
    }

    private fun hasSensitivePermissions(): Boolean {
        Log.d("logtag_MainActivity", "has Sensitive")
        for (perm in sensitivePermissionsList) if (ContextCompat.checkSelfPermission(
                this,
                perm
            ) != PackageManager.PERMISSION_GRANTED
        ) return false
        return true
    }

    private fun getPermissions(localPermissionsList: Array<String>) {
        Log.d("logtag_MainActivity", "Get Perm")
        ActivityCompat.requestPermissions(this, localPermissionsList, 101)
    }

    private val isLocationEnabled: Boolean
        private get() {
            Log.d("logtag_MainActivity", "isLoc Enabled")
            val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
            )
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d("logtag_MainActivity", "on Perm Result")
        checkPermissions()
    }
}