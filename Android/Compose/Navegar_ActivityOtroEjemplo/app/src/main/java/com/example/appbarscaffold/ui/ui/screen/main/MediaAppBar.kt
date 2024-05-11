package com.example.appbarscaffold.ui.ui.screen.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.example.appbarscaffold.R


/*************************************************************
2° Optimizazión: Dividir el Composable en otros más pequeños
 **************************************************************
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar() {
    TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        actions={
            AppBarAction(
                imageVector = Icons.Default.Search,
                onclick = { /*TODO*/ }
            )
            AppBarAction(
                imageVector = Icons.Default.Share,
                onclick = { /*TODO*/ }
            )

        }
    )
}

@Composable
private fun AppBarAction(
    imageVector: ImageVector,
    onclick: () -> Unit
    )
    {
    IconButton(onClick = { /*TODO*/ }) {
        Icon(
            imageVector = imageVector,
            contentDescription = null
        )
    }
}

