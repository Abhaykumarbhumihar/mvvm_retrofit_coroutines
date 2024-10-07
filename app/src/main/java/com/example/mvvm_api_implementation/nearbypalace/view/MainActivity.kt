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
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson


class MainActivity : AppCompatActivity(), ResponseListener {

    private lateinit var viewModel: SearchNearByViewModel
    private lateinit var repository: SearchNearByRepository
    private lateinit var viewModelProviders: SearchNearByViewModelProviders
    private lateinit var webServices: WebServices
    private lateinit var binding: ActivityMainBinding

    private var fusedLocationClient: FusedLocationProviderClient? = null

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var locationCallback: LocationCallback? = null

    private var latitude = 0.0
    private var longitude = 0.0
    private var locationString = ""
    var arrayList = ArrayList<PlacePojo.Result>()
    lateinit var placeLister: PlaceAdapter.PlaceLister
    lateinit var placeAdapter: PlaceAdapter

    // Permissions to be requested
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_NETWORK_STATE
    )

    private val PERMISSION_REQUEST_CODE = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webServices = WebServices()
        repository = SearchNearByRepository(webServices)
        viewModelProviders = SearchNearByViewModelProviders(repository)
        viewModel = ViewModelProvider(this, viewModelProviders)[SearchNearByViewModel::class.java]
        viewModel.responseListener = this

        if (checkAndRequestPermissions()) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            init()
        }
    }

    private fun init() {
        if (isGooglePlayServicesAvailable()) {
            startLocationUpdates()
        }
        placeLister = object : PlaceAdapter.PlaceLister {
            override fun click(position: Int) {
                val intent = Intent()
                intent.putExtra("address", arrayList[position].vicinity)

            }
        }
        binding.searhplce.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                Log.w("TextChanged", "afterTextChanged")
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.w("TextChanged", "beforeTextChanged")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.w("TextChanged", "onTextChanged")

                if (p0.toString().length >= 3) {
                    val queryParams = hashMapOf<String, Any>(
                        "type" to "restaurant",
                        "location" to locationString,
                        "name" to p0.toString(),
                        "opennow" to true,
                        "rankby" to "distance",
                        "key" to AppConstants.MAP_KEY // Put your actual API key here
                    )
                    viewModel.searchNearyByLocation(queryParams, AppConstants.NEARBYURL)
                }
            }
        })
    }

    // Check and request permissions
    private fun checkAndRequestPermissions(): Boolean {
        val listPermissionsNeeded = ArrayList<String>()

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(permission)
            }
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                val perms = HashMap<String, Int>()

                // Fill with results
                for (i in permissions.indices) {
                    perms[permissions[i]] = grantResults[i]
                }

                // Check if all permissions are granted
                if (perms.values.all { it == PackageManager.PERMISSION_GRANTED }) {
                    init()  // If permissions are granted, continue with initialization
                } else {
                    showSettingsDialog()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 10000 // 10 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("Location", "Lat: ${location.latitude}, Lng: ${location.longitude}")
                    latitude = location.latitude
                    longitude = location.longitude
                    locationString = "$latitude,$longitude"

                }
            }
        }

        fusedLocationClient?.requestLocationUpdates(locationRequest, locationCallback!!, null)
    }

    override fun onPause() {
        super.onPause()
        fusedLocationClient?.removeLocationUpdates(locationCallback!!)
    }

    override fun onStart() {
        super.onStart()
        checkAndRequestPermissions()
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(this, "Play Services not available", Toast.LENGTH_SHORT).show()
        } else {
            startLocationUpdates()
        }
    }

    private fun showSettingsDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Need Permissions")
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.")
        builder.setPositiveButton("GOTO SETTINGS") { dialog, _ ->
            dialog.cancel()
            openSettings()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    // Navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivityForResult(intent, 101)
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return status == ConnectionResult.SUCCESS
    }

    override fun onSuccess(message: String, url: String) {
        Log.w("onSuccess $url", message)
      runOnUiThread{
          arrayList.clear()
          val pojo = Gson().fromJson(message, PlacePojo::class.java)
          arrayList.addAll(pojo?.results!!)
          binding.rvPlacelist.layoutManager =
              LinearLayoutManager(this@MainActivity, RecyclerView.VERTICAL, false)
          binding.rvPlacelist.setHasFixedSize(true)
          placeAdapter = PlaceAdapter(this@MainActivity, arrayList, placeLister)
          binding.rvPlacelist.adapter = placeAdapter
      }
    }

    override fun onFailure(message: String, url: String) {
        // Handle failure
    }
}
