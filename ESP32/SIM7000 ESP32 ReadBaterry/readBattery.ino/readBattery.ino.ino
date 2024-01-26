#include <esp_adc_cal.h>
#include  "BluetoothSerial.h"

BluetoothSerial SerialBT;

#define ADC_PIN     35
int vref = 1100;
uint32_t timeStamp = 0;

void setup() 
{
  // put your setup code here, to run once:
  SerialBT.begin(9600);
  Serial.begin(9600);
  
  SerialBT.println("Leyendo bateria....");
  delay(1000);

  esp_adc_cal_characteristics_t adc_chars;
  esp_adc_cal_value_t val_type = esp_adc_cal_characterize(ADC_UNIT_1, ADC_ATTEN_DB_11, ADC_WIDTH_BIT_12, 1100, &adc_chars);    //Check type of calibration value used to characterize ADC
  if (val_type == ESP_ADC_CAL_VAL_EFUSE_VREF)
  {
      SerialBT.printf("eFuse Vref:%u mV", adc_chars.vref);
      vref = adc_chars.vref;
  }
  else if (val_type == ESP_ADC_CAL_VAL_EFUSE_TP) 
  {
      SerialBT.printf("Two Point --> coeff_a:%umV coeff_b:%umV\n", adc_chars.coeff_a, adc_chars.coeff_b);
  }
  else
  {
      SerialBT.println("Default Vref: 1100mV");
  }

}

void loop() 
{
  // put your main code here, to run repeatedly:
 
    if (millis() - timeStamp > 1000)
     {
            timeStamp = millis();
            uint16_t v = analogRead(ADC_PIN);
            float battery_voltage = ((float)v / 4095.0) * 2.0 * 3.3 * (vref / 1000.0);
            String voltage = "Voltage :" + String(battery_voltage) + "V\n";

            // When connecting USB, the battery detection will return 0,
            // because the adc detection circuit is disconnected when connecting USB
            SerialBT.println(voltage);
            Serial.println(voltage);

            if (voltage == "0.00") {
                SerialBT.println("USB is connected, please disconnect USB.");
                Serial.println("USB is connected, please disconnect USB.");
            }

        }


}
