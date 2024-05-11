package com.example.appbarscaffold.ui.ui.screen.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.appbarscaffold.R
import com.example.appbarscaffold.model.MediaItem


@Composable
fun Thumb(mediaItem: MediaItem, modifier: Modifier=Modifier) {
//Box se usa para colocar componentes uno arriba de otro
    Box(
        modifier = modifier
            .height(dimensionResource(id = R.dimen.cell_thum_height))
            .fillMaxWidth()
            .background(color = Color.Red)
    )
    {
        //Con esta biblioteca se carga una imagen
        AsyncImage(
            //con model se indica la url de la imagen
            //modifier=Modifier.clip(CircleShape),//Esta es la 1° opcion para hacer circular la imagen
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaItem.thumb)
                .crossfade(10)//le agrega un efecto a la foto
                //.transformations(CircleCropTransformation())//Esta es la 2° opcion para hacer circular la imagen
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),//en este caso la imagen con fillmaxsize no ocupa todo el box
            contentScale = ContentScale.Crop//Por eso conCrop ensancha la imagen y la recorta
        )
        //Si no es video no se muestra el icono play
        if (mediaItem.type == MediaItem.Type.VIDEO) {

            //si le agrego sobre la imagen un icono predefinidos de Android
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(dimensionResource(id = R.dimen.cell_play_icon_size))
                    .align(Alignment.Center)
            )

        }
    }
}
