package com.example.comunicationwearmobile.ui.view.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.comunicationwearmobile.R

class LoginDialogFragmentDialogFragment(): DialogFragment() {

    interface LoginListener {
        fun onLogin(username: String, password: String)
    }

    private var listener: LoginListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? LoginListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(),android.R.style.Theme_Material_Light_Dialog_Alert)
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_dialog_login, null)

        val editTextUsername = view.findViewById<EditText>(R.id.editTextUsername)
        val editTextPassword = view.findViewById<EditText>(R.id.editTextPassword)
        val buttonLogin = view.findViewById<Button>(R.id.buttonLogin)
        val buttonCancel = view.findViewById<Button>(R.id.buttonCancel)

        val dialog = builder.setView(view).create()

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            listener?.onLogin(username, password)
            dialog.dismiss()
        }

        buttonCancel.setOnClickListener {
            dialog.dismiss()
        }

        return dialog
    }
}