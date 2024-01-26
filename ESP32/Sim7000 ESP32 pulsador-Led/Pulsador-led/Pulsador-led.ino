// Declaramos el pin al que estará conectado el pulsador
int pinPulsador=33;
// Declaramos el pin al que estará conectado el led
int pinLed=32;
int pulsador=0;

void setup()
{
  Serial.begin(9600);
  //Determinamos que el pin del pulsador sera para recibir 
  pinMode(pinPulsador, INPUT);
  //Determinamos que el pin del led sera para salir
  pinMode(pinLed, OUTPUT);
}
void loop()
{
  pulsador=digitalRead(pinPulsador);
  Serial.println(pulsador);

  //Si la señal del pulsador es activa encendemos el led
  if (pulsador == HIGH) {
    digitalWrite(pinLed, HIGH);
  } 
  //de lo contrario apagamos el led
  else {
    digitalWrite(pinLed, LOW);
  }
  delay(100);
}