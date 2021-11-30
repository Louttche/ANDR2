package com.example.stalkr

import android.location.Location
import com.example.stalkr.activities.AuthActivity
import com.example.stalkr.data.GroupData
import com.google.firebase.firestore.SetOptions

object AuthUserObject {

    // Fields
    var uid: String = ""
    var name: String = ""
    var isActive: Boolean = false
    var groups: MutableList<GroupData>? = mutableListOf()

    fun updateUserLocationInDB(location: Location){
        val userLocation = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        val users = AuthActivity.db.collection("users")
        val userQuery = users
            .whereEqualTo("uid", this.uid)
            .get()
        userQuery.addOnSuccessListener {
            users.document(it.first().id).set(userLocation, SetOptions.merge())
        }
    }
}