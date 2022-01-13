package com.example.stalkr.activities

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.content.ContentValues.TAG
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.stalkr.AuthUserObject
import com.example.stalkr.R
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.stalkr.databinding.ActivityMainBinding
import com.example.stalkr.services.CompassService
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.lang.NullPointerException
import androidx.navigation.ui.AppBarConfiguration
import android.content.DialogInterface


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var compassIntent: Intent

    companion object NavData {
        var navHostFragment: NavHostFragment? = null
    }

    var appBarConfiguration: AppBarConfiguration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel("Notification channel", "A channel for sending notifications")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Init compass intent service
        compassIntent = Intent(this, CompassService::class.java)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment? ?: return
        val navController = navHostFragment!!.navController

        appBarConfiguration = AppBarConfiguration.Builder(navController.graph).build()
        NavigationUI.setupActionBarWithNavController(
            this,
            navController, appBarConfiguration!!
        )

        // hide fragment titles from actionbar
        supportActionBar!!.setDisplayShowTitleEnabled(false)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavBar)
        bottomNav?.setupWithNavController(navController)
    }

    /* NAVIGATION */
    override fun onSupportNavigateUp(): Boolean {
        // TODO: FIX
        return NavigationUI.navigateUp(navHostFragment!!.navController, appBarConfiguration!!)
                || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_home_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.action_logout == item.itemId) {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", object : DialogInterface.OnClickListener {
                    override fun onClick(arg0: DialogInterface?, arg1: Int) {
                        signOut()
                    }
                }).create().show()
        } else {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
        return true
    }

    // function to change the fragment which is used to reduce the lines of code
    private fun changeFragment(fragmentToChange: Fragment): Boolean {
        try {
            supportFragmentManager.beginTransaction().apply {
                //replace(binding.fragmentContainerViewMain.id, fragmentToChange) // normal fragment container
                replace(binding.navHostFragment.id, fragmentToChange) // navhost fragment container
                addToBackStack(null)
                commit()
            }

            return true
        } catch (e: Exception) {
            Log.ERROR
        }

        return false
    }

    /* Auth */
    private fun signOut() {
        try {
            // Update user login status to inActive
            setUserToInactive()

            // SignOut from firebase (After signedOut AuthUser will be null)
            Firebase.auth.signOut()
        } catch (e: NullPointerException) {
            Log.d(TAG, "Could not sign out - $e")
        } finally {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setUserToInactive() {
        // Set auth user as 'inactive' in DB
        AuthUserObject.isActive = false
        val users = FirebaseFirestore.getInstance().collection("users")

        users.whereEqualTo("uid", Firebase.auth.uid.toString())
            .get()
            .addOnSuccessListener { documents ->
                val userActive = hashMapOf("isActive" to false)
                users.document(documents.single().id).set(userActive, SetOptions.merge())
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

        // Start compass service
        startService(compassIntent)
    }

    override fun onPause() {
        super.onPause()

        // Stop compass service
        stopService(compassIntent)
    }
}