package com.example.comunicationwearmobile.ui.Fragments

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Spinner
import android.widget.TextView
import com.example.comunicationwearmobile.Interface.InterfaceConfigGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.presenter.maps.MapsPresenter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfigGeofenceFragment(
    private val mapsActivtyPresenter: MapsPresenter ,
    private var radius: Float ,
    EnableGeofenceButton: Boolean
) : BottomSheetDialogFragment() {
    private var seekBar: SeekBar? = null
    private var cmdConfirmar: Button? = null
    private var cmdLimpiar: Button? = null
    private var cmdCalcularRuta: Button? = null
    private var lblMetros: TextView? = null
    private val caller: InterfaceConfigGeofence? = null
    private var spSpinner: Spinner? = null
    private var actualAction = 0

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        handleUserExit()
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog , style: Int) {
        val contentView = View.inflate(context , R.layout.fragment_config_geofence , null)
        dialog.setContentView(contentView)

        val mBottomSheetBehavior = BottomSheetBehavior.from(
            (contentView
                .parent as View)
        )
        mBottomSheetBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        mBottomSheetBehavior.peekHeight = 1200

        seekBar = contentView.findViewById(R.id.seekBar)
        cmdConfirmar = contentView.findViewById(R.id.cmdConfirmar)
        cmdLimpiar = contentView.findViewById(R.id.cmdLimpiar)
        cmdCalcularRuta = contentView.findViewById(R.id.cmdCalcularRuta)
        lblMetros = contentView.findViewById(R.id.lblMetros)
        spSpinner = contentView.findViewById(R.id.spinner)

        seekBar?.setOnSeekBarChangeListener(listenerSeekBar)
        cmdConfirmar?.setOnClickListener(listenerCmdConfirmar)
        cmdCalcularRuta?.setOnClickListener(listenerCmdCalcularRuta)
        cmdLimpiar?.setOnClickListener(listenerCmdLimpiar)
        spSpinner?.onItemSelectedListener = listenerSpinner
        seekBar?.progress = radius.toInt()

        configSpinner()

        setStateControlGeofences(actualAction == ACTION_GEOFENCE)
    }

    private fun configSpinner() {
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext() ,
            R.array.geofenceAreas , android.R.layout.simple_spinner_item
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spSpinner!!.adapter = adapter

        spSpinner!!.setSelection(positionIdArea)
    }

    private fun setStateControlGeofences(state: Boolean) {
        cmdConfirmar!!.isEnabled = state
        spSpinner!!.isEnabled = state
        seekBar!!.isEnabled = state

        cmdCalcularRuta!!.isEnabled = !state
    }

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View , newState: Int) {
            var state: String? = null

            when (newState) {
                BottomSheetBehavior.STATE_COLLAPSED -> state = "STATE_COLLAPSED"
                BottomSheetBehavior.STATE_DRAGGING -> state = "STATE_DRAGGING"
                BottomSheetBehavior.STATE_EXPANDED -> state = "STATE_EXPANDED"
                BottomSheetBehavior.STATE_SETTLING -> state = "STATE_SETTLING"
                BottomSheetBehavior.STATE_HIDDEN -> {
                    state = "STATE_HIDDEN"
                    //call ALWAYS dismiss to hide the modal background
                    handleUserExit()
                    dismiss()
                }
            }
            Log.d(ConfigGeofenceFragment::class.java.simpleName , state!!)
        }

        override fun onSlide(bottomSheet: View , slideOffset: Float) {
            Log.d(ConfigGeofenceFragment::class.java.simpleName , slideOffset.toString())
        }
    }

    private val listenerCmdConfirmar =
        View.OnClickListener { mapsActivtyPresenter.generateGeofencesManual() }

    private val listenerCmdLimpiar = View.OnClickListener { mapsActivtyPresenter.clearGeofences() }

    private val listenerCmdCalcularRuta =
        View.OnClickListener { mapsActivtyPresenter.generateRouteManual() }

    private val listenerSeekBar: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar , progress: Int , b: Boolean) {
            if (actualAction != ACTION_GEOFENCE) return

            radius = progress.toFloat()

            lblMetros!!.text = progress.toString()

            mapsActivtyPresenter.updateCircleRadius(radius)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }

    private val listenerSpinner: AdapterView.OnItemSelectedListener =
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*> ,
                view: View ,
                position: Int ,
                id: Long
            ) {
                if (actualAction != ACTION_GEOFENCE) return

                positionIdArea = position
                mapsActivtyPresenter.saveSelectedGeofencesArea(
                    parent.getItemAtPosition(position).toString()
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    // TODO: Rename and change types of parameters
    init {
        actualAction = if (EnableGeofenceButton) ACTION_GEOFENCE
        else ACTION_POINT_ROUTE
    }


    //@Override
    private fun handleUserExit() {
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ACTION_GEOFENCE = 1
        private const val ACTION_POINT_ROUTE = 2
        private var positionIdArea = 0
    }
}


