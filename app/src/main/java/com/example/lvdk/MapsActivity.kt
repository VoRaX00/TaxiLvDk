package com.example.lvdk

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.ContextCompat

import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.lvdk.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private lateinit var binding: ActivityMapsBinding
    private var locationPermission : Boolean = false
    private var lastKnownLocation : Location? = null
    private lateinit var mDataBase : DatabaseReference
    private var userKey : String = "User"


    private val defaultLocation = LatLng(43.11822917387299, 131.88625569109405)
    //private var distance : Int = 0

    private var cameraPosition : CameraPosition? = null

    private lateinit var placeClient : PlacesClient

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private lateinit var mCreatZakaz : Button
    private lateinit var mFromTo : EditText
    private lateinit var mWhere : EditText

    companion object {
        private const val PERMISSION_ON_LOCATION_CODE = 1
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        private const val DEFAULT_ZOOM = 15
        private val TAG = MapsActivity::class.java.simpleName
        private const val MAPS_API_KEY = "AIzaSyBI1q1y5JqH4-BBZn4pluFjk2mIiquqnqY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_maps)
       // init()
        /*if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }*/

        setContentView(R.layout.activity_maps)
        //binding = ActivityMapsBinding.inflate(layoutInflater)
        //setContentView(binding.root)
       // Places.initialize(applicationContext, MAPS_API_KEY)
       // placeClient = Places.createClient(this)
        init()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        //mMap.addMarker(MarkerOptions().position(vladivostok).title("Vladivostok"))
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))

        getLocalPermission()
        updateLocationUI()
        getDeviceLocation()

        /*mMap.setOnMarkerClickListener {marker ->
            binding.deleteMarkers.visibility = View.VISIBLE
            binding.remmarkerimage.setOnClickListener{
                marker.remove()
                binding.deleteMarkers.visibility = View.GONE
            }
            true
        }*/
    }

    private fun init()
    {
        mDataBase = FirebaseDatabase.getInstance().getReference(userKey)
        mFromTo = findViewById(R.id.fromToEditText)
        mWhere = findViewById(R.id.whereEditText)
        mCreatZakaz = findViewById(R.id.createAnOrder)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        mMap?.let { map->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    private fun getLocalPermission()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            locationPermission = true
        }
        else
        {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_ON_LOCATION_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationPermission = false
        when(requestCode)
        {
            PERMISSION_ON_LOCATION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    locationPermission = true
                }
            }
            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        updateLocationUI()
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI()
    {
        if(mMap == null)
        {
            return
        }
        try{
            if(locationPermission)
            {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            }
            else
            {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocalPermission()
            }
        }
        catch (e : SecurityException)
        {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun getDeviceLocation()
    {
        try
        {
            if(locationPermission)
            {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this){ task ->
                    if(task.isSuccessful)
                    {
                        lastKnownLocation = task.result
                        if(lastKnownLocation != null)
                        {
                            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation!!.latitude, lastKnownLocation!!.longitude), DEFAULT_ZOOM.toFloat()
                            ))
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat()))
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        }
        catch (e : SecurityException){
            Log.e("Exception: %s", e.message, e)
        }
    }

    fun MakeInOrder(view: View)
    {
        val where = mWhere.text.toString()
        val fromTo = mFromTo.text.toString()
        if(where.isNotEmpty() && fromTo.isNotEmpty())
        {
            //Добавление информации о заказе в БД (Не тестировано, нужно проверить, и добавить информацию о пользователе, лучше сделать уникальное id)
            val order  = Order(fromTo, where)
            mDataBase.push().setValue(order)
            Toast.makeText(this,"Заказ сформирован", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        return
    }
}