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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.maps.MapView

class mapFragment : Fragment(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LocationListener,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnMyLocationButtonClickListener{

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // AUTH + DB
    private val userCollectionRef = FirebaseFirestore.getInstance().collection("users")
    // temp - for debug
    private var userName: String = ""
    private var uid: String = "qd1VVWwkWtM57spPALvAjUyaZG02"

    // MAP
    private var mapView: MapView? = null
    private lateinit var mMap: GoogleMap

    // LOCATION
    private lateinit var currentLocation: Location
    private lateinit var lastLocation: Location
    private var locationUpdateState = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var userLocationMarker: Marker? = null
    private var otherUserLocationMarker: Marker? = null
    private var otherUserLocationMarkers: List<Marker>? = null
    private var userPositionBounds : LatLngBounds = LatLngBounds(LatLng(0.0,0.0), LatLng(0.0,0.0))
    private var changeBounds: Boolean = true

    companion object{
        private const val LOCATION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

    private fun setupLocationCallback(){
        Log.d(TAG,"setupLocationCallback")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                Log.d(TAG,"onLocationResult")

                if (::currentLocation.isInitialized){
                    lastLocation = currentLocation
                }
                else
                    lastLocation = p0.lastLocation

                currentLocation = p0.lastLocation
                setupLocationViewport()
                saveLocationToDb(currentLocation)
                placeMarkerOnMap(currentLocation)
                retrieveOtherUsersLocationFromDB()
            }
        }
    }

    fun setupLocationViewport(){

        if (changeBounds){
            // if marker goes beyond the view bounds, center the camera on user
            var meters_offset : Double = 30.0
            var latOffset : Double = metersToLat(meters_offset) // y
            var longOffset : Double = metersToLong(meters_offset, currentLocation.latitude) // x

            userPositionBounds = LatLngBounds(
                LatLng(currentLocation.latitude - latOffset, currentLocation.longitude - longOffset),  // SW corner
                LatLng(currentLocation.latitude + latOffset, currentLocation.longitude + longOffset) // NE corner
            )
            changeBounds = false
        }

        val currentlatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
        if (!userPositionBounds.contains(currentlatLng)){
            changeBounds = true
            //Log.d("viewport","user is out of viewport bounds - change: $changeBounds")
            moveCamera(currentLocation)
        }
        else
            Log.d("viewport","user is inside bounds - change: $changeBounds")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true // not working with mapview for some reason
        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);

        setupMap()
    }

    private fun setupMap() {
        Log.d(TAG,"setupMap")
        // If permissions are granted, set up map
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

    private fun placeMarkerOnMap(location: Location) {
        Log.d(TAG, "placeMarkerOnMap")
        val currentlatLng = LatLng(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            //Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.position(currentlatLng)
            //markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
            if (userName.isNotEmpty())
                markerOptions.title(userName)
            userLocationMarker = mMap.addMarker(markerOptions)
        } else {
            //use the previously created marker
            userLocationMarker!!.position = currentlatLng
            //userLocationMarker!!.rotation = location.bearing
        }
    }

    // TODO: change string to USER data type in 'user' param
    private fun placeOtherMarkerOnMap(latLng: LatLng, user: String) {
        if (otherUserLocationMarkers != null){
            // if the user already has a marker, just update position
            if (otherUserLocationMarkers!!.any{it.title == user}){
                // TODO: old user marker stays where it is
                otherUserLocationMarkers!!.find{it.title == user}!!.position = latLng
            } else {
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                markerOptions.title(user)

                otherUserLocationMarker = mMap.addMarker(markerOptions)
                otherUserLocationMarkers!!.toMutableList().add(otherUserLocationMarker!!)
            }
        } else{
            otherUserLocationMarkers = emptyList()
        }
    }

    private fun retrieveOtherUsersLocationFromDB() {
        val userQuery = userCollectionRef
            .get()
        userQuery.addOnSuccessListener {
            for (document in it) {
                val latitude = document.get("latitude").toString().toDouble()
                val longitude = document.get("longitude").toString().toDouble()
                val latLng = LatLng(latitude, longitude)

                // TODO: retrieve static user model properties somewhere else ONCE
                userName = document.get("name").toString()

                if (document.get("uid").toString() != uid)
                    placeOtherMarkerOnMap(latLng, document.get("name").toString())
            }
        }
        userQuery.addOnFailureListener { exception ->
            Log.w(ContentValues.TAG, "Error getting documents.", exception)
        }
    }

    private fun saveLocationToDb(location: Location) {
        Log.d(TAG,"saveLocationToDb")
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        val userQuery = userCollectionRef
            .whereEqualTo("uid", uid)
            .get()
        userQuery.addOnSuccessListener {
            for(document in it) {
                db.collection("users").document(document.id).set(user, SetOptions.merge())
            }
        }
    }

    // TODO: Pop up little window to show some more info/options for the selected user
    override fun onMarkerClick(p0: Marker) = false

    override fun onLocationChanged(location: Location) {
        // This really doesn't do anything, but I left it for testing purposes.
        placeMarkerOnMap(location)
        saveLocationToDb(location)
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
            // 6
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
        if (requestCode == mapFragment.REQUEST_CHECK_SETTINGS) {
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
        Log.d("camera", "in changeCamera");

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

    override fun onMyLocationButtonClick(): Boolean {
        //changeCamera(lastLocation)
        return true
    }

    private fun metersToLat(meters: Double) : Double {
        // assume 111,111 meters is 1 degree of latitude in y direction
        return meters/111111
    }

    private fun metersToLong(meters: Double, lat: Double) : Double {
        // assume 111,111 * cos(latitude) meters is 1 degree of longitude in the x direction
        return meters/111111 * kotlin.math.cos(lat)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}