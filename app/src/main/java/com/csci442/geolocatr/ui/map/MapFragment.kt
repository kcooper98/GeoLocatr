package com.csci442.geolocatr.ui.map

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.csci442.geolocatr.R
import com.csci442.geolocatr.api.OpenWeatherResponse
import com.csci442.geolocatr.api.WeatherFetchr
import com.csci442.geolocatr.data.Checkpoint
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_locatr.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.*
import kotlin.math.log

class MapFragment : SupportMapFragment() {
    companion object {
        const val REQUEST_LOC_ON = 0
        private const val REQUEST_LOC_PERMISSION = 1
        private const val PERIODIC_POLL_NAME = "unique_poll"
        const val ARGS_LATITUDE = "latitude_key"
        const val ARGS_LONGITUDE = "longitude_key"
        val UNKNOWN_POSITION = -999.0
    }

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var googleMap: GoogleMap
    private lateinit var lastLocation: Location
    private lateinit var workManager: WorkManager
    private lateinit var fab: FloatingActionButton
    private lateinit var checkpointViewModel: MapViewModel

    private var locationUpdateState = false

    private val logTag = "448.weathrTrackr"
    private val weatherLiveData: MutableLiveData<OpenWeatherResponse> = MutableLiveData()

    // Weather data passed as parameters, sue me
    private fun updateUI(temp: Double?, description: String?) {
        // make sure we have a map and a location
        if (!::googleMap.isInitialized || !::lastLocation.isInitialized) {
            return
        }

        // Get point for marker
        val myLocationPoint = LatLng(
            lastLocation.latitude,
            lastLocation.longitude
        )

        val date = Calendar.getInstance().time.toString()

        val toastMessage = "Lat/Lng: (${lastLocation.latitude}, ${lastLocation.longitude})\n" +
                "You were here: ${date}\nTemp: ${temp.toString()} (${description})"

        Toast.makeText(
            requireContext(),
            toastMessage,
            Toast.LENGTH_LONG
        ).show()

        // Store new marker in database
        val checkpoint = Checkpoint(
            lat = lastLocation.latitude,
            lon = lastLocation.longitude,
            address = getAddress(lastLocation),
            datetime = date,
            temp = temp,
            description = description
        )

        checkpointViewModel.addCheckpoint(checkpoint)

        // add the new marker
//        googleMap.addMarker(newMarker)
        // include all points that should be within the bounds of the zoom
        // convex hull
        val bounds = LatLngBounds.Builder()
            .include(myLocationPoint)
            .build()
        // add a margin
        val margin = resources.getDimensionPixelSize(R.dimen.map_inset_margin)
        // create a camera to smoothly move the map view
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin)
        // move our camera!
        googleMap.animateCamera(cameraUpdate)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag, "onCreate() called")

        if (savedInstanceState == null) {
            retainInstance = true
        }
        // Set up viewmodel
        val factory = MapViewModelFactory(requireContext())
        checkpointViewModel = ViewModelProvider(this, factory)
            .get(MapViewModel::class.java)

        // Set up work request
        workManager = WorkManager.getInstance(requireContext())

        // Configure locationRequest
        locationRequest = LocationRequest.create()
        locationRequest.apply {
            interval = 0
            numUpdates = 1
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireContext())
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                Log.d(logTag, "Got a location:${locationResult?.lastLocation}")
                if (locationResult != null) {
                    // Get location, request weather, and updateUI with weather data
                    lastLocation = locationResult.lastLocation
                    val weatherRequest =
                        WeatherFetchr().weatherApi.fetchWeather(
                            lastLocation.latitude,
                            lastLocation.longitude,
                            "4120291d548d88e3201b37be29a00791"
                        )
                    weatherRequest.enqueue(object : Callback<OpenWeatherResponse> {
                        override fun onFailure(call: Call<OpenWeatherResponse>, t: Throwable) {
                            Log.d(logTag, t.localizedMessage)
                        }

                        override fun onResponse(
                            call: Call<OpenWeatherResponse>,
                            response: Response<OpenWeatherResponse>
                        ) {
                            updateUI(
                                response.body()?.main?.temp,
                                response.body()?.weatherItems?.get(0)?.description
                            )
                        }
                    })
                }
            }
        }

        // Had to use different key because android?
        val latitude = arguments?.getDouble(
            "latitude",
            UNKNOWN_POSITION
        )
            ?: UNKNOWN_POSITION
        val longitude = arguments?.getDouble(
            "longitude",
            UNKNOWN_POSITION
        )
            ?: UNKNOWN_POSITION
        if (latitude != UNKNOWN_POSITION && longitude != UNKNOWN_POSITION) {
            lastLocation = Location("")
            lastLocation.longitude = longitude
            lastLocation.latitude = latitude
        }

        // Setup map
        getMapAsync { map ->
            googleMap = map

            map.setOnMarkerClickListener {
                // Stored UUID as tag on marker in construction of marker
                val id: UUID = it.tag as UUID
                Log.d(logTag, id.toString())

                checkpointViewModel.getCheckpoint(id).observe(
                    viewLifecycleOwner,
                    androidx.lifecycle.Observer {
                        if (it != null) {
                            // Create snackbar
                            val checkpoint = it
                            Snackbar.make(
                                requireView(),
                                "You were here: ${it.datetime}\nTemp: ${it.temp.toString()} (${it.description})",
                                Snackbar.LENGTH_LONG
                            ).setAction(R.string.delete, {
                                checkpointViewModel.deleteCheckpoint(checkpoint)
                            }).show()
                        }
                    }
                )
                true
            }
        }
    }

    override fun onCreateView(p0: LayoutInflater, p1: ViewGroup?, p2: Bundle?): View? {
        val mapView = super.onCreateView(p0, p1, p2)
        val view = p0.inflate(R.layout.fragment_locatr, p1, false)

        view.mapViewContainer.addView(mapView)

        fab = view.findViewById(R.id.fab)
        fab.setOnClickListener { checkPermissionAndGetLocation() }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }
        if (requestCode == REQUEST_LOC_ON) {
            locationUpdateState = true
            //requireActivity().invalidateOptionsMenu()
        }
    }

    private fun checkPermissionAndGetLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PERMISSION_GRANTED
        ) {
            // permission not granted check if we should ask
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_toast),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // ask for permission
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_LOC_PERMISSION
                )
            }
        } else {
            // permission granted
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        }
    }

    private fun getAddress(location: Location): String {
        val geocoder = Geocoder(requireActivity())
        val addressTextBuilder = StringBuilder()

        try {
            val addresses = geocoder.getFromLocation(
                location.latitude,
                location.longitude,
                1
            )
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                for (i in 0..address.maxAddressLineIndex) {
                    if (i > 0) {
                        addressTextBuilder.append("\n")
                    }
                    addressTextBuilder.append(address.getAddressLine(i))
                }
            }
        } catch (e: IOException) {
            Log.e(logTag, e.localizedMessage!!)
        }
        return addressTextBuilder.toString()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOC_PERMISSION) {
            if (!permissions.isEmpty()
                and (permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION)
                and !grantResults.isEmpty()
                and (grantResults[0] == PERMISSION_GRANTED)
            ) {
                checkPermissionAndGetLocation()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun checkIfLocationCanBeRetrieved() {
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener {
            locationUpdateState = true
        }
        task.addOnFailureListener { exc ->
            locationUpdateState = false
            if (exc is ResolvableApiException) {
                try {
                    exc.startResolutionForResult(
                        requireActivity(),
                        REQUEST_LOC_ON
                    )
                } catch (e: IntentSender.SendIntentException) {
                    // do nothing, they cancelled so ignore error
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag, "onStart() called")

        checkIfLocationCanBeRetrieved()
    }

    override fun onResume() {
        super.onResume()

        checkpointViewModel.checkpointLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { checkpoints ->
                Log.i(logTag, "Got checkpoints ${checkpoints.size}")
                if (::googleMap.isInitialized) {
                    googleMap.clear()

                    for (checkpoint in checkpoints) {
                        // For each checkpoint, create and add marker
                        val myLocationPoint = LatLng(
                            checkpoint.lat,
                            checkpoint.lon
                        )

                        val marker = MarkerOptions()
                            .position(myLocationPoint)
                            .title(checkpoint.address)

                        googleMap.addMarker(marker).tag = checkpoint.id
                    }
                }
            }
        )
    }
}