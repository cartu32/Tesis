package com.example.comunicationwearmobile.ui.ui.view.activities

import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import com.example.comunicationwearmobile.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : FragmentActivity() , OnMapReadyCallback , OnMapLongClickListener ,
    OnMapClickListener , LocationListener  {
    private lateinit var mMap: GoogleMap
    private lateinit var alert: AlertDialog
    private lateinit var circle: Circle
    private lateinit var geoFenceMarker: MarkerOptions

    companion object {
        private const val TAG = "MapsActivity"

        // intent request code to handle updating play services if needed.
        private const val RC_HANDLE_GMS = 9001
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)


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

        mMap.setOnMapLongClickListener(this)
        mMap.setOnMapClickListener(this)

    }

    override fun onMapLongClick(p0: LatLng) {
        TODO("Not yet implemented")
    }

    override fun onMapClick(p0: LatLng) {
        TODO("Not yet implemented")
    }

    override fun onLocationChanged(location: Location) {
        TODO("Not yet implemented")
    }


}

