package com.example.comunicationwearmobile.ui.utils.Helpers

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.ui.viewmodel.GenericViewModelFactory
import com.example.comunicationwearmobile.ui.viewmodel.ViewmodelMapsActivity

/**
 Helper para gestionar la lógica común de actividades de mapas que usan ViewmodelMapsActivity.
 Permite usar composición en lugar de herencia para reutilizar funcionalidad.
 */
class MapsActivityHelper(
    private val lifecycleOwner: LifecycleOwner,
    private val viewModelProvider: ViewModelProvider
) {
    var viewmodelMapsActivity: ViewmodelMapsActivity? = null

    fun initializeViewModel() {
        val factory = GenericViewModelFactory {
            ViewmodelMapsActivity((lifecycleOwner as android.app.Application))
        }
        viewmodelMapsActivity = viewModelProvider.get(ViewmodelMapsActivity::class.java)
    }


    //Configura los observadores base que se usan en todas las actividades con ViewmodelMapsActivity
    fun configObservers(
        cleanMap: () -> Unit,
        drawArea: (Double, Double, Double, Int) -> Unit,
        setIdDrawnArea: (Long) -> Unit,
        showToast: (String) -> Unit
    ) {
        configObserverShowMessage(showToast)
        configObserverGetAllAreasGeofence(cleanMap, drawArea, setIdDrawnArea)
    }


    private fun configObserverShowMessage(showToast: (String) -> Unit) {
        viewmodelMapsActivity?.showMessage?.observe(lifecycleOwner) { message ->
            showToast(message)
        }
    }


    private fun configObserverGetAllAreasGeofence(
        cleanMap: () -> Unit,
        drawArea: (Double, Double, Double, Int) -> Unit,
        setIdDrawnArea: (Long) -> Unit
    ) {
        viewmodelMapsActivity?.allAreas?.observe(lifecycleOwner) { listAllAreas ->
            cleanMap()
            listAllAreas.forEach {
              // Descomentar el if cuando se haga la vista de asistencia
               if(it.type_area!=Definition.TYPE_AREA_DESC_ASSISTANCE) {
                    drawArea(it.latitude.toDouble(), it.longitude.toDouble(), it.meters.toDouble(), it.color)
                    setIdDrawnArea(it.id_area)
                }
            }
        }
    }


    fun cleanUp() {
        viewmodelMapsActivity?.onDestroyed()
        viewmodelMapsActivity?.allAreas?.removeObservers(lifecycleOwner)
        viewmodelMapsActivity?.showMessage?.removeObservers(lifecycleOwner)
    }
} 