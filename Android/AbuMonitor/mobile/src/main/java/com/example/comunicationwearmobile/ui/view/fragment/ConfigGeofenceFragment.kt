package com.example.comunicationwearmobile.ui.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.view.activities.PropertiesGeofenceActivity
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfigGeofenceFragment() : BottomSheetDialogFragment() {
    private var seekBar: SeekBar? = null
    private var cmdConfigArea: Button? = null
    private var lblMetros: TextView? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var radius: Int =Definition.GEOFENCE_RADIUS_DEFAULT

    var onDismissCallback: (() -> Unit)? = null


    private var viewmodelMapsActivity:ViewmodelMapsActivity?=null



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
        activityResultLauncher=null

        onDismissCallback?.invoke()

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

        initializeViewModel()
        configActivityResult()
    }

    private fun initializeViewModel() {
        // Obtén una instancia del ViewModel usando el GenericViewModelFactory
        val factory = GenericViewModelFactory {
            ViewmodelMapsActivity(requireActivity().application)
        }

        viewmodelMapsActivity = ViewModelProvider(requireActivity(), factory)[ViewmodelMapsActivity::class.java]
    }

    private fun configActivityResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // Obtén los datos del Intent
                val data = result.data

                val itemEvent = data?.getIntExtra("Intent_Event" , 0)
                val itemPriority = data?.getIntExtra("Intent_Priority" , 0)
                val isSecurityZone = data?.getBooleanExtra("Intent_SecurityZone" , false)
                val dwellTime = data?.getIntExtra("Intent_Dweel_Time" , 0)
                val description = data?.getStringExtra("Intent_Description")


                viewmodelMapsActivity?.saveAreaGeofence(
                    itemEvent ,
                    itemPriority ,
                    isSecurityZone ,
                    dwellTime ,
                    description ,
                )
               }
            else{
                viewmodelMapsActivity?.cancelInMap()
            }

            dismiss()

        }
    }

    private val mBottomSheetBehaviorCallback: BottomSheetCallback = object : BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View , newState: Int) {
            var state: String? = null

            when(newState) {
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

                BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    TODO()
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
        activityResultLauncher?.launch(intent)
    }



    private val listenerSeekBar: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        @SuppressLint("SetTextI18n")
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {

            radius = progress
            lblMetros?.text = progress.toString()

            viewmodelMapsActivity?.updateCircleRadius(radius)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }
    //@Override
    private fun handleUserExit() {

        Log.d(Definition.TAG_DEBUG,"Fragment cerrado por el usuario")
        viewmodelMapsActivity?.cancelInMap()
    }
}


