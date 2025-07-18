package com.example.comunicationwearmobile.ui.view.activities.common

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Helpers.DrawAreaGeofHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng

//Esta es la clase padre que se usa para crear todos los mapas
abstract class BaseMapActivity : AppCompatActivity() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, GoogleMap.OnCircleClickListener {


    var mMap: GoogleMap? = null
    private val RC_HANDLE_GMS = 9001

    var drawAreaGeofHelper: DrawAreaGeofHelper?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_base)

        Tools.desactiveStrictMode()

        initGooglePlayServices()
        initMap()

        //instancio la clase para usar el helper que dibuja en el mapa
        drawAreaGeofHelper= DrawAreaGeofHelper(mMap)

    }

     private fun initGooglePlayServices(){
         val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

         if (resultCode != ConnectionResult.SUCCESS) {
             val dlg = resultCode.let {
                 GoogleApiAvailability.getInstance().getErrorDialog(this, it, RC_HANDLE_GMS)
             }
             dlg?.show()
             Log.e("MAP_DEBUG", "Google Play Services no está disponible. Código: $resultCode")

         }

     }
    private fun initMap() {
        try {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            if (mapFragment == null) {
                Log.e(Definition.TAG_DEBUG, "No se pudo encontrar el fragmento del mapa.")
                return
            }

            mapFragment.getMapAsync(this@BaseMapActivity)

        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al inicializar Google Maps: ${e.message}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawAreaGeofHelper = DrawAreaGeofHelper(mMap)
        mMap?.setOnCircleClickListener(this)

        enableFeatureMaps()
    }


    private fun enableFeatureMaps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this,"No hay permisos de ACCESS_FINE_LOCATION oACCESS_COARSE_LOCATION ",Toast.LENGTH_LONG).show()
            return
        }
        mMap?.isMyLocationEnabled = true




        mMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true
    }



    override fun onMapClick(latLng: LatLng) {

    }

    override fun onMapLongClick(latLng: LatLng) {

    }


    override fun onCircleClick(circle: Circle) {

    }




    open fun cleanMap(){
        mMap?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Limpiar referencias del círculo
        mMap?.setOnCircleClickListener(null)
        mMap=null


    }


}