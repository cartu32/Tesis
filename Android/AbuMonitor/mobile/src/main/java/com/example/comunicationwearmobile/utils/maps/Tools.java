package com.example.comunicationwearmobile.utils.maps;

import com.google.android.gms.maps.model.LatLng;

public class Tools {

    public static final int GEOFENCE_ROUTE =0;
    public static final int GEOFENCE_TRANSITION=1;
    public static final int OPERATION_UPDATE_ACTIVE_ROUTE=2;
    public static final int OPERATION_CLEAR_ACTIVE_ROUTE=3;
    public static final String DEFAULT_ACTIVE_AREA="Ruta Z";
    private static final String FIRST_PARENTHESIS="(";
    private static final String SECOND_PARENTHESIS=")";
    private static final String COMMA=",";
    public static final String WORD_INITIAL="Ruta ";




    private static  final int OFFSET=1;

    public static synchronized LatLng convertStringToLatLng(String latngString){
        LatLng latng;
        int indexFirstParenthesis=0;
        int indexSecondParenthesis=0;
        int indexComma=0;
        double latitude,longitude;

        indexFirstParenthesis=latngString.indexOf(FIRST_PARENTHESIS);
        indexComma=latngString.indexOf(COMMA, indexFirstParenthesis);
        indexSecondParenthesis =latngString.indexOf(SECOND_PARENTHESIS,indexComma);


        latitude= Double.parseDouble(latngString.substring(indexFirstParenthesis+OFFSET,indexComma));
        longitude= Double.parseDouble(latngString.substring(indexComma+OFFSET,indexSecondParenthesis));

        latng=new LatLng(latitude,longitude);
        return latng;
    }
}
