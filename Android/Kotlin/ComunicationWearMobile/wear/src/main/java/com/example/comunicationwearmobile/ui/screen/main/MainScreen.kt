package com.example.comunicationwearmobile.ui.screen.main


import android.app.Application
import androidx.activity.ComponentActivity
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
import com.example.comunicationwearmobile.models.MobileMsgModel
import com.example.comunicationwearmobile.viewModels.MobileViewModel


@Composable
fun UsuarioScreenLV(messageMobile: MobileMsgModel, setMessage: (String) -> Unit, incrementNumberMessage: () -> Unit) {

    CenteredColumn()
    {

        CustomText(text = "Number: ${messageMobile.numberMsg}" )
        CustomText(text = "Msg: ${messageMobile.message}")

        CustomButton(
            text = "Actualizar Usuario",
            onClick = {setMessage("pepito")}
        )
        CustomButton(
            text = "incrementar numero",
            onClick = {incrementNumberMessage()}
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

/*Debido a que viewmodel no funciona con PreviewSe tuvo que hacer 2 llamadas separadas
  de UsuarioScreenLV:

    1)para poder poder hacer el Preview (PreviewUsuarioScreenLV), que funciona con datos estaticos
    2)UsuarioScreenLV que es para poder ejecutar la app en el fisico y en el simulador, con los
      datos del viewmodel
 */

@Composable
fun UsuarioScreenLV(
    activity: ComponentActivity,
    model: MobileViewModel = ViewModelProvider(activity).get(MobileViewModel::class.java)
) {

    val messageMobile by model.observeMobileMessage(activity,model)

    UsuarioScreenLV(
        messageMobile = messageMobile,
        setMessage = { model.setMessage(it) },
        incrementNumberMessage = { model.incrementNumberMessage() }
    )
}

@Preview(showBackground = true, device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
@Composable
fun PreviewUsuarioScreenLV() {
    // Proporcionar datos estáticos para la vista previa
    UsuarioScreenLV(
        messageMobile = MobileMsgModel(message = "Mensaje Prueba", numberMsg = 42),
        setMessage = {},
        incrementNumberMessage = {}
    )
}