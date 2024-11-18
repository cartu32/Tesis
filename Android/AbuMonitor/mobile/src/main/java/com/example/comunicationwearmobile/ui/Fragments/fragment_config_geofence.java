package com.example.comunicationwearmobile.ui.Fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.comunicationwearmobile.Interface.InterfaceConfigGeofence;
import com.example.comunicationwearmobile.presenter.maps.MapsActivtyPresenter;
import com.example.comunicationwearmobile.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;


public class fragment_config_geofence extends BottomSheetDialogFragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final int ACTION_GEOFENCE =1;
    private static final int ACTION_POINT_ROUTE =2;
    private SeekBar seekBar;
    private Button  cmdConfirmar;
    private Button  cmdLimpiar;
    private Button  cmdCalcularRuta;
    private TextView lblMetros;
    private InterfaceConfigGeofence caller=null;
    private float radius;
    private Spinner spSpinner;
    private static int  positionIdArea;

    private int actualAction=0;

    private MapsActivtyPresenter mapsActivtyPresenter;

    // TODO: Rename and change types of parameters
     public fragment_config_geofence(MapsActivtyPresenter mapsActivtyPresenter, float radiusDefault, Boolean EnableGeofenceButton) {
        this.radius= radiusDefault;
        //this.caller=(InterfaceConfigGeofence) activity;
        this.mapsActivtyPresenter = mapsActivtyPresenter;

            if(EnableGeofenceButton==true)
                actualAction=ACTION_GEOFENCE;
            else
                actualAction=ACTION_POINT_ROUTE;
    }


    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
        handleUserExit();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @SuppressLint("RestrictedApi")
    public void setupDialog(Dialog dialog, int style) {
        View contentView = View.inflate(getContext(), R.layout.fragment_config_geofence, null);
        dialog.setContentView(contentView);

        BottomSheetBehavior<View> mBottomSheetBehavior = BottomSheetBehavior.from(((View) contentView
                .getParent()));
        mBottomSheetBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback);
        mBottomSheetBehavior.setPeekHeight(1200);

        seekBar = (SeekBar) contentView.findViewById(R.id.seekBar);
        cmdConfirmar = (Button) contentView.findViewById(R.id.cmdConfirmar);
        cmdLimpiar = (Button) contentView.findViewById(R.id.cmdLimpiar);
        cmdCalcularRuta = (Button) contentView.findViewById(R.id.cmdCalcularRuta);
        lblMetros = (TextView) contentView.findViewById(R.id.lblMetros);
        spSpinner = (Spinner) contentView.findViewById(R.id.spinner);

        seekBar.setOnSeekBarChangeListener(listenerSeekBar);
        cmdConfirmar.setOnClickListener(listenerCmdConfirmar);
        cmdCalcularRuta.setOnClickListener(listenerCmdCalcularRuta);
        cmdLimpiar.setOnClickListener(listenerCmdLimpiar);
        spSpinner.setOnItemSelectedListener(listenerSpinner);
        seekBar.setProgress((int)radius);

        configSpinner();

        if(actualAction==ACTION_GEOFENCE)
            setStateControlGeofences(true);
        else
            setStateControlGeofences(false);



    }

    private void configSpinner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.geofenceAreas, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spSpinner.setAdapter(adapter);

        spSpinner.setSelection(positionIdArea);
    }

    private void setStateControlGeofences(boolean state){
        cmdConfirmar.setEnabled(state);
        spSpinner.setEnabled(state);
        seekBar.setEnabled(state);

        cmdCalcularRuta.setEnabled(!state);
    }
    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new
            BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(View bottomSheet, int newState) {
                    String state = null;

                    switch (newState) {
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            state = "STATE_COLLAPSED";
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            state = "STATE_DRAGGING";
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            state = "STATE_EXPANDED";
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            state = "STATE_SETTLING";
                            break;
                        case BottomSheetBehavior.STATE_HIDDEN:
                            state = "STATE_HIDDEN";
                            //call ALWAYS dismiss to hide the modal background
                            handleUserExit();
                            dismiss();
                            break;
                    }

                    Log.d(fragment_config_geofence.class.getSimpleName(), state);
                }

                @Override
                public void onSlide(View bottomSheet, float slideOffset) {
                    Log.d(fragment_config_geofence.class.getSimpleName(), String.valueOf(slideOffset));
                }
    };

    private Button.OnClickListener listenerCmdConfirmar = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
                mapsActivtyPresenter.generateGeofencesManual();
            }
    };

    private final Button.OnClickListener listenerCmdLimpiar = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mapsActivtyPresenter.clearGeofences();
        }
    };

    private Button.OnClickListener listenerCmdCalcularRuta = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            mapsActivtyPresenter.generateRouteManual();


        }
    };

    private SeekBar.OnSeekBarChangeListener listenerSeekBar = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            if(actualAction!=ACTION_GEOFENCE)
                return;

            radius=(float)progress;

            lblMetros.setText(String.valueOf(progress));

            mapsActivtyPresenter.updateCircleRadius(radius);

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    };

    private AdapterView.OnItemSelectedListener listenerSpinner= new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if(actualAction!=ACTION_GEOFENCE)
                return;

            positionIdArea=position;
            mapsActivtyPresenter.saveSelectedGeofencesArea(parent.getItemAtPosition(position).toString());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }


    };

       //@Override
        private void handleUserExit()
    {

    }

}


