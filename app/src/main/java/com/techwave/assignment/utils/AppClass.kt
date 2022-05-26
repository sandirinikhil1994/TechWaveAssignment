package com.techwave.assignment.utils

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.techwave.assignment.R
import com.techwave.assignment.models.CallData
import com.techwave.assignment.models.LocationCoords

class AppClass : Application() {
    override fun onCreate() {
        super.onCreate()
        Globals.global_ctx = this
        FirebaseApp.initializeApp(this)
        if (dbRef_LocData == null) {
            dbRef_LocData = FirebaseDatabase.getInstance().getReference(
                LocationCoords::class.java.simpleName
            )
            dbRef_LocData!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Globals.toast(Globals.getStr(R.string.sent_location_to_firebase))
                }

                override fun onCancelled(error: DatabaseError) {
                    Globals.toast(Globals.getStr(R.string.couldnt_send_locdata_to_firebase))
                }
            })
        }
        if (dbRef_CallData == null) {
            dbRef_CallData =
                FirebaseDatabase.getInstance().getReference(CallData::class.java.simpleName)
            dbRef_CallData!!.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Globals.toast(Globals.getStr(R.string.sent_calldata_to_firebase))
                }

                override fun onCancelled(error: DatabaseError) {
                    Globals.toast(Globals.getStr(R.string.couldnt_send_calldata_to_firebase))
                }
            })
        }
    }

    companion object {
        var dbRef_LocData: DatabaseReference? = null
        var dbRef_CallData: DatabaseReference? = null
        var savedNumber: String? = ""
    }
}