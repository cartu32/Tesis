package com.example.comunicationwearmobile.ui.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.ViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.view.activities.MapsActivity
import com.example.comunicationwearmobile.ui.view.activities.PropertiesGeofenceActivity
import com.example.comunicationwearmobile.ui.viewmodel.ViewModelManager
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfigGeofenceFragment(
    private var radius: Int ,
    EnableGeofenceButton: Boolean
) : BottomSheetDialogFragment() {
    private var seekBar: SeekBar? = null
    private var cmdConfigArea: Button? = null
    private var lblMetros: TextView? = null
    private var actualAction = 0

//    private lateinit var viewmodelMapsActivity: ViewmodelMapsActivity
 //   private lateinit var factory: ViewModelFactory

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        handleUserExit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            if (actualAction != ACTION_GEOFENCE)
                return

            radius = progress

            lblMetros?.text = progress.toString()

            ViewModelManager.sharedViewmodelMapsActivity.updateCircleRadius(radius)
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
              /*  mapsActivtyPresenter.saveSelectedGeofencesArea(
                    parent.getItemAtPosition(position).toString()
                )*/
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

    // TODO: Rename and change types of parameters
    init {
        actualAction = if (EnableGeofenceButton) ACTION_GEOFENCE
        else ACTION_POINT_ROUTE
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Definition.TAG_DEBUG,"Se desturye fragment")
    }

    override fun onStop() {
        super.onStop()
        Log.d(Definition.TAG_DEBUG,"Se destiene fragment")
    }

    //@Override
    private fun handleUserExit() {

        Log.d(Definition.TAG_DEBUG,"Fragment cerrado por el usuario")
        ViewModelManager.sharedViewmodelMapsActivity.cancelInMap()
    }

    companion object {
        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private const val ACTION_GEOFENCE = 1
        private const val ACTION_POINT_ROUTE = 2
        private var positionIdArea = 0
    }
}


