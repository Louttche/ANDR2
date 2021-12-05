package com.example.stalkr.data

import android.content.ContentValues
import android.util.Log
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ChatData(var cid: String, var members: MutableList<UserProfileData>) {

    // Fields (constructed)
    var messages: MutableList<MessageData> = mutableListOf()

    // Methods
    fun UpdateChatFromDB(){
        AuthActivity.db.collection("chats")
            .whereEqualTo("cid", this.cid)
            .get().addOnSuccessListener { groups ->
                // TODO: update members + messages ?
            }.addOnFailureListener(){
                Log.w(ContentValues.TAG, "Error getting messages from chat: ${this.cid}.", it)
            }
    }

    fun AddMessage(message: MessageData){
        if (!this.messages.contains(message))
            this.messages.add(message)
    }
    fun RemoveMessage(message: MessageData){
        if (this.messages.contains(message))
            this.messages.remove(message)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GroupData
        if (cid != other.gid) return false
        return true
    }
    override fun hashCode(): Int {
        return cid.hashCode()
    }
}
