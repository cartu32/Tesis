#ifndef BATTERY_H
#define BATTERY_H

#include <esp_adc_cal.h>
#include <events.h>
#include <config.h>

#define ADC_PIN                   35
#define OPTIMUM_BATTERY_THRESHOLD 4
#define TIMEOUT_READ_BATTERY      1000

class Battery
{
    public:
        Battery();
        void init();
        bool checkBattery();
    private:
        int vref = 1100;
        uint32_t timeStamp = 0;

};

#endif