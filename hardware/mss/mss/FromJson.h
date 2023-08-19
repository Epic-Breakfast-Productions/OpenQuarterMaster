#ifndef FROM_JSON_H
#define FROM_JSON_H

#include <ArduinoJson.h>

class FromJson {
public:
    static virtual FromJson* setupFromJson(JsonDocument json) = 0;
};

#endif
