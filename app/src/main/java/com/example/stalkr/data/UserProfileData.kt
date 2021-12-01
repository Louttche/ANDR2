package com.example.stalkr.data
import android.location.Location
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.rpc.context.AttributeContext

// Common data for all users // TODO: make it into Profile?
@IgnoreExtraProperties
data class UserProfileData (val uid: String, var name: String? = null) {

    // Fields
    var isActive: Boolean = false
    var groups: MutableList<GroupData>? = mutableListOf()

    // Methods

    /**
     *  @should set name to a default string if empty or null
     */
    fun updateUserProfileFromDB(document: QueryDocumentSnapshot){
        this.name = document.data["name"].toString()
        if (this.name == null || this.name == "")
            this.name = "N/A"
        this.isActive = document.data["isActive"].toString().toBoolean()
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