package com.example.layout

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.layout.ui.theme.LayoutTheme
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreenBox()
        }
    }

}


@Composable
fun MainScreenRow() {
    Row(modifier = Modifier.fillMaxSize() ,
        horizontalArrangement =  Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    )
    {
        Text(text = "Antonio", modifier=Modifier.background(Color.LightGray))
        Text(text = "Android", modifier=Modifier.background(Color.Yellow))
        Text(text = "Windows", modifier=Modifier.background(Color.Green))
    }

}


@Composable
fun MainScreenColumn() {
    Column(modifier = Modifier.fillMaxSize(),
           verticalArrangement =  Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally)
    {
        Text(text = "Antonio", modifier=Modifier.background(Color.LightGray))
        Text(text = "Android", modifier=Modifier.background(Color.Yellow))
        Text(text = "Windows", modifier=Modifier.background(Color.Green))
    }

}

@Composable
fun MainScreenBox()
{
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {
        Text(text = "Antonio", modifier = Modifier.align(Alignment.BottomEnd))
        Text(text = "Android")
    }

}


@Preview(showBackground = true, widthDp =  400, heightDp = 200)
@Composable
fun MainScreen()
{
//    MainScreenBox()
    //MainScreenColumn()
    MainScreenRow()

}


