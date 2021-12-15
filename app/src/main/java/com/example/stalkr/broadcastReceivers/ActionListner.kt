package com.example.stalkr.broadcastReceivers

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.stalkr.services.SensorService
import com.example.stalkr.services.SensorService.SensorServiceKeys.KEY_NOTIFICATION_ID
import com.example.stalkr.services.SensorService.SensorServiceKeys.KEY_NOTIFICATION_STOP_ACTION

class ActionListener : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // If broad receiver receives and intent for stop the notification then stop service
        if (intent != null && intent.action != null) {
            if (intent.action.equals(KEY_NOTIFICATION_STOP_ACTION)) {
                context?.let {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val intent = Intent(context, SensorService::class.java)
                    context.stopService(intent)
                    val notificationId = intent.getIntExtra(KEY_NOTIFICATION_ID, -1)

                    if (notificationId != -1) {
                        notificationManager.cancel(notificationId)
                    }
                }
            }
        }
    }
}