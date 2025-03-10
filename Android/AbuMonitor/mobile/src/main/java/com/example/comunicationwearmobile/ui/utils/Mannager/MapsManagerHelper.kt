package com.example.comunicationwearmobile.ui.utils.Mannager

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng


class MapsManagerHelper : Fragment() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, LocationListener, GoogleMap.OnCircleClickListener {

    private var mMap: GoogleMap? = null
    private var circle: Circle? = null
    private val RC_HANDLE_GMS = 9001

    private val circlesMap = mutableMapOf<String?, Circle?>()

    private var tempIdSelected:String?=null

    // Para notificar a la actividad sobre eventos
    var onMapClickListener: ((LatLng) -> Unit)? = null
    var onMapLongClickListener: ((LatLng) -> Unit)? = null
    var onMapCircleClickListener: ((Circle) -> Unit)? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        val resultCode = context?.let {
            GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(it)
        }

        if (resultCode != ConnectionResult.SUCCESS) {
            val dlg = resultCode?.let {
                GoogleApiAvailability.getInstance().getErrorDialog(requireActivity(), it, RC_HANDLE_GMS)
            }
            dlg?.show()
        }

        mMap?.setOnMapLongClickListener(this)
        mMap?.setOnMapClickListener(this)
        mMap?.setOnCircleClickListener(this)

        enableFeatureMaps()
    }

    private fun enableFeatureMaps() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "No se han otorgados permisos de Ubicacion", Toast.LENGTH_SHORT).show()
            return
        }
        mMap?.isMyLocationEnabled = true
        mMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true
    }


    override fun onMapClick(latLng: LatLng) {
        onMapClickListener?.invoke(latLng)
    }

    override fun onMapLongClick(latLng: LatLng) {
        onMapLongClickListener?.invoke(latLng)
    }


    override fun onCircleClick(circle: Circle) {
        tempIdSelected=circle.tag.toString()
        onMapCircleClickListener?.invoke(circle)

    }


    fun cleanMap(){
        mMap?.clear()
    }

    fun drawGeofenceArea(latitude: Double, longitude: Double, meters: Double=Definition.GEOFENCE_RADIUS_DEFAULT): Circle? {
        val latLng=LatLng(latitude,longitude)

        val circle = addCircle(latLng,meters)


        return circle
    }

    fun deleteDrawnArea() {
        circlesMap[tempIdSelected]?.remove() //Borra el círculo del mapa (visualmente)
        circlesMap.remove(tempIdSelected)    //Elimina la referencia del círculo en el Map
    }

    fun addCircle(latLng: LatLng, radius: Double): Circle? {
        val alpha = 64
        val colorCircle = Color.BLUE
        this.circle = mMap?.addCircle(
            CircleOptions()
                .center(latLng)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle, alpha))
                .radius(radius.toDouble())
                .strokeWidth(4f)
                .clickable(true)
        )
        return circle
    }

    fun positionUpdate(location: Location): LatLng {
        val zoomLevel = 16.0f //This goes up to 21
        val latLng = LatLng(location.latitude , location.longitude)

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , zoomLevel))

        return latLng
    }

    fun setIdDrawnArea(id:Long){
        val tag:String

        tag=id.toString()
        circle?.tag= tag
        circlesMap[tag]=circle
    }

    fun cancelDrawnArea(){
        circle?.remove()
    }

    override fun onLocationChanged(p0: Location) {
        positionUpdate(p0)
    }

    fun removeAllCircle(){
        circlesMap.values.forEach{it?.remove()}
        circlesMap.clear()

        // Limpiar referencias del círculo
        circle?.remove() // Esto elimina el círculo del mapa
        circle=null

    }
    fun clean() {
        //libero todos los circulos
        removeAllCircle()

        // Liberar listeners del mapa
        onMapClickListener=null
        onMapLongClickListener=null
        onMapCircleClickListener=null

        mMap?.setOnMapLongClickListener(null)
        mMap?.setOnMapClickListener(null)
        mMap?.setOnCircleClickListener(null)
        mMap=null

    }




}