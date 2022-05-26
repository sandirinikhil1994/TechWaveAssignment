package com.techwave.assignment.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.techwave.assignment.utils.AppClass
import com.techwave.assignment.utils.Globals

abstract class PhonecallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
            tempNum = intent.extras!!.getString(TelephonyManager.EXTRA_INCOMING_NUMBER)
            if (!isTempNumValid && intent.hasExtra("android.intent.extra.PHONE_NUMBER")) tempNum =
                intent.extras!!
                    .getString("android.intent.extra.PHONE_NUMBER")
        }
        if (isTempNumValid) AppClass.savedNumber = tempNum
        if (intent.action != "android.intent.action.NEW_OUTGOING_CALL") {
            try {
                val stateStr = intent.extras!!.getString(TelephonyManager.EXTRA_STATE)
                var state = 0
                if (stateStr == TelephonyManager.EXTRA_STATE_IDLE) {
                    state = TelephonyManager.CALL_STATE_IDLE
                } else if (stateStr == TelephonyManager.EXTRA_STATE_OFFHOOK) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK
                } else if (stateStr == TelephonyManager.EXTRA_STATE_RINGING) {
                    state = TelephonyManager.CALL_STATE_RINGING
                }
                onCallStateChanged(context, state)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val isTempNumValid: Boolean
        private get() = tempNum != null && !tempNum!!.isEmpty() && tempNum != "null"

    //Derived classes should override these to respond to specific events of interest
    protected abstract fun onIncomingCallReceived(ctx: Context?, start: Long)
    protected abstract fun onIncomingCallAnswered(ctx: Context?, start: Long)
    protected abstract fun onIncomingCallEnded(ctx: Context?, start: Long, end: Long)
    protected abstract fun onOutgoingCallStarted(ctx: Context?, start: Long)
    protected abstract fun onOutgoingCallEnded(ctx: Context?, start: Long, end: Long)
    protected abstract fun onMissedCall(ctx: Context?, start: Long)

    //Deals with actual events
    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    fun onCallStateChanged(context: Context?, state: Int) {
        if (lastState == state) {
            //No change, debounce extras
            return
        }
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                callStartTime = Globals.tS
                onIncomingCallReceived(context, callStartTime)
            }
            TelephonyManager.CALL_STATE_OFFHOOK ->                 //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false
                    callStartTime = Globals.tS
                    onOutgoingCallStarted(context, callStartTime)
                } else {
                    isIncoming = true
                    callStartTime = Globals.tS
                    onIncomingCallAnswered(context, callStartTime)
                }
            TelephonyManager.CALL_STATE_IDLE ->                 //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, callStartTime)
                } else if (isIncoming) {
                    onIncomingCallEnded(context, callStartTime, Globals.tS)
                } else {
                    onOutgoingCallEnded(context, callStartTime, Globals.tS)
                }
        }
        lastState = state
    }

    companion object {
        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var callStartTime: Long = 0
        private var isIncoming = false
        private var tempNum: String? = ""
    }
}