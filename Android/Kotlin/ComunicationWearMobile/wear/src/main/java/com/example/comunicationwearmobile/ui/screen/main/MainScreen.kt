package com.example.comunicationwearmobile.ui.screen.main


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.wear.compose.material.Text
import com.example.comunicationwearmobile.MainActivity
import com.example.comunicationwearmobile.viewModels.MobileViewModel


@Composable
fun UsuarioScreenLV(activity: MainActivity) {

    val model: MobileViewModel = ViewModelProvider(activity).get(MobileViewModel::class.java)
    val messageMobile by model.observeMobileMessage(activity, model)

    CenteredColumn()
    {

        CustomText(text = "Number: ${messageMobile.numberMsg}" )
        CustomText(text = "Msg: ${messageMobile.message}")

        CustomButton(
            text = "Actualizar Usuario",
            onClick = {model.setMessage("pepito")}
        )
        CustomButton(
            text = "incrementar numero",
            onClick = {model.incrementNumberMessage()}
        )
    }
}

@Composable
fun CenteredColumn(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        content = content
    )
}
@Composable
fun CustomText(
    text: String,
    modifier: Modifier=Modifier
){
   Text(text = " $text")

}
@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        colors = ButtonDefaults.buttonColors( Color.Red),
        onClick = onClick,
        modifier = modifier
    ) {
        Text(text = text)
    }
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
    UsuarioScreenLV(MainActivity())
}