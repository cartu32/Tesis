#include <battery.h>

extern enum Event event;

Battery::Battery()
{

}

void Battery::init()
{
    esp_adc_cal_characteristics_t adc_chars;
    esp_adc_cal_value_t val_type = esp_adc_cal_characterize(ADC_UNIT_1, ADC_ATTEN_DB_11, ADC_WIDTH_BIT_12, 1100, &adc_chars);    //Check type of calibration value used to characterize ADC
    if (val_type == ESP_ADC_CAL_VAL_EFUSE_VREF)
    {
        DebugPrint("eFuse Vref:"+String(adc_chars.vref));
        vref = adc_chars.vref;
    }
    else
    {
        DebugPrint("Default Vref: 1100mV");
    }

}

bool Battery::checkBattery()
{
    bool resp=false;

    if (millis() - timeStamp > TIMEOUT_READ_BATTERY)
     {
            timeStamp = millis();
            uint16_t v = analogRead(ADC_PIN);
            
            float battery_voltage = ((float)v / 4095.0) * 2.0 * 3.3 * (vref / 1000.0);
            
            // When connecting USB, the battery detection will return 0,
            // because the adc detection circuit is disconnected when connecting USB
            DebugPrint("Voltage :" + String(battery_voltage));

            if (battery_voltage == 0) 
            {
                DebugPrint("USB is connected, please disconnect USB.");
                
            }

            if((battery_voltage<OPTIMUM_BATTERY_THRESHOLD)&&
                (battery_voltage!=0))
            {
                event=Event::EV_Battery_low;
                resp=true;
            }
        }
    return resp;
}

