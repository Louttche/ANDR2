package com.example.stalkr.data

import android.content.ContentValues
import android.util.Log
import com.example.stalkr.activities.AuthActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.SetOptions

@IgnoreExtraProperties
data class GroupData(var gid: String, var title: String, var chat: ChatData){

    // Fields
    var members: MutableList<UserProfileData> = mutableListOf()

    // Methods
    fun AddMember(userProfile: UserProfileData){
        if (!this.members.contains(userProfile))
            this.members.add(userProfile)
    }

    fun RemoveMember(userProfile: UserProfileData){
        if (this.members.contains(userProfile))
            this.members.remove(userProfile)
    }

    fun UpdateGroupInDB(){
        val groupsCollectionReference = FirebaseFirestore.getInstance().collection("groups")
        groupsCollectionReference.whereEqualTo("gid", this.gid)
            .get()
            .addOnSuccessListener { documents ->
                val group = hashMapOf(
                    "title" to title,
                    "chat" to chat,
                    "members" to members
                )

                groupsCollectionReference.document(documents.first().id).set(group, SetOptions.merge())
            }
    }

    fun UpdateGroupFromDB(){
        val groups = AuthActivity.db.collection("groups")
        val groupsQuery = groups.whereEqualTo("gid", this.gid)
            .get().addOnSuccessListener { groups ->
                this.title = groups.first().data["title"].toString()
                this.chat.UpdateChatFromDB()

                // TODO: Update members

        }.addOnFailureListener(){
                Log.w(ContentValues.TAG, "Error getting group ${this.gid}.", it)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as GroupData
        if (gid != other.gid) return false
        return true
    }
    override fun hashCode(): Int {
        return gid.hashCode()
    }
}
