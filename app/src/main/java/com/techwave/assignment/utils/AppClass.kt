package com.techwave.assignment.utils;

import static com.techwave.assignment.utils.Globals.getStr;
import static com.techwave.assignment.utils.Globals.global_ctx;
import static com.techwave.assignment.utils.Globals.toast;

import android.app.Application;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.techwave.assignment.R;
import com.techwave.assignment.models.CallData;
import com.techwave.assignment.models.LocationCoords;

import java.util.ArrayList;
import java.util.List;

public class AppClass extends Application {

    public static DatabaseReference dbRef_LocData, dbRef_CallData;
    public static String savedNumber;

    @Override
    public void onCreate() {
        super.onCreate();
        global_ctx = this;
        FirebaseApp.initializeApp(global_ctx);
        if (dbRef_LocData == null) {
            dbRef_LocData = FirebaseDatabase.getInstance().getReference(LocationCoords.class.getSimpleName());
            dbRef_LocData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    toast(getStr(R.string.sent_location_to_firebase));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    toast(getStr(R.string.couldnt_send_locdata_to_firebase));
                }
            });
        }
        if (dbRef_CallData == null) {
            dbRef_CallData = FirebaseDatabase.getInstance().getReference(CallData.class.getSimpleName());
            dbRef_CallData.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    toast(getStr(R.string.sent_calldata_to_firebase));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    toast(getStr(R.string.couldnt_send_calldata_to_firebase));
                }
            });
        }
    }
}
