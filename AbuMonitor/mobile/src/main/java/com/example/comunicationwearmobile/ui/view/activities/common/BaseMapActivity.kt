package com.example.comunicationwearmobile.ui.view.activities.common

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.Helpers.Maps.DrawAreaGeofHelper
import com.example.comunicationwearmobile.ui.utils.Tools
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

//Esta es la clase padre que se usa para crear todos los mapas
abstract class BaseMapActivity : AppCompatActivity() , OnMapReadyCallback, OnMapLongClickListener,
    OnMapClickListener, GoogleMap.OnCircleClickListener {


    var mMap: GoogleMap? = null
    private val RC_HANDLE_GMS = 9001

    var txtAddress:EditText?=null
    var btnSearch:Button?=null
    var txtLeyends: TextView?=null

    private lateinit var geocoder: Geocoder

    var drawAreaGeofHelper: DrawAreaGeofHelper?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_base)

        Tools.desactiveStrictMode()

        initComponents()
        initGooglePlayServices()
        initMap()

        //instancio la clase para usar el helper que dibuja en el mapa
        drawAreaGeofHelper= DrawAreaGeofHelper(mMap)

    }

    open fun initComponents(){
        txtAddress=findViewById<EditText>(R.id.txtAdress)
        btnSearch=findViewById<Button>(R.id.cmdSearch)
        txtLeyends=findViewById<TextView>(R.id.txtLeyends)

        btnSearch?.setOnClickListener{listenerClickCmdSerach()}
    }


    fun listenerClickCmdSerach() {
        val addressText = txtAddress?.text.toString()
        if (addressText.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                clickCmdSerachAndroid13(addressText)
            } else {
                clickCmdSerachAndroid12(addressText)
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun clickCmdSerachAndroid13(addressText: String) {
        geocoder.getFromLocationName(addressText, 1, object : Geocoder.GeocodeListener {
            override fun onGeocode(addresses: MutableList<Address>) {
                if (addresses.isNotEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                } else {
                    Toast.makeText(this@BaseMapActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onError(errorMessage: String?) {
                Toast.makeText(this@BaseMapActivity, "Error: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun clickCmdSerachAndroid12(addressText: String){
        lifecycleScope.launch {
            try {
                @Suppress("DEPRECATION")
                val addresses = withContext(Dispatchers.IO) {
                    geocoder.getFromLocationName(addressText, 1)
                }

                if (!addresses.isNullOrEmpty()) {
                    val location = addresses[0]
                    val latLng = LatLng(location.latitude, location.longitude)
                    mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                } else {
                    Toast.makeText(this@BaseMapActivity, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@BaseMapActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initGooglePlayServices(){
         val resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)

         if (resultCode != ConnectionResult.SUCCESS) {
             val dlg = resultCode.let {
                 GoogleApiAvailability.getInstance().getErrorDialog(this, it, RC_HANDLE_GMS)
             }
             dlg?.show()
             Log.e("MAP_DEBUG", "Google Play Services no está disponible. Código: $resultCode")

         }

     }
    private fun initMap() {
        try {
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            if (mapFragment == null) {
                Log.e(Definition.TAG_DEBUG, "No se pudo encontrar el fragmento del mapa.")
                return
            }

            mapFragment.getMapAsync(this@BaseMapActivity)
            
            geocoder = Geocoder(this, Locale.getDefault())

        } catch (e: Exception) {
            Log.e(Definition.TAG_DEBUG, "Error al inicializar Google Maps: ${e.message}")
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        drawAreaGeofHelper = DrawAreaGeofHelper(mMap)
        mMap?.setOnCircleClickListener(this)

        enableFeatureMaps()
    }


    private fun enableFeatureMaps() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this,"No hay permisos de ACCESS_FINE_LOCATION oACCESS_COARSE_LOCATION ",Toast.LENGTH_LONG).show()
            return
        }
        mMap?.isMyLocationEnabled = true

        mMap?.uiSettings?.setAllGesturesEnabled(true)
        mMap?.uiSettings?.isMyLocationButtonEnabled = true
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.uiSettings?.isMapToolbarEnabled = true

        changeLocationButtonGps()
    }

    fun changeLocationButtonGps() {
        try {
            val mapView = (supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment).view
            val locationButton = mapView?.findViewWithTag<View>("GoogleMapMyLocationButton")
            locationButton?.let {
                val layoutParams = it.layoutParams as RelativeLayout.LayoutParams

                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, 0) // quitar esquina derecha
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE) // esquina izquierda

                val scale = resources.displayMetrics.density
                val marginHorizontal = (200 * scale + 0.5f).toInt() // margen desde izquierda (dp)
                val marginBottom = (30 * scale + 0.5f).toInt() // margen inferior

                layoutParams.setMargins(marginHorizontal, 0, 0, marginBottom)
                it.layoutParams = layoutParams
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    override fun onMapClick(latLng: LatLng) {

    }

    override fun onMapLongClick(latLng: LatLng) {

    }


    override fun onCircleClick(circle: Circle) {

    }




    open fun cleanMap(){
        mMap?.clear()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Limpiar referencias del círculo
        mMap?.setOnCircleClickListener(null)
        mMap=null


    }


}
