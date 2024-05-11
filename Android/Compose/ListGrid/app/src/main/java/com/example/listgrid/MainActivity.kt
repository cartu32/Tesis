package com.example.listgrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults.contentPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImage
import coil.request.ImageRequest

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            //MediaListItem
            MediaList()
        }
    }
}

@Preview (showBackground = true)
@Composable
fun MediaList(){
//Este codigo arma el listado como columna el Lazy
/*    LazyColumn(
      contentPadding = PaddingValues(4.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp)
  ) {
          //items(100){
          //En vez de un N° a item le pasamos un listado como parametro
           items(getMedia()){item->
                MediaListItem(item, Modifier.padding(2.dp))
           }
  }
*/
    //Este codigo arma como grillas el listado con Lazy
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 200.dp)
    ) {
    //items(100){
        //En vez de un N° a item le pasamos un listado como parametro
        items(getMedia()){item->
            MediaListItem(item, Modifier.padding(2.dp))
        }
    }

}

//@Preview(showBackground = true)
@Composable
private fun MediaListItem(item: MediaItem, modifier: Modifier=Modifier)
{
    Column(
        modifier=modifier
    ){
        //Box se usa para colocar componentes uno arriba de otro
        Box(modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .background(color = Color.Red)
        )
        {
            //Con esta biblioteca se carga una imagen
            AsyncImage(
                //con model se indica la url de la imagen
                //modifier=Modifier.clip(CircleShape),//Esta es la 1° opcion para hacer circular la imagen
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.thumb)
                    .crossfade(10)//le agrega un efecto a la foto
                    //.transformations(CircleCropTransformation())//Esta es la 2° opcion para hacer circular la imagen
                    .build(),
                contentDescription = null,
                modifier=Modifier.fillMaxWidth(),//en este caso la imagen con fillmaxsize no ocupa todo el box
                contentScale = ContentScale.Crop//Por eso conCrop ensancha la imagen y la recorta
            )
            //Si no es video no se muestra el icono play
            if(item.type == MediaItem.Type.VIDEO) {

                //si le agrego sobre la imagen un icono predefinidos de Android
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(92.dp)
                        .align(Alignment.Center)
                )

            }
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp))
        {
            Text(text = item.title)
        }

    }

}
