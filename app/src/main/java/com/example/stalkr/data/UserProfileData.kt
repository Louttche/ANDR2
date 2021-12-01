package com.example.stalkr.data
import android.location.Location
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.SetOptions
import com.google.rpc.context.AttributeContext

// Common data for all users // TODO: make it into Profile?
@IgnoreExtraProperties
data class UserProfileData (var uid: String, var name: String?) {

    // Fields
    var isActive: Boolean = false
    var groups: MutableList<GroupData>? = mutableListOf()

    // Methods

    /**
     *  @should set name to empty if null
     */
    fun updateUserProfileFromDB(id: String){
        val users = AuthActivity.db.collection("users")
        this.uid = id
        users.whereEqualTo("uid", this.uid)
            .get()
            .addOnSuccessListener { usersDocuments ->
                val namedb = usersDocuments.first().data["name"].toString()
                if (namedb == null)
                    this.name = ""
                else
                    this.name = namedb

                this.isActive = usersDocuments.first().data["isActive"].toString().toBoolean()

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

    /**
     * @should throw a NullPointerException if location is null
     */
    fun updateUserProfileLocationInDB(location: Location){
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as UserProfileData
        if (uid != other.uid) return false
        return true
    }
    override fun hashCode(): Int {
        return uid.hashCode()
    }
}