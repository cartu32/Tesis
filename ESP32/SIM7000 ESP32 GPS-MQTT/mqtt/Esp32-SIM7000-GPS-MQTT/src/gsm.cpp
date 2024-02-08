#include <gsm.h>

const char apn[]      = "wap.gprs.unifon.com.ar";
const char gprsUser[] = "wap";
const char gprsPass[] = "wap";
const char* broker = "industrial.api.ubidots.com";
const char* mqtt_user = "BBFF-KlzMyMgB7jnNEgxnrFVIUkHKJ0hVan";
const char* mqtt_pass = "BBFF-KlzMyMgB7jnNEgxnrFVIUkHKJ0hVan";


TinyGsmClient client(modem);
PubSubClient mqtt(client);

JsonDocument Gsm::generateJson()
{
    JsonDocument doc;
    
    doc["value"]=String(random(100));   
    

    return doc;
}

Gsm::Gsm()
{

}

void Gsm::init()
{
  DebugPrint("Initializing modem...");
  modemMannager::restart();

  String modemInfo = modemMannager::getInfo();
  DebugPrint("Modem Info: "+modemInfo);
  

  #if TINY_GSM_USE_GPRS
    // Unlock your SIM card with a PIN if needed
    //if (GSM_PIN && modem.getSimStatus() != 3) { modemMannager::simUnlock(GSM_PIN); }
    if (GSM_PIN && modemMannager::getSimStatus() != 3) { modemMannager::simUnlock(GSM_PIN); }
  #endif

  DebugPrint("Waiting for network...");
  if (!modemMannager::waitForNetwork()) 
  {
    DebugPrint(" fail");
    delay(10000);
    return;
  }
  DebugPrint(" success");

  if (modemMannager::isNetworkConnected()) { DebugPrint("Network connected"); }

  if(!gsmReconnect())
  {
    return;
  }

  // MQTT Broker setup
  mqtt.setServer(broker, 1883);

  //Genero una funcion Lambda y dentro dentro de ella llamo al metodo mqttcallback
  mqtt.setCallback([this](char* topic, uint8_t* payload, unsigned int length) 
  {
     this->mqttCallback(topic, payload, length);
  });

  // Espera un poco antes de publicar nuevamente
  delay(5000);
}


boolean Gsm::mqttConnect()
{

  Serial.print("Connecting to ");
  Serial.print(broker);
  
  boolean status = mqtt.connect(broker, mqtt_user, mqtt_pass);

  if (status == false) {
    DebugPrint(" fail");
    return false;
  }

  DebugPrint(" success");
  mqtt.subscribe(TOPIC_PULSADOR);
  
  return mqtt.connected();
}



bool Gsm::sendMessageBroker(String topic, JsonDocument json)
{

  bool resp=false;
  String resultado;

  char jsonCharArray[256];
  
  serializeJson(json, jsonCharArray);

  DebugPrint("Enviando a broker..");
  DebugPrint(jsonCharArray);
    
  resp=mqtt.publish(topic.c_str(),jsonCharArray);
  resultado = (resp) ? "true" : "false";
  
  DebugPrint("respuesta publish: "+ resultado);
  
  
  return true;
}



void Gsm:: mqttCallback(char* topic, byte* payload, unsigned int len)
{ 
  char stMessage[len + 1];

  memcpy(stMessage, payload, len);
  stMessage[len] = '\0';
  lastMessage = stMessage;
  lastTopic = topic;

  DebugPrint("Message arrived ["+lastTopic+"]: "+lastMessage+"\n");

}

boolean Gsm:: checkLastMessage()
{

  if (lastTopic == "")
    return false;

  // Only proceed if incoming message's topic matches
  if (lastTopic == TOPIC_PULSADOR) 
  {
    if(lastMessage==previousMessage)
      return false;
    
    previousMessage=lastMessage;
    lastTopic   = "";
    lastMessage = "";
    
    event = Event::EV_Person_out_area;
    
    return true;
  }
  return false;
}

bool Gsm::gsmDisconnect()
{
  modemMannager::gprsDisconnect();
  return modemMannager::isGprsConnected();
}

boolean Gsm::checkGsmConnected()
{
// Make sure we're still registered on the network
  if (!modemMannager::isNetworkConnected()) 
  {
    DebugPrint("Network disconnected");
    if (!modemMannager::waitForNetwork(180000L, true)) 
    {
      DebugPrint(" fail");
      delay(10000);
      return false;
    }
    if(!gsmReconnect())
    {
      return false;
    }

    if (modemMannager::isNetworkConnected()) 
    {
     DebugPrint("Network re-connected");
    }

  }


  return true;
}


bool Gsm::gsmReconnect()
{
  
    
    if (!modemMannager::isGprsConnected())
    {
      DebugPrint("GPRS disconnected!");
      DebugPrint("Connecting to "+String(apn));
      
      if (!modemMannager::gprsConnect(apn, gprsUser, gprsPass))
      {
        DebugPrint(" fail");
        delay(10000);
        return false;
      }
      if (modemMannager::isGprsConnected()) { DebugPrint("GPRS reconnected"); }
    }
    DebugPrint("¡¡GSM Ya esta conectado!!");

    // MQTT Broker setup
    mqtt.setServer(broker, 1883);

    //Genero una funcion Lambda y dentro dentro de ella llamo al metodo mqttcallback
    mqtt.setCallback([this](char* topic, uint8_t* payload, unsigned int length) 
    {
      this->mqttCallback(topic, payload, length);
    });

    return true;

}
void Gsm::mqttReconnect()
{
  DebugPrint("=== MQTT NOT CONNECTED ===");
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


