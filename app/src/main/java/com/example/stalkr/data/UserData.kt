package com.example.stalkr.data
import android.content.ContentValues
import android.util.Log
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.IgnoreExtraProperties


@IgnoreExtraProperties
data class UserData (var uid: String, var name: String) {

    // Fields
    var isActive: Boolean = false
    var groups: MutableList<GroupData>? = mutableListOf()

    // Methods
    fun updateUserFromDB(id: String){
        this.uid = id
        AuthActivity.userCollectionRef.whereEqualTo("uid", this.uid)
            .get()
            .addOnSuccessListener { users ->
                this.name = users.first().data["name"].toString()
                this.isActive = users.first().data["isActive"].toString().toBoolean()

                /*
                // update groups
                AuthActivity.groupCollectionRef.get().addOnSuccessListener { groups ->
                    for (group in groups){
                        this.groups!!.find{ it.gid == group.get("gid") }!!.UpdateGroupFromDB()
                    }
                }.addOnFailureListener{
                    Log.w(ContentValues.TAG, "Error getting groups for user: $uid.", it)
                }
                 */
            }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserData
        if (uid != other.uid) return false
        return true
    }
    override fun hashCode(): Int {
        return uid.hashCode()
    }
}