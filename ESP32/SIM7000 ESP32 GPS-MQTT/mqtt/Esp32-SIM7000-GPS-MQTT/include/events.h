#ifndef EVENTS_H
#define EVENTS_H

#include <WString.h>

enum Event
{
    EV_Continue,
    EV_Person_out_area,
    EV_Gps_coordinates,
    EV_Gps_timeout_to_activate,
    EV_Gps_timeout_to_read
};

const String events_s[] = {
    "Continue",
    "Persona Fuera De Area",
    "Se leyo coordenadas de GPS",
    "Timeout para activar el GPS",
    "Timeout para leer el GPS"

  };

#define MAX_EVENTS 5

#endif
