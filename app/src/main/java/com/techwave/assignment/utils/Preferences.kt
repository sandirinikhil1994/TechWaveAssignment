package com.techwave.assignment.utils;

import static com.techwave.assignment.utils.Globals.global_ctx;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {

    public static String DB_TABLE = "TechWave";
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;
    public static final String PREF_LATITUDE = "prefLatitude";
    public static final String PREF_LONGITUDE = "prefLongitude";
    public static final String PREF_LAST_WM_TS = "prefLastWMTs";

    public static void savePreferences(String key, String value) {
        initPref();
        editor.putString(key, value);
        editor.apply();
    }

    public static String loadPreferences(String key) {
        initPref();
        String val;
        try {
            if (key.length() > 0) key = key.trim();
            val = sharedPreferences.getString(key, "");
        } catch (Exception e) {
            val = "";
        }
        return val;
    }

    public static void saveIntPreferences(String key, int value) {
        initPref();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int loadIntPreferences(String key) {
        initPref();
        int val;
        try {
            val = sharedPreferences.getInt(key, 0);
        } catch (Exception e) {
            val = 0;
        }
        return val;
    }

    public static void saveLongPreferences(String key, long value) {
        initPref();
        editor.putLong(key, value);
        editor.apply();
    }

    public static long loadLongPreferences(String key) {
        initPref();
        long val;
        try {
            val = sharedPreferences.getLong(key, 0);
        } catch (Exception e) {
            val = 0;
        }
        return val;
    }

    public static void saveDoublePreferences(String key, double value) {
        initPref();
        editor.putLong(key, Double.doubleToRawLongBits(value));
        editor.apply();
    }

    public static double loadDoublePreferences(String key) {
        initPref();
        return (Double) Double.longBitsToDouble(sharedPreferences.getLong(key, Double.doubleToLongBits(0)));
    }

    private static void initPref() {
        if (editor == null) {
            sharedPreferences = global_ctx.getSharedPreferences(DB_TABLE, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
    }
}
