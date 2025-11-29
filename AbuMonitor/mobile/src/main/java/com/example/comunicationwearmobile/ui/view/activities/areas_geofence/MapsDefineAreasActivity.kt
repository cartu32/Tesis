package com.example.comunicationwearmobile.ui.view.activities.areas_geofence

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Helpers.Maps.MapsActivityHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.example.comunicationwearmobile.ui.utils.interfaces.OnDataSentListenerMapAct
import com.example.comunicationwearmobile.ui.view.activities.common.BaseMapActivity
import com.example.comunicationwearmobile.ui.view.fragment.ConfigDefineAreasFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng


//Esta es la clase que usa el helper MapsActivityHelper para reutilizar funcionalidad
//Esta clase define metodos que solo deben usarse en este mapa.
//Por ejemplo showDeleteGeofenceDialog: ya que este se usa para borrar los circulos del mapa,
//cuando se crean las areas de geofence
class MapsDefineAreasActivity : BaseMapActivity(), OnDataSentListenerMapAct {

    //como no se puede hacer herencia multiple en kotlin y para no complicar el codigo hago lo siguiente:
    //como el viewmodelmapsacivity se usa en 2 activities: MapsDefineAreasActivity y MapElderlyTrackActivty
    //lo que hago es un helper que contiene las funcionalidades que usan ambas activities del
    //viewmodelmapsactivity.
    private lateinit var mapsHelper: MapsActivityHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inicializar el helper
        mapsHelper = MapsActivityHelper(this, ViewModelProvider(this))
        mapsHelper.initializeViewModel()
        mapsHelper.configObservers(
            cleanMap = { cleanMap() },
            drawArea = { lat, lng, meters, color ->
                drawAreaGeofHelper?.drawGeofenceArea(lat, lng, meters, color)
            },
            setIdDrawnArea = { id -> drawAreaGeofHelper?.setIdDrawnArea(id) },
            showToast = { msg -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }
        )
        initComponents()
    }

    override fun initComponents() {
        super.initComponents()

        val leyend1 = getString(R.string.leyends_security_zone)
        val leyend2 = getString(R.string.leyend_normal_area)
        txtLeyends?.text = getString(R.string.leyends_combined, leyend1, leyend2)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        mMap?.setOnMapClickListener(this)

        configOberserverLivedata()
        Log.d(Definition.TAG_DEBUG, "Mapa inicializado completo")
    }

    //estos observer que solo se usan en el mapa para definir areas de geofence
    fun configOberserverLivedata() {

        configObserverIdNewArea()
        configObserverResultDelete()
        configObserverSecurityZone()
    }

    private fun configObserverSecurityZone() {
        mapsHelper.viewmodelMapsActivity?.securityZone?.observe(this) { isSecurity ->
            drawAreaGeofHelper?.changeColorCircle(Definition.TYPE_AREA_COLOR_SECURITY_ZONE)
        }
    }

    private fun configObserverResultDelete() {
        mapsHelper.viewmodelMapsActivity?.isDeleteArea?.observe(this) { idCircleToDelete ->
            if (idCircleToDelete != null) {
                drawAreaGeofHelper?.deleteCircleWithId(idCircleToDelete)
                Toast.makeText(this, "Area eliminada", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se pudo eliminar el area", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configObserverIdNewArea() {
        mapsHelper.viewmodelMapsActivity?.idNewAreaGeof?.observe(this) { id ->
            when {
                id > Definition.ERROR_NULL -> {
                    drawAreaGeofHelper?.setIdDrawnArea(id)
                    Toast.makeText(this, "Area nueva registrada", Toast.LENGTH_SHORT).show()
                }

                id == Definition.ERROR_INSERT_BD_GEOF -> {
                    drawAreaGeofHelper?.cancelDrawnArea()
                    Toast.makeText(this, "No se pude registrar el Area en la BD", Toast.LENGTH_SHORT).show()
                }

                id == Definition.ERROR_ACTIVATE_GEOF -> {
                    drawAreaGeofHelper?.cancelDrawnArea()
                    Toast.makeText(this, "No se pude activar el area de geof", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onMapClick(latLng: LatLng) {
        super.onMapClick(latLng)

        //grafico en el mapa la nueva area
        drawAreaGeofHelper?.drawGeofenceArea(latLng.latitude, latLng.longitude,Definition.GEOFENCE_RADIUS_DEFAULT,Definition.TYPE_AREA_COLOR_NORMAL)
        showConfigGeofenceFragment(latLng)

        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude}")

    }

    override fun onCircleClick(circle: Circle) {
        super.onCircleClick(circle)

        if (circle.tag != null) {
            showDeleteGeofenceDialog(circle.tag.toString().toLong())
            Log.d(Definition.TAG_DEBUG, "Circulo id:${circle.tag}")
            Toast.makeText(this, "Id Circulo${circle.tag}", Toast.LENGTH_SHORT).show()
        }else
            Toast.makeText(this, "No se pudo obtener el id del circulo por que es null", Toast.LENGTH_SHORT).show()
    }




    private fun showDeleteGeofenceDialog(idArea:Long) {

        // Crear el cuadro de diálogo de confirmación
        val dialog = AlertDialog.Builder(this) // 'this' puede ser tu contexto, dependiendo de donde estés llamando a la función
            .setTitle("Eliminar área de geofence")
            .setMessage("¿Estás seguro de que deseas eliminar esta área de geofence?")
            .setPositiveButton("Sí") { _, _ ->
                // Acción cuando el usuario confirma
                mapsHelper.viewmodelMapsActivity?.deleteAreaGeof(this, idArea)
            }
            .setNegativeButton("No") { dialog, _ ->
                // Acción cuando el usuario cancela
                dialog.dismiss()
            }
            .create()

        // Mostrar el cuadro de diálogo
        if (!isFinishing) dialog.show()
    }


    private fun showConfigGeofenceFragment(latLng: LatLng) {
        val fragmentManager = supportFragmentManager
        val configDefineAreasFragment = ConfigDefineAreasFragment()
        val ft = fragmentManager.beginTransaction()

        //muestro el fragment de configuracion
        configDefineAreasFragment.show(ft,"configGeofenceFragment")

        //configuro el listener de respuesta del fragment
        configListenerResultFragment(latLng)
    }



    private fun configListenerResultFragment(latLng: LatLng) {
        supportFragmentManager.setFragmentResultListener(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA, this) { _, bundle ->

            val result=bundle.getInt(Definition.INTENT_STATE_OPERATION)

            if(result== Activity.RESULT_OK) {
                operationResultOK(bundle,latLng)
            }else if(result==Activity.RESULT_CANCELED){
                operationResultCanceled()
            }
        }
    }

    private fun operationResultCanceled() {
        drawAreaGeofHelper?.cancelDrawnArea()
        Toast.makeText(this,"Cancelado",Toast.LENGTH_SHORT).show()
    }

    private fun operationResultOK(bundle: Bundle, latLng: LatLng) {
        val dataNewAreaGeofence = Tools.extractDataNewAreaOfIntent(bundle)

        dataNewAreaGeofence?.entityAreaGeofence?.latitude = latLng.latitude.toString()
        dataNewAreaGeofence?.entityAreaGeofence?.longitude = latLng.longitude.toString()

        if (dataNewAreaGeofence != null) {
            mapsHelper.viewmodelMapsActivity?.insertAreaGeof(this, dataNewAreaGeofence)
        }
    }

    override fun updateCircleRadius(meters: Double) {
        drawAreaGeofHelper?.updateCircleRadius(meters)
    }


    override fun onDestroy() {
        super.onDestroy()

        supportFragmentManager.clearFragmentResult(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA)

        // Limpiar el helper
        mapsHelper.cleanUp()

        // Eliminar los observadores de LiveData específicos de esta actividad
        mapsHelper.viewmodelMapsActivity?.isDeleteArea?.removeObservers(this)
        mapsHelper.viewmodelMapsActivity?.idNewAreaGeof?.removeObservers(this)
        mapsHelper.viewmodelMapsActivity?.securityZone?.removeObservers(this)

        mMap?.setOnMapClickListener(null)

        drawAreaGeofHelper?.freeResources()

        Log.d(Definition.TAG_DEBUG, "Ondestroy MapsActivity")
    }

}

