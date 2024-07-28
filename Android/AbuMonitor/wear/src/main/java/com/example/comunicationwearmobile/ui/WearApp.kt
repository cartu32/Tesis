package com.example.comunicationwearmobile.ui

import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.MaterialTheme
import com.example.comunicationwearmobile.ui.theme.ComunicationWearMobileTheme

@Composable
fun WearApp(content:@Composable () -> Unit) {
    //aca aplica el theme
    ComunicationWearMobileTheme {
        // A surface container using the 'background' color from the theme

        Box(modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)) {

            content()
        }
    }
}