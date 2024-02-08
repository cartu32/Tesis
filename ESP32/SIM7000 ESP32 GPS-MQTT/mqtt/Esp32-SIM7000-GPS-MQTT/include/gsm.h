#ifndef GSM_H
#define GSM_H

#include <ArduinoJson.h>
#include <events.h>
#include <modemMannager.h>
#include <PubSubClient.h>
#include <config.h>

extern enum Event event;



#define TINY_GSM_USE_GPRS true
#define TINY_GSM_USE_WIFI false
#define TINY_GSM_DEBUG Serial
#define GSM_PIN ""


// Just in case someone defined the wrong thing..
#if TINY_GSM_USE_GPRS && not defined TINY_GSM_MODEM_HAS_GPRS
#undef TINY_GSM_USE_GPRS
#undef TINY_GSM_USE_WIFI
#define TINY_GSM_USE_GPRS false
#define TINY_GSM_USE_WIFI true
#endif
#if TINY_GSM_USE_WIFI && not defined TINY_GSM_MODEM_HAS_WIFI
#undef TINY_GSM_USE_GPRS
#undef TINY_GSM_USE_WIFI
#define TINY_GSM_USE_GPRS true
#define TINY_GSM_USE_WIFI false
#endif

#define MSG_JSON_MAX_SIZE   240

#define TOPIC_PULSADOR          "/v1.6/devices/asaa/pulsador/lv"
#define TOPIC_POTENCIOMETRO     "/v2.0/devices/asaa/pote"
#define TOPIC_GPS               "/V2.0/devices/dispositivo1"



extern TinyGsmClient client;
extern PubSubClient mqtt;

class Gsm
{
    public:
        Gsm();
        void init();
        void checkMqtt();
        bool checkLastMessage();
        bool sendMessageBroker(String topic, JsonDocument json);
        bool gsmDisconnect();
        bool gsmReconnect();
        
        JsonDocument generateJson();


    private:
        uint32_t lastReconnectAttempt = 0;   
        String lastMessage= "";
        String lastTopic  = "";

        boolean mqttConnect();
        boolean checkGsmConnected();
        void mqttReconnect();
        void mqttCallback(char *topic, byte *payload, unsigned int len);
};

#endif

