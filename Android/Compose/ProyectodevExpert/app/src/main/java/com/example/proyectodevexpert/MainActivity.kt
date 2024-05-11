package com.example.proyectodevexpert


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }

    @Preview(showBackground = true)
    @Composable
    private fun MainScreen()
    {
        Column{
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
                        .data("https://loremflickr.com/400/400/cat?lock=1")
                        .crossfade(500)//le agrega un efecto a la foto
                        //.transformations(CircleCropTransformation())//Esta es la 2° opcion para hacer circular la imagen
                        .build(),
                    contentDescription = null,
                    modifier=Modifier.fillMaxWidth(),//en este caso la imagen con fillmaxsize no ocupa todo el box
                    contentScale = ContentScale.Crop//Por eso conCrop ensancha la imagen y la recorta
                )

                //si le agrego sobre la imagen un icono predefinidos de Android
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(92.dp)
                        .align(Alignment.Center)
                )

                //si le agrego iconos agregados por mi en la carpeta drawable
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription =null,
                    tint=Color.Green

                )

            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.secondary)
                .padding(16.dp))
            {
                Text(text = "Title1")
            }

        }

    }

    //Greting recibe un Modifier. Sino le envio un por parametro, usa uno por defecto
    //Esto se determina por al hacer =MOdifier en la llamada
    @Composable
    private fun Greeting(name: String, modifier: Modifier = Modifier){

        Text(
            text = "hello $name",
            modifier = modifier
        )
    }
}
