package com.example.comunicationwearmobile.ui.screen.main


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicationwearmobile.ui.component.ViewPagerDotsIndicator
import com.example.comunicationwearmobile.ui.component.ViewPagerItem
import com.example.comunicationwearmobile.viewModels.AlertsViewModel
import com.example.shared_library.SharedData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

const val INITIAL_PAGE: Int =0

/**************************************************************************************
 ************************** FUNCIONES QUE CREAN LA VIEW********************************
 **************************************************************************************
 */

@Composable
fun PageContent(page: Int, msgAlert:SharedData.MsgNotification) {
    CustomColumn {
        Text(text = msgAlert.title, color = Color.Red,fontSize = 15.sp,)
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = msgAlert.message)
        Spacer(modifier = Modifier.height(10.dp))

        CustomRow(){
            Text(text = msgAlert.date)
            Spacer(modifier = Modifier.width(35.dp))
            Text(text = msgAlert.hour)
        }
        Spacer(modifier = Modifier.height(2.dp))

        when(msgAlert.typeNotification){
            SharedData.TypeNotification.Reminder -> FloatingActionButtonOK()
            SharedData.TypeNotification.Alert -> FloatingActionButtonAlert()
            SharedData.TypeNotification.WithoutNotifications -> FloatingActionButtonNoNotification()
        }
    }
}

@Composable
fun FloatingActionButtonOK() {
    FloatingActionButton(
        onClick = { print("Hello") },
        shape = CircleShape,
        containerColor=Color.Green,
        contentColor = Color.White,
        modifier = Modifier.size(50.dp),
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
        ) {
        Icon(Icons.Filled.DateRange, "Floating action button.")
    }
}


@Composable
fun FloatingActionButtonAlert() {
    FloatingActionButton(
        onClick = { print("Hello") },
        shape = CircleShape,
        containerColor=Color.Red,
        contentColor = Color.White,
        modifier = Modifier.size(50.dp),
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
    ) {
        Icon(Icons.Filled.Warning, "Floating action button.")
    }
}


@Composable
fun FloatingActionButtonNoNotification() {
    FloatingActionButton(
        onClick = { print("Hello") },
        shape = CircleShape,
        containerColor=Color.Blue,
        contentColor = Color.White,
        modifier = Modifier.size(50.dp),
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
    ) {
        Icon(Icons.Filled.ThumbUp, "Floating action button.")
    }
}
@Composable
@OptIn(ExperimentalPagerApi::class)
fun HorizontalPagerWithDotsIndicatorScreen(alertsViewModel: AlertsViewModel) {
    val pageCount = alertsViewModel.getCountItemList()
    val pagerState = rememberPagerState(INITIAL_PAGE)

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = pageCount,
            state = pagerState,
            modifier = Modifier.fillMaxSize(),

        ) { page ->
            ViewPagerItem(page = page) {
                val msgAlert= alertsViewModel.state.alertsList.get(page)
                PageContent(page = page,msgAlert)
            }

        }

        ViewPagerDotsIndicator(
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            pageCount = alertsViewModel.getCountItemList(),
            currentPage = pagerState.currentPage
        )
    }
}

@Composable
fun CustomColumn(
    content: @Composable ()-> Unit
) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        content()
    }
}

@Composable
fun CustomRow(content:@Composable () -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ){
        content()
    }
}
/**************************************************************************************
 ************** FUNCIONES QUE LLAMAN A LAS QUE CREAN LA VIEW***************************
 **************************************************************************************
 */


@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
// Simulación de ViewModel para la vista previa
   // val model = remember { AlertsViewModel() }
   // HorizontalPagerWithDotsIndicatorScreen(model)
}