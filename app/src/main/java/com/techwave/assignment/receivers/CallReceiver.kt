package com.techwave.assignment.receivers

import android.content.Context
import android.util.Log
import com.techwave.assignment.R
import com.techwave.assignment.interfaces.Delay
import com.techwave.assignment.models.CallData
import com.techwave.assignment.utils.AppClass
import com.techwave.assignment.utils.Globals
import com.techwave.assignment.utils.Globals.getStr

class CallReceiver : PhonecallReceiver() {
    override fun onIncomingCallReceived(ctx: Context?, start: Long) {
        execLater("${getStr(R.string.incoming)} received", start, 0, true)
    }

    override fun onIncomingCallAnswered(ctx: Context?, start: Long) {
        execLater("${getStr(R.string.incoming)} start", start, 0, true)
    }

    override fun onIncomingCallEnded(ctx: Context?, start: Long, end: Long) {
        execLater("${getStr(R.string.incoming)} end", start, end - start, false)
    }

    override fun onOutgoingCallStarted(ctx: Context?, start: Long) {
        execLater("${getStr(R.string.outgoing)} start", start, 0, true)
    }

    override fun onOutgoingCallEnded(ctx: Context?, start: Long, end: Long) {
        execLater("${getStr(R.string.outgoing)} end", start, end - start, false)
    }

    override fun onMissedCall(ctx: Context?, start: Long) {
        execLater(getStr(R.string.missed), start, 0, false)
    }

    private fun execLater(type: String, start: Long, end: Long, justLog: Boolean) {
        Globals.delayMainThread(1500, object : Delay {
            override fun done() {
                var tempS = if (end != 0L) ", $end" else ""
                Log.d("sdfdsfdsc", "$type-> ${AppClass.savedNumber} ($start $tempS)")
                if (!justLog)
                    Globals.saveCallData(CallData(AppClass.savedNumber!!, type, start, end))
            }
        })
    }
}