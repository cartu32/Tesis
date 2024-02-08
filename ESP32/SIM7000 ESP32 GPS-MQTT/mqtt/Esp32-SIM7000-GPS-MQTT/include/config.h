#ifndef CONFIG_H
#define CONFIG_H

#include <BluetoothSerial.h>

extern BluetoothSerial BT;

#define SERIAL_DEBUG_ENABLED 1
#define USB 0
#define BLUETOOTH 1
#define MODE_DEBUG BLUETOOTH

#if SERIAL_DEBUG_ENABLED
  #define DebugPrint(str)\
      {\
        if(MODE_DEBUG==USB)\
          Serial.println(str);\
        else\
          BT.println(str);\
      }
#else
  #define DebugPrint(str)
#endif

#define DebugPrintEstado(estado,evento)\
      {\
        String est = estado;\
        String evt = evento;\
        String str;\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
        str = "EST-> [" + est + "]: " + "EVT-> [" + evt + "].";\
        DebugPrint(str);\
        str = "-----------------------------------------------------";\
        DebugPrint(str);\
      }

#endif

#if SERIAL_DEBUG_ENABLED
  #define initDebug()\
      {\
      if(MODE_DEBUG==BLUETOOTH)\
          BT.begin("ESP32_SIM7000");\
      else\
          Serial.begin(115200);\
      }
#endif