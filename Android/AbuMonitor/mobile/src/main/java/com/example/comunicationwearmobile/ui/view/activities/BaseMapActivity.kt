package com.example.comunicationwearmobile.ui.view.activities

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng


abstract class BaseMapActivity : AppCompatActivity() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, GoogleMap.OnCircleClickListener {

    private val RC_HANDLE_GMS = 9001

    var mMap: GoogleMap? = null
    var circle: Circle? = null

    val circlesMap = mutableMapOf<String?, Circle?>()

    var tempIdSelected:String?=null


    open override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    open override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            val dlg = resultCode.let {
                GoogleApiAvailability.getInstance().getErrorDialog(this, it, RC_HANDLE_GMS)
            }
            dlg?.show()
        }

        mMap?.setOnCircleClickListener(this)
        mMap?.setOnMapClickListener (this)
        enableFeatureMaps()
    }

    @SuppressLint("MissingPermission")
    private fun enableFeatureMaps() {
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true
    }


    open override fun onMapClick(latLng: LatLng) {

    }

    open override fun onMapLongClick(latLng: LatLng) {

    }


    open override fun onCircleClick(circle: Circle) {
        tempIdSelected=circle.tag.toString()

    }


    open fun cleanMap(){
        mMap?.clear()
    }

    open fun drawGeofenceArea(latitude: Double, longitude: Double, meters: Double=Definition.GEOFENCE_RADIUS_DEFAULT): Circle? {
        val latLng=LatLng(latitude,longitude)

        val circle = addCircle(latLng,meters)


        return circle
    }


    open fun addCircle(latLng: LatLng, radius: Double): Circle? {
        val alpha = 64
        val colorCircle = Color.BLUE
        this.circle = mMap?.addCircle(
            CircleOptions()
                .center(latLng)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle, alpha))
                .radius(radius.toDouble())
                .strokeWidth(4f)
                .clickable(true)
        )
        return circle
    }



    open fun setIdDrawnArea(id:Long){
        val tag:String

        tag=id.toString()
        circle?.tag= tag
        circlesMap[tag]=circle
    }




    fun removeAllCircle(){
        circlesMap.values.forEach{it?.remove()}
        circlesMap.clear()

        // Limpiar referencias del círculo
        circle?.remove() // Esto elimina el círculo del mapa
        circle=null

    }

    override fun onDestroy() {
        super.onDestroy()

        //libero todos los circulos
        removeAllCircle()

        mMap?.setOnMapLongClickListener(null)
        mMap?.setOnMapClickListener(null)
        mMap?.setOnCircleClickListener(null)
        mMap=null

    }

    //***********************



}