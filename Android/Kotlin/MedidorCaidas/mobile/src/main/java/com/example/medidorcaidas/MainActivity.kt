package com.example.medidorcaidas

import android.graphics.Color
import com.example.medidorcaidas.R
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class MainActivity : AppCompatActivity() {
    private var graph: GraphView? =null
    //graphviewSeries
    var temperatura: LineGraphSeries<DataPoint?>? =null
    var potenciometro: LineGraphSeries<DataPoint>? =null
    //valor que se suma al eje x despues de cada actualizacion
    var ejeX = 0.6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.graph)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left , systemBars.top , systemBars.right , systemBars.bottom)
            insets
        }
        graph= findViewById<View>(R.id.graph) as GraphView

/*        val series = LineGraphSeries(
            arrayOf(
                DataPoint(0.0 , 1.0) ,
                DataPoint(1.0 , 5.0) ,
                DataPoint(2.0 , 3.0) ,
                DataPoint(3.0 , 2.0) ,
                DataPoint(4.0 , 6.0)
            )
        )
        graph?.addSeries(series)*/
        initGraph()
        for(i in 1..10) {
            ejeX += 0.6
            temperatura?.appendData(DataPoint(ejeX , i.toDouble()) , true , 22)
        }

    }

    private fun initGraph() {
        //permitime controlar los ejes manualmente
        graph?.viewport?.isXAxisBoundsManual = true;
        graph?.viewport?.setMinX(0.0);
        graph?.viewport?.setMaxX(10.0);
        graph?.viewport?.setMaxY(1024.0)
        graph?.viewport?.setMinY(0.0)

        //permite realizar zoom y ajustar posicion eje x
        graph?.viewport?.isScalable = true
        graph?.viewport?.setScalableY(true)

        temperatura = LineGraphSeries()
        //draw points
        temperatura?.isDrawDataPoints = true;
        //draw below points
        temperatura?.isDrawBackground = true;
        //color series
        temperatura?.color = Color.RED



        potenciometro = LineGraphSeries()
        //draw points
        potenciometro?.isDrawDataPoints = true;
        //draw below points
        potenciometro?.isDrawBackground = true;
        //color series
        potenciometro?.color = Color.BLUE

        //opcionales
        //potenciometro.setTitle("pot")
        //temperatura?.setTitle("temp")
        //graph?.getLegendRender().setVisible(true)
        //graph?.getLegendRender().setAlign(LegenderRender.LegendAlign.TOP)

        graph?.addSeries(temperatura);
        graph?.addSeries(potenciometro)
    }



}