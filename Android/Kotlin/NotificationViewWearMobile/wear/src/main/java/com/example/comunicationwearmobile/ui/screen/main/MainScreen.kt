package com.example.comunicationwearmobile.ui.screen.main


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.example.comunicationwearmobile.common.getDate
import com.example.comunicationwearmobile.common.getHour
import com.example.comunicationwearmobile.models.MsgAlertModel
import com.example.comunicationwearmobile.ui.component.ViewPagerDotsIndicator
import com.example.comunicationwearmobile.ui.component.ViewPagerItem
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**************************************************************************************
 ************************** FUNCIONES QUE CREAN LA VIEW********************************
 **************************************************************************************
 */

@Composable
fun PageContent(page: Int, msgAlert:MsgAlertModel) {
    Column(modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally) {
        //Text(text = "Page: $page")
       //Spacer(modifier = Modifier.height(2.dp))
        Text(text = msgAlert.title)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = msgAlert.message)
        Spacer(modifier = Modifier.height(2.dp))

        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center){
            Text(text = getDate(msgAlert.date) )
            Text(text = getHour(msgAlert.date))
        }
        Spacer(modifier = Modifier.height(2.dp))
        Button(onClick = { /*TODO*/ }) {
            Text("Texto")
        }
    }
}

@Composable
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
                val msgAlert=MsgAlertModel("Recordatorio de HOY","Turno Cardiologo")
                PageContent(page = page,msgAlert)
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
    HorizontalPagerWithDotsIndicatorScreen()
}