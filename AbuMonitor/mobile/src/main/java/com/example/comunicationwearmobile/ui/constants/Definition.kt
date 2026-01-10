package com.example.abumonitor.constants

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import kotlin.time.Duration

object Definition {


    /********************************************************
     ******* constante de la maquina de estados de goefence**
     ********************************************************
     */
    //estados de la FSM
    const val ST_INIT             =   "ST_INIT"
    const val ST_INSIDE           =   "ST_INSIDE"
    const val ST_OUTSIDE          =   "ST_OUTSIDE"

    //eventos de la fsm
    const val EVT_EXIT            =   "EVT_EXIT"
    const val EVT_ENTER           =   "EVT_ENTER"
    const val EVT_CONTINUE      =   "EVENT_CONTINUE"

    //acciones que genera la FSM dentro de los eventos de cada estado
    const val ACT_CONTINUE        =   "ACT_CONTINUE"
    const val ACT_ENTER_ONLY      =   "ACT_ENTER_ONLY"
    const val ACT_ENTER_AND_DWELL =   "ACT_ENTER_AND_DWELL"
    const val ACT_DWELL_ONLY      =   "ACT_DWELL_ONLY"
    const val ACT_SILENT_ENTER    =   "ACT_SILENT_ENTER"
    const val ACT_EXIT            =   "ACT_EXIT"
    const val ACT_SILENT_EXIT     =   "ACT_SILENT_EXIT"

    //constante para determinar la velocidad de la persona para saber si esta caminando o en auto
    const val KM_PER_HOUR = 15f // ~15 km/h (velocidad maxima en que puede correr una persona)
    const val CONVESION_METER_PER_SECOND = 3.6f
    const val LIMIT_SPEED_WALKING = KM_PER_HOUR / CONVESION_METER_PER_SECOND

    // --- Configuración de zona gris por imprecisión del GPS ---
    // Esta zona sirve para evitar falsos positivos cuando el usuario
    // está cerca del borde del geofence y el GPS fluctúa.
    // El margen se calcula en base a:
    // 1) Un mínimo fijo absoluto (en metros)
    // 2) Un máximo relativo al tamaño del radio del área

    val MIN_BORDER_MARGIN   = 5f      // margen mínimo absoluto en metros
    val MAX_BORDER_FRACTION = 0.30f   // como mucho 30% del radio

    // --- Configuración de histeresis espacial ---
    const val BASE_ENTER_FACTOR = 0.8f   // 80% del radio para considerar "ENTRA" (desde afuera)
    const val BASE_EXIT_FACTOR  = 1.2f   // 120% del radio para considerar "SALE" (desde adentro)

    // --- Tiempo mínimo entre cambios de estado (para evitar rebotes) ---
    const val MIN_STATE_CHANGE_INTERVAL_MS = 15_000L  // 15 segundos (modo caminando)

    /********************************************************
     ******* constante de nombre de usuario por defecto*****
     ********************************************************
     */

    const val DEFAULT_NAME_USER     = "abuelo"

    /********************************************************
     ************** constantes de configuracion de alarmas*****
     ********************************************************
     */

    const val ACTION_ALARM_FOR_DWELL_TIME         = "ACTION_ALARM_FOR_DWELL_TIME"
    const val ACTION_ALARM_FOR_ACTIVATION_AREA    = "ACTION_ALARM_FOR_ACTIVATION_AREA"
    const val ACTION_ALARM_FOR_DESACTIVATION_AREA = "ACTION_ALARM_FOR_DESACTIVATION_AREA"
    const val ACTION_ALARM_FOR_REMINDER           = "ACTION_ALARM_FOR_REMINDER"


    const val INTENT_ALARM_ID                  = "INTENT_ALARM_ID"
    const val INTENT_ALARM_TIME                = "INTENT_ALARM_TIME"



    //constantes que indican cada cuanto tiempo se hace el checkeo de asistencia.
    //ademas de activar y desactivar las areas de geofence del dia de hoy

    const val DEFAULT_HOUR_REMEMER_APPOINTMENT        = 0
    const val DEFAULT_MINUTE_REMEMER_APPOINTMENT      = 10

    const val MAX_HOUR_DTPICKER_ALARMCHECKS: Int      = 2
    const val MAX_HOUR_DTPICKER_REMEMBER: Int         = 5


    //constante que indica la cuanto tiempo se debe mantener activa la geofence para una cita
    //por defecto se mantiene activa por 4 horas
    const val DEFAULT_TIME_DESACTIVATION_APPOINTMENT = 10

    //constante que se retorna cuando hay un error al programar una alarma
    const val ERROR_IN_SET_ALARM    = -1

    //constante del id que identifica la alarmas
    const val ALARM_ID_FOR_ACTIVATION_AREAS:Long    = 700L
    const val ALARM_ID_FOR_DESACTIVATION_AREAS:Long = 800L
    const val ALARM_ID_FOR_REMINDER:Long            = 900L

    //constante que indica que no hay tiempo de alarma alamcenado en el shared preference
    val NO_STORED_VALUE: Long =-1L

    //TAG para hacer los logs
    const val TAG_DEBUG    = "ABUMONITOR_DEBUG"

    //ID del Request permisson
    //   lateinit var application:Application

    /********************************************************
     ************** constantes de configuracion del mapa*****
     ********************************************************
     */

    //Definicion de configuracion del mapa
    const val ZOOM_MAP: Float = 15f
    const val GEOFENCE_RADIUS_DEFAULT:Double = 100.0

    /********************************************************
     ************** constantes de la base de datos **********
     ********************************************************
     */

    //nombre del archivo de la  base de datos Room
    const val DATABASE_NAME= "AbuMonitorDatabase.db"

    const val COLOR_BLUE = Color.BLUE
    const val COLOR_LIGHT_BLUE = 0xFF02196F3.toInt()
    const val COLOR_GREEN = 0xFF04CAF50.toInt()
    const val COLOR_RED = Color.RED
    const val COLOR_AMBAR = 0xFF0FFC107.toInt()
    const val COLOR_GRIS = Color.GRAY
    const val COLOR_MAGENTA = Color.MAGENTA

    const val TYPE_AREA_ID_NORMAL           = 1
    const val TYPE_AREA_ID_SECURITY_ZONE    = 2
    const val TYPE_AREA_ID_ASSISTANCE       = 3
    const val TYPE_AREA_ID_DWELL_TIME       = 4

    const val TYPE_AREA_DESC_NORMAL         ="Area Normal"
    const val TYPE_AREA_DESC_SECURITY_ZONE  ="Zona de Seguridad"
    const val TYPE_AREA_DESC_ASSISTANCE     ="Area de Asistencia"
    const val TYPE_AREA_DESC_DWELL_TIME     ="Area de Dwell Time"

    const val TYPE_AREA_COLOR_NORMAL= COLOR_LIGHT_BLUE
    const val TYPE_AREA_COLOR_SECURITY_ZONE= COLOR_GREEN
    const val TYPE_AREA_COLOR_ASSISTANCE= COLOR_AMBAR
    const val TYPE_AREA_COLOR_ERROR= COLOR_RED

    const val GEOFENCE_EVENT_ID_ENTER = 1
    const val GEOFENCE_EVENT_ID_EXIT = 2
    const val GEOFENCE_EVENT_ID_DWELL = 3
    const val GEOFENCE_EVENT_DESC_ENTER = "Entrar"
    const val GEOFENCE_EVENT_DESC_EXIT = "Salir"
    const val GEOFENCE_EVENT_DESC_DWELL = "Permanecer"

    //constantes que indican la prioridad de las notificaciones
    const val PRIORITY_ID_LOW   = 1
    const val PRIORITY_ID_MEDIUM   = 2
    const val PRIORITY_ID_HIGH  = 3
    const val PRIORITY_DESC_LOW = "Baja"
    const val PRIORITY_DESC_MEDIUM = "Media"
    const val PRIORITY_DESC_HIGH = "Alta"

    /********************************************************
     ************** constantes para intents *****************
     ********************************************************
     */

    //Intent que se usa para pasar al viewmodel los datos de la nueva area de gofecne
    // cuando el usuario crea una nueva
    const val INTENT_DATA_NEW_AREA_GEOF = "INTENT_DATA_NEW_AREA_GEOF"
    const val INTENT_STATE_OPERATION = "INTENT_STATE_OPERATION"
    const val INTENT_DATA_LATITUDE = "INTENT_DATA_LATITUDE"
    const val INTENT_DATA_LONGITUDE = "INTENT_DATA_LONGITUDE"
    const val INTENT_DATA_METERS = "INTENT_DATA_METERS"
    const val BUNDLE_FRAGMENT_RESULT_NEW_AREA = "BUNDLE_FRAGMENT_RESULT_NEW_AREA"

    /********************************************************
     ************** constantes para detectar las geofence****
     ********************************************************
     */
    //Esta constante sirve para activar la deteccion de geofence a traves del broadcast
    const val ACTION_GEOFENCE_EVENT_BROADCAST: String="com.example.app.ACTION_GEOFENCE_EVENT"
    const val PARAMETER_SERVICE: String = "PARAMETER_SERVICE"

    //constante de tiempo que se usa para saber si la ubicacion es fresca
    //o sea esta actualizada, o si ya es vieja y se debe ignorar
    const val TIME_FRESH_LAST_LOCATION_MS: Int = 2 * 60_000


    //cantidad de citas maximas que se pueden agendar por cada dia
    const val COUNT_MAX_DATE_FOR_DAY = 5

    //tiempo de maximo de respuesta en que el S.O notifica al broadcastt cuando
    //detecta un evento de geofence
    const val NOTIFICATION_MAX_RESPONSIVENESS_GEOFENCE = 10000
    /********************************************************
     ************** constantes de tiempo*****************
     ********************************************************
     */

    //constantes que indican cada cuanto tiempo se lee del gps para mover el
    //mapa
    const val INTERVAL_MILLIS_ACTUALIZATION_POS_GPS:Long=5000
    const val SETUP_UPDATE_INTERVAL_MILLIS:Long = 2000
    //constante que indica cada cuanto tiempo se toma la ultima ubicación del gps que se envia por el flow
    //se el flow envia 10 ubicaciones en 60 segundos, se toma la ultima ubicación recibida dentro de los 60 segundos
    const val SAMPLE_TAKE_LOCATION_UPDATE: Long = 10_000 //segundos


    //rango de cantidad de minutos que se considera salida circunstancial de la zona de seguridad
    //Esto se usa para evitar falsos posirtivos de la zona de seguridad por ejemplo si la persona
    //pasa caminando por la zona de seguridad

    const val TIME_MIN_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE = 1
    const val TIME_MAX_CIRCUMSTANTIAL_DURATION_SECURITY_ZONE = 3

    //rango de minutos que se considera dentro del horario de asistencia
    const val TIME_MIN_IN_ASSISTANCE_ZONE: Long  =1

    //Nombre de los serializables de los intent
    const val  RESOLVABLE_API_EXCEPTION: String = "Resolvable_Api"

    /********************************************************
     ************** constantes de errores********************
     ********************************************************
     */

    //constantes de errores
    const val ERROR_NULL:Long                = -1
    const val ERROR_INSERT_BD_GEOF:Long      = -2
    const val ERROR_ACTIVATE_GEOF:Long       = -3
    const val ERROR_INSERT_CONTACT:Long      = -4

    /********************************************************
     *****Constantes de checkeo de API Google Services*******
     ********************************************************
     */

    const val ERROR_PLAY_SERVICES_MISSING_OR_OUTDATED = 1
    const val ERROR_API_UNAVAILABLE                   = 2
    const val API_OK                                  = 3

    /********************************************************
     ************ constantes para mensajes a wearable********
     ********************************************************
     */
    const val PATH_SEND_DATA_TO_WEARABLE:String = "PATH_SEND_DATA_TO_WEARABLE"
    const val MSG_TO_WEARABLE:String="MSG_TO_WEARABLE"

    @SuppressLint("InlinedApi")
    val permissonNecesary = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.BLUETOOTH,

        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN, 
        Manifest.permission.BLUETOOTH_ADVERTISE,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        Manifest.permission.WAKE_LOCK,
        Manifest.permission.READ_CONTACTS,
      //  Manifest.permission.USE_EXACT_ALARM,
        //Manifest.permission.SCHEDULE_EXACT_ALARM,

        //backoground location se pide en el viewmodel despues
        //de comprobar de que los permisos generales fueron otorgados
        //sobre todo access_fine_location, ya que es necesario para funcionar
        //Manifest.permission.ACCESS_BACKGROUND_LOCATION,

        )



}
