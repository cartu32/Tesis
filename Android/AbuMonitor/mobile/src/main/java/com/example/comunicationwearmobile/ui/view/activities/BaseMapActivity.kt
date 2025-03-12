package com.example.comunicationwearmobile.ui.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng


abstract class BaseMapActivity : AppCompatActivity() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, GoogleMap.OnCircleClickListener {


    var mMap: GoogleMap? = null
    var circle: Circle? = null
    val RC_HANDLE_GMS = 9001

    var viewmodelMapsActivity: ViewmodelMapsActivity?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        initializeViewModel()

    }


    open fun initializeViewModel() {
        // Obtén una instancia del ViewModel usando el GenericViewModelFactory
        val factory = GenericViewModelFactory {
            ViewmodelMapsActivity(application)
        }

        viewmodelMapsActivity = ViewModelProvider(this, factory)[ViewmodelMapsActivity::class.java]

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            val dlg = resultCode.let {
                GoogleApiAvailability.getInstance().getErrorDialog(this, it, RC_HANDLE_GMS)
            }
            dlg?.show()
        }

        mMap?.setOnCircleClickListener(this)

        enableFeatureMaps()
        configOberserverLivedata()
    }


    @SuppressLint("MissingPermission")
    private fun enableFeatureMaps() {
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true
    }


    open fun configOberserverLivedata() {
        configObserverShowMessage()
        configObserverGetAllAreasGeofence()
    }

    private fun configObserverGetAllAreasGeofence() {

        viewmodelMapsActivity?.allAreas?.observe(this){listAllAreas->
            cleanMap()
            listAllAreas.forEach{
                Log.d(Definition.TAG_DEBUG,"Id:{${it.id_area} Description{${it.description}}")
                circle=drawGeofenceArea(it.latitude.toDouble(),it.longitude.toDouble(),it.meters.toDouble())
                setIdDrawnArea(it.id_area)
            }
        }
    }

    private fun configObserverShowMessage() {
        viewmodelMapsActivity?.showMessage?.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapClick(latLng: LatLng) {

    }

    override fun onMapLongClick(latLng: LatLng) {

    }


    override fun onCircleClick(circle: Circle) {

    }


    open fun drawGeofenceArea(latitude: Double, longitude: Double, meters: Double=Definition.GEOFENCE_RADIUS_DEFAULT): Circle? {
        val latLng=LatLng(latitude,longitude)
        var newCicle:Circle?=null

        val circleOptions=viewmodelMapsActivity?.createCircle(latLng,meters)

        circleOptions?.let {
            newCicle=mMap?.addCircle(it)
        }
        return newCicle
    }


    open fun setIdDrawnArea(idCircle: Long) {
        circle?.let {
            circle?.tag = idCircle
            viewmodelMapsActivity?.addCircleInList(it)
        }
    }

    open fun cleanMap(){
        mMap?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        viewmodelMapsActivity?.allAreas?.removeObservers(this)
        viewmodelMapsActivity?.showMessage?.removeObservers(this)

        // Limpiar referencias del ViewModel si es necesario
        viewmodelMapsActivity?.onDestroyed()

        // Limpiar referencias del círculo
        circle?.remove() // Esto elimina el círculo del mapa
        mMap?.setOnCircleClickListener(null)

        circle=null

    }


}