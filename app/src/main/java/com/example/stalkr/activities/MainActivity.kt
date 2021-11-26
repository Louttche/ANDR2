package com.example.stalkr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log

import com.example.stalkr.databinding.ActivityMainBinding
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    // MAIN
    private lateinit var binding: ActivityMainBinding

    // AUTH
    var db: FirebaseFirestore? = FirebaseFirestore.getInstance()
    //private var userName: String = "Sally"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel("Notification channel", "A channel for sending notifications");

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // when app is initially opened the Map Fragment should be visible
        changeFragment(MapFragment())
    }

    // function to change the fragment which is used to reduce the lines of code
    private fun changeFragment(fragmentToChange: Fragment): Unit {
        supportFragmentManager.beginTransaction().apply {
            replace(binding.fragmentContainerViewMain.id, fragmentToChange)
            addToBackStack(null)
            commit()
        }
    }

    private fun createNotificationChannel(name: String, descriptionText: String) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("NOTIFICATIONS", "Created a notification channel")
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("notification_channel", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}