// Blink + mensajes por Serial a 115200

#include <Arduino.h>

#define LED_PIN 2  

void setup() {
  Serial.begin(115200);
  delay(200);
  Serial.println("\n[Eco-Tochi] Blink + Serial listo a 115200");

  pinMode(LED_PIN, OUTPUT);
}

void loop() {
  digitalWrite(LED_PIN, HIGH);
  Serial.println("LED: ON");
  delay(500);

  digitalWrite(LED_PIN, LOW);
  Serial.println("LED: OFF");
  delay(500);
}
