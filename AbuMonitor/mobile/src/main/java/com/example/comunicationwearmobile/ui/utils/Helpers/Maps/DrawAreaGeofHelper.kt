package com.example.comunicationwearmobile.ui.utils.Helpers.Maps

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.example.abumonitor.constants.Definition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CircleOptions

class DrawAreaGeofHelper(private val mMap: GoogleMap?) {

    private var circlesMap = mutableMapOf<Long?, Circle?>()
    private var currentCircle: Circle?=null

    fun drawGeofenceArea(latitude: Double, longitude: Double, meters: Double = Definition.GEOFENCE_RADIUS_DEFAULT, colorCircle:Int=Definition.TYPE_AREA_COLOR_ERROR): Circle? {
        val latLng= LatLng(latitude,longitude)


        val circleOptions=createCircle(latLng,meters,colorCircle)

        circleOptions.let {
            currentCircle=mMap?.addCircle(it)
        }
        return currentCircle
    }

    fun createCircle(latLng: LatLng, radius: Double, circleBackground: Int): CircleOptions {
        val alpha = 100
        val circleBorder= Color.BLACK

        val circleOptions = CircleOptions()
            .center(latLng)
            .strokeColor(circleBorder)
            .fillColor(ColorUtils.setAlphaComponent(circleBackground, alpha))
            .radius(radius)
            .strokeWidth(4f)
            .clickable(true)
        return circleOptions
    }

    fun cancelDrawnArea(){
        currentCircle?.remove()
    }

    fun updateCircleRadius(meters: Double) {
        currentCircle?.radius= meters
    }

    fun removeAllCircle(){
        circlesMap.values.forEach{it?.remove()} // Elimina los círculos del mapa
        circlesMap.clear() // Limpia todas las referencias del Map

    }

    fun deleteCircleWithId(id: Long) {
        // Obtén el círculo del mapa
        val circle = circlesMap[id]
        // Elimínalo visualmente del mapa
        circle?.remove()
        // Elimina la referencia del mapa interno
        circlesMap.remove(id)
    }


     fun setIdDrawnArea(idCircle: Long) {
        currentCircle?.let {
            currentCircle?.tag = idCircle
            addCircleInList(it)
        }
    }

    fun addCircleInList(circleWithId:Circle){
        val id:Long

        id= circleWithId.tag as Long
        circlesMap[id]=circleWithId
    }

    fun freeResources(){
        currentCircle?.remove()
        currentCircle=null
    }

    fun changeColorCircle(color:Int){
        val alpha=100

        currentCircle?.fillColor = ColorUtils.setAlphaComponent(color, alpha)
    }
}