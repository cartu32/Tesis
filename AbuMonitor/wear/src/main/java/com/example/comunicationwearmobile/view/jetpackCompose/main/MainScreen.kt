package com.example.comunicationwearmobile.view.jetpackCompose.main


import android.app.Application
import android.util.Log
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.comunicationwearmobile.utils.isScreenLock
import com.example.comunicationwearmobile.utils.isScreenOn
import com.example.comunicationwearmobile.models.entities.DataClass_MsgAlertState
import com.example.comunicationwearmobile.view.jetpackCompose.component.ViewPagerDotsIndicator
import com.example.comunicationwearmobile.viewModels.AlertsViewModel
import com.example.comunicationwearmobile.viewModels.FakeMainScreenViewModel
import com.example.shared_library.SharedData
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

const val TAG: String="MainScreen"

/**************************************************************************************
 ************************** FUNCIONES QUE CREAN LA VIEW********************************
 **************************************************************************************
 */
@Composable
fun PageContent( msgAlert: SharedData.MsgNotification, alertsViewModel: AlertsViewModel) {
    CustomColumn {
        Text(text = msgAlert.title, color = Color.Red, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = msgAlert.message)
        Spacer(modifier = Modifier.height(10.dp))
        CustomRow {
            Text(text = msgAlert.date)
            Spacer(modifier = Modifier.width(35.dp))
            Text(text = msgAlert.hour)
        }
        Spacer(modifier = Modifier.height(2.dp))

        when (msgAlert.typeNotification) {
            SharedData.TypeNotification.Reminder -> showButtonReminder(msgAlert.idMsgMobile,alertsViewModel)
            SharedData.TypeNotification.Alert -> showButtonAlert(msgAlert.idMsgMobile,alertsViewModel)
            SharedData.TypeNotification.WithoutNotifications -> showButtonDefault()
            SharedData.TypeNotification.FallDetection -> showButtonsFallDetection(alertsViewModel)
        }
    }
}

@Composable
fun showButtonReminder(idMsgMobile: Int, alertsViewModel: AlertsViewModel) {
    CustomFloatingActionButton(Icons.Filled.DateRange, "Floating action button.", buttonBackgroundColor =  Color.Green) {
        alertsViewModel.removeMsgAlertInAllDevices(idMsgMobile) }
}

@Composable
fun showButtonAlert(idMsgMobile: Int, alertsViewModel: AlertsViewModel) {
    CustomFloatingActionButton(Icons.Filled.Warning, "Floating action button.",buttonBackgroundColor =  Color.Red) {
        alertsViewModel.removeMsgAlertInAllDevices(idMsgMobile) }
}

@Composable
fun showButtonsFallDetection(alertsViewModel: AlertsViewModel) {
    Row( // <- Aquí usamos Row en lugar de dejarlos sueltos
        horizontalArrangement = Arrangement.spacedBy(16.dp), // espacio entre botones
        verticalAlignment = Alignment.CenterVertically
    ) {
        CustomFloatingActionButton(
            Icons.Filled.Phone,
            "Floating action button.",
            buttonBackgroundColor = Color.Red,
            onClick = {alertsViewModel.notifyFallBySmartPhone()}
        )
        CustomFloatingActionButton(
            Icons.Filled.Close,
            "Floating action button.",
            buttonBackgroundColor = Color.White,
            buttonContentColor = Color.Black,
            onClick = {alertsViewModel.cancelNotifyFallBySmartPhone()}
        )
    }
}

@Composable
fun showButtonDefault(){
    CustomFloatingActionButton(Icons.Filled.ThumbUp, "Floating action button.", Color.Blue) {}
}

@Composable
fun CustomFloatingActionButton(
    icon: ImageVector ,
    description: String ,
    buttonBackgroundColor: Color ,
    buttonContentColor: Color = Color.White,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = buttonBackgroundColor,
        contentColor = buttonContentColor,
        modifier = Modifier.size(50.dp),
        elevation = FloatingActionButtonDefaults.elevation(8.dp),
    ) {
        Icon(icon, description)
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun NotificationPagerView(pageCount: Int, alertsViewModel: AlertsViewModel) {
    val pagerState = rememberPagerState()
    val state by alertsViewModel.stateListNotif.observeAsState(initial = DataClass_MsgAlertState())

    LaunchedEffect(pageCount) {
        if (pageCount > 0) {
            pagerState.scrollToPage(pageCount - 1)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            count = pageCount,
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) { page ->
            val msgAlert = state.alertsList?.get(page)
            msgAlert?.let { PageContent( msgAlert = it, alertsViewModel = alertsViewModel) }
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


@Composable
fun CheckCompleteRecomposition(alertsViewModel: AlertsViewModel) {
    //cuando se termina de recomponer toda la vista le avisa al viewmodel sobre esto
    SideEffect {
        Log.d(TAG,"Se completo recomposition")
        Log.d(TAG,"MainScreen"+" thread: " + Thread.currentThread().id)

        alertsViewModel.setCompleteRecomposition()
    }
}

@Composable
fun DefaultView(alertsViewModel: AlertsViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CustomColumn {
            Text(
                text = "No hay Alertas disponibles",
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center,
                color = Color.White
            )
            showButtonDefault()
            //showButtonsFallDetection()
        }
    }

    CheckCompleteRecomposition(alertsViewModel)
}

@Composable
fun HorizontalPagerWithDotsIndicatorScreen(alertsViewModel: AlertsViewModel) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val state by alertsViewModel.stateListNotif.observeAsState(initial = DataClass_MsgAlertState())
    val pageCount = state.alertsList?.size ?: 0

    if (pageCount != 0 && !isScreenLock(application) && isScreenOn(application)) {
        NotificationPagerView(pageCount = pageCount, alertsViewModel = alertsViewModel)
    } else {
        DefaultView(alertsViewModel = alertsViewModel)
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
    val fakeViewModel = remember { FakeMainScreenViewModel() }
    DefaultView(alertsViewModel = fakeViewModel)
}
