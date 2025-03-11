package com.example.comunicationwearmobile.ui.view.activities

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.ui.utils.interfaces.OnDataSentListenerMapAct
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng


class MapsDefineAreasActivity : BaseMapActivity(),OnDataSentListenerMapAct{
    private var viewmodelMapsActivity:ViewmodelMapsActivity?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeViewModel()
        configOberserverLivedata()
    }


    private fun initializeViewModel() {
        // Obtén una instancia del ViewModel usando el GenericViewModelFactory
        val factory = GenericViewModelFactory {
            ViewmodelMapsActivity(application)
        }

        viewmodelMapsActivity = ViewModelProvider(this, factory)[ViewmodelMapsActivity::class.java]
    }


    private fun configOberserverLivedata() {
        configObserverShowMessage()
        configObserverGetAllAreasGeofence()
        configObserverIdNewArea()
        configObserverResultDelete()
    }

    override fun onMapClick(latLng: LatLng) {
        super.onMapClick(latLng)

        //grafico en el mapa la nueva area
        drawGeofenceArea(latLng.latitude,latLng.longitude)
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
        val error:Int=-1
        viewmodelMapsActivity?.resultDeleteArea?.observe(this){result->

            if(result!=error){
                deleteDrawnArea()
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

    private fun configObserverGetAllAreasGeofence() {
        viewmodelMapsActivity?.allAreas?.observe(this){listAllAreas->
            cleanMap()

            listAllAreas.forEach{
                Log.d(Definition.TAG_DEBUG,"Id:{${it.id_area} Description{${it.description}}")
                drawGeofenceArea(it.latitude.toDouble(),it.longitude.toDouble(),it.meters.toDouble())
                setIdDrawnArea(it.id_area)
            }
        }
    }

    private fun configObserverShowMessage() {
        viewmodelMapsActivity?.showMessage?.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
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

    private fun extractDataNewAreaOfIntent(data: Bundle): EntityAreaGeofence? {
        //Recibo los datos desde la activty PropertiesGeofence Activty
        val dataNewAreaGeof = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelable<EntityAreaGeofence>(Definition.INTENT_DATA_NEW_AREA_GEOF,EntityAreaGeofence::class.java)
        } else {
            data.getParcelable<EntityAreaGeofence>(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }
        return dataNewAreaGeof
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
        var dataNewAreaGeofence = extractDataNewAreaOfIntent(bundle)

        dataNewAreaGeofence?.latitude= latLng.latitude.toString()
        dataNewAreaGeofence?.longitude= latLng.longitude.toString()

        if (dataNewAreaGeofence != null) {
            viewmodelMapsActivity?.insertAreaInBD(dataNewAreaGeofence)
        }
    }
    fun cancelDrawnArea(){
        circle?.remove()
    }


    open fun deleteDrawnArea() {
        circlesMap[tempIdSelected]?.remove() //Borra el círculo del mapa (visualmente)
        circlesMap.remove(tempIdSelected)    //Elimina la referencia del círculo en el Map
    }

    override fun onDestroy() {
        super.onDestroy()


        supportFragmentManager.clearFragmentResult(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA)

        // Eliminar los observadores de LiveData
        viewmodelMapsActivity?.showMessage?.removeObservers(this)
        viewmodelMapsActivity?.resultDeleteArea?.removeObservers(this)
        viewmodelMapsActivity?.idNewAreaGeof?.removeObservers(this)
        viewmodelMapsActivity?.allAreas?.removeObservers(this)

        // Limpiar referencias del ViewModel si es necesario
        viewmodelMapsActivity?.onDestroyed() // Personalizado si existe en tu implementación
        viewmodelMapsActivity=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy MapsActivity")
    }



}

