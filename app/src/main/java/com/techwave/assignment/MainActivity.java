package com.techwave.assignment;

import static android.Manifest.permission.ACCESS_BACKGROUND_LOCATION;
import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ANSWER_PHONE_CALLS;
import static android.Manifest.permission.MANAGE_ONGOING_CALLS;
import static android.Manifest.permission.MANAGE_OWN_CALLS;
import static android.Manifest.permission.PROCESS_OUTGOING_CALLS;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.Intent.ACTION_CALL;
import static android.content.Intent.ACTION_CALL_BUTTON;
import static android.content.Intent.ACTION_NEW_OUTGOING_CALL;
import static com.techwave.assignment.utils.Globals.getStr;
import static com.techwave.assignment.utils.Globals.global_ctx;
import static com.techwave.assignment.utils.Globals.initWork;
import static com.techwave.assignment.utils.Globals.showAlertIntface;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.techwave.assignment.receivers.CallReceiver;
import com.techwave.assignment.workmanagers.WorkManager_CareTaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RequiresApi(api = Build.VERSION_CODES.Q)
public class MainActivity extends AppCompatActivity {

    private final ArrayList<String> permissionsArray = new ArrayList<>(Arrays.asList(READ_PHONE_STATE, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, PROCESS_OUTGOING_CALLS, READ_CALL_LOG));
    private final ArrayList<String> sensitivePermissionsArray = new ArrayList<>(Collections.singletonList(ACCESS_BACKGROUND_LOCATION));
    private String[] permissionsList, sensitivePermissionsList;
    //Though we are setting runtime permission request limit to 5, the max allowed is 2 only. We are setting it to 5 as the counter keeps increasing even when
    //user touches outside the runtime permission rationale.
    private int permissionDenialLimit = 5, sensitivePermissionDenialLimit = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        global_ctx = this;
        setContentView(R.layout.activity_main);
        permissionsList = permissionsArray.toArray(new String[0]);
        sensitivePermissionsList = sensitivePermissionsArray.toArray(new String[0]);
        initPermissions();
//        saveLoc(new LocationCoords(1.2, 2.4));
//        saveCallData(new CallData("9666633201", "Incoming"));
    }

    private void initPermissions() {
        Log.d("logtag_MainActivity", "init Permissions");
        if (hasPermissions()) checkSensitivePermission();
        else checkPermissions();
    }

    private boolean hasPermissions() {
        Log.d("logtag_MainActivity", "has Permissions");
        for (String perm : permissionsList)
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    private void checkPermissions() {
        Log.d("logtag_MainActivity", "check Permissions");
        if (--permissionDenialLimit >= 0) {
            if (hasPermissions()) checkSensitivePermission();
            else getPermissions(permissionsList);
        } else
            showAlertIntface(getStr(R.string.runtime_permission_required), a -> bgLocationPermissionResultLauncher.launch(getSettingsIntent()));
    }

    private Intent getSettingsIntent() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", global_ctx.getPackageName(), null);
        intent.setData(uri);
        return intent;
    }

    private void initPeriodicWork() {
        PeriodicWorkRequest wrkReq = new PeriodicWorkRequest.Builder(WorkManager_CareTaker.class, 15, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
        WorkManager.getInstance(global_ctx).enqueueUniquePeriodicWork("pwm", ExistingPeriodicWorkPolicy.KEEP, wrkReq);
    }

    private void checkSensitivePermission() {
        Log.d("logtag_MainActivity", "check senstive");
        if (--sensitivePermissionDenialLimit >= 0) {
            if (hasSensitivePermissions()) checkLocationPermission();
            else getPermissions(sensitivePermissionsList);
        } else showAlertIntface(getStr(R.string.background_permission_not_provided), a -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            bgLocationPermissionResultLauncher.launch(intent);
        });
    }

    private void checkLocationPermission() {
        Log.d("logtag_MainActivity", "check location");
        if (isLocationEnabled()) initWorkManagers_And_Receivers();
        else getLocationPermission();
    }

    private void initWorkManagers_And_Receivers() {
        Log.d("logtag_MainActivity", "init WMs");
        initWork();
        initPeriodicWork();
        IntentFilter intentFilters = new IntentFilter();
        intentFilters.addAction("android.intent.action.TIME_TICK");
        intentFilters.addAction("android.intent.action.PHONE_STATE");
        intentFilters.addAction(ACTION_NEW_OUTGOING_CALL);
        intentFilters.addAction(PROCESS_OUTGOING_CALLS);
        intentFilters.addAction(ANSWER_PHONE_CALLS);
        intentFilters.addAction(MANAGE_ONGOING_CALLS);
        intentFilters.addAction(MANAGE_OWN_CALLS);
        intentFilters.addAction(ACTION_CALL);
        intentFilters.addAction(ACTION_CALL_BUTTON);
        intentFilters.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(new CallReceiver(), intentFilters);
    }

    ActivityResultLauncher<Intent> bgLocationPermissionResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("logtag_MainActivity", "Result bgLoc");
                if (!hasSensitivePermissions()) checkSensitivePermission();
                else checkLocationPermission();
            });

    private void getLocationPermission() {
        Log.d("logtag_MainActivity", "getLoc Perm");
        showAlertIntface(getStr(R.string.please_turn_on_gps), a -> gpsResultLauncher.launch(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
    }

    ActivityResultLauncher<Intent> gpsResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("logtag_MainActivity", "Result GPS");
                checkLocationPermission();
            });

    private boolean hasSensitivePermissions() {
        Log.d("logtag_MainActivity", "has Sensitive");
        for (String perm : sensitivePermissionsList)
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                return false;
        return true;
    }

    private void getPermissions(String[] localPermissionsList) {
        Log.d("logtag_MainActivity", "Get Perm");
        ActivityCompat.requestPermissions(this, localPermissionsList, 101);
    }

    private boolean isLocationEnabled() {
        Log.d("logtag_MainActivity", "isLoc Enabled");
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void
    onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("logtag_MainActivity", "on Perm Result");
        checkPermissions();
    }
}