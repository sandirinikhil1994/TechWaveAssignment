package com.techwave.assignment.workmanagers;

import static com.techwave.assignment.utils.Globals.delayMainThread;
import static com.techwave.assignment.utils.Globals.getTS;
import static com.techwave.assignment.utils.Globals.initWork;
import static com.techwave.assignment.utils.Preferences.PREF_LAST_WM_TS;
import static com.techwave.assignment.utils.Preferences.loadLongPreferences;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

//This Work Manager is to re-enable our OneTime WorkManager, in case Android OS kills it.
public class WorkManager_CareTaker extends Worker {

    public WorkManager_CareTaker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //This logic is to check if the OneTime WorkManager got executed in the last 5 minutes. If not, trigger it from here.
        delayMainThread(3000, () -> {
            //If Android OS pauses all background processes due to low battery, etc. OS will trigger all paused processes at once. That will cause
            // our both Onetime & Periodic WorkManager to trigger at once. Periodic WorkManager will assume Onetime Work Manager didn't execute in last 5 minutes.
            // And it will try to execute again. Hence, We are save last WM Execution Timestamp in initial phase of Onetime Work Manager and pausing the below
            // last execution check for 3 seconds.
            if (getTS() - lastWMExecutionTS() > 5 * 60 * 1000)
                initWork();
        });
        return Result.success();
    }

    private long lastWMExecutionTS() {
        return loadLongPreferences(PREF_LAST_WM_TS);
    }
}