#ifndef GPS_H
#define GPS_H

#include <SPI.h>
#include <SD.h>
#include <Ticker.h>
#include <events.h>
#include <modemMannager.h>
#include <ArduinoJson.h>

#define UART_BAUD           9600
#define PIN_DTR             25
#define PIN_TX              27
#define PIN_RX              26
#define PWR_PIN             4

#define SD_MISO             2
#define SD_MOSI             15
#define SD_SCLK             14
#define SD_CS               13
#define LED_PIN             12

#define UMBRAL_DIFERENCIA_TIMEOUT 10000

class Gps
{
    public:
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minutes = 0;
        String lat = "";
        String lon = "";
        String msl_alt = "";
        int gps_satellites_used       = 0;

        Gps();
        void init();
        void enableGPS();
        void disableGPS();
        bool checkGps();
        JsonDocument readSavedGpsValues();
        
        void activateTimerGps();
        void initTimer();
        bool checkTimeoutGps();

    private:
        long last_current_time = 0;
        bool timeout = false; //Indica si se cumplio el Timeout (tiempo del timer)
        bool activatedTimer=false; //Indica si se activo e inicializo el timer

};
#endif