package com.example.comunicationwearmobile.ui.viewmodel

import android.Manifest
import android.app.AlarmManager
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.repository.RepositorySecurityZoneSPref
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ViewmodelMainActivity(application: Application): AndroidViewModel(application) {

    private var tempAreaGeof:EntityAreaGeofence?=null

    private val _permissionsToRequest = MutableLiveData<List<String>?>()
    val permissionsToRequest: LiveData<List<String>?> = _permissionsToRequest

    private val _backgroundPermissionRequired = MutableLiveData<Boolean?>()
    val backgroundPermissionRequired: LiveData<Boolean?> = _backgroundPermissionRequired

    private val _allPermissionGranted = MutableLiveData<Boolean?>()
    val allPermissionGranted: LiveData<Boolean?> = _allPermissionGranted

    private val _requestExactAlarmPermission = MutableLiveData<Boolean?>()
    val requestExactAlarmPermission: LiveData<Boolean?> = _requestExactAlarmPermission

        init {
        // Lanzamos una coroutine asincrónica en el viewModelScope
        viewModelScope.launch(Dispatchers.IO) {
            val repositorySecurityZoneSPref= RepositorySecurityZoneSPref.getInstance(application.applicationContext)

            repositorySecurityZoneSPref.clearSharedPreferences()

            // Después de obtener la base de datos en IO, podemos cambiar al hilo principal para actualizar el estado
            withContext(Dispatchers.Main) {
                Log.d(Definition.TAG_DEBUG, "Geo: Base de datos Inicializada")
                tempAreaGeof = EntityAreaGeofence()
            }
        }
    }


    fun checkPermissions() {
        val context = getApplication<Application>().applicationContext

        val missingPermissions=checkGeneralPermissions(context)
        checkFineLocation(context,missingPermissions)
        checkAlarmPermisson(context)
    }

    private fun checkAlarmPermisson(context: Context?) {
        val alarmManager =context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            Log.d(Definition.TAG_DEBUG, "No se tiene permisos de alarma")
            _requestExactAlarmPermission.value=false
        }else{
            Log.d(Definition.TAG_DEBUG, "tiene permisos de alarma")
            _requestExactAlarmPermission.value=true
        }

    }

    private fun checkFineLocation(context: Context,missingPermissions: List<String>) {

        if (missingPermissions.isNotEmpty()) {
            _permissionsToRequest.value = missingPermissions
        } else if (ContextCompat.checkSelfPermission(context , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkBackgroundLocation(context)
        }
    }

    private fun checkBackgroundLocation(context: Context) {
        if(ContextCompat.checkSelfPermission(context , Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED){
            //si Backgruond_location esta otorgado, entonces quiere decir que todos los permisos fueron otorgados
            _allPermissionGranted.value=true
        }else{
            //si background_location no esta otorgado, entonces se solicita
            _backgroundPermissionRequired.value = true
        }

    }

    private fun checkGeneralPermissions(context: Context): List<String> {
        val missingPermissions = Definition.permissonNecesary.filter {
            if((it==Manifest.permission.FOREGROUND_SERVICE_LOCATION)&&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE))
                false
            else if(ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED)
                true
            else
                false
        }
        return missingPermissions

    }

    fun onBackgroundPermissionHandled() {
        _backgroundPermissionRequired.value = false
    }


    private fun isWearOsAppInstalled(): Boolean {
        val appCtx = getApplication<Application>().applicationContext
        val pm = appCtx.packageManager
        val pkg = "com.google.android.wearable.app" // principal

        return try {
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(pkg, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }




    fun checkWearableApiAvailable(context: Context): Int {
        val gmsAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

        val wearOsInstalled = isWearOsAppInstalled()

        if (!gmsAvailable) {
            Log.d(Definition.TAG_DEBUG,"Google Play Services no está disponible o actualizado.")
            return Definition.ERROR_PLAY_SERVICES_MISSING_OR_OUTDATED
        }

        if (!wearOsInstalled) {
            return Definition.ERROR_API_UNAVAILABLE
        }

        return Definition.API_OK
    }

    fun onDestroyed(){
        //por si uso alguna corutina la cancelo
        viewModelScope.cancel()

        //limpio los livedata
        _allPermissionGranted.value = null
        _permissionsToRequest.value = null
        _backgroundPermissionRequired.value = null

        tempAreaGeof=null
    }
}