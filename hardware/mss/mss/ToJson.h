#ifndef TO_JSON_H
#define TO_JSON_H

#include <ArduinoJson.h>

class ToJson {
public:
    virtual JsonDocument* toJson() = 0;
};

#endif
