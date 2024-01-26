#include <gsm.h>

const char apn[]      = "wap.gprs.unifon.com.ar";
const char gprsUser[] = "wap";
const char gprsPass[] = "wap";
const char* broker = "industrial.api.ubidots.com";
const char* mqtt_user = "BBFF-KlzMyMgB7jnNEgxnrFVIUkHKJ0hVan";
const char* mqtt_pass = "BBFF-KlzMyMgB7jnNEgxnrFVIUkHKJ0hVan";

const char* topicPulsador  = "/v1.6/devices/asaa/pulsador/lv";
const char* topicInit      = "/v2.0/devices/asaa/pote";

TinyGsm modem(SerialAT);
TinyGsmClient client(modem);
PubSubClient mqtt(client);

String Gsm::generateJson()
{
    JsonDocument doc;
    String json;
    
    json="";

    doc["value"]=String(random(100));   
    serializeJson(doc,json) ;           

    return json;
}
Gsm::Gsm()
{

}

void Gsm::init()
{
  SerialMon.println("Initializing modem...");
  modem.restart();

  String modemInfo = modem.getModemInfo();
  SerialMon.print("Modem Info: ");
  SerialMon.println(modemInfo);

  #if TINY_GSM_USE_GPRS
    // Unlock your SIM card with a PIN if needed
    if (GSM_PIN && modem.getSimStatus() != 3) { modem.simUnlock(GSM_PIN); }
  #endif

  SerialMon.print("Waiting for network...");
  if (!modem.waitForNetwork()) 
  {
    SerialMon.println(" fail");
    delay(10000);
    return;
  }
  SerialMon.println(" success");

  if (modem.isNetworkConnected()) { SerialMon.println("Network connected"); }

  #if TINY_GSM_USE_GPRS
    // GPRS connection parameters are usually set after network registration
    SerialMon.print(F("Connecting to "));
    SerialMon.print(apn);
    
    if (!modem.gprsConnect(apn, gprsUser, gprsPass))
    {
      SerialMon.println(" fail");
      delay(10000);
      return;
    }
    SerialMon.println(" success");

    if (modem.isGprsConnected()) { SerialMon.println("GPRS connected"); }
  #endif

  // MQTT Broker setup
  mqtt.setServer(broker, 1883);

  //Genero una funcion Lambda y dentro dentro de ella llamo al metodo mqttcallback
  mqtt.setCallback([this](char* topic, uint8_t* payload, unsigned int length) 
  {
     this->mqttCallback(topic, payload, length);
  });
}


boolean Gsm::mqttConnect()
{

  SerialMon.print("Connecting to ");
  SerialMon.print(broker);
  
  boolean status = mqtt.connect(broker, mqtt_user, mqtt_pass);

  if (status == false) {
    SerialMon.println(" fail");
    return false;
  }

  SerialMon.println(" success");
  mqtt.subscribe(topicPulsador);
  
  return mqtt.connected();
}

boolean Gsm::sendMessageBrokerTest(String json)
{
  if(json.length()>MSG_JSON_MAX_SIZE)
  {
     SerialMon.println("\n****ERROR!!!!Tamano superado del msg al Broker.\n");
     return false;
  }

  char charMsgToSend[MSG_JSON_MAX_SIZE+1];
   
  json.toCharArray(charMsgToSend, json.length()+1);
  
  Serial.println("Enviando a broker..");
  Serial.println(charMsgToSend);
  
  mqtt.publish(topicInit, (const char *)charMsgToSend);
  return true;
}



void Gsm:: mqttCallback(char* topic, byte* payload, unsigned int len)
{ 
  char stMessage[len + 1];

  memcpy(stMessage, payload, len);
  stMessage[len] = '\0';
  lastMessage = stMessage;
  lastTopic = topic;

  SerialMon.print("Message arrived [");
  SerialMon.print(lastTopic);
  SerialMon.print("]: ");
  SerialMon.print(lastMessage);
  SerialMon.println();

}

boolean Gsm:: checkLastMessage()
{

  if (lastTopic == "")
    return false;

  // Only proceed if incoming message's topic matches
  if (lastTopic == topicPulsador) 
  {
    lastTopic   = "";
    lastMessage = "";
    event = Event::PersonaFueraDeArea;
    
    /*json=generateJson();
    sendMessageBrokerTest(json);*/
    return true;
  }
  return false;
}


boolean Gsm::checkGsmConnected()
{
// Make sure we're still registered on the network
  if (!modem.isNetworkConnected()) 
  {
    SerialMon.println("Network disconnected");
    if (!modem.waitForNetwork(180000L, true)) 
    {
      SerialMon.println(" fail");
      delay(10000);
      return false;
    }
    if (modem.isNetworkConnected()) 
    {
      SerialMon.println("Network re-connected");
    }

   #if TINY_GSM_USE_GPRS
      // and make sure GPRS/EPS is still connected
      if (!modem.isGprsConnected())
      {
        SerialMon.println("GPRS disconnected!");
        SerialMon.print(F("Connecting to "));
        SerialMon.print(apn);
        if (!modem.gprsConnect(apn, gprsUser, gprsPass))
        {
          SerialMon.println(" fail");
          delay(10000);
          return false;
        }
        if (modem.isGprsConnected()) { SerialMon.println("GPRS reconnected"); }
      }
  #endif
  }


  return true;
}

void Gsm::mqttReconnect()
{
  SerialMon.println("=== MQTT NOT CONNECTED ===");
  // Reconnect every 10 seconds
  uint32_t t = millis();
  if (t - lastReconnectAttempt > 10000L) 
  {
    lastReconnectAttempt = t;
    if (mqttConnect()) { lastReconnectAttempt = 0; }
  }
  delay(100);
}



void Gsm::checkMqtt()
{
  if (!checkGsmConnected())
    return;

  if (!mqtt.connected())
  {
    mqttReconnect();
    return;
  }

  mqtt.loop();  
}


