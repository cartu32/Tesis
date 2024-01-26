#ifndef EVENTS_H
#define EVENTS_H

#include <WString.h>

enum Event
{
    Continue,
    PersonaFueraDeArea
};

const String events_s[] = {
    "Continue",
    "Persona Fuera De Area"
  };

#define MAX_EVENTS 1

#endif