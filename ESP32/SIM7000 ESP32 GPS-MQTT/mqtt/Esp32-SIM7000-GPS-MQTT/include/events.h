#ifndef EVENTS_H
#define EVENTS_H

#include <WString.h>

enum Event
{
    Continue,
    PersonaFueraDeArea,
    CoordenadasGPS
};

const String events_s[] = {
    "Continue",
    "Persona Fuera De Area"
    "Se obtuvo coordenadas de GPS"
  };

#define MAX_EVENTS 3

#endif
