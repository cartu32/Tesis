package com.example.comunicationwearmobile.ui.view.activities.elderly_track

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.Helpers.MapsActivityHelper
import com.example.comunicationwearmobile.ui.view.activities.common.BaseMapActivity
import com.example.comunicationwearmobile.ui.view.activities.common.PropertiesGeofenceActivity
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng

//Esta es la clase que usa el helper MapsActivityHelper para reutilizar funcionalidad
//Esta clase define metodos que solo deben usarse en este mapa.
//Por ejemplo updateMapLocation: que actualiza la camara del mapa a la ubicacion actual

class MapsElderlyTrackActivity : BaseMapActivity() {
    private lateinit var mapsHelper: MapsActivityHelper
    private var viewmodelLoaction: ViewmodelLocation?=null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el helper
        mapsHelper = MapsActivityHelper(this, ViewModelProvider(this))
        mapsHelper.initializeViewModel()
        mapsHelper.configObservers(
            cleanMap = { cleanMap() },
            drawArea = { lat, lng, meters, securityZone ->
                drawAreaGeofHelper?.drawGeofenceArea(lat, lng, meters, securityZone)
            },
            setIdDrawnArea = { id -> drawAreaGeofHelper?.setIdDrawnArea(id) },
            showToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
        )

        initializeViewModel()
        configOberserverLivedata()
        configActivityResult()
    }



    fun initializeViewModel() {
        viewmodelLoaction = ViewmodelLocation(application)
    }

    fun configOberserverLivedata() {
        configObserverLocation()
        configObserverClickInCircle()
    }

    private fun configObserverClickInCircle() {
        // Observamos una sola vez
        mapsHelper.viewmodelMapsActivity?.areaGeofenceForId?.observe(this) { dato ->
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

        mapsHelper.viewmodelMapsActivity?.getAreaGeofWithId(circle.tag.toString().toLong())

        Toast.makeText(this, "Cargando información del área...", Toast.LENGTH_SHORT).show()
    }

    private fun configActivityResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(Definition.TAG_DEBUG,"Result: ${result.resultCode}")
        }
    }
    override fun onDestroy() {
        super.onDestroy()

        // Limpiar el helper
        mapsHelper.cleanUp()

        viewmodelLoaction?.locationLiveData?.removeObservers(this)
        viewmodelLoaction?.onDestroyed()
        viewmodelLoaction = null

        // Eliminar observadores específicos de esta actividad
        mapsHelper.viewmodelMapsActivity?.areaGeofenceForId?.removeObservers(this)

        Log.d(Definition.TAG_DEBUG, "Ondestroy MapsElderlyAcivity")
    }
}

