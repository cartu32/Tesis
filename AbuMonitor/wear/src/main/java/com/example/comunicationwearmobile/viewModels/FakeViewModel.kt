package com.example.comunicationwearmobile.viewModels

import android.app.Application

//Clases viewmodels falsas para poder hacer las previews de las vistas.
//Se utilizan solo para las previews que usan las screen de jetpack compose.
class FakeMainScreenViewModel(app: Application = Application()) : AlertsViewModel(app) {
}