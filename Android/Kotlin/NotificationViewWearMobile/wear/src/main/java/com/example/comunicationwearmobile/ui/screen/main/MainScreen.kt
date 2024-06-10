package com.example.comunicationwearmobile.ui.screen.main

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.comunicationwearmobile.ui.component.ViewPagerDotsIndicator
import com.example.comunicationwearmobile.ui.component.ViewPagerItem

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

}
*/
@OptIn(ExperimentalFoundationApi::class)
fun HorizontalPagerWithDotsIndicatorScreen() {
    val pageCount = 5
    val pagerState = rememberPagerState(pageCount = { pageCount })

    Box(modifier = Modifier) {
        HorizontalPager(state = pagerState) { page ->
            ViewPagerItem(page = page)
        }

        ViewPagerDotsIndicator(
            Modifier
                .height(50.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            pageCount = pageCount,
            currentPageIteration = pagerState.currentPage
        )
    }
}


/**************************************************************************************
 ************** FUNCIONES QUE LLAMAN A LAS QUE CREAN LA VIEW***************************
 **************************************************************************************
Debido a que viewmodel no funciona con Preview se tuvo que hacer 2 llamadas separadas
de UsuarioScreenLV:

1)para poder poder hacer el Preview (PreviewUsuarioScreenLV), que funciona con datos
estaticos

2)UsuarioScreenLV que es para poder ejecutar la app en el fisico y en el simulador, con los
  datos del viewmodel

 ****************************************************************************************
 */


@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
    // Proporcionar datos estáticos para la vista previa
    HorizontalPagerWithDotsIndicatorScreen()

}