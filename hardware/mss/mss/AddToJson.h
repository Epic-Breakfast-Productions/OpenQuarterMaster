#ifndef ADD_TO_JSON_H
#define ADD_TO_JSON_H

#include <ArduinoJson.h>

class AddToJson {
public:
    virtual void addToJson(JsonObject* obj) = 0;
};

#endif
