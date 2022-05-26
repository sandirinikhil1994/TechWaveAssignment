package com.techwave.assignment.utils

import android.content.Context
import android.content.SharedPreferences

object Preferences {
    var DB_TABLE = "TechWave"
    private var sharedPreferences: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    const val PREF_LATITUDE = "prefLatitude"
    const val PREF_LONGITUDE = "prefLongitude"
    const val PREF_LAST_WM_TS = "prefLastWMTs"
    fun savePreferences(key: String?, value: String?) {
        initPref()
        editor!!.putString(key, value)
        editor!!.apply()
    }

    fun loadPreferences(key: String): String? {
        var key = key
        initPref()
        var `val`: String?
        try {
            if (key.isNotEmpty()) key = key.trim { it <= ' ' }
            `val` = sharedPreferences!!.getString(key, "")
        } catch (e: Exception) {
            `val` = ""
        }
        return `val`
    }

    fun saveIntPreferences(key: String?, value: Int) {
        initPref()
        editor!!.putInt(key, value)
        editor!!.apply()
    }

    fun loadIntPreferences(key: String?): Int {
        initPref()
        val `val`: Int = try {
            sharedPreferences!!.getInt(key, 0)
        } catch (e: Exception) {
            0
        }
        return `val`
    }

    fun saveLongPreferences(key: String?, value: Long) {
        initPref()
        editor!!.putLong(key, value)
        editor!!.apply()
    }

    fun loadLongPreferences(key: String?): Long {
        initPref()
        val `val`: Long = try {
            sharedPreferences!!.getLong(key, 0)
        } catch (e: Exception) {
            0
        }
        return `val`
    }

    fun saveDoublePreferences(key: String?, value: Double) {
        initPref()
        editor!!.putLong(key, java.lang.Double.doubleToRawLongBits(value))
        editor!!.apply()
    }

    fun loadDoublePreferences(key: String?): Double {
        initPref()
        return java.lang.Double.longBitsToDouble(
            sharedPreferences!!.getLong(
                key,
                java.lang.Double.doubleToLongBits(0.0)
            )
        )
    }

    private fun initPref() {
        if (sharedPreferences == null)
            sharedPreferences =
                Globals.global_ctx!!.getSharedPreferences(DB_TABLE, Context.MODE_PRIVATE)
        if (editor == null)
            editor = sharedPreferences?.edit()
    }
}