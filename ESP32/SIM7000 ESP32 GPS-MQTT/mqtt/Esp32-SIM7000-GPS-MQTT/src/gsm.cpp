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
  SerialMon.println("Initializing modem...");
  modemMannager::restart();

  String modemInfo = modemMannager::getInfo();
  SerialMon.print("Modem Info: ");
  SerialMon.println(modemInfo);

  #if TINY_GSM_USE_GPRS
    // Unlock your SIM card with a PIN if needed
    //if (GSM_PIN && modem.getSimStatus() != 3) { modemMannager::simUnlock(GSM_PIN); }
    if (GSM_PIN && modemMannager::getSimStatus() != 3) { modemMannager::simUnlock(GSM_PIN); }
  #endif

  SerialMon.print("Waiting for network...");
  if (!modemMannager::waitForNetwork()) 
  {
    SerialMon.println(" fail");
    delay(10000);
    return;
  }
  SerialMon.println(" success");

  if (modemMannager::isNetworkConnected()) { SerialMon.println("Network connected"); }

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

  SerialMon.print("Connecting to ");
  SerialMon.print(broker);
  
  boolean status = mqtt.connect(broker, mqtt_user, mqtt_pass);

  if (status == false) {
    SerialMon.println(" fail");
    return false;
  }

  SerialMon.println(" success");
  mqtt.subscribe(TOPIC_PULSADOR);
  
  return mqtt.connected();
}



bool Gsm::sendMessageBroker(String topic, JsonDocument json)
{

  bool resp=false;
  char jsonCharArray[256];
  
  serializeJson(json, jsonCharArray);

  Serial.println("Enviando a broker..");
  Serial.println(jsonCharArray);
    
  resp=mqtt.publish(topic.c_str(),jsonCharArray);
  
  Serial.println("respuesta publish: ");
  Serial.print( resp);
  
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
  if (lastTopic == TOPIC_PULSADOR) 
  {
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
    SerialMon.println("Network disconnected");
    if (!modemMannager::waitForNetwork(180000L, true)) 
    {
      SerialMon.println(" fail");
      delay(10000);
      return false;
    }
    if(!gsmReconnect())
    {
      return false;
    }

    SerialMon.println(" Aca!!");
    if (modemMannager::isNetworkConnected()) 
    {
      SerialMon.println("Network re-connected");
    }

  }


  return true;
}


bool Gsm::gsmReconnect()
{
  
    
    if (!modemMannager::isGprsConnected())
    {
      SerialMon.println("GPRS disconnected!");
      SerialMon.print(F("Connecting to "));
      SerialMon.print(apn);
      if (!modemMannager::gprsConnect(apn, gprsUser, gprsPass))
      {
        SerialMon.println(" fail");
        delay(10000);
        return false;
      }
      if (modemMannager::isGprsConnected()) { SerialMon.println("GPRS reconnected"); }
    }
    SerialMon.println("¡¡GSM Ya esta conectado!!");

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


