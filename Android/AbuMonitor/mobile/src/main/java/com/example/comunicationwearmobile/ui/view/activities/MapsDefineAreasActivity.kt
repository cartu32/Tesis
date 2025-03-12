package com.example.comunicationwearmobile.ui.view.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.ui.utils.interfaces.OnDataSentListenerMapAct
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng


class MapsDefineAreasActivity : BaseMapActivity(),OnDataSentListenerMapAct{

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap)

        mMap?.setOnMapClickListener (this)

        configOberserverLivedata()
    }

    override fun configOberserverLivedata() {
        super.configOberserverLivedata()

        configObserverIdNewArea()
        configObserverResultDelete()
    }

    override fun onMapClick(latLng: LatLng) {
        super.onMapClick(latLng)

        //grafico en el mapa la nueva area
        circle=drawGeofenceArea(latLng.latitude,latLng.longitude)
        showConfigGeofenceFragment(latLng)

        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude}")

    }

    override fun onCircleClick(circle: Circle) {
        super.onCircleClick(circle)

        showDeleteGeofenceDialog(circle.tag.toString().toLong())
        Log.d(Definition.TAG_DEBUG,"Circulo id:${circle.tag}")
        Toast.makeText(this,"Id Circulo${circle.tag}",Toast.LENGTH_SHORT).show()
    }



    private fun configObserverResultDelete() {
        viewmodelMapsActivity?.resultDeleteArea?.observe(this){circleToDelete->

            if(circleToDelete!=null){
                circleToDelete.remove()
                Toast.makeText(this,"Area eliminada",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"No se pudo eliminar el area",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun configObserverIdNewArea() {
        val error:Long=-1L

        viewmodelMapsActivity?.idNewAreaGeof?.observe(this){id->
            if (id!=error){
               setIdDrawnArea(id)
               Toast.makeText(this,"Area nueva registrada",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this,"No se pude registrar el Area",Toast.LENGTH_SHORT).show()
            }

        }
    }

    fun cancelDrawnArea(){
        circle?.remove()
    }

    override fun updateCircleRadius(meters: Double) {
        circle?.radius= meters
    }

    fun showDeleteGeofenceDialog(idArea:Long) {

        // Crear el cuadro de diálogo de confirmación
        val dialog = AlertDialog.Builder(this) // 'this' puede ser tu contexto, dependiendo de donde estés llamando a la función
            .setTitle("Eliminar área de geofence")
            .setMessage("¿Estás seguro de que deseas eliminar esta área de geofence?")
            .setPositiveButton("Sí") { _, _ ->
                // Acción cuando el usuario confirma
                viewmodelMapsActivity?.deleteAreaInBD(idArea)
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
        val configGeofenceFragment = ConfigGeofenceFragment()
        val ft = fragmentManager.beginTransaction()

        //muestro el fragment de configuracion
        configGeofenceFragment.show(ft,"configGeofenceFragment")

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
        cancelDrawnArea()
        Toast.makeText(this,"Cancelado",Toast.LENGTH_SHORT).show()
    }

    private fun operationResultOK(bundle: Bundle,latLng: LatLng) {
        var dataNewAreaGeofence = viewmodelMapsActivity?.extractDataNewAreaOfIntent(bundle)

        dataNewAreaGeofence?.latitude= latLng.latitude.toString()
        dataNewAreaGeofence?.longitude= latLng.longitude.toString()

        if (dataNewAreaGeofence != null) {
            viewmodelMapsActivity?.insertAreaInBD(dataNewAreaGeofence)
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        supportFragmentManager.clearFragmentResult(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA)

        // Eliminar los observadores de LiveData
        viewmodelMapsActivity?.resultDeleteArea?.removeObservers(this)
        viewmodelMapsActivity?.idNewAreaGeof?.removeObservers(this)

        mMap?.setOnMapClickListener(null)

        //Esto se debe realizar en la clase hija, no en la clase padre
        viewmodelMapsActivity=null
        mMap=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy MapsActivity")
    }
}

