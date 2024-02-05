#ifndef EVENTS_H
#define EVENTS_H

#include <WString.h>

enum Event
{
    EV_Continue,
    EV_Person_out_area,
    EV_Gps_coordinates,
    EV_Gps_activate_timeout,
    EV_Notifiy_broker
};

const String events_s[] = {
    "Continue",
    "Persona Fuera De Area",
    "Se leyo coordenadas de GPS",
    "Timeout de GPS",
    "Enviar mensaje a Broker"

  };

#define MAX_EVENTS 5

#endif
