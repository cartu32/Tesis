#include <Arduino.h>
#include <gsm.h>
#include <gps.h>
#include <states.h>
#include <config.h>

Gps gps = Gps();
Gsm gsmManager = Gsm();

enum Event event;
enum State state;

bool timeout;
long last_current_time;

typedef void (*transition)();

void activateGps()
{
  //activo el GPS
  gps.enableGPS();
    
  state=State::ST_Reading_Gps;
  Serial.println("GPS Habilitados");
  
  //desconecto el GSM
  /*if(!gsmManager.gsmDisconnect())
  {
     Serial.println("GPRS disconnected");

     //activo el GPS
     gps.enableGPS();
    
    state=State::ST_Reading_Gps;

     Serial.println("GPS Habilitados");
  } 
  else
  {
    Serial.println("GPRS disconnect: Failed.");
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
    gps.initTimer();

    state=State::ST_Doing_control;

    Serial.println("GPS desactivado");
    
    Serial.println("valor leidos de GPS...");
    
    json=gps.readSavedGpsValues();  

    gsmManager.sendMessageBroker(TOPIC_GPS,json);   

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
  Serial.println("Control");
  
  event=Event::EV_Continue;
}


void sendMessage()
{

}


void error()
{
}

void none()
{
  Serial.println("none");
}

transition state_table[MAX_STATES][MAX_EVENTS] =
{
      {contControl  , alertOldPeople      ,  none            ,  activateGps         , sendMessage       },//ST_Doing_control
      {none         , none                ,  desactivateGps  ,  none                , none              },//ST_Readin_Gps
      //EV_CONTINUE EV_Person_out_area    EV_Gps_coordinates  EV_Gps_activate_timeout EV_Notifiy_broker
};


void setup()
{
  // Set console baud rate
  SerialMon.begin(115200);
  delay(10);

  SerialMon.println("Wait...");


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

  if(gsmManager.checkLastMessage()||gps.checkTimeoutGps()||gps.checkGps())
  {
    return;
  }

  Serial.println("****************CONTINUE!!!************");
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
