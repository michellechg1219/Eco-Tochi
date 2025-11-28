#include <WiFi.h>
#include <WebServer.h>
#include "max6675.h"   // Librería del MAX6675

// === Configura tu WiFi ===
const char* WIFI_SSID = "Megacable_2.4G_ECAC";
const char* WIFI_PASS = "Yy7ErPDa";

// Puerto del servidor HTTP
WebServer server(80);

// Estado de actuador lógico
bool deviceOn = false;

// ======================================================
// === FC-28: Sensor de humedad de suelo (analógico) ====
// ======================================================
const int SOIL_PIN = 34;   // pin ADC del ESP32 (cámbialo si usas otro)

// Valores de calibración (AJÚSTALOS con tus lecturas)
int SOIL_DRY = 3500;       // lectura en suelo muy seco
int SOIL_WET = 1300;       // lectura en suelo muy húmedo

// ======================================================
// === MAX6675: Termopar tipo K para temperatura ========
// ======================================================
// Pines (puedes cambiarlos si quieres)
int thermoSO  = 19;   // SO / DO del MAX6675
int thermoCS  = 5;    // CS del MAX6675
int thermoCLK = 18;   // SCK del MAX6675

MAX6675 thermocouple(thermoCLK, thermoCS, thermoSO);

// ======================================================
// === RELÉ controlado por /on y /off ===================
// ======================================================
const int RELAY_PIN = 23;   // GPIO para el relé (cámbialo si quieres)

// ----------------- UTILIDADES -----------------
int getRandom4to10() {
  return random(4, 10);
}

// Utilidad: envía JSON con encabezados correctos
void sendJson(int code, const String& payload) {
  server.sendHeader("Access-Control-Allow-Origin", "*");           // CORS simple
  server.sendHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
  server.send(code, "application/json", payload);
}

// Construye un JSON mínimo (sin librerías externas)
String makeJson(const String& endpoint, const String& dataJson) {
  unsigned long ts = millis();
  String s = "{";
  s += "\"ok\":true,";
  s += "\"endpoint\":\"" + endpoint + "\",";
  s += "\"data\":" + dataJson + ",";
  s += "\"ts\":" + String(ts);
  s += "}";
  return s;
}

// === Rutas ===
void handleRoot() {
  // DEBUG
  Serial.println("[HTTP] GET /");

  String info =
    "{"
      "\"ok\":true,"
      "\"endpoints\":[\"/temperature\",\"/humidity\",\"/ph\",\"/on\",\"/off\"],"
      "\"note\":\"API JSON para pruebas con sensores reales\""
    "}";
  sendJson(200, info);
}

// ======================================================
// === ENDPOINT /temperature usando MAX6675 =============
// ======================================================
void handleTemperature() {
  // DEBUG
  Serial.println("[HTTP] GET /temperature");

  double tempC = thermocouple.readCelsius();

  Serial.print("MAX6675 temp C: ");
  Serial.println(tempC);

  String data;

  if (isnan(tempC) || tempC < -50 || tempC > 1350) {
    data = "{";
    data += "\"message\":\"error reading temperature\",";
    data += "\"value\":null,";
    data += "\"units\":\"C\"";
    data += "}";
    sendJson(500, makeJson("temperature", data));
    return;
  }

  data = "{";
  data += "\"message\":\"thermocouple temperature\",";
  data += "\"value\":" + String(tempC, 2) + ",";
  data += "\"units\":\"C\"";
  data += "}";

  sendJson(200, makeJson("temperature", data));
}

// ======================================================
// === ENDPOINT /humidity usando FC-28 ==================
// ======================================================
void handleHumidity() {
  // DEBUG
  Serial.println("[HTTP] GET /humidity");

  int raw = analogRead(SOIL_PIN);

  Serial.print("FC-28 raw: ");
  Serial.println(raw);

  int rawClamped = constrain(raw, SOIL_WET, SOIL_DRY);

  int humidityPercent = map(rawClamped, SOIL_DRY, SOIL_WET, 0, 100);
  humidityPercent = constrain(humidityPercent, 0, 100);

  String data = "{";
  data += "\"message\":\"soil moisture\",";
  data += "\"raw\":" + String(raw) + ",";
  data += "\"value\":" + String(humidityPercent) + ",";
  data += "\"units\":\"%\"";
  data += "}";

  sendJson(200, makeJson("humidity", data));
}

void handlePh() {
  // DEBUG
  Serial.println("[HTTP] GET /ph");

  int r = getRandom4to10();
  String data = "{\"message\":\"testing ph\",\"value\":" + String(r) + ",\"units\":null}";
  sendJson(200, makeJson("ph", data));
}

// ======================================================
// === ENDPOINT /on: enciende relé ======================
// ======================================================
void handleOn() {
  // DEBUG
  Serial.println("[HTTP] GET /on  -> ENCENDER RELÉ");

  deviceOn = true;

  // Si tu relé es activo en LOW, cambia HIGH por LOW
  digitalWrite(RELAY_PIN, HIGH);

  String data = "{";
  data += "\"message\":\"relay on\",";
  data += "\"device_on\":true";
  data += "}";

  sendJson(200, makeJson("on", data));
}

// ======================================================
// === ENDPOINT /off: apaga relé ========================
// ======================================================
void handleOff() {
  // DEBUG
  Serial.println("[HTTP] GET /off -> APAGAR RELÉ");

  deviceOn = false;

  // Si tu relé es activo en LOW, cambia LOW por HIGH
  digitalWrite(RELAY_PIN, LOW);

  String data = "{";
  data += "\"message\":\"relay off\",";
  data += "\"device_on\":false";
  data += "}";

  sendJson(200, makeJson("off", data));
}

void handleNotFound() {
  // DEBUG
  Serial.print("[HTTP] 404 Not Found -> ");
  Serial.println(server.uri());

  String s = "{"
    "\"ok\":false,"
    "\"error\":\"Not Found\","
    "\"path\":\"" + server.uri() + "\""
  "}";
  sendJson(404, s);
}

void setup() {
  Serial.begin(115200);
  delay(300);

  randomSeed(esp_random());

  // FC-28 como entrada
  pinMode(SOIL_PIN, INPUT);

  // Relé como salida
  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW); // relé apagado al inicio

  Serial.println();
  Serial.println("== ESP32 HTTP JSON Server con FC-28 + MAX6675 + RELÉ ==");

  Serial.printf("Conectando a WiFi SSID: %s\n", WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED) {
    delay(400);
    Serial.print(".");
    if (millis() - start > 20000) {
      Serial.println("\nNo se pudo conectar a WiFi. Reiniciando...");
      ESP.restart();
    }
  }

  Serial.printf("\nConectado. IP: %s\n", WiFi.localIP().toString().c_str());

  // Rutas
  server.on("/",          HTTP_GET, handleRoot);
  server.on("/temperature", HTTP_GET, handleTemperature);  // MAX6675
  server.on("/humidity",    HTTP_GET, handleHumidity);     // FC-28
  server.on("/ph",          HTTP_GET, handlePh);
  server.on("/on",          HTTP_GET, handleOn);           // Relé ON
  server.on("/off",         HTTP_GET, handleOff);          // Relé OFF

  server.onNotFound(handleNotFound);

  server.begin();
  Serial.println("Servidor HTTP iniciado.");
}

void loop() {
  server.handleClient();
}
