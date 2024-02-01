#ifndef UTILITIES_H
#define UTILITIES_H

#include <WString.h>

String splitter(String data, char separator, int index)
{
  int stringData = 0;
  String dataPart = "";

  for (int i = 0; i < data.length(); i++)
  {

    if (data[i] == separator)
    {

      stringData++;
    }
    else if (stringData == index)
    {

      dataPart.concat(data[i]);
    }
    else if (stringData > index)
    {

      return dataPart;
      break;
    }
  }

  return dataPart;
}

#endif