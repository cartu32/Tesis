package com.example.appbarscaffold.ui.ui.screen.detail

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.appbarscaffold.model.getMedia
import com.example.appbarscaffold.ui.ui.screen.common.Thumb


@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DetailScreen(mediaId: Int?) {
    val mediaItem = remember { getMedia().first { it.id == mediaId } }

    Scaffold(
        topBar = { TopAppBar(title = { Text(text = mediaItem.title) }) }
    ){
        padding->
        Thumb(mediaItem = mediaItem, Modifier.padding(padding))
    }
}

