package com.example.stalkr.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.stalkr.AuthUserObject
import com.example.stalkr.CustomInfoWindowForGoogleMap
import com.example.stalkr.R
import com.example.stalkr.activities.MainActivity
import com.example.stalkr.data.UserProfileData

import com.example.stalkr.databinding.FragmentMapBinding
import com.example.stalkr.services.LocationService
import com.example.stalkr.services.NotificationManager
import com.example.stalkr.services.CompassService
import com.example.stalkr.utils.MapUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.MapView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import android.content.Intent

import android.content.BroadcastReceiver
import android.content.Context.LOCATION_SERVICE
import com.example.stalkr.utils.ImageUtils
import org.jetbrains.anko.doAsync


class MapFragment : Fragment(),
    OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnInfoWindowClickListener,
    GoogleMap.OnCameraMoveStartedListener,
    GoogleMap.OnCameraMoveListener,
    GoogleMap.OnCameraMoveCanceledListener,
    GoogleMap.OnCameraIdleListener {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    // AUTH + DB
    private val userCollectionRef = FirebaseFirestore.getInstance().collection("users")
    private val storageRef = FirebaseStorage.getInstance().reference

    // temp - for debug
    private var uid: String = Firebase.auth.currentUser?.uid.toString()

    //private val currentUser get() = AuthActivity.userData
    // MAP
    private var mapView: MapView? = null
    private lateinit var mMap: GoogleMap

    // LOCATION
    private var currentLocation: Location? = null
    private var userPositionViewport: LatLngBounds =
        LatLngBounds(LatLng(0.0, 0.0), LatLng(0.0, 0.0))
    private var changeBounds: Boolean = true

    private lateinit var locationService: LocationService
    private var bound: Boolean = false
    private var locationInitiated: Boolean = false

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as LocationService.LocalBinder
            locationService = binder.getService()
            bound = true

            if (!locationInitiated) {
                initLocationService()
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
            locationInitiated = false
        }
    }

    // Google Markers
    private var userLocationMarker: Marker? = null
    private var otherUserProfileLocationMarkers: HashMap<UserProfileData, Marker>? = null

    private val othersInBoundsList: ArrayList<String> = arrayListOf()
    private lateinit var notificationManager: NotificationManager

    // Filter
    private var isFilteredByRadius = false

    private lateinit var broadcastReceiver: BroadcastReceiver

    companion object {
        const val LOCATION_REQUEST_CODE = 100
        private const val REQUEST_CHECK_SETTINGS = 2
        const val PICK_IMAGE_REQUEST = 22
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        // My location click handler
        binding.btnMyLocation.setOnClickListener {
            moveCamera(currentLocation)
        }

        // Picture upload click handler
//        binding.btnAddPicture.setOnClickListener {
//            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(
//                Intent.createChooser(
//                    intent,
//                    "Select Image from here..."
//                ),
//                PICK_IMAGE_REQUEST
//            )
//        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = _binding?.mapView
        mapView?.onCreate(savedInstanceState)

        val textViewDirection = view.findViewById<TextView>(R.id.textViewDirection)
        val imageViewCompass = view.findViewById<ImageView>(R.id.imageViewCompass)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val direction = intent.getStringExtra(CompassService.KEY_DIRECTION)
                val angle = intent.getDoubleExtra(CompassService.KEY_ANGLE, 0.0)
                val angleWithDirection = "$angle  $direction"
                textViewDirection.text = angleWithDirection
                imageViewCompass.rotation = angle.toFloat() * -1
            }
        }

        LocalBroadcastManager.getInstance(this.requireContext()).registerReceiver(
            broadcastReceiver,
            IntentFilter(CompassService.KEY_ON_SENSOR_CHANGED_ACTION)
        )

        notificationManager = NotificationManager(requireContext())
    }

    fun startLocation() {
        doAsync {
            // Initialize location manager
            val locationManager =
                activity?.getSystemService(LOCATION_SERVICE) as LocationManager

            // First check for location permissions
            if (!isAllowedToUseGPS()) {
                // if permissions not granted, request them
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ), LOCATION_REQUEST_CODE
                )
            } else {
                // Check if gps or network provider is on
                // if true then start getting user location
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                ) {
                    // Start location service
                    boundLocationService()
                } else {
                    // Show GPS settings
                    showGpsSettings()
                }
            }
        }
    }

    private fun initLocationService() {
        // Setup Location Callback
        locationService.setupLocationService(requireContext(),
            object : LocationCallback() {
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    currentLocation = p0.lastLocation
                    setupLocationViewport()
                    //saveLocationToDb(currentLocation) // moved to AuthUserObject data class
                    AuthUserObject.updateUserLocationInDB(p0.lastLocation)
                    placeMarkerOnMap(p0.lastLocation)
                    retrieveOtherUsersLocationFromDB()
                }
            }
        )

        mapView?.getMapAsync(this)
        locationService.createLocationRequest() // will start locationListener too

        locationInitiated = true
    }

    private fun boundLocationService() {
        if (!bound) {
            Intent(requireContext(), LocationService::class.java).also { intent ->
                activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener(this)
        mMap.setOnCameraIdleListener(this)
        mMap.setOnCameraMoveStartedListener(this)
        mMap.setOnCameraMoveListener(this)
        mMap.setOnCameraMoveCanceledListener(this)

        // Set up the custom info window
        val customInfoWindow = CustomInfoWindowForGoogleMap(requireContext())
        mMap.setInfoWindowAdapter(customInfoWindow)
        mMap.setOnInfoWindowClickListener(this)

        setupMap()
    }

    private fun setupMap() {
        // Check permissions for fusedLocationClient listener
        if (isAllowedToUseGPS()) {
            mapView?.onResume()
            locationService.lastLocation { location ->
                placeMarkerOnMap(location)
                currentLocation = location
                moveCamera(currentLocation)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_sorting_actions, menu)
        val menuItem = menu.findItem(R.id.sortingSpinner)
        val spinner = menuItem?.actionView as Spinner
        val adapter = ArrayAdapter(
            this.requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.filter_names)
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0) {
                    isFilteredByRadius = false
                } else if (position == 1) {
                    isFilteredByRadius = true
                }
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    fun setupLocationViewport() {
        if (currentLocation != null) {
            val currentLocation: Location = this.currentLocation as Location
            if (changeBounds) {
                // if marker goes beyond the view bounds, center the camera on user
                val metersOffset = 30.0
                val latOffset: Double = MapUtils.metersToLat(metersOffset) // y
                val longOffset: Double =
                    MapUtils.metersToLong(metersOffset, currentLocation.latitude) // x

                userPositionViewport = LatLngBounds(
                    LatLng(
                        currentLocation.latitude - latOffset,
                        currentLocation.longitude - longOffset
                    ),  // SW corner
                    LatLng(
                        currentLocation.latitude + latOffset,
                        currentLocation.longitude + longOffset
                    ) // NE corner
                )
                changeBounds = false
            }

            val currentLatLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            if (!userPositionViewport.contains(currentLatLng)) {
                changeBounds = true
                moveCamera(currentLocation)
            }
        }
    }

    private fun isAllowedToUseGPS(): Boolean {
        // when location service is not enabled and permission is not denied then open location settings
        return (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                )
    }

    private fun showGpsSettings() {
        if (isAllowedToUseGPS()) {
            startActivity(
                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    /**
     *  @should Set the marker title as the user name
     *  @should Set the user name to empty if is null
     */
    private fun placeMarkerOnMap(location: Location) {
        val currentLatLng = LatLng(location.latitude, location.longitude)

        if (userLocationMarker == null) {
            //Create a new marker
            val markerOptions = MarkerOptions()
            markerOptions.position(currentLatLng)
            markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
            markerOptions.title(AuthUserObject.name)
            markerOptions.snippet(AuthUserObject.pfpURL)
            userLocationMarker = mMap.addMarker(markerOptions)
            userLocationMarker?.tag = markerOptions.title
        } else {
            //use the previously created marker
            userLocationMarker?.position = currentLatLng
        }
    }

    /**
     * @should Set the user profile photo to default if is empty or null
     */
    private fun placeOtherMarkerOnMap(latLng: LatLng, userProfile: UserProfileData) {
        if (otherUserProfileLocationMarkers != null) {
            // if the user already has a marker, just update position
            if (otherUserProfileLocationMarkers!!.any { it.key == userProfile }) {
                // TODO: old user marker stays where it is
                otherUserProfileLocationMarkers!![userProfile]!!.position = latLng
            } else {
                val markerOptions = MarkerOptions()
                markerOptions.position(latLng)
                markerOptions.anchor(0.5.toFloat(), 0.5.toFloat())
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                markerOptions.title(userProfile.name)
                markerOptions.snippet(userProfile.pfpURL)
                val otherUserLocationMarker: Marker? = mMap.addMarker(markerOptions)
                if (otherUserLocationMarker != null) {
                    otherUserProfileLocationMarkers?.putIfAbsent(
                        userProfile,
                        otherUserLocationMarker
                    )
                }
            }
        } else {
            otherUserProfileLocationMarkers = HashMap()
        }
    }

    /**
     * @should Place a marker for other user on the map
     */
    private fun retrieveOtherUsersLocationFromDB() {
        val userQuery = FirebaseFirestore.getInstance().collection("users")
            .whereNotEqualTo("uid", AuthUserObject.uid)
            .get()

        userQuery.addOnSuccessListener {
            for (document in it) {
                val latitude = document.get("latitude").toString().toDouble()
                val longitude = document.get("longitude").toString().toDouble()
                val latLng = LatLng(latitude, longitude)

                val otherUser = UserProfileData(document.get("uid").toString())
                // Make sure the user is updated from the DB before displaying info about them
                otherUser.UpdateUserProfileFromDB(document)
                placeOtherMarkerOnMap(latLng, otherUser)
                // Check if other user in 10 meters range

                // ! Now we get 2 notifications because the `startLocationUpdate()` is called 2 times.
                // TODO Call `startLocationUpdate()` once.

                // Shows notification when users are within the radius of 10m
                checkForStalkers(otherUser, latLng)

            }
            userQuery.addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
            // Filter user markers by radius of 1000m
            filterOtherMarkersByRadius(1000.0)
        }
        // Filter user markers by radius of 1000m (seems to be quicker?)
        filterOtherMarkersByRadius(1000.0)
    }

    override fun onMarkerClick(p0: Marker) = false

    /**
     * @should show notification if other users are within the radius of 10m
     */
    private fun checkForStalkers(otherUser: UserProfileData, latLng: LatLng) {
        val otherInBounds = isOtherUserInBound(50.0, latLng)
        val otherInOutsideBounds = isOtherUserInBound(80.0, latLng)
        val otherAlreadyInBounds = othersInBoundsList.contains(otherUser.uid)

        if (otherInBounds && !otherAlreadyInBounds) {
            val otherUserName = otherUser.name
            othersInBoundsList.add(otherUser.uid)
            notificationManager.show("You are being stalked", "$otherUserName is stalking you!")
        } else if (!otherInOutsideBounds && otherAlreadyInBounds) {
            othersInBoundsList.remove(otherUser.uid)
        }
    }

    /**
     * @should filter other user markers within a certain radius
     */
    private fun filterOtherMarkersByRadius(offsetInMeter: Double) {

        otherUserProfileLocationMarkers?.forEach { (_, marker) ->
            val latLng = LatLng(marker.position.latitude, marker.position.longitude)
            if (isFilteredByRadius) {
                marker.isVisible = isOtherUserInBound(offsetInMeter, latLng)
            } else {
                marker.isVisible = true
            }
        }
    }

    /**
     * @should check if other user is within a certain radius
     */
    private fun isOtherUserInBound(offsetInMeter: Double, otherUserLatLng: LatLng): Boolean {
        if (currentLocation != null) {
            val currentLocation: Location = this.currentLocation as Location
            val latOffset: Double = MapUtils.metersToLat(offsetInMeter)
            val longOffset: Double = MapUtils.metersToLong(offsetInMeter, currentLocation.latitude)
            val othersAroundBounds = LatLngBounds(
                LatLng(
                    currentLocation.latitude - latOffset,
                    currentLocation.longitude - longOffset
                ),  // SW corner
                LatLng(
                    currentLocation.latitude + latOffset,
                    currentLocation.longitude + longOffset
                ) // NE corner
            )
            return othersAroundBounds.contains(otherUserLatLng)
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Upload image
        ImageUtils.uploadImage(requireContext(), requestCode, resultCode, data, storageRef, uid, binding, userCollectionRef)
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onResume() {
        super.onResume()
        // Startup map
        mapView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            activity?.unbindService(connection)
            bound = false
            locationInitiated = false
        }
    }
    override fun onStart() {
        super.onStart()
        // Start getting current location
        startLocation()
    }

    /* CAMERA STUFF */

    private fun moveCamera(location: Location?) {
        if (location != null) {
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(location.latitude, location.longitude))
                .zoom(16f)            // Sets the zoom
                .build()                    // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    // when the camera starts moving.
    override fun onCameraMoveStarted(p0: Int) {}

    // while the camera is moving or the user is interacting with the touch screen.
    override fun onCameraMove() {}

    // when the current camera movement has been interrupted.
    override fun onCameraMoveCanceled() {}

    // when the camera stops moving and the user has stopped interacting with the map.
    override fun onCameraIdle() {}

    override fun onDestroyView() {
        LocalBroadcastManager.getInstance(this.requireContext())
            .unregisterReceiver(broadcastReceiver)
        super.onDestroyView()
        _binding = null
    }

    override fun onInfoWindowClick(p0: Marker) {
        for ((userData, marker) in otherUserProfileLocationMarkers!!) {
            if (marker == p0) {
                val action = MapFragmentDirections.actionMapFragmentToProfileFragment(userData.uid)
                MainActivity.navHostFragment!!.navController.navigate(action)
            }
        }
    }
}