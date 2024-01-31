#ifndef MODEMANNAGER_H
#define MODEMANNAGER_H

#define TINY_GSM_MODEM_SIM7000

#include <TinyGsmClient.h>

#define SerialAT Serial1

extern TinyGsm modem;

class modemMannager
{
    public:
        modemMannager();

        static void restart();
        static String getInfo();
        static bool simUnlock(const char * gsmPin);
        static SimStatus getSimStatus();    
        static bool waitForNetwork();
        static bool waitForNetwork(uint32_t timeout_ms, bool check_signal);
        static bool isNetworkConnected();
        static bool gprsConnect(const char *apn, const char *gprsUser,const char *gprsPass);
        static bool isGprsConnected();
};

#endif