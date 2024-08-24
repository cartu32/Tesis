package com.example.heartrate.presentation.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.example.heartrate.R
import com.example.heartrate.presentation.ui.theme.HeartRateTheme

class MainScreen {

    @Composable
    fun WearApp(valueHeartRate: Int) {
        HeartRateTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background) ,
                contentAlignment = Alignment.Center
            ) {
                showHeartRate(valueHeartRate = 0)
            }
        }
    }

    @Composable
    fun showHeartRate(valueHeartRate: Int) {
        Text(
            modifier = Modifier.fillMaxWidth() ,
            textAlign = TextAlign.Center ,
            color = MaterialTheme.colors.primary ,
            text = stringResource(R.string.valueHeartRate , valueHeartRate)
        )
    }

    @Preview(device = Devices.WEAR_OS_SMALL_ROUND , showSystemUi = true)
    @Composable
    fun DefaultPreview() {
        WearApp(0)
    }
}