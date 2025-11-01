*** Settings ***
Library    RequestsLibrary
Library    Collections

*** Variables ***
${BASE_URL}      http://192.168.1.9    # cámbialo con -v BASE_URL:...
${TIMEOUT_S}     5

*** Keywords ***
Conectar
    Create Session    esp    ${BASE_URL}    timeout=${TIMEOUT_S}

Cerrar
    Delete All Sessions


GET JSON
    [Arguments]    ${path}
    ${resp}=    GET On Session    esp    ${path}
    Should Be Equal As Integers    ${resp.status_code}    200
    ${json}=    Set Variable    ${resp.json()}
    RETURN    ${json}


Debe Responder OK Con Endpoint
    [Arguments]    ${json}    ${endpoint}
    Should Be True    ${json["ok"]}
    Should Be Equal   ${json["endpoint"]}    ${endpoint}

Validar Root
    ${json}=    GET JSON    /
    Should Be True    ${json["ok"]}
    ${eps}=    Set Variable    ${json["endpoints"]}
    List Should Contain Value    ${eps}    /temperature
    List Should Contain Value    ${eps}    /humidity
    List Should Contain Value    ${eps}    /on
    List Should Contain Value    ${eps}    /off

Encender Y Validar
    ${json}=    GET JSON    /on
    Debe Responder OK Con Endpoint    ${json}    on
    Should Be Equal    ${json["data"]["device_on"]}    ${True}

Apagar Y Validar
    ${json}=    GET JSON    /off
    Debe Responder OK Con Endpoint    ${json}    off
    Should Be Equal    ${json["data"]["device_on"]}    ${False}

Validar Temperatura
    ${json}=    GET JSON    /temperature
    Debe Responder OK Con Endpoint    ${json}    temperature
    Dictionary Should Contain Key    ${json["data"]}    message

Validar Humedad
    ${json}=    GET JSON    /humidity
    Debe Responder OK Con Endpoint    ${json}    humidity
    Dictionary Should Contain Key    ${json["data"]}    message
