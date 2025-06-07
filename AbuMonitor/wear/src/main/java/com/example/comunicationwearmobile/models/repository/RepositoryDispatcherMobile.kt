package com.example.comunicationwearmobile.models.repository

import android.content.Context
import android.content.Intent
import com.example.comunicationwearmobile.utils.services.SenderToMobileService
import com.example.shared_library.SharedData

object RepositoryDispatcherMobile {

    fun sendMessageMobile(context: Context , path:String , body: ByteArray?){
        val serviceIntent = Intent(context , SenderToMobileService::class.java).apply {
            putExtra(SharedData.ParamIntent.MESSAGE_PATH.name , path)
            putExtra(SharedData.ParamIntent.MESSAGE_BODY.name , body)
        }
        context.startService(serviceIntent)
    }

}