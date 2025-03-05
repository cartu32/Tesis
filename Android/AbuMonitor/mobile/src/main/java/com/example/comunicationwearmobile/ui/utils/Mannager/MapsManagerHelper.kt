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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions


class MapsManagerHelper : Fragment() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, LocationListener {

    private var mMap: GoogleMap? = null
    private var circle: Circle? = null
    private var marker:Marker? = null
    private val RC_HANDLE_GMS = 9001

    // Para notificar a la actividad sobre eventos
    var onMapClickListener: ((LatLng) -> Unit)? = null
    var onMapLongClickListener: ((LatLng) -> Unit)? = null

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

    fun updateCircleColorGraphic(colorCircle: Int) {
        val alpha = 64
        circle?.strokeColor = colorCircle
    }

    fun drawGeofenceArea(latLng: LatLng): Pair<Circle?, Marker?> {
        val marker = addMarker(latLng)
        val circle = addCircle(latLng)

        return Pair(circle,marker)
    }

    fun addMarker(latLng: LatLng): Marker? {
        val geoFenceMarker = MarkerOptions().position(latLng)
        marker = mMap?.addMarker(geoFenceMarker)
        return marker
    }

    fun addCircle(latLng: LatLng, radius: Int = Definition.GEOFENCE_RADIUS_DEFAULT): Circle? {
        val alpha = 64
        val colorCircle = Color.BLUE
        this.circle = mMap?.addCircle(
            CircleOptions()
                .center(latLng)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle, alpha))
                .radius(radius.toDouble())
                .strokeWidth(4f)
        )
        return circle
    }

    fun positionUpdate(location: Location): LatLng {
        val zoomLevel = 16.0f //This goes up to 21
        val latLng = LatLng(location.latitude , location.longitude)

        mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , zoomLevel))

        return latLng
    }

    override fun onLocationChanged(p0: Location) {
        positionUpdate(p0)
    }

    fun clean() {
        // Limpiar referencias del círculo
        circle?.remove() // Esto elimina el círculo del mapa
        circle=null

        marker?.remove()
        marker=null

        onMapClickListener=null
        onMapLongClickListener=null

        // Liberar listeners del mapa
        mMap?.setOnMapLongClickListener(null)
        mMap?.setOnMapClickListener(null)
        mMap=null

    }
}