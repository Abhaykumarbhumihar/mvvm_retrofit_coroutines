package com.example.mvvm_api_implementation.nearbypalace.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mvvm_api_implementation.databinding.ActivityMainBinding
import com.example.mvvm_api_implementation.nearbypalace.model.PlacePojo
import com.example.mvvm_api_implementation.nearbypalace.repository.SearchNearByRepository
import com.example.mvvm_api_implementation.nearbypalace.view.adpter.PlaceAdapter
import com.example.mvvm_api_implementation.nearbypalace.viewmodel.SearchNearByViewModel
import com.example.mvvm_api_implementation.nearbypalace.viewmodel.SearchNearByViewModelProviders
import com.example.mvvm_api_implementation.network.AppConstants
import com.example.mvvm_api_implementation.network.Coroutines
import com.example.mvvm_api_implementation.network.ResponseListener
import com.example.mvvm_api_implementation.network.WebServices
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.gson.Gson

class MainActivity : AppCompatActivity(), ResponseListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: SearchNearByViewModel
    private lateinit var repository: SearchNearByRepository
    private lateinit var placeAdapter: PlaceAdapter
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var latitude = 0.0
    private var longitude = 0.0
    private var locationString = ""
    private val arrayList = ArrayList<PlacePojo.Result>()

    // Permissions to be requested
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    private val PERMISSION_REQUEST_CODE = 123
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        if (checkAndRequestPermissions()) {
            initializeLocationClient()
            initUI()
        }
    }

    private fun setupViewModel() {
        val webServices = WebServices()
        repository = SearchNearByRepository(webServices)
        val viewModelProviders = SearchNearByViewModelProviders(repository)
        viewModel = ViewModelProvider(this, viewModelProviders)[SearchNearByViewModel::class.java]
        viewModel.responseListener = this
    }

    private fun initUI() {
        if (isGooglePlayServicesAvailable()) {
            startLocationUpdates()
        }

        binding.searhplce.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length >= 3) {
                    performLocationSearch(s.toString())
                }
            }
        })

        // Setup the place click listener
        placeAdapter = PlaceAdapter(this, arrayList, placeLister = object :
            PlaceAdapter.PlaceLister {
            override fun click(position: Int) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun performLocationSearch(query: String) {
        val queryParams = hashMapOf<String, Any>(
            "type" to "restaurant",
            "location" to locationString,
            "name" to query,
            "opennow" to true,
            "rankby" to "distance",
            "key" to AppConstants.MAP_KEY
        )
        viewModel.searchNearyByLocation(queryParams, AppConstants.NEARBYURL)
    }

    private fun checkAndRequestPermissions(): Boolean {
        val permissionsNeeded = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            initUI()  // Reinitialize UI components
        } else {
            showSettingsDialog()
        }
    }

    private fun initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.locations.forEach { location ->
                        latitude = location.latitude
                        longitude = location.longitude
                        locationString = "$latitude,$longitude"
                        Log.d("Location", "Lat: $latitude, Lng: $longitude")
                    }
                }
            }

            fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback!!, null)
        }
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
    }

    override fun onStart() {
        super.onStart()
        if (!checkAndRequestPermissions() && !isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Play Services not available", Toast.LENGTH_SHORT).show()
        } else {
            startLocationUpdates()
        }
    }

    private fun showSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Need Permissions")
            .setMessage("This app needs permission to use this feature. You can grant them in app settings.")
            .setPositiveButton("GOTO SETTINGS") { dialog, _ ->
                dialog.cancel()
                openSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivityForResult(intent, 101)
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS
    }

    override fun onSuccess(message: String, url: String) {
        Log.w("onSuccess $url", message)
        runOnUiThread {
            arrayList.clear()
            val pojo = Gson().fromJson(message, PlacePojo::class.java)
            arrayList.addAll(pojo?.results ?: emptyList())
            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        binding.rvPlacelist.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rvPlacelist.setHasFixedSize(true)
        binding.rvPlacelist.adapter = placeAdapter
    }

    override fun onFailure(message: String, url: String) {
        // Handle failure (e.g., show a Toast or log the error)
    }
}
