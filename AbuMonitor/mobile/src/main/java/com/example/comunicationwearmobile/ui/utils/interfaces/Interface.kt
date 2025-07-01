package com.example.comunicationwearmobile.ui.utils.interfaces

// Defino la interfaz para comunicar el fragment con mapsActivity
interface OnDataSentListenerMapAct {
    fun updateCircleRadius(meters: Double)
}

//defino una interfaz para notificarle a la activty que se
// elecciono un checkbox en el spinner
interface OnCheckboxClickListener {
    fun onCheckboxClicked(position: Int, isChecked: Boolean)
}