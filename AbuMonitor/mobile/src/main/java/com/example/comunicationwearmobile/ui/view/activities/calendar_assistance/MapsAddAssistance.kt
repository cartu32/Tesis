package com.example.comunicationwearmobile.ui.view.activities.calendar_assistance

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.interfaces.OnDataSentListenerMapAct
import com.example.comunicationwearmobile.ui.view.activities.common.BaseMapActivity
import com.example.comunicationwearmobile.ui.view.fragment.configAssistanceAddFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng

class MapsAddAssistance: BaseMapActivity(), OnDataSentListenerMapAct {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initComponents()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)
        mMap?.setOnMapClickListener (this)

    }
    override fun onMapClick(latLng: LatLng) {
        super.onMapClick(latLng)

        drawAreaGeofHelper?.drawGeofenceArea(latLng.latitude, latLng.longitude, Definition.GEOFENCE_RADIUS_DEFAULT, Definition.TYPE_AREA_COLOR_ASSISTANCE)

        showConfigGeofenceFragment(latLng)

        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude}")
    }

    override fun initComponents() {
        super.initComponents()

        txtLeyends?.text = getString(R.string.leyend_date_zone)
    }

    private fun showConfigGeofenceFragment(latLng: LatLng) {
        val fragmentManager = supportFragmentManager
        val configAssistanceAddFragment = configAssistanceAddFragment()
        val ft = fragmentManager.beginTransaction()

        //muestro el fragment de configuracion
        configAssistanceAddFragment.show(ft,"configAssistanceAddFragment")

        //configuro el listener de respuesta del fragment
        configListenerResultFragment(latLng)
    }

    private fun configListenerResultFragment(latLng: LatLng) {
        supportFragmentManager.setFragmentResultListener(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA, this) { _, bundle ->

            val result=bundle.getInt(Definition.INTENT_STATE_OPERATION)
            val meters = bundle.getInt(Definition.INTENT_DATA_NEW_AREA_GEOF)

            if(result== Activity.RESULT_OK) {
                operationResultOK(meters,latLng)
            }else if(result== Activity.RESULT_CANCELED){
                operationResultCanceled()
            }
        }
    }

    private fun operationResultCanceled() {
        drawAreaGeofHelper?.cancelDrawnArea()
        Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()
    }

    private fun operationResultOK(meters: Int, latLng: LatLng) {

        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude} meters${meters}")

        val intent=Intent()

        intent.putExtra(Definition.INTENT_DATA_METERS,meters)
        intent.putExtra(Definition.INTENT_DATA_LATITUDE,latLng.latitude.toString())
        intent.putExtra(Definition.INTENT_DATA_LONGITUDE,latLng.longitude.toString())

        setResult(Activity.RESULT_OK,intent)

        finish()
    }

    override fun updateCircleRadius(meters: Double) {
        drawAreaGeofHelper?.updateCircleRadius(meters)
    }

    override fun onDestroy() {
        super.onDestroy()

        supportFragmentManager.clearFragmentResult(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA)


        mMap?.setOnMapClickListener(null)

        drawAreaGeofHelper?.freeResources()
        drawAreaGeofHelper=null
        Log.d(Definition.TAG_DEBUG, "Ondestroy MapsActivity")
    }
}