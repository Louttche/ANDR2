package com.example.stalkr.fragments

import com.example.stalkr.MainActivity
import org.junit.Assert.*
import org.junit.Test
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

internal class MapFragmentTest{

    //val VALID_MAINACTIVITY : MainActivity = mock(MainActivity.class)

    @Test
    fun MapFocusesUserWhenMapStarts() {

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