package com.example.comunicationwearmobile.ui.screen.main


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import com.example.comunicationwearmobile.ui.component.ViewPagerDotsIndicator
import com.example.comunicationwearmobile.ui.component.ViewPagerItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**************************************************************************************
 ************************** FUNCIONES QUE CREAN LA VIEW********************************
 **************************************************************************************
 */
@Composable/*
fun MsgAlertScreen(alertsViewModel: AlertsViewModel= viewModel<AlertsViewModel>()) {
    val state=alertsViewModel.state

        LazyColumn(modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ){
            items(state.alertsList){
            Box(modifier = Modifier.fillMaxSize().padding(vertical=16.dp, horizontal = 10.dp)){
                Text(text="Titulo: "+it.title)
                Text(text="Mensaje: "+it.message)
                Text(text="Fecha: "+ getDate(it.date))
                Text(text="Hora: "+ getHour(it.date))
                Button(
                    modifier = Modifier.background(Color.Green),
                    onClick = { alertsViewModel.onBoolClcicked(it) }
                ) {

                }
                HorizontalDivider()
            }
        }
    }

}*/

@OptIn(ExperimentalPagerApi::class)
fun HorizontalPagerWithDotsIndicatorScreen() {
    val pageCount = 5
    val pagerState = rememberPagerState(0)

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = pageCount,
            state = pagerState,
            modifier = Modifier.fillMaxSize(),

        ) { page ->
            ViewPagerItem(page = page) {
                    Column {
                    Text(text = "Page: $page")
                    Spacer(modifier = Modifier.height(2.dp)) // Espacio entre cada componente Text
                    Text(text = "Titulo: ")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Mensaje: ")
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Fecha: ")
                  //  Spacer(modifier = Modifier.height(2.dp))
//                    Text(text = "Hora: ")
                    Spacer(modifier = Modifier.height(2.dp))
                    Button(onClick = { /*TODO*/ }) {
                            Text("Texto")
                        }
                }
            }

        }

        ViewPagerDotsIndicator(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            pageCount = pageCount,
            currentPage = pagerState.currentPage
        )
    }
}

/**************************************************************************************
 ************** FUNCIONES QUE LLAMAN A LAS QUE CREAN LA VIEW***************************
 **************************************************************************************
 */


@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
    // Proporcionar datos estáticos para la vista previa
    HorizontalPagerWithDotsIndicatorScreen()

}