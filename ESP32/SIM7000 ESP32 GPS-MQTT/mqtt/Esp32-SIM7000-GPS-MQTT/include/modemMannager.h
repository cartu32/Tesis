#ifndef MODEMANNAGER_H
#define MODEMANNAGER_H

#define TINY_GSM_MODEM_SIM7000

#include <TinyGsmClient.h>

#define SerialAT Serial1

#define PWR_PIN             4
#define PIN_TX              27
#define PIN_RX              26

#define UART_BAUD           9600

extern TinyGsm modem;

class modemMannager
{
    public:
        modemMannager();

        static bool init();

        static void modemPowerOn();
        static void modemPowerOff();
        static void modemRestart();
        

        static void restart();
        static String getInfo();
        static bool simUnlock(const char * gsmPin);
        static SimStatus getSimStatus();    
        static bool waitForNetwork();
        static bool waitForNetwork(uint32_t timeout_ms, bool check_signal);
        static bool isNetworkConnected();
        static bool gprsConnect(const char *apn, const char *gprsUser,const char *gprsPass);
        static bool isGprsConnected();
        static void sendAT(const char *command);
        static int8_t waitResponse(uint32_t timeout_ms);
        static void enableGps();
        static void disableGps();
        static String gpsRaw();
        
};

#endif
