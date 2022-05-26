package com.techwave.assignment.workmanagers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.techwave.assignment.interfaces.Delay
import com.techwave.assignment.utils.Globals
import com.techwave.assignment.utils.Preferences

//This Work Manager is to re-enable our OneTime WorkManager, in case Android OS kills it.
class WorkManagerCareTaker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        //This logic is to check if the OneTime WorkManager got executed in the last 5 minutes. If not, trigger it from here.
        Globals.delayMainThread(3000, object : Delay {
            override fun done() {
                //If Android OS pauses all background processes due to low battery, etc. OS will trigger all paused processes at once. That will cause
                // our both Onetime & Periodic WorkManager to trigger at once. Periodic WorkManager will assume Onetime Work Manager didn't execute in last 5 minutes.
                // And it will try to execute again. Hence, We are save last WM Execution Timestamp in initial phase of Onetime Work Manager and pausing the below
                // last execution check for 3 seconds.
                if (Globals.tS - lastWMExecutionTS() > 5 * 60 * 1000) Globals.initWork()
            }
        })
        return Result.success()
    }

    private fun lastWMExecutionTS(): Long {
        return Preferences.loadLongPreferences(Preferences.PREF_LAST_WM_TS)
    }
}