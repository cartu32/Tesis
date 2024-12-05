package com.example.comunicationwearmobile.presenter.maps.helper

import android.util.Log
import com.example.comunicationwearmobile.Interface.InterfaceConfigGeofence
import com.example.comunicationwearmobile.Interface.RouteMaker
import com.example.comunicationwearmobile.models.maps.RouteApiOSRMJson
import com.example.comunicationwearmobile.models.maps.RoutesList
import com.example.comunicationwearmobile.utils.maps.PolylineDecoder.decode
import com.google.android.gms.maps.model.LatLng
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsRouteHelper(activity: InterfaceConfigGeofence?) {

    companion object {
        private const val TAG = "MapsRoute"

        private const val BASE_URL = "https://router.project-osrm.org/route/v1/foot/"
    }
    private var caller: InterfaceConfigGeofence? = null

    init {
        this.caller = activity
    }

    fun getWaypoints(origin: LatLng , destination: LatLng , idArea: String?) {
        val url: String

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


        val coordenateOrigin = origin.longitude.toString() + "," + origin.latitude
        val coordenateDestination = destination.longitude.toString() + "," + destination.latitude

        url = BASE_URL + coordenateOrigin + ";" + coordenateDestination

        val service = retrofit.create(RouteMaker::class.java)

        val response = service.getAllWayPoints(url)
        response!!.enqueue(object : Callback<RouteApiOSRMJson?> {

            override fun onResponse(call: Call<RouteApiOSRMJson?> , response: Response<RouteApiOSRMJson?>) {
                val route: List<RoutesList>?
                val latLngList: List<LatLng?>?
                val geometry: String?

                if (response.body()!!.code!!.isNotEmpty()) {
                    route = response.body()!!.routes
                    geometry = route!![0].geometry

                    Log.d(TAG , response.body()!!.code!!)
                    latLngList = decode(geometry!!)

                    Log.d(TAG , latLngList.toString())

                    caller!!.graphRoute(latLngList , idArea)
                }            }

            override fun onFailure(call: Call<RouteApiOSRMJson?> , t: Throwable) {
                Log.e(TAG , "Error " + t.message)
                caller!!.showMessage("Error " + t.message)
            }
        })
    }

}

