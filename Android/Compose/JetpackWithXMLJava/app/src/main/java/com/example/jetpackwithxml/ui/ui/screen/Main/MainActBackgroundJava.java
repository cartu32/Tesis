package com.example.jetpackwithxml.ui.ui.screen.Main;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.jetpackwithxml.R;

public class MainActBackgroundJava {
    private static Button cmdButton;
    private static TextView txtTexto;
    static int cantClick=0;
    public static void Background(View view){
        cmdButton=view.findViewById(R.id.cmdButton);
        txtTexto=view.findViewById(R.id.txtTexto);

        cmdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listnerButton();

            }
        });
    }


    @SuppressLint("SetTextI18n")
    public static void listnerButton(){
        cantClick++;
        txtTexto.setText("Candidad de clicks Java:"+cantClick);
    }

}
