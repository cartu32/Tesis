package com.example.comunicationwearmobile.ui.view.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
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
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.utils.interfaces.OnDataSentListenerMapAct
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class configAssistanceAddFragment: BottomSheetDialogFragment() {

    private var seekBar: SeekBar? = null
    private var cmdActionArea: Button? = null
    private var lblMetros: TextView? = null
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null
    private var radius: Double = Definition.GEOFENCE_RADIUS_DEFAULT

    private val textCmdActionArea ="Guardar cita de asistencia"

    private var listener: OnDataSentListenerMapAct? = null


    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        //cuando el usuario presiona fuera del fragment se ejecuta este metodo
        handleUserExit()


    }


    override fun onStop() {
        super.onStop()
        Log.d(Definition.TAG_DEBUG, "OnStop en fragment")
    }


    override fun onDestroy() {
        super.onDestroy()

        seekBar = null
        cmdActionArea = null
        lblMetros = null
        activityResultLauncher = null
        listener = null


        Log.d(Definition.TAG_DEBUG, "Ondestroy ConfigFragment")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnDataSentListenerMapAct) {
            listener = context
        } else {
            throw RuntimeException("$context debe implementar OnDataSentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        val contentView = View.inflate(context, R.layout.fragment_geofence_config, null)
        dialog.setContentView(contentView)

        val mBottomSheetBehavior = BottomSheetBehavior.from(
            (contentView
                .parent as View)
        )
        mBottomSheetBehavior.peekHeight = 1200


        seekBar = contentView.findViewById(R.id.seekBar)
        cmdActionArea = contentView.findViewById(R.id.cmdActionArea)
        lblMetros = contentView.findViewById(R.id.lblMetros)

        cmdActionArea?.text = textCmdActionArea


        seekBar?.setOnSeekBarChangeListener(listenerSeekBar)
        cmdActionArea?.setOnClickListener(listenerCmdAction)
        seekBar?.progress = radius.toInt()

     }

    private val listenerCmdAction =View.OnClickListener {
        confirmNewAreaGeo()
    }

    private fun confirmNewAreaGeo(){
        val resultConfirm =Activity.RESULT_OK
        val meters: Int = lblMetros?.text.toString().toIntOrNull() ?: 0


        //Envio el objeto EntityAreaGeofence y el resultado(OK o Cancel)
        //al Maps Assistance
        sendDataNewAreaGeoToMapsAssitance(resultConfirm,meters)

        dismiss()


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


    private fun sendDataNewAreaGeoToMapsAssitance(resultCode: Int, meters: Int=-1) {
        //le retorno los datos a la mapsActivtivity(que es la actvity llamador
        val bundle= Bundle().apply {
            putInt(Definition.INTENT_DATA_NEW_AREA_GEOF,meters)
            putInt(Definition.INTENT_STATE_OPERATION,resultCode)
        }

        parentFragmentManager.setFragmentResult(Definition.BUNDLE_FRAGMENT_RESULT_NEW_AREA,bundle)

    }

    //@Override
    private fun handleUserExit() {
        val resultCanceled =Activity.RESULT_CANCELED

        Log.d(Definition.TAG_DEBUG,"Fragment cerrado por el usuario")
        sendDataNewAreaGeoToMapsAssitance(resultCanceled)
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
