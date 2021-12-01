package com.example.stalkr.activities

import org.junit.runner.RunWith

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner

import com.example.stalkr.MainActivity
import com.example.stalkr.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import java.lang.IllegalStateException
import android.util.Log
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.rule.GrantPermissionRule
import com.adevinta.android.barista.interaction.BaristaMenuClickInteractions.clickMenu
import org.junit.*

@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    var grantFineLocationPermissionRule: GrantPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_FINE_LOCATION")
    @get:Rule
    var grantCoarseLocationPermissionRule: GrantPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_COARSE_LOCATION")

    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()

    // makes each test method execute one after the other (good for async stuff)
    //@get:Rule
    //var instantTaskExecutorRule = InstantTaskExecutorRule()

    // launch this activity globally (doesn't work well with firebase emulators)
    //@get:Rule
    //val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup(){
        try {
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

    @Test
    fun changeToMapFragmentWhenCreated(){
        // Launch main activity
        ActivityScenario.launch(MainActivity::class.java)
        // Check currently displayed view
        onView(withId(R.id.map)).check(matches(isDisplayed()))
    }

    @Test
    fun changeToAuthActivityWhenSignedOut(){
        // Launch main activity
        ActivityScenario.launch(MainActivity::class.java)

        // Act signing out from the actionbar menu
        clickMenu(R.id.action_logout)
        // Check currently displayed view
        onView(withId(R.id.auth)).check(matches(isDisplayed()))
    }
}