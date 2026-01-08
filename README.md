## Tesis

Este repositorio contiene el trabajo de **Tesis de Maestría del Ing. Esteban Carnuccio**, el cual consiste en el desarrollo de un **prototipo de sistema de geocercas (Geofence)** para el monitoreo de adultos mayores.
El sistema desarrollado se denomina **AbuMonitor** y está compuesto por dos partes principales:

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