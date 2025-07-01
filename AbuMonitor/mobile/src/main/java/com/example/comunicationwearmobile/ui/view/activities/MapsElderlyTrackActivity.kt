package com.example.comunicationwearmobile.ui.view.activities

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.data.model.JoinAreaGeofence
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng

//Esta es la clase hija que hereda de BaseMapActivity
//Esta clase hija define metodos que solo deben usarse en este mapa.
//Por ejemplo updateMapLocation: que actualiza la camara del mapa a la ubicacion actual

class MapsElderlyTrackActivity : BaseMapActivity(){
    private var viewmodelLoaction: ViewmodelLocation?=null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
        configActivityResult()
    }



        override fun initializeViewModel() {
        super.initializeViewModel()

        viewmodelLoaction= ViewmodelLocation(application)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        configOberserverLivedata()
    }

    fun configOberserverLivedata() {
        configObserverLocation()
        configObserverClickInMap()
    }

    private fun configObserverClickInMap() {
        // Observamos una sola vez
        viewmodelMapsActivity?.areaGeofenceForId?.observe(this) { dato ->
            dato?.let {

                with(dato.areaGeofence) {
                    val intent = Intent(applicationContext, PropertiesGeofenceActivity::class.java)
                    intent.putExtra(Definition.INTENT_DATA_NEW_AREA_GEOF, it)
                    startActivity(intent)

                    Log.d(Definition.TAG_DEBUG, "Area recibida: ${latitude}, ${longitude}")
                }
            }
        }
    }

    private fun configObserverLocation(){
        viewmodelLoaction?.locationLiveData?.observe(this){ location->
            updateMapLoaction(location)
            //Log.d(Definition.TAG_DEBUG,"Nueva ubicacion in MapsEld: ${location.latitude}, ${location.longitude}")
        }

    }

    private fun updateMapLoaction(location: Location?){

        val newLatLng = LatLng(location?.latitude ?: 0.0, location?.longitude ?:0.0 )
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,Definition.ZOOM_MAP ))
    }

    override fun onCircleClick(circle: Circle) {
        super.onCircleClick(circle)

        viewmodelMapsActivity?.getAreaGeofWithId(circle.tag.toString().toLong())

        Toast.makeText(this, "Cargando información del área...", Toast.LENGTH_SHORT).show()
    }

    private fun showPropertiesAreaGeof(areaGeofence: JoinAreaGeofence?) {
        val intent= Intent(this, PropertiesGeofenceActivity::class.java)
        activityResultLauncher?.launch(intent)

    }

    private fun configActivityResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(Definition.TAG_DEBUG,"Result: ${result.resultCode}")
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        viewmodelLoaction?.locationLiveData?.removeObservers(this)
        viewmodelLoaction?.onDestroyed()
        viewmodelLoaction=null

        //Esto se debe realizar en la clase hija, no en la clase padre
        viewmodelMapsActivity?.areaGeofenceForId?.removeObservers(this)
        viewmodelMapsActivity=null
        mMap=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy MapsElderlyAcivity")
    }
}

