package com.example.stalkr.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class MessageData(var mid: String, var content: String, var from: UserProfileData){

    // Fields
    var timestamp: Timestamp = Timestamp.now()

    // Methods

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MessageData
        if (mid != other.mid) return false
        if (timestamp != other.timestamp) return false
        return true
    }
    override fun hashCode(): Int {
        var result = mid.hashCode()
        result = 31 * result + timestamp.hashCode()
        return result
    }
}
