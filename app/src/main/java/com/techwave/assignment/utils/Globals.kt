package com.techwave.assignment.utils;

import static com.techwave.assignment.utils.AppClass.dbRef_CallData;
import static com.techwave.assignment.utils.AppClass.dbRef_LocData;
import static com.techwave.assignment.utils.Preferences.PREF_LATITUDE;
import static com.techwave.assignment.utils.Preferences.PREF_LONGITUDE;
import static com.techwave.assignment.utils.Preferences.saveDoublePreferences;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.techwave.assignment.R;
import com.techwave.assignment.interfaces.BtnClk;
import com.techwave.assignment.interfaces.Delay;
import com.techwave.assignment.models.CallData;
import com.techwave.assignment.models.LocationCoords;
import com.techwave.assignment.workmanagers.WorkManager_Location;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Globals {

    public static Context global_ctx;

    public static void initWork() {
        OneTimeWorkRequest.Builder wrkReq = new OneTimeWorkRequest.Builder(WorkManager_Location.class).setInitialDelay(5, TimeUnit.MINUTES);
        wrkReq.setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build());
        WorkManager.getInstance(global_ctx).enqueueUniqueWork("loc_5min", ExistingWorkPolicy.REPLACE, wrkReq.build());
    }

    public static String getFullDateFromTs(long ts) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy hh:mm:ss a", Locale.getDefault());
        return formatter.format(new Date(ts));
    }

    public static void saveLoc(LocationCoords locationCoords) {
        locationCoords.setTs(getTS() + "");
        dbRef_LocData.push().setValue(locationCoords);
    }

    public static void saveCallData(CallData callData) {
        callData.setTs(getTS());
        dbRef_CallData.push().setValue(callData);
    }

    public static void toast(String msg) {
        toastExecutor(Toast.LENGTH_LONG, msg);
    }

    public static void toastShort(String msg) {
        toastExecutor(Toast.LENGTH_SHORT, msg);
    }

    public static void toastExecutor(int a, String msg) {
        try {
            if (global_ctx instanceof Activity)
                ((Activity) global_ctx).runOnUiThread(() -> Toast.makeText(global_ctx, msg, a).show());
            else Toast.makeText(global_ctx, msg, a).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delayMainThread(long millis, final Delay delay) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> handler.post(() -> new Handler().postDelayed(delay::done, millis)));
    }

    public static void saveLocalLocation(LocationCoords locationCoords) {
        saveDoublePreferences(PREF_LATITUDE, locationCoords.getLat());
        saveDoublePreferences(PREF_LONGITUDE, locationCoords.getLon());
    }

    public static void showAlert(boolean a, String message) {
        if (!message.isEmpty()) {
            final Dialog dialog = new Dialog(global_ctx, R.style.AlertDialogCustom);
            dialog.setContentView(R.layout.custom_alert);
            dialog.setCancelable(false);
            TextView tv1 = dialog.findViewById(R.id.tv1);
            if (message.length() > 200)
                tv1.setTextSize(12f);
            Button b1 = dialog.findViewById(R.id.b1);
            tv1.setText(message);
            b1.setOnClickListener(v -> {
                if (a) dialog.dismiss();
                else close();
            });
            try {
                dialog.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void showAlertIntface(String message, final BtnClk btnClk) {
        final Dialog dialog = new Dialog(global_ctx, R.style.AlertDialogCustom);
        dialog.setContentView(R.layout.custom_alert);
        dialog.setCancelable(false);
        TextView tv1 = dialog.findViewById(R.id.tv1);
        if (message.length() > 200)
            tv1.setTextSize(12f);
        Button b1 = dialog.findViewById(R.id.b1);
        tv1.setText(message);
        b1.setOnClickListener(v -> {
            dialog.dismiss();
            btnClk.btnclk(0);
        });
        try {
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        try {
            ((Activity) global_ctx).finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStr(int string) {
        return global_ctx.getResources().getString(string);
    }

    public static long getTS() {
        return System.currentTimeMillis();
    }
}
