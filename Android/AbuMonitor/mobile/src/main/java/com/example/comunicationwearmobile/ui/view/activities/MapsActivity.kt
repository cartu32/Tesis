package com.example.comunicationwearmobile.ui.view.activities

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Mannager.MapsManagerHelper
import com.example.comunicationwearmobile.ui.view.fragment.ConfigGeofenceFragment
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.gms.maps.model.LatLng

class MapsActivity : AppCompatActivity() {
    private var mapsActivity:MapsManagerHelper ?= null
    private var viewmodelMapsActivity:ViewmodelMapsActivity?=null

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        configInitial()
        configListener()
        initializeViewModel()
        configOberserverLivedata()
    }

    private fun configListener() {
        // Establecer listeners para el mapa
        mapsActivity?.onMapClickListener = { latLng ->
            // Manejar el click en el mapa
            listenerMapClick(latLng)
        }

        mapsActivity?.onMapLongClickListener = { latLng ->
            // Manejar el click largo en el mapa
            showDeleteGeofenceDialog(latLng)
        }

    }

    private fun configInitial() {
        setContentView(R.layout.activity_maps)
        mapsActivity=MapsManagerHelper()

        // Configurar el mapa con callbacks
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapsActivity!!)
            .commit()
    }


    private fun configOberserverLivedata() {
        viewmodelMapsActivity?.showMessage?.observe(this) { message ->
              Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        }
    }

    private fun initializeViewModel() {
        // Obtén una instancia del ViewModel usando el GenericViewModelFactory
        val factory = GenericViewModelFactory {
            ViewmodelMapsActivity(application)
        }

        viewmodelMapsActivity = ViewModelProvider(this, factory)[ViewmodelMapsActivity::class.java]
    }


    fun listenerMapClick(latLng: LatLng) {

        val areaGraphic= mapsActivity?.drawGeofenceArea(latLng)
        showConfigGeofenceFragment()
        viewmodelMapsActivity?.storeInTemporaryArea(latLng,areaGraphic?.first,areaGraphic?.second)

        Log.d(Definition.TAG_DEBUG,"Locacion Lat:${latLng.latitude} Longitude${latLng.longitude}")

    }

  fun showDeleteGeofenceDialog(latLng: LatLng) {
        Log.d(Definition.TAG_DEBUG,"LocacionLat:${latLng.latitude} Longitude:${latLng.longitude}")

        // Crear el cuadro de diálogo de confirmación
        val dialog = AlertDialog.Builder(this) // 'this' puede ser tu contexto, dependiendo de donde estés llamando a la función
            .setTitle("Eliminar área de geofence")
            .setMessage("¿Estás seguro de que deseas eliminar esta área de geofence?")
            .setPositiveButton("Sí") { _, _ ->
                // Acción cuando el usuario confirma
                viewmodelMapsActivity?.deleteAreaGeofence(latLng)
            }
            .setNegativeButton("No") { dialog, _ ->
                // Acción cuando el usuario cancela
                dialog.dismiss()
            }
            .create()

        // Mostrar el cuadro de diálogo
        if (!isFinishing) dialog.show()
    }


    private fun showConfigGeofenceFragment() {
        val fragmentManager = supportFragmentManager
        val configGeofenceFragment = ConfigGeofenceFragment()
        val ft = fragmentManager.beginTransaction()

        configGeofenceFragment.show(ft,"configGeofenceFragment")
    }



    override fun onDestroy() {
        super.onDestroy()

        mapsActivity?.clean()
        mapsActivity=null

        // Eliminar los observadores de LiveData
        viewmodelMapsActivity?.showMessage?.removeObservers(this)

        // Limpiar referencias del ViewModel si es necesario
        viewmodelMapsActivity?.onDestroyed() // Personalizado si existe en tu implementación
        viewmodelMapsActivity=null

        Log.d(Definition.TAG_DEBUG,"Ondestroy MapsActivity")
    }


}

