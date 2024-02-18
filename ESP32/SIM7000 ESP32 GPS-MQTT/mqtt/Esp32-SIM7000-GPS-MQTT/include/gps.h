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

#define UMBRAL_TIMEOUT_TO_ACTIVATE_GPS 120000 //cada tanto mseg se activa el GPS
#define UMBRAL_TIMEOUT_TO_READ_GPS     60000 //se usa para saber cuando se debe dejar de leer el gps

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

        void initTimerToActivateGPs();
        bool checkTimeoutToActivateGps();

        void initTimerToReadGPs();
        bool checkTimeoutToReadGps();
        void desactivateTimerToReadGps();

    private:

        //Variables que sirven para el timer1 utilizado para indicar cuando se debe activar el GPS
        long last_current_time1 = 0;
        bool timeout1 = false; //Indica si se cumplio el Timeout (tiempo del timer)
        bool activatedTimer1=false; //Indica si se activo e inicializo el timer


        //Variables que sirven para el timer1 utilizado que se usa para indicar hasta cuando se debe leer el GPS
        long last_current_time2 = 0;
        bool timeout2 = false; //Indica si se cumplio el Timeout (tiempo del timer)
        bool activatedTimer2=false; //Indica si se activo e inicializo el timer

};
#endif