#include <gps.h>
#include <utilities.h>
#include <config.h>


extern enum Event event;

Gps::Gps()
{

}

void Gps::init()
{
    DebugPrint("Start positioning . Make sure to locate outdoors.");
    DebugPrint("The blue indicator light flashes to indicate positioning.");

    this->enableGPS();
    this->initTimer();
}


void Gps::enableGPS(void)
{
  modemMannager::sendAT("+SGPIO=0,4,1,1");
  if (modemMannager::waitResponse(10000L) != 1) 
  {
    DBG(" SGPIO=0,4,1,1 false ");
    DebugPrint("**************EROORRRRRRR");
  }
  modemMannager::enableGps();


}

void Gps::disableGPS(void)
{
  modemMannager::sendAT("+SGPIO=0,4,1,0");
  if (modemMannager::waitResponse(10000L) != 1)
  {
    DBG(" SGPIO=0,4,1,0 false ");
  }
  modemMannager::disableGps();
}

bool Gps::checkGps()
{
   String gps_raw = modemMannager::gpsRaw();

  if (gps_raw != "")
   {
      String date = splitter(gps_raw, ',', 2); //yyyyMMddhhmm ss.sss
      
      year = date.substring(0, 4).toInt();
      month = date.substring(4, 6).toInt();
      day = date.substring(6, 8).toInt();
      hour = date.substring(8, 10).toInt();

      lat = splitter(gps_raw, ',', 3).toFloat();//±dd.dddddd
      lon = splitter(gps_raw, ',', 4).toFloat();//±ddd.dddddd
      msl_alt = splitter(gps_raw, ',', 5).toFloat();//meters
      gps_satellites_used = splitter(gps_raw, ',', 15).toInt();

      
      if((lat!=0) && (lon!=0))
      {
        ("----------------------------------");
        Serial.println("Latitude:"); Serial.println(lat, 6);
        Serial.print("Longitude:"); Serial.println(lon, 6);
        Serial.print("MSL Altitude:"); Serial.println(msl_alt, 6);
        Serial.print("GPS Satellites Used:"); Serial.println(gps_satellites_used);

        event = Event::EV_Gps_coordinates;  

        return true;
      }
 
   }
  return false;
}

JsonDocument Gps::getGpsValue()
{
    JsonDocument doc;

    
    return doc;
}

void Gps::initTimer()
{
    //se activa el timer
    timeout = false;
    activatedTimer=true;

    //se inicializa el contado de tiempo
    last_current_time = millis();
    
    DebugPrint("Se activo el Timer GPS!!");
}

bool Gps::checkTimeoutGps()
{

  bool resp=false;

  //sale si no se activo el timer al llamar initTimer()
  if(!activatedTimer)
    return resp;

  //Si se activo el timer se ejecuta
  long current_time = millis();
  int diff = current_time - last_current_time;
  timeout = (diff > UMBRAL_DIFERENCIA_TIMEOUT) ? true : false;

  //si se cumplio el tiempo del timer
  if (timeout)
  {
    DebugPrint("****Se cumplio Timeout!!");
    
    resp=true;
    timeout = false;
    activatedTimer=false;

    event=Event::EV_Gps_activate_timeout;
  }
  
  return resp;

}