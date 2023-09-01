#ifndef MSS_CONNECTOR_H
#define MSS_CONNECTOR_H

#include <ArduinoJson.h>
#include "MssCommand.h"
#include "MssCommandResponse.h"

class MssConnector {
private:
public:
    virtual void init() = 0;

    virtual bool hasCommand() = 0;

    virtual Command getCommand() = 0;

//    virtual void send(String message) = 0;

    virtual void send(JsonDocument &payload) = 0;
//    {
//        String output;
//        serializeJson(payload, output);
//        this->send(output);
//    }

    void send(ResponseType status, AddToJson &payload) {
        StaticJsonDocument<256> doc;

        doc[F("status")] = responseTypeFromEnum(status);
        JsonObject response = doc.createNestedObject(F("response"));
        payload.addToJson(response);

        this->send(doc);
    }

    void send(AddToJson &payload) {
        this->send(ResponseType::OK, payload);
    }

    void send(
            ResponseType status
    ) {
        DynamicJsonDocument doc(16);

        doc[F("status")] = responseTypeFromEnum(status);
        this->send(doc);
    }

    void send(
            ResponseType status,
            const char *description
    ) {
        DynamicJsonDocument doc(256);

        doc[F("status")] = responseTypeFromEnum(status);
        doc[F("description")] = description;

        this->send(doc);
    }

    void send(
            ResponseType status,
            const __FlashStringHelper *description
    ) {
        DynamicJsonDocument doc(256);

        doc[F("status")] = responseTypeFromEnum(status);
        doc[F("description")] = description;

        this->send(doc);
    }

};

#endif
