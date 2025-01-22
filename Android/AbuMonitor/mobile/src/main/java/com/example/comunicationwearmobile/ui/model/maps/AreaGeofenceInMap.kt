package com.example.comunicationwearmobile.ui.model.maps

import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.Serializable
import java.lang.ref.WeakReference

/*Esta clase se usa para tener en memoria un listado de las areas de geofence que se
muestran en el mapa en el momento.Hice esta clase para ocupar menos memoria en los datos
que necesito.Es para no utilizar toda la
todos los datos de EntityAreaGeofence que corresponde a la base de datos
*/
class AreaGeofenceInMap : Serializable {
    var id_area: Int =0
    lateinit var latLng: LatLng
    lateinit var circle: Circle
    lateinit var marker: Marker
}
