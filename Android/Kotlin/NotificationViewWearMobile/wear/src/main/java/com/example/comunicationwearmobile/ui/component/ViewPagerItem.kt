@file:OptIn(ExperimentalFoundationApi::class)

package com.example.comunicationwearmobile.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun ViewPagerItem(
    page: Int,
    preferredBackground: Color? = null,
    content: @Composable () -> Unit
) {
    val backgroundColor = preferredBackground ?: Color.White
    Box(
        modifier = Modifier
            .background(color = backgroundColor)
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}