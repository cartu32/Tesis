package com.example.comunicationwearmobile.ui.Activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentActivity
import com.example.comunicationwearmobile.Interface.InterfaceConfigGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.presenter.maps.MapsActivityPresenter
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
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsActivity : FragmentActivity() , OnMapReadyCallback , OnMapLongClickListener ,
    OnMapClickListener , LocationListener , InterfaceConfigGeofence {
    private var mMap: GoogleMap? = null
    private var alert: AlertDialog? = null
    private var mapsActivtyPresenter: MapsActivityPresenter? = null
    private var circle: Circle? = null
    private var geoFenceMarker: MarkerOptions? = null

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


        mapsActivtyPresenter = MapsActivityPresenter(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
            this
        )
        if (resultCode != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(
                this , resultCode , RC_HANDLE_GMS
            )
            dlg?.show()
        }

        mMap!!.setOnMapLongClickListener(this)
        mMap!!.setOnMapClickListener(this)

        mapsActivtyPresenter!!.checkPermisson()

        //elimino todos los geofences que pudieron haber quedado cargados, si la aplicacion se cerro
        // anteriormente por la ocurrencia de algun error.
        mapsActivtyPresenter!!.clearGeofenceMaps()
    }

    override fun showMessage(message: String?) {
        Toast.makeText(this , message , Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int ,
        permissions: Array<String> ,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode , permissions , grantResults)
        mapsActivtyPresenter!!.onRequestPermissionsResult(requestCode , permissions , grantResults)
    }


    val location: Location?
        get() = mapsActivtyPresenter?.getLocation()


    override fun onLocationChanged(location: Location) {
        val latLng = positionUpdate(location)

        mapsActivtyPresenter!!.checkGeofenceRoute(latLng)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(s: String , i: Int , bundle: Bundle) {
    }

    override fun onProviderEnabled(s: String) {
    }

    override fun onProviderDisabled(s: String) {
    }

    fun positionUpdate(location: Location): LatLng {
        val zoomLevel = 16.0f //This goes up to 21
        val latLng = LatLng(location.latitude , location.longitude)

        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng , zoomLevel))

        return latLng
    }


    fun enableMap() {
        if (ActivityCompat.checkSelfPermission(
                this ,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this , Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.uiSettings.setAllGesturesEnabled(true)
        mMap!!.uiSettings.isMyLocationButtonEnabled = true
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.uiSettings.isMapToolbarEnabled = true

        location
    }

    private fun addMarker(latLng: LatLng?) {
        geoFenceMarker = MarkerOptions().position(latLng!!)
        mMap!!.addMarker(geoFenceMarker!!)
    }

    override fun addMarkerGeofence(latLng: LatLng? , radius: Float) {
        addMarker(latLng)
        addCircle(latLng , radius)
    }


    private fun addCircle(latLng: LatLng? , radius: Float) {
        val alpha = 64

        val colorCircle = mapsActivtyPresenter!!.determineColor()

        this.circle = mMap!!.addCircle(
            CircleOptions()
                .center(latLng!!)
                .strokeColor(colorCircle)
                .fillColor(ColorUtils.setAlphaComponent(colorCircle , alpha))
                .radius(radius.toDouble())
                .strokeWidth(4f)
        )
    }


    override fun onMapLongClick(latLng: LatLng) {
        handleMapLongClick(latLng)
    }


    fun alertNoGps() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("El sistema GPS esta desactivado, ¿Desea activarlo?")
            .setCancelable(false)
            .setPositiveButton("Si") { dialog , id ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                mapsActivtyPresenter!!.setPositionGPS()
            }
            .setNegativeButton("No") { dialog , id -> dialog.cancel() }
        alert = builder.create()
        alert!!.show()
    }

    override fun onDestroy() {
        super.onDestroy()

        mapsActivtyPresenter!!.clearGeofenceMaps()

        if (alert != null) {
            alert!!.dismiss()
        }
    }

    override fun clearMaps() {
        mMap!!.clear()
        circle = null
    }


    override fun updateCircleRadiusGraphic(geofenceRadius: Float) {
        circle!!.radius = geofenceRadius.toDouble()
    }

    override fun updateCircleColorGraphic(id: String?) {
        val alpha = 64

        val colorCircle = mapsActivtyPresenter!!.determineColor()

        circle!!.strokeColor = colorCircle
        circle!!.fillColor = ColorUtils.setAlphaComponent(colorCircle , alpha)
    }


    override fun graphRoute(coordenateList: List<LatLng?>? , idArea: String?) {
        mMap!!.addPolyline(PolylineOptions().addAll(coordenateList!!))

        mapsActivtyPresenter!!.saveRouteInFile(coordenateList , idArea.toString())
    }

    private fun handleMapLongClick(latLng: LatLng) {
        mapsActivtyPresenter!!.addAreaGeofenceInList(latLng)
    }

    override fun onMapClick(latLng: LatLng) {
        mapsActivtyPresenter!!.markRoutePoint(latLng)
    }

    companion object {
        private const val TAG = "MapsActivity"

        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001
    }
}

