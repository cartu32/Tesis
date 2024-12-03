package com.example.comunicationwearmobile.utils.maps

import com.google.android.gms.maps.model.LatLng

object Tools {
    const val GEOFENCE_ROUTE: Int = 0
    const val GEOFENCE_TRANSITION: Int = 1
    const val OPERATION_UPDATE_ACTIVE_ROUTE: Int = 2
    const val OPERATION_CLEAR_ACTIVE_ROUTE: Int = 3
    const val DEFAULT_ACTIVE_AREA: String = "Ruta Z"
    private const val FIRST_PARENTHESIS = "("
    private const val SECOND_PARENTHESIS = ")"
    private const val COMMA = ","
    const val WORD_INITIAL: String = "Ruta "


    private const val OFFSET = 1

    @JvmStatic
    @Synchronized
    fun convertStringToLatLng(latngString: String): LatLng {
        val latng: LatLng
        val indexFirstParenthesis: Int
        val indexSecondParenthesis: Int
        val indexComma: Int

        indexFirstParenthesis = latngString.indexOf(FIRST_PARENTHESIS)
        indexComma = latngString.indexOf(COMMA , indexFirstParenthesis)
        indexSecondParenthesis = latngString.indexOf(SECOND_PARENTHESIS , indexComma)


        val latitude = latngString.substring(indexFirstParenthesis + OFFSET , indexComma).toDouble()
        val longitude =
            latngString.substring(indexComma + OFFSET , indexSecondParenthesis).toDouble()

        latng = LatLng(latitude , longitude)
        return latng
    }
}
