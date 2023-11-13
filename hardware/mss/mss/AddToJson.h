#ifndef ADD_TO_JSON_H
#define ADD_TO_JSON_H

#include <ArduinoJson.h>

class AddToJson {
public:
    virtual void addToJson(JsonObject& obj) = 0;
    void addToJson(JsonDocument& doc){
        JsonObject object = doc.to<JsonObject>();
        this->addToJson(object);
    }
};

#endif
