#include <Arduino.h>
#include <gsm.h>

Gsm gsmManager = Gsm();
enum Event event;

void setup()
{
  // Set console baud rate
  SerialMon.begin(115200);
  delay(10);

  SerialMon.println("Wait...");

  pinMode(4,OUTPUT);
  digitalWrite(4, LOW);
  delay(100);
  digitalWrite(4, HIGH);
  

  // Set GSM module baud rate and UART pins
  SerialAT.begin(9600, SERIAL_8N1,  26, 27);
  delay(3000);

  gsmManager.init();

}

void GenerateEvent()
{
  gsmManager.checkMqtt();

  if(gsmManager.checkLastMessage())
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
}