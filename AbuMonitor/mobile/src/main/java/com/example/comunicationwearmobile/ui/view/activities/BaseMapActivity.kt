package com.example.comunicationwearmobile.ui.view.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Tools
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Esta es la clase padre que se usa para crear los mapas con las areas de geofencing
//De esta clase heredan las demas.Por ejemplo:
//-MapDefineAreasActvity
//-MapsElderlyTrackActivity

abstract class BaseMapActivity : AppCompatActivity() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, GoogleMap.OnCircleClickListener {


    var mMap: GoogleMap? = null
    var circle: Circle? = null
    private val RC_HANDLE_GMS = 9001

    var viewmodelMapsActivity: ViewmodelMapsActivity?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base_map)

        Tools.desactiveStrictMode()

        initGooglePlayServices()
        initMap()

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
     private fun initMap(){
        try{

            lifecycleScope.launch(Dispatchers.IO) {

                val mapFragment =
                    supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                withContext(Dispatchers.Main) {
                    mapFragment.getMapAsync(this@BaseMapActivity)
                }
                initializeViewModel()
            }
        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "\"Error al inicializar Google Maps: ${e.message}\"")
        }
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

        mMap?.setOnCircleClickListener(this)

        enableFeatureMaps()
        configOberserverLivedata()
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