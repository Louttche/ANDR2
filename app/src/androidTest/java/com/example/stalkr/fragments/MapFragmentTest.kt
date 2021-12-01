package com.example.stalkr.fragments

import android.util.Log
import androidx.test.espresso.intent.Intents
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.stalkr.MainActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith
import com.google.common.truth.Truth.assertThat
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.lang.IllegalStateException

@RunWith(AndroidJUnit4::class)
internal class MapFragmentTest{

    @Rule
    var grantFineLocationPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_FINE_LOCATION")
    @Rule
    var grantCoarseLocationPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_COARSE_LOCATION")

    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()

    @Before
    fun setup(){
        try {
            //Intents.init()

            firestore.useEmulator("10.0.2.2", 8080) // Cloud Firestore Emulator
            firebaseAuth.useEmulator("10.0.2.2", 9099) // Authentication Emulator

            val settings : FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(false)
                .build()
            firestore.firestoreSettings = settings
        } catch (e: IllegalStateException){
            Log.d("tests", "Firestore useEmulator() already called. - $e")
        }
    }

    @After
    fun tearDown() {
        //Intents.release()
    }

    @Test
    fun MapFocusesUserWhenMapStarts() {

    }

    @Test
    fun StartMapOnlyIfUserAuthenticated(){

    }

    @Test
    fun CameraFocusesUserWhenUserIsOutOfViewport() {

    }

    @Test
    fun RequestLocationPermissionIfNotGranted() {

    }

    @Test
    fun CameraStopsFollowingUserWhenNavigatingMap() {
        // If the user is moving but is looking around the map
        // stop 'camera follow' until they manually refocus

    }
}