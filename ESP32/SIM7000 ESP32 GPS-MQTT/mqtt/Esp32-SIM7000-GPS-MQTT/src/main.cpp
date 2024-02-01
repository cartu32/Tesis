#include <Arduino.h>
#include <gsm.h>
#include <gps.h>

Gps gps = Gps();
Gsm gsmManager = Gsm();

enum Event event;

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


}

void GenerateEvent()
{
  gsmManager.checkMqtt();

  if(gsmManager.checkLastMessage()||gps.checkGps())
  //if(gsmManager.checkLastMessage())
  //if(gps.checkGps())
  {
    return;
  }

  event=Event::Continue;
}

void loop()
{
  GenerateEvent();

  if (event==Event::PersonaFueraDeArea)
  {
    String json=gsmManager.generateJson();
    gsmManager.sendMessageBrokerTest(json);
  }
  if (event==Event::CoordenadasGPS)
  {
    //  gps.getGpsValue();   
  }

  delay(1000);

}
