#include <WiFi.h>
#include <WebServer.h>

// === Configuración WiFi (red 2.4 GHz) ===
const char* WIFI_SSID = "Megacable_2.4G_ECAC";
const char* WIFI_PASS = "Yy7ErPDa";

// Puerto del servidor HTTP
WebServer server(80);

// Estado ficticio de un actuador
bool deviceOn = false;

// JSON con encabezados (CORS + Content-Type)
void sendJson(int code, const String& payload) {
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
  server.send(code, "application/json", payload);
}

// Construcción de un JSON simple
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

// Manejo genérico de preflight CORS
void handleOptions() {
  // 204 No Content con headers CORS
  server.sendHeader("Access-Control-Allow-Origin", "*");
  server.sendHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
  server.sendHeader("Access-Control-Allow-Headers", "Content-Type");
  server.send(204); 

// === Rutas ===
void handleRoot() {
  String info =
    "{"
      "\"ok\":true,"
      "\"endpoints\":[\"/temperature\",\"/humidity\",\"/on\",\"/off\"],"
      "\"note\":\"Respuestas de testing en formato JSON\""
    "}";
  sendJson(200, info);
}

void handleTemperature() {
  String data = "{\"message\":\"testing temperature\",\"value\":null,\"units\":null}";
  sendJson(200, makeJson("temperature", data));
}

void handleHumidity() {
  String data = "{\"message\":\"testing humidity\",\"value\":null,\"units\":null}";
  sendJson(200, makeJson("humidity", data));
}

void handleOn() {
  deviceOn = true;  // Solo cambia el estado local, no controla hardware real aún
  String data = "{\"message\":\"testing on\",\"device_on\":true}";
  sendJson(200, makeJson("on", data));
}

void handleOff() {
  deviceOn = false;
  String data = "{\"message\":\"testing off\",\"device_on\":false}";
  sendJson(200, makeJson("off", data));
}

void handleNotFound() {
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

  Serial.println();
  Serial.println("== ESP32 HTTP JSON Test Server ==");
  Serial.printf("Conectando a WiFi SSID: %s\n", WIFI_SSID);

  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);

  unsigned long start = millis();
  while (WiFi.status() != WL_CONNECTED) {
    delay(400);
    Serial.print(".");
    if (millis() - start > 20000) { // 20s timeout
      Serial.println("\nNo se pudo conectar a WiFi. Reiniciando...");
      ESP.restart();
    }
  }

  Serial.printf("\nConectado. IP: %s\n", WiFi.localIP().toString().c_str());

  // Rutas GET
  server.on("/",           HTTP_GET,     handleRoot);
  server.on("/temperature",HTTP_GET,     handleTemperature);
  server.on("/humidity",   HTTP_GET,     handleHumidity);
  server.on("/on",         HTTP_GET,     handleOn);
  server.on("/off",        HTTP_GET,     handleOff);

  // Preflight OPTIONS para todas esas rutas
  server.on("/",            HTTP_OPTIONS, handleOptions);
  server.on("/temperature", HTTP_OPTIONS, handleOptions);
  server.on("/humidity",    HTTP_OPTIONS, handleOptions);
  server.on("/on",          HTTP_OPTIONS, handleOptions);
  server.on("/off",         HTTP_OPTIONS, handleOptions);

  // 404
  server.onNotFound(handleNotFound);

  server.begin();
  Serial.println("Servidor HTTP iniciado en puerto 80.");
  Serial.println("Prueba en tu navegador o con curl:");
  Serial.println("  http://<IP_ESP32>/temperature");
  Serial.println("  http://<IP_ESP32>/humidity");
  Serial.println("  http://<IP_ESP32>/on");
  Serial.println("  http://<IP_ESP32>/off");
}

void loop() {
  server.handleClient();
}
