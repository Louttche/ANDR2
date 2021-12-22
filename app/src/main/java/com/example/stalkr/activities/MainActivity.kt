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
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.stalkr.activities.AuthActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.stalkr.databinding.ActivityMainBinding
import com.example.stalkr.services.SensorService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.lang.NullPointerException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel("Notification channel", "A channel for sending notifications")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        val navController = navHostFragment.navController

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        bottomNav?.setupWithNavController(navController)

        // transaction fragment change (not when using navhost)
        //changeFragment(MapFragment())
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
            setUserActivity(AuthActivity.userDbData!!.uid, false)
        } catch (e: NullPointerException){
            Log.d(TAG, "Could not sign out - $e")
        } finally {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setUserActivity(userID: String, active: Boolean){
        // Set auth user as 'inactive' in DB
        AuthUserObject.isActive = active
        val users = AuthActivity.db.collection("users")

        users.whereEqualTo("uid", userID)
            .get()
            .addOnSuccessListener { documents ->
                val userActive = hashMapOf("isActive" to active)
                users.document(documents.first().id).set(userActive, SetOptions.merge())
            }
    }

    // function to change the fragment which is used to reduce the lines of code
    private fun changeFragment(fragmentToChange: Fragment): Unit {
        supportFragmentManager.beginTransaction().apply {
            //replace(binding.fragmentContainerViewMain.id, fragmentToChange) // normal fragment container
            replace(binding.navHostFragment.id, fragmentToChange) // navhost fragment container
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
        // sign out
        setUserActivity(AuthActivity.userDbData!!.uid, false)
        super.onDestroy()
    }

    private fun startForegroundServiceForSensors(background: Boolean) {
        val intent = Intent(this, SensorService::class.java)
        intent.putExtra(SensorService.KEY_BACKGROUND, background)
        ContextCompat.startForegroundService(this, intent)
    }

}