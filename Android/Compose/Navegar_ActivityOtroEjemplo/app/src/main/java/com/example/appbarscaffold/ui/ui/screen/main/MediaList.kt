package com.example.appbarscaffold.ui.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavHostController
import com.example.appbarscaffold.R
import com.example.appbarscaffold.model.MediaItem
import com.example.appbarscaffold.model.getMedia
import com.example.appbarscaffold.ui.ui.screen.common.Thumb


@Composable
fun MediaList(navController: NavHostController, modifier: Modifier = Modifier ){
    //Este codigo arma como grillas el listado con Lazy
    LazyVerticalGrid(
        columns = GridCells.Adaptive(
            minSize = dimensionResource(R.dimen.cell_min_weidth))
    ) {
        //items(100){
        //En vez de un N° a item le pasamos un listado como parametro
        items(getMedia()){ item->
            MediaListItem(
                mediaItem = item,
                navController,
                modifier= Modifier.padding(dimensionResource(id = R.dimen.padding_xsamll))
            )
        }
    }

}

//@Preview(showBackground = true)
@Composable
private fun MediaListItem(
    mediaItem: MediaItem,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { navController.navigate("detail/${mediaItem.id}") }//Aca digo que cuando presione algun item del listado, se abra la activity detail
    ) {
        Thumb(mediaItem)
        Title(mediaItem)

    }

}

@Composable
fun Title(mediaItem: MediaItem) {
    Box(
        contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(dimensionResource(id = R.dimen.padding_medium))
    )
    {
        Text(text = mediaItem.title)
    }
}
