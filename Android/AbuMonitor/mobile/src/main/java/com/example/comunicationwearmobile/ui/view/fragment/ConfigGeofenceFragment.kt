package com.example.comunicationwearmobile.ui.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.view.activities.PropertiesGeofenceActivity
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfigGeofenceFragment(private var radius: Int) : BottomSheetDialogFragment() {
    private var seekBar: SeekBar? = null
    private var cmdConfigArea: Button? = null
    private var lblMetros: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        //cuando el usuario presiona fuera del fragment se ejecuta este metodo
        handleUserExit()
    }



    override fun onStop() {
        super.onStop()
        Log.d(Definition.TAG_DEBUG,"Se destiene fragment")
    }


    override fun onDestroy() {
        super.onDestroy()

        seekBar=null
        cmdConfigArea=null
        lblMetros=null

        Log.d(Definition.TAG_DEBUG,"Se desturye fragment")
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
        cmdConfigArea = contentView.findViewById(R.id.cmdConfigArea)
        lblMetros = contentView.findViewById(R.id.lblMetros)

        seekBar?.setOnSeekBarChangeListener(listenerSeekBar)
        cmdConfigArea?.setOnClickListener(listenerCmdConfig)
        seekBar?.progress = radius

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

    private val listenerCmdConfig =View.OnClickListener {
        val intent=Intent(context, PropertiesGeofenceActivity::class.java)
        startActivity(intent)
    }


    private val listenerSeekBar: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar , progress: Int , b: Boolean) {

            radius = progress
            lblMetros?.text = progress.toString()

            ViewModelManager.sharedViewmodelMapsActivity.updateCircleRadius(radius)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }
    //@Override
    private fun handleUserExit() {

        Log.d(Definition.TAG_DEBUG,"Fragment cerrado por el usuario")
        ViewModelManager.sharedViewmodelMapsActivity.cancelInMap()
    }
}


