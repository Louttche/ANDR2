package com.example.stalkr.broadcastReceivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stalkr.services.CompassService
import com.example.stalkr.services.CompassService.SensorServiceKeys.KEY_NOTIFICATION_ID
import com.example.stalkr.services.CompassService.SensorServiceKeys.KEY_NOTIFICATION_STOP_ACTION

class NotificationListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // If broad receiver receives and intent for stop the notification then stop service
        if (intent != null && intent.action != null) {
            if (intent.action.equals(KEY_NOTIFICATION_STOP_ACTION)) {
                context?.let {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val compassNotificationIntent = Intent(context, CompassService::class.java)
                    context.stopService(compassNotificationIntent)
                    val notificationId = compassNotificationIntent.getIntExtra(KEY_NOTIFICATION_ID, -1)

                    if (notificationId != -1) {
                        notificationManager.cancel(notificationId)
                    }
                }
            }
        }
    }
}