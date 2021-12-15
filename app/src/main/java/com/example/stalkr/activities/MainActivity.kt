package com.example.stalkr

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentFilter
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat


import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.stalkr.activities.AuthActivity
import com.example.stalkr.databinding.ActivityMainBinding
import com.example.stalkr.services.SensorService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel("Notification channel", "A channel for sending notifications")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val textViewDirection = findViewById<TextView>(R.id.textViewDirection)
        val imageViewCompass = findViewById<ImageView>(R.id.imageViewCompass)

        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val direction = intent.getStringExtra(SensorService.KEY_DIRECTION)
                val angle = intent.getDoubleExtra(SensorService.KEY_ANGLE, 0.0)
                val angleWithDirection = "$angle  $direction"
                textViewDirection.text = angleWithDirection
                imageViewCompass.rotation = angle.toFloat() * -1
            }
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter(SensorService.KEY_ON_SENSOR_CHANGED_ACTION)
        )

        // when app is initially opened the Map Fragment should be visible
        changeFragment(MapFragment())
    }

    /* Auth */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.action_logout == item.itemId) {
            Firebase.auth.signOut()
            signOut()
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun signOut() {
        try {
            // Set auth user as 'inactive' in DB
            AuthUserObject.isActive = false
            val users = AuthActivity.db.collection("users")

            users.whereEqualTo("uid", AuthActivity.userDbData!!.uid)
                .get()
                .addOnSuccessListener { documents ->
                    val userActive = hashMapOf("isActive" to false)
                    users.document(documents.first().id).set(userActive, SetOptions.merge())
                }
        } catch (e: NullPointerException){
            Log.d(TAG, "Could not sign out - $e")
        } finally {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
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

    override fun onResume() {
        super.onResume()
        startForegroundServiceForSensors(false)

    }

    override fun onPause() {
        super.onPause()
        startForegroundServiceForSensors(true)
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    private fun startForegroundServiceForSensors(background: Boolean) {
        val intent = Intent(this, SensorService::class.java)
        intent.putExtra(SensorService.KEY_BACKGROUND, background)
        ContextCompat.startForegroundService(this, intent)
    }

}