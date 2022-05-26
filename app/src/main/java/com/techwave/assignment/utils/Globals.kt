package com.techwave.assignment.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.work.*
import com.techwave.assignment.R
import com.techwave.assignment.interfaces.BtnClk
import com.techwave.assignment.interfaces.Delay
import com.techwave.assignment.models.CallData
import com.techwave.assignment.models.LocationCoords
import com.techwave.assignment.workmanagers.WorkManagerLocation
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("StaticFieldLeak")
object Globals {
    var global_ctx: Context? = null
    fun initWork() {
        val wrkReq = OneTimeWorkRequest.Builder(WorkManagerLocation::class.java)
            .setInitialDelay(5, TimeUnit.MINUTES)
        wrkReq.setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        )
        WorkManager.getInstance(global_ctx!!)
            .enqueueUniqueWork("loc_5min", ExistingWorkPolicy.REPLACE, wrkReq.build())
    }

    fun getFullDateFromTs(ts: Long): String {
        val formatter = SimpleDateFormat("dd MMM yyyy hh:mm:ss a", Locale.getDefault())
        return formatter.format(Date(ts))
    }

    fun saveLoc(locationCoords: LocationCoords) {
        locationCoords.ts = tS.toString() + ""
        AppClass.Companion.dbRef_LocData!!.push().setValue(locationCoords)
    }

    fun saveCallData(callData: CallData) {
        callData.ts = tS
        AppClass.Companion.dbRef_CallData!!.push().setValue(callData)
    }

    fun toast(msg: String?) {
        toastExecutor(Toast.LENGTH_LONG, msg)
    }

    fun toastShort(msg: String?) {
        toastExecutor(Toast.LENGTH_SHORT, msg)
    }

    private fun toastExecutor(a: Int, msg: String?) {
        try {
            if (global_ctx is Activity) (global_ctx as Activity?)!!.runOnUiThread {
                Toast.makeText(
                    global_ctx, msg, a
                ).show()
            } else Toast.makeText(global_ctx, msg, a).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun delayMainThread(millis: Long, delay: Delay) {
        val executorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        executorService.execute {
            handler.post {
                Handler().postDelayed(
                    { delay.done() }, millis
                )
            }
        }
    }

    fun saveLocalLocation(locationCoords: LocationCoords) {
        Preferences.saveDoublePreferences(Preferences.PREF_LATITUDE, locationCoords.lat)
        Preferences.saveDoublePreferences(Preferences.PREF_LONGITUDE, locationCoords.lon)
    }

    fun showAlert(a: Boolean, message: String) {
        if (!message.isEmpty()) {
            val dialog = Dialog(global_ctx!!, R.style.AlertDialogCustom)
            dialog.setContentView(R.layout.custom_alert)
            dialog.setCancelable(false)
            val tv1 = dialog.findViewById<TextView>(R.id.tv1)
            if (message.length > 200) tv1.textSize = 12f
            val b1 = dialog.findViewById<Button>(R.id.b1)
            tv1.text = message
            b1.setOnClickListener { v: View? -> if (a) dialog.dismiss() else close() }
            try {
                dialog.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun showAlertIntface(message: String, btnClk: BtnClk) {
        val dialog = Dialog(global_ctx!!, R.style.AlertDialogCustom)
        dialog.setContentView(R.layout.custom_alert)
        dialog.setCancelable(false)
        val tv1 = dialog.findViewById<TextView>(R.id.tv1)
        if (message.length > 200) tv1.textSize = 12f
        val b1 = dialog.findViewById<Button>(R.id.b1)
        tv1.text = message
        b1.setOnClickListener { v: View? ->
            dialog.dismiss()
            btnClk.btnclk(0)
        }
        try {
            dialog.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun close() {
        try {
            (global_ctx as Activity?)!!.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStr(string: Int): String {
        return global_ctx!!.resources.getString(string)
    }

    val tS: Long
        get() = System.currentTimeMillis()
}