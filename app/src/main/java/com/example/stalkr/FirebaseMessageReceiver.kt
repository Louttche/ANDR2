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
        if (remoteMessage.getNotification() != null) {
            val title = remoteMessage.getNotification()!!.getTitle().toString()
            val body = remoteMessage.getNotification()!!.getBody().toString()
            notificationManager.show(
                title, body
            )
        }
    }
}