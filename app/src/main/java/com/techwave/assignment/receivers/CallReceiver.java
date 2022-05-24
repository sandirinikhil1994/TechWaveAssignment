package com.techwave.assignment.receivers;

import static com.techwave.assignment.utils.AppClass.savedNumber;
import static com.techwave.assignment.utils.Globals.delayMainThread;
import static com.techwave.assignment.utils.Globals.getStr;
import static com.techwave.assignment.utils.Globals.saveCallData;

import android.content.Context;
import android.util.Log;

import com.techwave.assignment.R;
import com.techwave.assignment.models.CallData;

public class CallReceiver extends PhonecallReceiver {

    @Override
    protected void onIncomingCallReceived(Context ctx, long start) {
        delayMainThread(1500, () -> Log.d("sdfdsfdsc", "incRec->" + savedNumber + " (" + start + ")"));
    }

    @Override
    protected void onIncomingCallAnswered(Context ctx, long start) {
        delayMainThread(1500, () -> Log.d("sdfdsfdsc", "incAns->" + savedNumber + " (" + start + ")"));
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, long start, long end) {
        delayMainThread(1500, () -> {
            Log.d("sdfdsfdsc", "incEnd->" + savedNumber + " (" + start + "," + end + ")");
            saveCallData(new CallData(savedNumber, getStr(R.string.incoming), start, end - start));
        });
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, long start) {
        delayMainThread(1500, () -> Log.d("sdfdsfdsc", "outStart->" + savedNumber + " (" + start + ")"));
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, long start, long end) {
        delayMainThread(1500, () -> {
            Log.d("sdfdsfdsc", "outEnd->" + savedNumber + " (" + start + "," + end + ")");
            saveCallData(new CallData(savedNumber, getStr(R.string.outgoing), start, end - start));
        });
    }

    @Override
    protected void onMissedCall(Context ctx, long start) {
        delayMainThread(1500, () -> {
            Log.d("sdfdsfdsc", "miss->" + savedNumber + " (" + start + ")");
            saveCallData(new CallData(savedNumber, getStr(R.string.missed), start, 0));
        });
    }

}