package com.example.stalkr

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import com.example.stalkr.activities.AuthActivity
import com.example.stalkr.data.UserProfileData

import com.example.stalkr.databinding.FragmentMapBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.MapView
import com.google.firebase.firestore.QueryDocumentSnapshot

class MapFragment : Fragment(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    //private val currentUser get() = AuthActivity.userData

    // MAP
    private var mapView: MapView? = null
    private lateinit var mMap: GoogleMap

    // LOCATION
    private lateinit var currentLocation: Location
    private var locationUpdateState = false
    private var userPositionViewport : LatLngBounds = LatLngBounds(LatLng(0.0,0.0), LatLng(0.0,0.0))
    private var changeBounds: Boolean = true
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    // Google Markers
    private var userLocationMarker: Marker? = null
    private var otherUserProfileLocationMarkers: Map<UserProfileData, Marker>? = null

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        binding.btnMyLocation.setOnClickListener {
            moveCamera(currentLocation)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = _binding!!.mapView;
        mapView!!.onCreate(savedInstanceState)

        // First check for location permissions
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
            // if permissions not granted, request them
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE)
        } else{
            setupLocationCallback()
            mapView!!.getMapAsync(this);
            createLocationRequest()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true // not working with mapview for some reason
        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraIdleListener(this)
        mMap.setOnCameraMoveStartedListener(this)
        mMap.setOnCameraMoveListener(this)
        mMap.setOnCameraMoveCanceledListener(this)

        // Set up the custom info window
        val customInfoWindow = CustomInfoWindowForGoogleMap(requireContext())
        mMap!!.setInfoWindowAdapter(customInfoWindow)

        setupMap()
    }

    private fun setupMap() {
        Log.d(TAG,"setupMap")
        // Check permissions for fusedLocationClient listener
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mapView!!.onResume()
            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { location ->
                if (location != null){
                    placeMarkerOnMap(location)
                    currentLocation = location
                    moveCamera(currentLocation);
                }
            }
        }
    }

    fun setupLocationViewport(){
        if (changeBounds){
            // if marker goes beyond the view bounds, center the camera on user
            var meters_offset : Double = 30.0
            var latOffset : Double = MapUtils.metersToLat(meters_offset) // y
            var longOffset : Double = MapUtils.metersToLong(meters_offset, currentLocation.latitude) // x

            userPositionViewport = LatLngBounds(
                LatLng(currentLocation.latitude - latOffset, currentLocation.longitude - longOffset),  // SW corner
                LatLng(currentLocation.latitude + latOffset, currentLocation.longitude + longOffset) // NE corner
            )
            changeBounds = false
        }

        val currentlatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        if (!userPositionViewport.contains(currentlatLng)){
            changeBounds = true
            moveCamera(currentLocation)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "onRequestPermissionsResult")
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.size > 0 && grantResults[0] === PackageManager.PERMISSION_GRANTED) {
                    // permission was granted by the user
                    setupLocationCallback()
                    mapView!!.getMapAsync(this);
                    createLocationRequest()
                } else {
                    // permission was denied by the user
                    // TODO: decide what to do when permission was denied
                    mapView!!.onStop()
                }
                return
            }
        }
    }

    /**
     *  @should Set the marker title as the user name
     *  @should Set the user name to empty if is null
     */
    private fun placeMarkerOnMap(location: Location) {
        Log.d(TAG, "placeMarkerOnMap")
        val currentlatLng = LatLng(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            //Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.position(currentlatLng)
            markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
            markerOptions.title(AuthUserObject.name) //AuthActivity.userData
            userLocationMarker = mMap.addMarker(markerOptions)
            userLocationMarker!!.tag = markerOptions.title
        } else {
            //use the previously created marker
            userLocationMarker!!.position = currentlatLng
        }
    }

    /**
     * @should Set the user profile photo to default if is empty or null
     */
    private fun placeOtherMarkerOnMap(latLng: LatLng, userProfile: UserProfileData) {
        if (otherUserProfileLocationMarkers != null){
            // if the user already has a marker, just update position
            if (otherUserProfileLocationMarkers!!.any{it.key == userProfile}){
                // TODO: old user marker stays where it is
                otherUserProfileLocationMarkers!![userProfile]!!.position = latLng
            } else {
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                markerOptions.title(userProfile.name)

                val otherUserLocationMarker: Marker? = mMap.addMarker(markerOptions)
                otherUserProfileLocationMarkers!!.toMutableMap().putIfAbsent(userProfile, otherUserLocationMarker!!)
            }
        } else{
            otherUserProfileLocationMarkers = mutableMapOf()
        }
    }

    /**
     * @should Place a marker for other user on the map
     */
    private fun retrieveOtherUsersLocationFromDB() {
        val users = AuthActivity.db.collection("users")
        val userQuery = users
            .whereNotEqualTo("uid", AuthActivity.userDbData!!.uid)
            .get()
        userQuery.addOnSuccessListener {
            for (document in it) {
                if (document.get("isActive").toString().toBoolean()){
                    val latitude = document.get("latitude").toString().toDouble()
                    val longitude = document.get("longitude").toString().toDouble()
                    val latLng = LatLng(latitude, longitude)

                    val otherUser = UserProfileData(document.get("uid").toString())
                    // Make sure the user is updated from the DB before displaying info about them
                    otherUser.updateUserProfileFromDB(document)
                    placeOtherMarkerOnMap(latLng, otherUser)
                }
            }
        }
        userQuery.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error getting documents.", exception)
        }
    }

    override fun onMarkerClick(p0: Marker) = false

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged")
        // This really doesn't do anything, but I left it for testing purposes.
        placeMarkerOnMap(location)
        //saveLocationToDb(location)
        AuthUserObject.updateUserLocationInDB(location)
    }

    private fun setupLocationCallback(){
        Log.d(TAG,"setupLocationCallback")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                Log.d(TAG,"onLocationResult")

                currentLocation = p0.lastLocation
                setupLocationViewport()
                //saveLocationToDb(currentLocation) // moved to AuthUserObject data class
                AuthUserObject.updateUserLocationInDB(currentLocation)
                placeMarkerOnMap(currentLocation)
                retrieveOtherUsersLocationFromDB()
            }
        }
    }

    private fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates")
        if (checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
        }
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            locationUpdateState = true
            startLocationUpdates()
        }
        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(requireActivity(),
                        REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "MainActivity - onActivityResult")
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (::fusedLocationClient.isInitialized)
            fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            if (checkSelfPermission(
                    binding.root.context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
            }
        }
    }

    /* CAMERA STUFF */

    private fun moveCamera(location: Location){
        Log.d("camera", "in moveCamera");
        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(16f)            // Sets the zoom
            .build()                    // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }
    // when the camera starts moving.
    override fun onCameraMoveStarted(p0: Int) {
        Log.d(ContentValues.TAG, "onCameraMoveStarted");
    }
    // while the camera is moving or the user is interacting with the touch screen.
    override fun onCameraMove() {
        Log.d(ContentValues.TAG, "onCameraMove");
    }
    // when the current camera movement has been interrupted.
    override fun onCameraMoveCanceled() {
        Log.d(ContentValues.TAG, "onCameraMoveCanceled");
    }
    // when the camera stops moving and the user has stopped interacting with the map.
    override fun onCameraIdle() {
        Log.d(ContentValues.TAG, "onCameraIdle");
    }

    // -- //

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}