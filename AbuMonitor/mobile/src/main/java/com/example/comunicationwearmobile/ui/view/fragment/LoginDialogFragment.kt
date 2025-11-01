package com.example.comunicationwearmobile.ui.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.abumonitor.constants.Definition
import com.example.comunicationwearmobile.R
import com.example.comunicationwearmobile.ui.common.SharedVariables

class LoginDialogFragmentDialogFragment(): DialogFragment() {

    interface LoginListener {
        fun onLogin(username: String, password: String)
    }

    private var listener: LoginListener? = null

    private var editTextUsername:EditText? = null
    private var editTextPassword:EditText?=null
    private var buttonLogin:Button?=null
    private var buttonCancel:Button?=null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? LoginListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(),android.R.style.Theme_Material_Light_Dialog_Alert)
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_dialog_login, null)

        editTextUsername = view.findViewById<EditText>(R.id.editTextUsername)
        editTextPassword = view.findViewById<EditText>(R.id.editTextPassword)
        buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = builder.setView(view).create()

        buttonLogin?.setOnClickListener {listenerLogin()}

        buttonCancel?.setOnClickListener { listenerCancel() }

        return dialog
    }

    private fun listenerCancel() {
        dialog?.dismiss()
    }

    private fun listenerLogin() {
        val username = editTextUsername?.text.toString().trim()
        val password = editTextPassword?.text.toString().trim()
        listener?.onLogin(username, password)

        if (username == SharedVariables.USER_ADMIN && password == SharedVariables.password) {
            dialog?.dismiss()
        }else{
            Toast.makeText(requireContext(), "Usuario/contraseña incorrecta", Toast.LENGTH_SHORT).show()
        }
    }
}