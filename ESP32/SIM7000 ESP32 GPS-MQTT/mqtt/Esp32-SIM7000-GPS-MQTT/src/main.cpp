#include <Arduino.h>
#include <gsm.h>
#include <gps.h>
#include <states.h>
#include <battery.h>


Gps gps = Gps();
Gsm gsmManager = Gsm();
Battery battery = Battery();

enum Event event;
enum State state;

bool timeout;
long last_current_time;

typedef void (*transition)();

void activateGps()
{
  //activo el GPS 
  gps.enableGPS();
  //activo el timer que me indica hasta cuando se puede leer el gps.
  gps.initTimerToReadGPs();

  state=State::ST_Reading_Gps;
  DebugPrint("GPS Habilitados");
  
  //desconecto el GSM
  /*if(!gsmManager.gsmDisconnect())
  {
     DebugPrint("GPRS disconnected");

     //activo el GPS
     gps.enableGPS();
    
    state=State::ST_Reading_Gps;

     DebugPrint("GPS Habilitados");
  } 
  else
  {
    DebugPrint("GPRS disconnect: Failed.");
  }*/

  
}

void desactivateGps()
{
    JsonDocument json;
    //desactivo el GPS
    gps.disableGPS();
    //vuelvo a activar el GSM
    //gsmManager.gsmReconnect();

    delay(1000);
    //inicializo el timer para volver activar el GPS
    gps.initTimerToActivateGPs();

    state=State::ST_Doing_control;

    DebugPrint("GPS desactivado");
    
    DebugPrint("valor leidos de GPS...");
    
    json=gps.readSavedGpsValues();  

    if(json.size()!=0)
    {
      gsmManager.sendMessageBroker(TOPIC_GPS,json);   
    }
    else
    {
      DebugPrint("No se envia coordenadas, ya que estan vacias")
    }

}

void alertOldPeople()
{
  
   JsonDocument json=gsmManager.generateJson();
   gsmManager.sendMessageBroker(TOPIC_POTENCIOMETRO,json);

   state=State::ST_Doing_control;
}


void contControl()
{
  gsmManager.checkMqtt();
  
  event=Event::EV_Continue;
}

void alertBattery()
{
  DebugPrint("********NIVEL DE BATERIA BAJO");
}

void sendMessage()
{

}


void error()
{
}

void none()
{
  
}

transition state_table[MAX_STATES][MAX_EVENTS] =
{
      {contControl  , alertOldPeople      ,  none            ,  activateGps         , none                     , alertBattery      },//ST_Doing_control
      {none         , none                ,  desactivateGps  ,  none                , desactivateGps           , none              },//ST_Readin_Gps
      //EV_CONTINUE EV_Person_out_area    EV_Gps_coordinates  EV_Gps_timeout_to_activate EV_Gps_timeout_to_read EV_Battery_low
};


void setup()
{
  
  initDebug();

  DebugPrint("Wait...");

  if(!modemMannager::init())
  {
    return;
  }

  gsmManager.init();
  gps.init();
 
  state=State::ST_Doing_control;
  event=Event::EV_Continue;

}

void GenerateEvent()
{

  if(gsmManager.checkLastMessage()||
    gps.checkTimeoutToActivateGps()||
    gps.checkTimeoutToReadGps()||
    gps.checkGps()||
    battery.checkBattery())
  {
    return;
  }

   event=Event::EV_Continue;
}

void StateMachine()
{
  GenerateEvent();

  if ((event >= 0) && (event < MAX_EVENTS) && (state >= 0) && (state < MAX_STATES))
  {
    /*if (event != Event::EV_Continue)
    {*/
      DebugPrintEstado(states_s[state], events_s[event]);
    //}

    state_table[state][event]();
  }
  
  event = Event::EV_Continue;
}

void loop()
{

  StateMachine();
  delay(1000); 

}
