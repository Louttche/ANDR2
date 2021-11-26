package com.example.stalkr

import android.util.Log
import com.example.stalkr.services.NotificationManager


import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseMessageReceiver : FirebaseMessagingService() {
    private val notificationManager: NotificationManager =
        NotificationManager(this);

    override fun onNewToken(token: String) {
        Log.d("FirebaseLog", "The token: $token");
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // First case when notifications are received via
        // data event
        // Here, 'title' and 'message' are the assumed names of JSON
        // attributes. Since here we do not have any data
        // payload, This section is commented out. It is
        // here only for reference purposes.
        /*if(remoteMessage.getData().size()>0){
            showNotification(remoteMessage.getData().get("title"),
                          remoteMessage.getData().get("message"));
        }*/

        // Second case when notification payload is received.
        if (remoteMessage.getNotification() != null) {
            val title = remoteMessage.getNotification()!!.getTitle().toString()
            val body = remoteMessage.getNotification()!!.getBody().toString()
            notificationManager.show(
                title, body
            )
        }
    }
}