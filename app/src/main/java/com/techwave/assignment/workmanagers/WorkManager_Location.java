package com.techwave.assignment.workmanagers;

import static com.techwave.assignment.utils.Globals.delayMainThread;
import static com.techwave.assignment.utils.Globals.getTS;
import static com.techwave.assignment.utils.Globals.initWork;
import static com.techwave.assignment.utils.Globals.saveLoc;
import static com.techwave.assignment.utils.Preferences.PREF_LAST_WM_TS;
import static com.techwave.assignment.utils.Preferences.saveLongPreferences;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.techwave.assignment.models.LocationCoords;

public class WorkManager_Location extends Worker {

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    public WorkManager_Location(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("dsfewewf", "doWork");
        wmExecutionDone();
        if (locationRequest == null) {
            locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setNumUpdates(1);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            delayMainThread(0, () -> mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper()));
        initWork();
        return Result.success();
    }

    private void wmExecutionDone() {
        saveLongPreferences(PREF_LAST_WM_TS, getTS());
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Location currentLocation = locationResult.getLastLocation();
            Log.d("Work updServ lat", currentLocation.getLatitude() + "z");
            Log.d("Work updServ long", currentLocation.getLongitude() + "z");
            saveLoc(new LocationCoords(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
    };
}