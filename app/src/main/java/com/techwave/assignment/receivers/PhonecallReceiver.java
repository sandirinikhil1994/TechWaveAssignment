package com.techwave.assignment.receivers;

import static com.techwave.assignment.utils.AppClass.savedNumber;
import static com.techwave.assignment.utils.Globals.getTS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

public abstract class PhonecallReceiver extends BroadcastReceiver {

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static long callStartTime;
    private static boolean isIncoming;
    private static String tempNum = "";

    @Override
    public void onReceive(Context context, Intent intent) {
        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.hasExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
            tempNum = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if (!isTempNumValid() && intent.hasExtra("android.intent.extra.PHONE_NUMBER"))
                tempNum = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        }
        if (isTempNumValid())
            savedNumber = tempNum;
        if (!intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            try {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                onCallStateChanged(context, state);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isTempNumValid() {
        return tempNum != null && !tempNum.isEmpty() && !tempNum.equals("null");
    }

    //Derived classes should override these to respond to specific events of interest
    protected abstract void onIncomingCallReceived(Context ctx, long start);

    protected abstract void onIncomingCallAnswered(Context ctx, long start);

    protected abstract void onIncomingCallEnded(Context ctx, long start, long end);

    protected abstract void onOutgoingCallStarted(Context ctx, long start);

    protected abstract void onOutgoingCallEnded(Context ctx, long start, long end);

    protected abstract void onMissedCall(Context ctx, long start);

    //Deals with actual events

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = getTS();
                onIncomingCallReceived(context, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = getTS();
                    onOutgoingCallStarted(context, callStartTime);
                } else {
                    isIncoming = true;
                    callStartTime = getTS();
                    onIncomingCallAnswered(context, callStartTime);
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, callStartTime, getTS());
                } else {
                    onOutgoingCallEnded(context, callStartTime, getTS());
                }
                break;
        }
        lastState = state;
    }
}