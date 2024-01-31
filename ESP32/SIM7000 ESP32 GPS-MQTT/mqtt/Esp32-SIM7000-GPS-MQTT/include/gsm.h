#include <ArduinoJson.h>
#include <events.h>
#include <modemMannager.h>
#include <PubSubClient.h>

extern enum Event event;

#define SerialMon Serial

#define TINY_GSM_USE_GPRS true
#define TINY_GSM_USE_WIFI false
#define TINY_GSM_DEBUG SerialMon
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

extern TinyGsmClient client;
extern PubSubClient mqtt;

class Gsm
{
    public:
        Gsm();
        void init();
        void checkMqtt();
        boolean checkLastMessage();
        boolean sendMessageBrokerTest(String json);

        String generateJson();

    private:
        uint32_t lastReconnectAttempt = 0;   
        String lastMessage= "";
        String lastTopic  = "";

        boolean mqttConnect();
        boolean checkGsmConnected();
        void mqttReconnect();
        void mqttCallback(char* topic, byte* payload, unsigned int len);
        
};



