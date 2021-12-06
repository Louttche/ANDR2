package com.example.stalkr.services

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.stalkr.MainActivity
import com.example.stalkr.R

/**
 * A notification manager
 *
 * This class enables creating and showing a notification on the screen.
 *
 * @param context Mainly pass "this" if in an Activity. Pass "requireContext()" if in Fragment.
 */
class NotificationManager(private val context: Context) {
    companion object {
        var notificationId = 0
    }

    private fun getCustomDesign(
        title: String,
        message: String
    ): RemoteViews {
        val remoteViews = RemoteViews(
            context.packageName,
            R.layout.notification
        )
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.message, message)
        remoteViews.setImageViewResource(
            R.id.icon,
            R.drawable.stalkr_logo
        )
        return remoteViews
    }

    /**
     * Creates and shows a notification using  to this group.
     * @return the new size of the group.
     */
    fun show(title: String, message: String) {
        // Pass the intent to switch to the MainActivity
        val intent = Intent(context, MainActivity::class.java)
        // Assign channel ID
        // Here FLAG_ACTIVITY_CLEAR_TOP flag is set to clear
        // the activities present in the activity stack,
        // on the top of the Activity that is to be launched
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Pass the intent to PendingIntent to start the
        // next Activity
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        // Create a Builder object using NotificationCompat
        // class. This will allow control over all the flags
        var builder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            "notification_channel"
        )
            .setSmallIcon(R.drawable.stalkr_logo)
            .setAutoCancel(true)
            .setVibrate(
                longArrayOf(
                    1000, 1000
                )
            )
            .setSound(defaultSoundUri)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        // A customized design for the notification can be
        // set only for Android versions 4.1 and above. Thus
        // condition for the same is checked here.
        builder = if (Build.VERSION.SDK_INT
            >= Build.VERSION_CODES.JELLY_BEAN
        ) {
            builder.setContent(
                getCustomDesign(title, message)
            )
        } // If Android Version is lower than Jelly Beans,
        else {
            builder.setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.stalkr_logo)
        }

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId++, builder.build())
        }
    }

}