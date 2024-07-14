package com.example.comunicationwearmobile.ui.screen.main


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicationwearmobile.models.MsgAlertState
import com.example.comunicationwearmobile.ui.component.ViewPagerDotsIndicator
import com.example.comunicationwearmobile.viewModels.AlertsViewModel
import com.example.shared_library.SharedData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

/**************************************************************************************
 ************************** FUNCIONES QUE CREAN LA VIEW********************************
 **************************************************************************************
 */

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PageContent(page: Int, msgAlert: SharedData.MsgNotification, alertsViewModel: AlertsViewModel) {
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
            SharedData.TypeNotification.Reminder -> FloatingActionButtonOK(page,alertsViewModel)
            SharedData.TypeNotification.Alert -> FloatingActionButtonAlert(page,alertsViewModel)
            SharedData.TypeNotification.WithoutNotifications -> FloatingActionButtonNoNotification()
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun FloatingActionButtonOK(currentPage: Int, alertsViewModel: AlertsViewModel) {
    FloatingActionButton(
        onClick = {alertsViewModel.removeMsgAlertList(currentPage)},
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
fun FloatingActionButtonAlert(currentPage: Int, alertsViewModel: AlertsViewModel) {
    FloatingActionButton(
        onClick = { alertsViewModel.removeMsgAlertList(currentPage)},
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
        onClick = {},
        shape = CircleShape,
        containerColor=Color.Blue,
        contentColor = Color.White,
        modifier = Modifier.size(50.dp),
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
    ) {
        Icon(Icons.Filled.ThumbUp, "Floating action button.")
    }
}
@OptIn(ExperimentalPagerApi::class)
@Composable
fun HorizontalPagerWithDotsIndicatorScreen(alertsViewModel: AlertsViewModel) {
    val state by alertsViewModel.stateListNotif.observeAsState(initial = MsgAlertState())
    val pageCount = state.alertsList?.size

    if (pageCount != 0) {
        if (pageCount != null) {
            NotificationPagerView(pageCount = pageCount, alertsViewModel = alertsViewModel)
        }
    } else {
        DefaultView()
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NotificationPagerView(pageCount: Int, alertsViewModel: AlertsViewModel) {
    val pagerState = rememberPagerState()
    val state by alertsViewModel.stateListNotif.observeAsState(initial = MsgAlertState())
    val lastPageIndex = pageCount - 1

    LaunchedEffect(pageCount) {
        if (pageCount > 0) {
            pagerState.scrollToPage(lastPageIndex)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = pageCount,
            state = pagerState,
            modifier = Modifier.fillMaxSize().background(Color.White)
        ) { page ->
            val msgAlert = state.alertsList?.get(page)
            if (msgAlert != null) {
                PageContent(page = page, msgAlert = msgAlert, alertsViewModel = alertsViewModel)
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


@Composable
fun CustomColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        content()
    }
}

@Composable
fun CustomRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        content()
    }
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultView() {
    Box(modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CustomColumn {
            Text(
                text = "No hay Alertas disponibles",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                color = Color.White
            )
            FloatingActionButtonNoNotification()
        }

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
