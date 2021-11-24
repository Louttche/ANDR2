package com.example.stalkr.activities

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import com.example.stalkr.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.stalkr.databinding.ActivityMapsBinding
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    LocationListener,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener,
    GoogleMap.OnMyLocationButtonClickListener {

    // DB
    private val userCollectionRef = FirebaseFirestore.getInstance().collection("users")

    // MAIN
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    // LOCATION
    private lateinit var currentLocation: Location
    private lateinit var lastLocation: Location
    private var locationUpdateState = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var userLocationMarker: Marker? = null
    private var otherUserLocationMarker: Marker? = null
    private var otherUserLocationMarkers: List<Marker>? = null

    // AUTH
    private var userName: String = "Sally"

    companion object {
        private const val LOCATION_REQUEST_CODE = 1
        private const val REQUEST_CHECK_SETTINGS = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set name of the logged in user
        setUsername()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                super.onLocationResult(p0)
                Log.d(TAG, "onLocationResult")

                if (::currentLocation.isInitialized)
                    lastLocation = currentLocation
                else
                    lastLocation = p0.lastLocation

                currentLocation = p0.lastLocation
                saveLocationToDb(currentLocation)
                placeMarkerOnMap(currentLocation)
                retrieveLocationFromDB()
            }
        }
        createLocationRequest()
    }

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
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setUsername() {
        val firebaseAuth = Firebase.auth
        FirebaseFirestore.getInstance().collection("users")
            .whereEqualTo("uid", firebaseAuth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                userName = documents.first().data["name"].toString()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.setOnMarkerClickListener(this)

        setupMap()
    }

    private fun setupMap() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
            mMap.setOnMyLocationButtonClickListener(this)
            mMap.setOnCameraIdleListener(this)
            mMap.setOnCameraMoveStartedListener(this)
            mMap.setOnCameraMoveListener(this)
            mMap.setOnCameraMoveCanceledListener(this)

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                placeMarkerOnMap(location)
                currentLocation = location
                moveCamera(currentLocation)
            }
        }
    }

    private fun placeMarkerOnMap(location: Location) {
        val currentlatLng = LatLng(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            //Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.position(currentlatLng)
            //markerOptions.rotation(location.bearing)
            markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
            markerOptions.title(userName)
            userLocationMarker = mMap.addMarker(markerOptions)
        } else {
            //use the previously created marker
            userLocationMarker!!.position = currentlatLng
            //userLocationMarker!!.rotation = location.bearing
        }

        if (::lastLocation.isInitialized) {
            // if location has not actually changed, don't move camera
            val lastlatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            if (currentlatLng != lastlatLng)
                moveCamera(location)
        }
    }

    // TODO: change string to USER data type in 'user' param
    private fun placeOtherMarkerOnMap(latLng: LatLng, user: String) {

        if (otherUserLocationMarkers != null) {
            // if the user already has a marker, just update position
            if (otherUserLocationMarkers!!.any { it.title == user }) {
                otherUserLocationMarkers!!.find { it.title == user }!!.position = latLng
            } else {
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                markerOptions.title(user)

                otherUserLocationMarker = mMap.addMarker(markerOptions)
                otherUserLocationMarkers!!.toMutableList().add(otherUserLocationMarker!!)
            }
        } else {
            otherUserLocationMarkers = emptyList()
        }
    }

    private fun retrieveLocationFromDB() {
        val userQuery = userCollectionRef
            //.whereNotEqualTo("name", userName)
            .get()
        userQuery.addOnSuccessListener {
            for (document in it) {
                //Log.d("DB - user","retrieveLocationFromDB - ${document.get("name").toString()}")

                val latitude = document.get("latitude").toString().toDouble()
                val longitude = document.get("longitude").toString().toDouble()
                val latLng = LatLng(latitude, longitude)
                //todo we should look for users based on uid, different users can have the same name
                if (document.get("name").toString() != userName)
                    placeOtherMarkerOnMap(latLng, document.get("name").toString())
            }
        }
        userQuery.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }
    }

    private fun saveLocationToDb(location: Location) {
        val db = FirebaseFirestore.getInstance()
        val user = hashMapOf(
            "latitude" to location.latitude,
            "longitude" to location.longitude
        )

        val userQuery = userCollectionRef
            .whereEqualTo("name", userName)
            .get()
        userQuery.addOnSuccessListener {
            for (document in it) {
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

    // 1
    private lateinit var locationCallback: LocationCallback

    // 2
    private lateinit var locationRequest: LocationRequest

    private fun startLocationUpdates() {
        //1
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        //2
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null /* Looper */
        )
    }

    private fun createLocationRequest() {
        // 1
        locationRequest = LocationRequest()
        // 2
        locationRequest.interval = 5000
        // 3
        locationRequest.fastestInterval = 1000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        // 4
        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        // 5
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
                    e.startResolutionForResult(
                        this@MapsActivity,
                        REQUEST_CHECK_SETTINGS
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    // 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == Activity.RESULT_OK) {
                locationUpdateState = true
                startLocationUpdates()
            }
        }
    }

    // 2
    override fun onPause() {
        super.onPause()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    // 3
    public override fun onResume() {
        super.onResume()
        if (!locationUpdateState) {
            startLocationUpdates()
        }
    }


    /* CAMERA STUFF */

    private fun moveCamera(location: Location) {
        Log.d("camera", "in changeCamera")

        val cameraPosition = CameraPosition.Builder()
            .target(LatLng(location.latitude, location.longitude))
            .zoom(12f)            // Sets the zoom
            .build()                    // Creates a CameraPosition from the builder

        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    // when the camera starts moving.
    override fun onCameraMoveStarted(p0: Int) {
        Log.d(TAG, "onCameraMoveStarted")
    }

    // while the camera is moving or the user is interacting with the touch screen.
    override fun onCameraMove() {
        Log.d(TAG, "onCameraMove")
    }

    // when the current camera movement has been interrupted.
    override fun onCameraMoveCanceled() {
        Log.d(TAG, "onCameraMoveCanceled")
    }

    // when the camera stops moving and the user has stopped interacting with the map.
    override fun onCameraIdle() {
        Log.d(TAG, "onCameraIdle")
    }

    override fun onMyLocationButtonClick(): Boolean {
        //changeCamera(lastLocation)
        return true
    }
}