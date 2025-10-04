# Eco-Tochi — Configuración de entorno 

**Objetivo:** Dejar operativo el entorno para desarrollar Eco-Tochi: IDEs instalados, ESP32 detectable, Wi‑Fi funcionando con endpoints de prueba y app Android creada.

---

## 1) Estructura del repositorio

```
eco-tochi/
├─ README.md
├─ docs/
│  ├─ screenshots/
│  │  ├─ ide_install_ok.png
│  │  ├─ blink_serial_ok.png
│  │  ├─ wifi_ip_ok.png
│  │  ├─ curl_temperature_ok.png
│  │  └─ android_project_created.png
│  └─ avance_2025-10-03.md
├─ firmware/
│  ├─ BlinkSerial/
│  │  └─ BlinkSerial.ino
│  └─ HttpJsonServer/
│     └─ HttpJsonServer.ino
├─ android/
│  └─ EcoTochiApp/         # Proyecto Android (API 25+)
└─ test/
   └─ jmeter/
      ├─ README-jmeter.md
      └─ EcoTochi_HTTP_TestPlan.jmx  # (opcional)
```

---

## 2) Entorno instalado

- **Arduino IDE** (versión: 2.3.6)
- **Paquete de placas ESP32** (Espressif Systems)  
- **Driver CP210x VCP** (Windows) para puerto COM del ESP32
- **Android Studio** (proyecto “Eco-Tochi”, **Android 7.1+ / API 25**)

*Evidencia:* capturas en `docs/screenshots/`.

---

## 3) Prueba “Blink + Serial”

**Ruta:** `firmware/BlinkSerial/BlinkSerial.ino`

Mensajes por Serial a **115200**:
```
[Eco-Tochi] Blink + Serial listo a 115200
LED: ON / OFF …
```

**Cómo ejecutar**
1. Conectar la placa (ver puerto COM en *Tools → Port*).
2. *Tools → Board*: **ESP32 Dev Module**
3. Subir el sketch y abrir **Serial Monitor** a **115200**.

*Evidencia:* `docs/screenshots/blink_serial_ok.png`.

---

## 4) Servidor Wi‑Fi (HTTP + JSON)

**Ruta:** `firmware/HttpJsonServer/HttpJsonServer.ino`

**Importante:**  El ESP32 solo se conecta a redes 2.4 GHz.

Editar las credenciales en el .ino:
```cpp
const char* WIFI_SSID = "Megacable_2.4G_ECAC";
const char* WIFI_PASS = "Yy7ErPDa";
```

**Endpoints**
- `GET /` — índice con lista de endpoints
- `GET /temperature` — JSON ficticio de temperatura
- `GET /humidity` — JSON ficticio de humedad
- `GET /on` — activa estado `deviceOn`
- `GET /off` — desactiva estado `deviceOn`

**Salida esperada (Serial, 115200)**
```
== ESP32 HTTP JSON Test Server ==
Conectando a WiFi SSID: <SSID>
....
Conectado. IP: 192.168.0.123
Servidor HTTP iniciado en puerto 80.
```

**Pruebas rápidas (se reemplaza IP)**
```bash
curl http://192.168.0.123/
curl http://192.168.0.123/temperature
curl http://192.168.0.123/humidity
curl http://192.168.0.123/on
curl http://192.168.0.123/off
```

*Evidencia:* `docs/screenshots/wifi_ip_ok.png`, `docs/screenshots/curl_temperature_ok.png`.

---

## 5) Android

- Proyecto **Eco-Tochi** creado en Android Studio (API 25+).
- *Evidencia:* `docs/screenshots/android_project_created.png`.

---

## 6) Reproducibilidad (paso a paso)

1. Instalar **Arduino IDE**, paquete **ESP32** y **driver CP210x VCP**.
2. Conectar el ESP32 y seleccionar el **puerto COM** correcto.
3. Subir `BlinkSerial.ino` y verificar mensajes en **Serial (115200)**.
4. Subir `HttpJsonServer.ino` con SSID/clave correctos.
5. Leer la **IP** en Serial y probar endpoints con **curl** o navegador.

---

## 7) Roadmap inmediato
- **Pruebas con JMeter** (Smoke, Carga Ligera, Resiliencia).
- Persistencia de configuración (NVS/Preferences) y pruebas de reinicio.

---


## 9) Avance al 2025‑10‑03

- **Instalación de IDEs y librerías:** Terminado
- **Conexión y pruebas con ESP32:** Terminado (Blink + Serial 115200)
- **Configuración de comunicación (Wi‑Fi):** Terminado (HTTP + JSON con endpoints)
- **Validación del entorno:**  (EN PROGRESO BT/JMeter/persistencia)


## Autora

* **Michelle Chavez Gutierrez** 