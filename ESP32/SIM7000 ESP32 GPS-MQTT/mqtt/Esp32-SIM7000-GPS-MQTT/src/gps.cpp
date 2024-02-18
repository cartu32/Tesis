#include <gps.h>
#include <utilities.h>
#include <config.h>


extern enum Event event;

Gps::Gps()
{

}

void Gps::init()
{
    this->enableGPS();
    this->initTimerToActivateGPs();
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

      lat = splitter(gps_raw, ',', 3);//±dd.dddddd
      lon = splitter(gps_raw, ',', 4);//±ddd.dddddd
      msl_alt = splitter(gps_raw, ',', 5);//meters
      gps_satellites_used = splitter(gps_raw, ',', 15).toInt();

      
      if((lat!=0) && (lon!=0))
      {

/*        DebugPrint("----------------------------------");
        DebugPrint("Latitude:"+lat);
        DebugPrint("Longitude"+lon);
        DebugPrint("Altitude"+msl_alt);
        DebugPrint("GPS Satellites Used:"+String(gps_satellites_used));*/
        

        event = Event::EV_Gps_coordinates;  

        return true;
      }
 
   }
  return false;
}

JsonDocument Gps::readSavedGpsValues()
{
    
    // Crear el JsonDocument
    JsonDocument doc;

    if ((lat!=0)&&(lon!=0))
    {      
        // Añadir los valores al documento
        doc["position"]["latitude"] = lat;
        doc["position"]["longitude"] = lon;
      // doc["position"]["altitude"] = msl_alt;

        // Serializar el JsonDocument a una cadena JSON
        String jsonString;
        serializeJson(doc, jsonString);

        // Imprimir la cadena JSON
        DebugPrint(jsonString);

    }
    return doc;
}

void Gps::initTimerToActivateGPs()
{
    //se activa el timer
    timeout1 = false;
    activatedTimer1=true;

    //se inicializa el contado de tiempo
    last_current_time1 = millis();

    //se descativa el timer de lectura de GPS, ya que no deben ejecutarse los dos al mismo tiempo
    desactivateTimerToReadGps();
    
    DebugPrint("Se activo el Timer de activacion del GPS!!");
}

bool Gps::checkTimeoutToActivateGps()
{

  bool resp=false;

  //sale si no se activo el timer al llamar initTimer()
  if(!activatedTimer1)
    return resp;

  //Si se activo el timer se ejecuta
  long current_time = millis();
  int diff = current_time - last_current_time1;
  timeout1 = (diff > UMBRAL_TIMEOUT_TO_ACTIVATE_GPS) ? true : false;

  //si se cumplio el tiempo del timer
  if (timeout1)
  {    
    resp=true;
    timeout1 = false;
    activatedTimer1=false;

    event=Event::EV_Gps_timeout_to_activate;
  }
  
  return resp;

}


void Gps::initTimerToReadGPs()
{
    //se activa el timer
    timeout2 = false;
    activatedTimer2=true;

    //se inicializa el contado de tiempo
    last_current_time2 = millis();
    
    DebugPrint("Se activo el Timer  de lectura del GPS!!");
}

bool Gps::checkTimeoutToReadGps()
{

  bool resp=false;

  //sale si no se activo el timer al llamar initTimer()
  if(!activatedTimer2)
    return resp;

  //Si se activo el timer se ejecuta
  long current_time = millis();
  int diff = current_time - last_current_time2;
  timeout2 = (diff > UMBRAL_TIMEOUT_TO_READ_GPS) ? true : false;

  //si se cumplio el tiempo del timer
  if (timeout2)
  {    
    resp=true;
    timeout2 = false;
    activatedTimer2=false;
    DebugPrint("******Se cumplio timer lectura GPS");

    event=Event::EV_Gps_timeout_to_read;
  }
  
  return resp;

}

//Esta metodo sirve para cuando ocurre el evento EV_Gps_coordinates y en lugar de  EV_Gps_timeout_to_read,
//como ambos eventos transicionan al esto ST_doing_control. Ya que al suceder el evento EV_Gps_coordinates, 
//el timer2 va a serguir activado entonces hay que desacitivarlo, para que no siga corriendo.

void Gps::desactivateTimerToReadGps()
{
    timeout2 = false;
    activatedTimer2=false;
    DebugPrint("Se desactivo el Timer  de lectura del GPS!!");
}