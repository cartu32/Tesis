/* Get all possible data from MPU6050
 * Accelerometer values are given as multiple of the gravity [1g = 9.81 m/s²]
 * Gyro values are given in deg/s
 * Angles are given in degrees
 * Note that X and Y are tilt angles and not pitch/roll.
 *
 * License: MIT
 */

#include "Wire.h"
#include <MPU6050_light.h>
#include "BluetoothSerial.h"
BluetoothSerial SerialBT;

MPU6050 mpu(Wire);

long timer = 0;
float acelX=0;float acelY=0;float acelZ=0;

void setup() {
  SerialBT.begin(9600);
  Serial.begin(9600);
  Wire.begin();
  
  byte status = mpu.begin(3,3);
  Serial.print(F("MPU6050 status: "));
  Serial.println(status);
  while(status!=0){ } // stop everything if could not connect to MPU6050
  
  Serial.println(F("Calculating offsets, do not move MPU6050"));
  delay(1000);
  mpu.calcOffsets(true,true); // gyro and accelero
  Serial.println("Done!\n");
  
}

void loop() {
  mpu.update();

  if(millis() - timer > 200){ // print data every second
    //Serial.print(F("TEMPERATURE: "));Serial.println(mpu.getTemp());
    acelX=mpu.getAccX()*9.81;
    acelY=mpu.getAccY()*9.81;
    acelZ=mpu.getAccZ()*9.81;


    Serial.print(F("ACCELERO  X: "));Serial.print(acelX);
    Serial.print("\tY: "); Serial.print(acelY);
    Serial.print("\t\tZ: ");Serial.println(acelZ);

    SerialBT.print(F("ACCELERO  X: "));SerialBT.print(acelX);
    SerialBT.print("\tY: "); SerialBT.print(acelY);
    SerialBT.print("\t\tZ: ");SerialBT.println(acelZ);
  
    if((acelX>12)||(acelY>12)||(acelZ>12))
    {
      Serial.println("###################Mayor");    
      SerialBT.println("###################Mayor");    
    }
      
/*    Serial.print(F("GYRO      X: "));Serial.print(mpu.getGyroX());
    Serial.print("\tY: ");Serial.print(mpu.getGyroY());
    Serial.print("\tZ: ");Serial.println(mpu.getGyroZ());
  
    Serial.print(F("ACC ANGLE X: "));Serial.print(mpu.getAccAngleX());
    Serial.print("\tY: ");Serial.println(mpu.getAccAngleY());
    
    Serial.print(F("ANGLE     X: "));Serial.print(mpu.getAngleX());
    Serial.print("\tY: ");Serial.print(mpu.getAngleY());
    Serial.print("\tZ: ");Serial.println(mpu.getAngleZ());
    Serial.println(F("=====================================================\n"));*/
    timer = millis();
  }

}
