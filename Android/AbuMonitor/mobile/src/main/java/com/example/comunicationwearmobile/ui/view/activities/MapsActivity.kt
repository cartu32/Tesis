package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.constants.Definition.factory
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMainActivity
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : FragmentActivity() , OnMapReadyCallback , OnMapLongClickListener ,
    OnMapClickListener , LocationListener  {

    private lateinit var mMap: GoogleMap
    private lateinit var circle: Circle
    private lateinit var geoFenceMarker: MarkerOptions

    private lateinit var viewmodelMapsActivity: ViewmodelMapsActivity
    private lateinit var factory: ViewModelFactory

    private var frag: ConfigGeofenceFragment? = null


    companion object {

        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        configInsets()
        initializeViewModel()
        configOberserverLivedata()
    }

    private fun configOberserverLivedata() {
        viewmodelMapsActivity.updateCircleRadius.observe(this){radius->
            circle.radius= radius.toDouble()
            Log.d(Definition.TAG_DEBUG,"Circulo Radio${circle.radius}")
        }
    }

    private fun configInsets() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun initializeViewModel() {
        factory = Definition.factory
        viewmodelMapsActivity = ViewModelProvider(this, factory)[ViewmodelMapsActivity::class.java]
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this , resultCode , RC_HANDLE_GMS)
            dlg?.show()
        }

        mMap.setOnMapLongClickListener(this)
        mMap.setOnMapClickListener(this)

        enableFeatureMaps()
    }

    private fun enableFeatureMaps() {
        //En este metodo se habilitan los componentes del mapa. Por  ejemplo zoom, boton de ubicacion
        if (ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this,"No se han otorgados permisos de Ubicacion",Toast.LENGTH_SHORT).show()
            Log.e(Definition.TAG_DEBUG,"Error:Faltan permisos de FINE O COARSE LOCATION")
            return
        }
        mMap.isMyLocationEnabled = true
        mMap.uiSettings.setAllGesturesEnabled(true)
        mMap.uiSettings.isMyLocationButtonEnabled = true
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
    }

    override fun onMapLongClick(latLng: LatLng) {
        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude}")

        showConfigGeofenceFragment()
        addMarkerGeofence(latLng,Definition.GEOFENCE_RADIUS_DEFAULT)
    }

    override fun onMapClick(latLng: LatLng) {
        Log.d(Definition.TAG_DEBUG,"LocacionLat:${latLng.latitude} Longitude${latLng.longitude}")
        showConfigGeofenceFragment()
        addMarkerGeofence(latLng,Definition.GEOFENCE_RADIUS_DEFAULT)
    }

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }

    fun positionUpdate(location: Location): LatLng {
        val zoomLevel = 16.0f //This goes up to 21
        val latLng = LatLng(location.latitude , location.longitude)

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , zoomLevel))

        return latLng
    }


    fun showConfigGeofenceFragment(){
        frag = ConfigGeofenceFragment(Definition.GEOFENCE_RADIUS_DEFAULT, true)
        this.let { frag?.show(it.supportFragmentManager, ConfigGeofenceFragment::class.java.simpleName) }
    }

    private fun addMarkerGeofence(latLng: LatLng , radius: Float) {
        addMarker(latLng)
        addCircle(latLng , radius)
    }

    private fun addMarker(latLng: LatLng) {
        geoFenceMarker = MarkerOptions().position(latLng)
        mMap.addMarker(geoFenceMarker)
    }

    private fun addCircle(latLng: LatLng? , radius: Float) {
        val alpha = 64

        val colorCircle = viewmodelMapsActivity.determineColor()

        this.circle = mMap.addCircle(
            CircleOptions()
                .center(latLng!!)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle , alpha))
                .radius(radius.toDouble())
                .strokeWidth(4f)
        )
    }

    fun updateCircleRadiusGraphic(geofenceRadius: Float) {
        circle.radius = geofenceRadius.toDouble()
    }

    fun updateCircleColorGraphic(id: String?) {
        val alpha = 64

        val colorCircle = viewmodelMapsActivity.determineColor()

        circle.strokeColor = colorCircle
        circle.fillColor = ColorUtils.setAlphaComponent(colorCircle , alpha)
    }

}

