Este repositorio contiene el trabajo de Tesis de Maestría del Ing. Esteban Carnuccio, el cual consiste en el desarrollo de un prototipo de sistema de geocercas (Geofence) para el monitoreo de adultos mayores. El sistema desarrollado se denomina "AbuMonitor" y está compuesto por dos partes principales:

* **Smartphone**
* **Smartwatch**

Ambas aplicaciones funcionan sobre los Sistemas Operativos Android y Wear OS, por lo que fueron desarrolladas utilizando Android Studio como entorno de desarrollo y Kotlin como lenguaje de programación.

El smartphone es el encargado del procesamiento principal del sistema, incluyendo la detección y gestión de las áreas de geocerca.
Por su parte, el smartwatch se encarga de notificar al adulto mayor sobre los eventos de detección de geocercas, además de detectar caídas y alertar cuando ocurre dicho evento.

Adicionalmente, el smartphone actúa como un gateway entre el reloj y los contactos del adulto mayor. De esta manera, ante la ocurrencia de determinados eventos (por ejemplo, salida de una geocerca o detección de una caída), el sistema notifica lo sucedido a los contactos configurados mediante el envío de mensajes SMS.

---

## Estructura de directorios

El proyecto está compuesto por los siguientes directorios principales:

* **AbuMonitor**
* **APK**

Dentro del directorio AbuMonitor se encuentra el código fuente del proyecto Android, el cual se organiza en los siguientes subdirectorios:

* **Mobile**: contiene el código fuente de la aplicación para el smartphone.
* **Wear**: contiene el código fuente de la aplicación para el smartwatch.
* **Shared Library**: contiene bibliotecas compartidas utilizadas tanto por la aplicación del teléfono como por la del reloj.

Por otro lado, el directorio APK contiene los archivos APK generados, los cuales pueden instalarse en los dispositivos móviles y wearables compatibles.

---

## Demostración del funcionamiento del Sistema Abumonitor

Se generó una serie de nueve videos, en los cuales se explica detalladamente el funcionamiento de cada una de las funcionalidades del sistema AbuMonitor.

A continuación, se enumeran los videos que componen la demostración:

  1. [Introducción a la demostración](https://www.youtube.com/watch?v=5zlPaYfKqKQ)
  2. [Pantalla Principal](https://youtu.be/I5af_vEUFDk)
  3. [Menú Configuración](https://youtu.be/6okEt313hCg)
  4. [Definir Contactos](https://youtu.be/2k6AQZ4QdYY)
  5. [Creación de Area Normal de Geofencing](https://youtu.be/bzhAvdDtgJU)
  6. [Creación de Zonas Seguras](https://youtu.be/_UQ-u3-r_Uc)
  7. [Creación de Cita de Asistencia (ej: asistir a cita)](https://youtu.be/Ri-aVA3Bm5I)
  8. [Creación de Cita de Asistencia (ej: no asistir a cita)](https://youtu.be/2Z__qlds1Kg)
  9. [Validación y Alertas de caídas](https://youtu.be/Hr3wQPGS584) 