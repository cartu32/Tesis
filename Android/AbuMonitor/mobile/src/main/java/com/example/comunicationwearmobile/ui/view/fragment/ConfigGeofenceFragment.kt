package com.example.comunicationwearmobile.ui.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.interfaces.OnDataSentListenerMapAct
import com.example.comunicationwearmobile.ui.view.activities.PropertiesGeofenceActivity
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfigGeofenceFragment() : BottomSheetDialogFragment() {
    private var seekBar: SeekBar? = null
    private var cmdConfigArea: Button? = null
    private var lblMetros: TextView? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var radius: Double =Definition.GEOFENCE_RADIUS_DEFAULT

    private var listener: OnDataSentListenerMapAct? = null




    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        //cuando el usuario presiona fuera del fragment se ejecuta este metodo
        handleUserExit()


    }



    override fun onStop() {
        super.onStop()
        Log.d(Definition.TAG_DEBUG,"OnStop en fragment")
    }


    override fun onDestroy() {
        super.onDestroy()

        seekBar=null
        cmdConfigArea=null
        lblMetros=null
        activityResultLauncher=null
        listener=null


        Log.d(Definition.TAG_DEBUG,"Ondestroy ConfigFragment")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDataSentListenerMapAct){
            listener=context
        }else {
            throw RuntimeException("$context debe implementar OnDataSentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener=null
    }
    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog , style: Int) {
        val contentView = View.inflate(context , R.layout.fragment_config_geofence , null)
        dialog.setContentView(contentView)

        val mBottomSheetBehavior = BottomSheetBehavior.from(
            (contentView
                .parent as View)
        )
        mBottomSheetBehavior.peekHeight = 1200


        seekBar = contentView.findViewById(R.id.seekBar)
        cmdConfigArea = contentView.findViewById(R.id.cmdConfigArea)
        lblMetros = contentView.findViewById(R.id.lblMetros)

        seekBar?.setOnSeekBarChangeListener(listenerSeekBar)
        cmdConfigArea?.setOnClickListener(listenerCmdConfig)
        seekBar?.progress = radius.toInt()

        configActivityResult()
    }


    private fun configActivityResult() {
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Obtén los datos del Intent
            val data = result.data
            var dataNewAreaGeof:EntityAreaGeofence?=null

            //obtengo el objeto EntityAreaGeofence con
            //los datos que se ingresaron en la activty Properties
            dataNewAreaGeof=data?.let {
                extractDataNewAreaOfIntent(it)
            }

            //le agrego los metros al objeto EntityAreaGeofence
            dataNewAreaGeof?.meters=lblMetros?.text.toString().toInt()

            //Envio el objeto EntityAreaGeofence y el resultado(OK o Cancel)
            //al Maps Activty
            sendDataNewAreaGeoToMapsActivty(result.resultCode,dataNewAreaGeof)

            dismiss()

        }
    }

    private fun sendDataNewAreaGeoToMapsActivty(resultCode: Int, dataNewAreaGeof: EntityAreaGeofence?=null) {
        //le retorno los datos a la mapsActivtivity(que es la actvity llamador
        val bundle=Bundle().apply {
            putParcelable(Definition.INTENT_DATA_NEW_AREA_GEOF,dataNewAreaGeof)
            putInt(Definition.INTENT_STATE_OPERATION,resultCode)
        }

        parentFragmentManager.setFragmentResult(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA,bundle)

    }

    private fun extractDataNewAreaOfIntent(data:Intent): EntityAreaGeofence? {
        //Recibo los datos desde la activty PropertiesGeofence Activty
        val dataNewAreaGeof = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            data.getParcelableExtra<EntityAreaGeofence>(Definition.INTENT_DATA_NEW_AREA_GEOF,EntityAreaGeofence::class.java)
        } else {
            data.getParcelableExtra<EntityAreaGeofence>(Definition.INTENT_DATA_NEW_AREA_GEOF)
        }
        return dataNewAreaGeof
    }

    private val listenerCmdConfig =View.OnClickListener {
        val intent=Intent(context, PropertiesGeofenceActivity::class.java)
        activityResultLauncher?.launch(intent)
    }



    private val listenerSeekBar: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        @SuppressLint("SetTextI18n")
        override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {


            radius = progress.toDouble()
            lblMetros?.text = progress.toString()

            //le envio el radio a mapsActivty para actualizar el circulo en tiempo real.
            //No se lo envio como rerturn porque debe ser actualizado en tiempo real
            listener?.updateCircleRadius(radius)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar) {
        }
    }
    //@Override
    private fun handleUserExit() {
        val resultCanceled:Int=0

        Log.d(Definition.TAG_DEBUG,"Fragment cerrado por el usuario")
        sendDataNewAreaGeoToMapsActivty(resultCanceled)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        val fragment = activity?.supportFragmentManager?.findFragmentByTag("configGeofenceFragment")
        if (fragment is BottomSheetDialogFragment) {
            activity?.supportFragmentManager?.beginTransaction()?.remove(fragment)?.commitAllowingStateLoss()
            Log.d(Definition.TAG_DEBUG,"Se forzo la eliminacion del fragment en Ondismiss")
        }

    }
}





