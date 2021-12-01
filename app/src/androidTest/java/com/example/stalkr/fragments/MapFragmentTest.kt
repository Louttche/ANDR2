package com.example.stalkr.fragments

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

@RunWith(AndroidJUnit4::class)
internal class MapFragmentTest{

    @Rule
    var grantFineLocationPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_FINE_LOCATION")
    @Rule
    var grantCoarseLocationPermissionRule = GrantPermissionRule.grant("android.permission.ACCESS_COARSE_LOCATION")

    /* Setup Example
    private lateinit var logHistory: LogHistory

    @Before
    fun createLogHistory() {
        logHistory = LogHistory()
    }
     */

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