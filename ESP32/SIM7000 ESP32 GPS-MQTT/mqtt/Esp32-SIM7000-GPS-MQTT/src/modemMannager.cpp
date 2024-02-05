#include <modemMannager.h>

TinyGsm modem(SerialAT);


modemMannager::modemMannager()
{

}

bool modemMannager::gprsDisconnect()
{
    return modem.gprsDisconnect();
}

bool modemMannager::init()
{

    modemMannager::modemPowerOn();
    
    // Set GSM module baud rate and UART pins
    SerialAT.begin(UART_BAUD, SERIAL_8N1, PIN_RX, PIN_TX);
    delay(3000);

    if (!modem.testAT()) 
    {
        Serial.println("Failed to restart modem, attempting to continue without restarting");
        modemRestart();
        return false;
    }
    return true;
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

void modemMannager::modemPowerOn()
{
  pinMode(PWR_PIN, OUTPUT);
  digitalWrite(PWR_PIN, LOW);
  delay(1000);    //Datasheet Ton mintues = 1S
  digitalWrite(PWR_PIN, HIGH);

}

void modemMannager::modemPowerOff()
{
  pinMode(PWR_PIN, OUTPUT);
  digitalWrite(PWR_PIN, LOW);
  delay(1500);    //Datasheet Ton mintues = 1.2S
  digitalWrite(PWR_PIN, HIGH);
}

void modemMannager::modemRestart()
{
  modemPowerOff();
  delay(1000);
  modemPowerOn();
}

void modemMannager::sendAT(const char * command)
{
    modem.sendAT(command);
}

int8_t modemMannager::waitResponse(uint32_t timeout_ms)
{
    return modem.waitResponse(timeout_ms);
}


void modemMannager::enableGps()
{
    modem.enableGPS();
}

void modemMannager::disableGps()
{
    modem.disableGPS();
}

String modemMannager::gpsRaw()
{
    return modem.getGPSraw();
}
