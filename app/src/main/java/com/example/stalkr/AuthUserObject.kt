package com.example.stalkr

import android.content.ContentValues.TAG
import android.location.Location
import android.util.Log
import com.example.stalkr.activities.AuthActivity
import com.example.stalkr.data.GroupData
import com.google.firebase.firestore.SetOptions

object AuthUserObject {

    // Fields
    var uid: String = ""
    var name: String = ""
    var pfpURL: String = ""
    var isActive: Boolean = false
    var groups: MutableList<GroupData>? = mutableListOf()

    fun updateUserLocationInDB(location: Location){
        try {
            val userLocation = hashMapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude
            )

            val users = AuthActivity.db.collection("users")
            val userQuery = users
                .whereEqualTo("uid", this.uid)
                .get()
            userQuery.addOnSuccessListener {
                try {
                    users.document(it.first().id).set(userLocation, SetOptions.merge())
                } catch (e: NoSuchElementException) {
                    Log.d(TAG, "No such element - $e")
                }
            }
        } catch(e: NullPointerException){
            Log.d(TAG, "Could not update user's $name location in DB - $e")
        }
    }
}