#ifndef STATES_H
#define STATES_H

#include <WString.h>

enum State
{
    ST_Doing_control,
    ST_Reading_Gps
};

const String states_s[] = {
  "Realizar Control",
  "Leyengdo GPS"
};

#define MAX_STATES 2

#endif