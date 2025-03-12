package com.example.comunicationwearmobile.ui.view.activities

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng


class MapsElderlyTrackActivity : BaseMapActivity(){
    private var viewmodelLoaction: ViewmodelLocation?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
    }

    override fun initializeViewModel() {
        super.initializeViewModel()

        viewmodelLoaction= ViewmodelLocation(application)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        configOberserverLivedata()
    }

    override fun configOberserverLivedata() {
        super.configOberserverLivedata()

        configObserverLocation()
    }

     private fun configObserverLocation(){
        viewmodelLoaction?.locationLiveData?.observe(this){ location->
            updateMapLoaction(location)
        }
        viewmodelLoaction?.startTracking()

    }

    private fun updateMapLoaction(location: Location?){

        val newLatLng = LatLng(location?.latitude ?: 0.0, location?.longitude ?:0.0 )
        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng,Definition.ZOOM_MAP ))
    }

    override fun onCircleClick(circle: Circle) {
        super.onCircleClick(circle)

        showPropertiesAreaGeof(circle.tag.toString().toLong())
        Log.d(Definition.TAG_DEBUG,"Circulo id:${circle.tag}")
        Toast.makeText(this,"Id Circulo${circle.tag}",Toast.LENGTH_SHORT).show()
    }

    private fun showPropertiesAreaGeof(idArea: Long) {
        val intent= Intent(this, PropertiesGeofenceActivity::class.java)
        startActivity(intent)
    }

    private fun showConfigGeofenceFragment(latLng: LatLng) {
        val fragmentManager = supportFragmentManager
        val configGeofenceFragment = ConfigGeofenceFragment()
        val ft = fragmentManager.beginTransaction()

        //muestro el fragment de configuracion
        configGeofenceFragment.show(ft,"configGeofenceFragment")
    }

    override fun onDestroy() {
        super.onDestroy()

        viewmodelLoaction?.stopTracking()
        viewmodelLoaction?.locationLiveData?.removeObservers(this)
        viewmodelLoaction=null

        //Esto se debe realizar en la clase hija, no en la clase padre
        viewmodelMapsActivity=null
        mMap=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy MapsActivity")
    }
}

