package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment
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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : FragmentActivity() , OnMapReadyCallback , OnMapLongClickListener ,
    OnMapClickListener , LocationListener  {

    private var mMap: GoogleMap ?=null
    private var circle: Circle ?= null

    private var frag: ConfigGeofenceFragment? = null
    private var viewmodelMapsActivity:ViewmodelMapsActivity?=null

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
        viewmodelMapsActivity?.showMessage?.observe(this) { message ->
              Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        }
    }

    private fun configInsets() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun initializeViewModel() {
        //guardo el viewmodel para poder usarlo en el fragment y en propertiesGeofencesActivt
        viewmodelMapsActivity = ViewModelProvider(this, Definition.factory)[ViewmodelMapsActivity::class.java]
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(this , resultCode , RC_HANDLE_GMS)
            dlg?.show()
        }

        mMap?.setOnMapLongClickListener(this)
        mMap?.setOnMapClickListener(this)

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
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true
    }

    override fun onMapClick(latLng: LatLng) {
        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude}")

        addMarkerGeofence(latLng)
        showConfigGeofenceFragment()

    }

    override fun onMapLongClick(latLng: LatLng) {
        Log.d(Definition.TAG_DEBUG,"LocacionLat:${latLng.latitude} Longitude${latLng.longitude}")

        viewmodelMapsActivity?.determineWithinAnyCircle(latLng)
       }

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }

    fun positionUpdate(location: Location): LatLng {
        val zoomLevel = 16.0f //This goes up to 21
        val latLng = LatLng(location.latitude , location.longitude)

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , zoomLevel))

        return latLng
    }


    private fun showConfigGeofenceFragment() {
        frag = ConfigGeofenceFragment().apply {
            onDismissCallback = {freeFragment() }
        }
        // Muestra el fragmento
        frag?.show(supportFragmentManager, ConfigGeofenceFragment::class.java.simpleName)
    }

    private fun freeFragment() {
        // Limpia el fragmento y el callback
        frag?.let {
            it.dismiss()  // Elimina el fragmento del FragmentManager
            frag = null   // Libera la referencia al fragmento
            Log.d(Definition.TAG_DEBUG, "Se libera frag")
        }
    }


    private fun addMarkerGeofence(latLng: LatLng ) {
       val marker = addMarker(latLng)
       val circle = addCircle(latLng)

       viewmodelMapsActivity?.loadAreaTemporary(latLng,marker,circle)
    }

    private fun addMarker(latLng: LatLng): Marker? {
       val geoFenceMarker = MarkerOptions().position(latLng)
       val marker=mMap?.addMarker(geoFenceMarker)

       return marker
    }

    private fun addCircle(latLng: LatLng? , radius: Int=Definition.GEOFENCE_RADIUS_DEFAULT): Circle? {
        val alpha = 64

        val colorCircle = Color.BLUE

        this.circle = mMap?.addCircle(
            CircleOptions()
                .center(latLng!!)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle , alpha))
                .radius(radius.toDouble())
                .strokeWidth(4f)
        )

        return circle
    }

    fun updateCircleColorGraphic(colorCircle:Int) {
        val alpha = 64

        circle?.strokeColor = colorCircle

    }

    override fun onDestroy() {
        super.onDestroy()

        // Liberar listeners del mapa
        mMap?.setOnMapLongClickListener(null)
        mMap?.setOnMapClickListener(null)

        // Eliminar el fragmento si está presente
        frag?.dismissAllowingStateLoss()
        frag = null

        // Eliminar los observadores de LiveData
        viewmodelMapsActivity?.showMessage?.removeObservers(this)

        // Limpiar referencias del círculo
        circle?.remove() // Esto elimina el círculo del mapa
        circle=null
        
        mMap=null
        
        // Limpiar referencias del ViewModel si es necesario
        viewmodelMapsActivity?.onDestroyed() // Personalizado si existe en tu implementación
        viewmodelMapsActivity=null
    }


}

