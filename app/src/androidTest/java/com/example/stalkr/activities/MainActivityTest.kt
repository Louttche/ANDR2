package com.example.stalkr.activities

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.runner.RunWith

import org.junit.Assert.*
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending

import com.example.stalkr.MainActivity
import com.example.stalkr.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import org.mockito.Mockito
import java.lang.IllegalStateException
import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents

import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal

import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import com.adevinta.android.barista.interaction.BaristaMenuClickInteractions.clickMenu
import com.google.android.material.navigation.NavigationBarView
import org.hamcrest.Matchers.not
import org.junit.*


@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    private val firestore : FirebaseFirestore = FirebaseFirestore.getInstance()
    private val firebase_auth : FirebaseAuth = FirebaseAuth.getInstance()

    // makes each test method execute one after the other (good for async stuff)
    //@get:Rule
    //var instantTaskExecutorRule = InstantTaskExecutorRule()

    // launch this activity globally (doesn't work well with firebase emulators)
    //@get:Rule
    //val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup(){
        try {
            //Intents.init()

            firestore.useEmulator("10.0.2.2", 8080); // Cloud Firestore Emulator
            firebase_auth.useEmulator("10.0.2.2", 9099); // Authentication Emulator

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