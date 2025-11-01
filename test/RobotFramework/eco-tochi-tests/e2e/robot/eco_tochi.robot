*** Settings ***
Documentation     Suite E2E Eco-Tochi (Wi-Fi HTTP): valida endpoints JSON del ESP32.
Resource          eco_tochi.resource
Suite Setup       Conectar
Suite Teardown    Cerrar

*** Test Cases ***
Root lista endpoints esperados
    Validar Root

ON pone el dispositivo en true
    Encender Y Validar

OFF pone el dispositivo en false
    Apagar Y Validar

Temperature y Humidity responden JSON válido
    Validar Temperatura
    Validar Humedad

Ruta inexistente devuelve 404 (negativo)
    ${resp}=    GET On Session    esp    /no-existe    expected_status=404
    Should Be Equal As Integers    ${resp.status_code}    404
