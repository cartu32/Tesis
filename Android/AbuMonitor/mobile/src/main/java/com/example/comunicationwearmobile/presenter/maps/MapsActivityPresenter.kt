package com.example.comunicationwearmobile.presenter.maps


import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.example.comunicationwearmobile.models.maps.SharedPreferencesRoutes
import com.example.comunicationwearmobile.models.maps.iGeofence
import com.example.comunicationwearmobile.ui.Activities.MapsActivity
import com.example.comunicationwearmobile.ui.Fragments.fragment_config_geofence
import com.example.comunicationwearmobile.utils.maps.Tools
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import java.util.Objects

class MapsActivityPresenter @RequiresApi(api = Build.VERSION_CODES.TIRAMISU) constructor(private var activity: MapsActivity?) {
    companion object {
        const val TAG: String = "MapsActivityPresenter"
        const val SELECT_ORIGIN_POINT: Int = 1
        const val SELECT_DESTINATION_POINT: Int = 2
        const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 15 //metros
        const val MIN_TIME_BW_UPDATES: Long = (1000 * 30 ).toLong()
    }

    private var geofenceHelper: GeofenceHelper? = null
    private var geofencingClient: GeofencingClient ?= null


    private val hashMapId = HashMap<String , Int>()

    /*Se declara una variable de tipo LocationManager encargada de proporcionar acceso al servicio de localización del sistema.*/
    private var locationManager: LocationManager? = null

    /*Se declara una variable de tipo Location que accederá a la última posición conocida proporcionada por el proveedor.*/
    private var location: Location? = null
    private var isGPSEnabled = false
    private var isNetworkEnabled: Boolean? = false

    private var idAreaActiva = Tools.DEFAULT_ACTIVE_AREA

    private val GEOFENCE_RADIUS_DEFAULT = 100f
    private var selectPoint = SELECT_ORIGIN_POINT
    private var originPoint: LatLng? = null
    private var destinationPoint: LatLng? = null

    private var frag: fragment_config_geofence? = null
    private var mapsRoute: MapsRoute? = null
    private var waypointsActiveRoute: List<LatLng>? = null

    private var filtro: IntentFilter? = null
    private var filtroExterno: IntentFilter? = null
    private val receiver: ReceptorOperation = ReceptorOperation()


    //Array que contiene las areas de Geofencing agregadas manualmente por el usuario
    private val listAreaAddedManual = ArrayList<iGeofence>()


    init {
        geofenceHelper = GeofenceHelper(activity)
        geofencingClient = LocationServices.getGeofencingClient(activity!!)
        mapsRoute = MapsRoute(activity)
        configureBroadcastReciever()
    }


    @SuppressLint("ObsoleteSdkInt")
    fun checkPermisson() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            ) {
                activity!!.enableMap()
            } else {
                val MULTIPLE_PERMISSON_REQUEST_CODE = 10003
                activity!!.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_FINE_LOCATION , Manifest.permission.ACCESS_BACKGROUND_LOCATION) , MULTIPLE_PERMISSON_REQUEST_CODE
                )
            }
        } else {
            activity!!.enableMap()
        }
    }
    fun onRequestPermissionsResult(requestCode: Int , permissions: Array<String> , grantResults: IntArray)
    {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //We have the permission
            activity!!.showMessage("Ahora puedes agregar puntos de Georeferencia")
            activity!!.enableMap()
        } else {
            //We do not have the permission..
            activity!!.showMessage("El acceso a la ubicación en segundo plano es necesario para que se activen las geocercas...")
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    private fun configureBroadcastReciever() {
        //se asocia(registra) la  accion RESPUESTA_OPERACION, para que cuando el Servicio de recepcion la ejecute
        //se invoque automaticamente el OnRecive del objeto receiver
        filtro = IntentFilter("com.example.intentservice.intent.action.RESPUESTA_OPERACION")
        filtroExterno =IntentFilter("com.example.intentservice.intent.action.NOTIFICACION_FIREBASE")

        filtro!!.addCategory(Intent.CATEGORY_DEFAULT)
        filtroExterno!!.addCategory(Intent.CATEGORY_ALTERNATIVE)

        activity!!.applicationContext.registerReceiver(receiver , filtro , Context.RECEIVER_EXPORTED)
    }

    fun getLocation(): Location {
        try {
            if (checkconnection()) {
                // Si no hay proveedor habilitado
                //solicito que active el gps
                activity!!.alertNoGps()
            }

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                setPositionGPS()
            } else if (isNetworkEnabled!!) {
                setPositionNetwork()
            }
        } catch (e: Exception) {
            Log.e("getLocation" , e.message.toString())
        }
        return location!!
    }


    fun setPositionGPS() {
        if (location == null) {
            if (ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            locationManager!!.requestLocationUpdates(
                LocationManager.GPS_PROVIDER ,
                MIN_TIME_BW_UPDATES ,
                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat() , activity!!
            )

            // Si location es mutable globalmente, asignamos el valor de locationManager a location de manera segura
            locationManager?.let { manager ->
                location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let { currentLocation ->
                    activity?.positionUpdate(currentLocation)
                }
            } ?: run {
                println("locationManager es nulo")
            }

        }
    }

    private fun setPositionNetwork() {
        if (ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager!!.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER ,
            MIN_TIME_BW_UPDATES ,
            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat() , activity!!
        )
        locationManager?.let { manager->
            location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let { currentLocation ->
                activity!!.positionUpdate(currentLocation)
            }
        }

    }


    fun checkGeofenceRoute(latLng: LatLng?) {
        val resp: Boolean

        if (waypointsActiveRoute != null) {
            resp = PolyUtil.isLocationOnPath(latLng , waypointsActiveRoute , true , 300.0)
            Log.d("Respuesta" , resp.toString())

            if (!resp) {
                val intent = Intent(
                    activity!!.applicationContext ,
                    GeofenceTransitionService::class.java
                )
                intent.putExtra("Operation" , Tools.GEOFENCE_ROUTE)
                activity!!.startService(intent)
            }
        }
    }

    private fun generateGeofences(listLastGeofence: java.util.ArrayList<iGeofence>): Boolean {
        //Creo el identificador de cada zona de goefoence que empiece por una letra identifcadora.


        var idAux: Int

        if (listLastGeofence.isEmpty()) return false

        if (checkconnection()) {
            activity!!.showMessage("No hay conexion de GPS o Red")
            return false
        }

        Log.d("Alerta" , "Entrando en For")
        for (i in listLastGeofence.indices) {
            //cantGeofences++;
            idAux = createHashMapId(listLastGeofence[i].idArea.toString())
            geofenceHelper!!.addGeofenceList(
                listLastGeofence[i].idArea + idAux ,
                listLastGeofence[i].latitud!! ,
                listLastGeofence[i].longitud!! ,
                listLastGeofence[i].radius ,
                Geofence.GEOFENCE_TRANSITION_ENTER
            )
            Log.d("Alerta" , "Ejecutando For")
        }

        return activateGefenceRequest()
    }


    private fun createHashMapId(idArea: String): Int {
        // Usamos el operador getOrDefault para obtener el valor de la clave o un valor por defecto si es null
        var cantId = hashMapId.getOrDefault(idArea, 0)

        // Si no es nulo, incrementamos el valor
        cantId++

        // Actualizamos el valor en el mapa
        hashMapId[idArea] = cantId

        return cantId
    }


    private fun getHashMapIdOrigin(idArea: String): Int {
        var idOrigin: Int
        val DESTINO = 1

        idOrigin = hashMapId[idArea]!!

        //como este metodo es invocado cuando se genera la ruta, entonces
        //entonces en el Hashmap hay grabados dos ID. El primero el origen y despues el destino
        //Con lo cual para obtener el idOrigen correcto, debo restarle 1 para poder obtenerlo. Porque
        //sino me devolveria el idDestino.
        idOrigin -= DESTINO

        return idOrigin
    }

    private fun activateGefenceRequest(): Boolean {
        val geofencingRequest = geofenceHelper!!.geofencingRequest

        if (ActivityCompat.checkSelfPermission(activity!! , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
        geofencingClient!!.addGeofences(geofencingRequest , geofenceHelper!!.pendingIntent!!)
            .addOnSuccessListener {
                Log.d(TAG , "onSuccess: Geofence Added...")
            }
            .addOnFailureListener { e ->
                val errorMessage = geofenceHelper!!.getErrorString(e)
                Log.d(TAG , "onFailure: $errorMessage")
                activity!!.showMessage("on Failure$errorMessage")
            }
        return true
    }


    fun saveRouteInFile(listRoute: List<LatLng?>? , idArea: String) {
        val numberId = getHashMapIdOrigin(idArea)

        SharedPreferencesRoutes.setStringArrayPref(this.activity , idArea + numberId , listRoute)
    }

    private fun clearRouteInFile() {
        SharedPreferencesRoutes.clearSharedPreferences(this.activity)
    }

    private fun clearGeofencesIntent() {
        geofencingClient!!.removeGeofences(geofenceHelper!!.pendingIntent!!)
            .addOnSuccessListener(activity!!) { // Geofences removed
                // ...
                Log.d(TAG , "se borraron todos los geofences")
            }
            .addOnFailureListener(activity!!) { // Failed to remove geofences
                Log.d(TAG , "Error al borrar todos los geofences")
            }

        hashMapId.clear()
        geofenceHelper!!.clearGeofenceList()
    }

    private fun createRetrofitGeofence(id: String? , latLng: LatLng , radius: Float): iGeofence {
        val obj = iGeofence()
        obj.idArea = id
        obj.radius = radius
        obj.latitud = latLng.latitude
        obj.longitud = latLng.longitude

        return obj
    }


    fun determineColor(): Int {
        var color = 0

        when (idAreaActiva) {
            "A" -> color = Color.RED
            "B" -> color = Color.GREEN
            "C" -> color = Color.MAGENTA
            "Ruta Z" -> color = Color.BLUE
            else -> Log.e("TAG" , "Error al determinar el color")
        }
        return color
    }


    private fun checkconnection(): Boolean {
        activity!!.applicationContext
        locationManager = activity!!.applicationContext
            .getSystemService(Context.LOCATION_SERVICE) as LocationManager

        locationManager?.let{manager->
            // getting GPS status
            isGPSEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER)

            // getting network status
            isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }
        return (!isGPSEnabled &&  !isNetworkEnabled!!)
    }

    private fun closeFragment() {
        val prev = activity!!.supportFragmentManager.findFragmentById(
            frag!!.id
        )


        if (prev != null) activity!!.supportFragmentManager.beginTransaction().remove(prev).commit()
    }

    fun addAreaGeofenceInList(latLng: LatLng) {
        listAreaAddedManual.add(createRetrofitGeofence(idAreaActiva, latLng, GEOFENCE_RADIUS_DEFAULT))

        activity?.addMarkerGeofence(latLng, GEOFENCE_RADIUS_DEFAULT)

        frag = fragment_config_geofence(this, GEOFENCE_RADIUS_DEFAULT, true)
        activity?.let { frag?.show(it.supportFragmentManager, fragment_config_geofence::class.java.simpleName) }
    }


    fun generateGeofencesManual() {
        if (generateGeofences(listAreaAddedManual)) {
            closeFragment()
            activity!!.showMessage("geofences agregadas")
        } else {
            activity!!.showMessage("Error al agragar geofences")
        }
        listAreaAddedManual.clear()
    }

    fun markRoutePoint(latLng: LatLng) {
        when (selectPoint) {
            SELECT_ORIGIN_POINT -> {
                originPoint = latLng
                selectPoint = SELECT_DESTINATION_POINT

                //dibujo en el mapa el area de origen
                activity!!.addMarkerGeofence(latLng , GEOFENCE_RADIUS_DEFAULT)
                listAreaAddedManual.add(
                    createRetrofitGeofence(
                        idAreaActiva ,
                        latLng ,
                        GEOFENCE_RADIUS_DEFAULT
                    )
                )
            }

            SELECT_DESTINATION_POINT -> {
                destinationPoint = latLng
                selectPoint = SELECT_ORIGIN_POINT

                //dibujo en el mapa el area de destino
                activity!!.addMarkerGeofence(latLng , GEOFENCE_RADIUS_DEFAULT)
                listAreaAddedManual.add(
                    createRetrofitGeofence(
                        idAreaActiva ,
                        latLng ,
                        GEOFENCE_RADIUS_DEFAULT
                    )
                )

                //abro el menu fragment
                frag = fragment_config_geofence(this , GEOFENCE_RADIUS_DEFAULT , false)
                frag?.show(
                    activity!!.supportFragmentManager ,
                    fragment_config_geofence::class.java.simpleName
                )
            }

            else -> activity!!.showMessage("Error en switch de ruta")
        }
    }

    fun generateRouteManual() {
        generateGeofencesManual()
        mapsRoute!!.getWaypoints(originPoint!! , destinationPoint!! , idAreaActiva)
        closeFragment()
    }

    fun updateCircleRadius(geofenceRadius: Float) {
        activity!!.updateCircleRadiusGraphic(geofenceRadius)

        //obtengo el ultimo item agregado al listado
        val aux = listAreaAddedManual[listAreaAddedManual.size - 1]
        //lo actualizo con el valor del id seleccionado
        aux.radius = geofenceRadius
        //actualizo el item en el listado
        listAreaAddedManual[listAreaAddedManual.size - 1] = aux
    }

    fun saveSelectedGeofencesArea(idArea: String?) {
        this.idAreaActiva = idArea!!
        activity!!.updateCircleColorGraphic(idAreaActiva)
        //obtengo el ultimo item agregado al listado
        val aux = listAreaAddedManual[listAreaAddedManual.size - 1]
        //lo actualizo con el valor del id seleccionado
        aux.idArea = idAreaActiva
        //actualizo el item en el listado
        listAreaAddedManual[listAreaAddedManual.size - 1] = aux

        activity!!.showMessage("Item Seleccionado" + this.idAreaActiva)
    }

    fun clearGeofences() {
        clearGeofenceMaps()
        clearRouteInFile()
        closeFragment()
    }

    fun clearGeofenceMaps() {
        selectPoint = SELECT_ORIGIN_POINT
        clearGeofencesIntent()
        listAreaAddedManual.clear()
        activity!!.clearMaps()
    }

    fun updateActiveRoute(intent: Intent) {
        waypointsActiveRoute = intent.getSerializableExtra("rutaActiva") as List<LatLng>?
    }

    fun clearActiveRoute() {
        originPoint = null
        destinationPoint = null
        selectPoint = SELECT_DESTINATION_POINT
        waypointsActiveRoute = null
    }

    inner class ReceptorOperation : BroadcastReceiver() {
        override fun onReceive(context: Context , intent: Intent) {
            val operacion = Objects.requireNonNull(intent.extras)?.getInt("Operacion")

            if (operacion == Tools.OPERATION_UPDATE_ACTIVE_ROUTE) {
                updateActiveRoute(intent)
            } else if (operacion == Tools.OPERATION_CLEAR_ACTIVE_ROUTE) {
                clearActiveRoute()
            }
        }
    }


}