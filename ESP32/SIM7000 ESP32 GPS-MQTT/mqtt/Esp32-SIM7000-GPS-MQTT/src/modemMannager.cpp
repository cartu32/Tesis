#include <modemMannager.h>

TinyGsm modem(SerialAT);


modemMannager::modemMannager()
{

}

void modemMannager::restart()
{
      modem.restart();
}

String modemMannager::getInfo()
{
    return modem.getModemInfo();
}


bool modemMannager::simUnlock(const char * gsmPin)
{
    return modem.simUnlock(gsmPin);
}

SimStatus modemMannager:: getSimStatus()
{
    return modem.getSimStatus();
}

bool modemMannager::waitForNetwork()
{
    return modem.waitForNetwork();
}

bool modemMannager::waitForNetwork(uint32_t timeout_ms, bool check_signal)
{
    return waitForNetwork(timeout_ms,check_signal);
}

bool modemMannager::isNetworkConnected()
{
    return modem.isNetworkConnected();
}

bool modemMannager::gprsConnect(const char *apn, const char *gprsUser,const char *gprsPass)
{
    return modem.gprsConnect(apn, gprsUser, gprsPass);
}

bool modemMannager::isGprsConnected()
{
    return modem.isGprsConnected();
}