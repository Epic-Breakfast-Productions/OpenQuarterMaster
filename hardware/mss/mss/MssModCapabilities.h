#ifndef MSS_MOD_CAPABILITIES_H
#define MSS_MOD_CAPABILITIES_H

#include "AddToJson.h"

class MssModCapabilities : public AddToJson {
private:
    bool blockLights;
    bool blockLightColor;
    bool blockLightBrightness;
public:
    MssModCapabilities(
            bool blockLights,
            bool blockLightColor,
            bool blockLightBrightness
    );

    void addToJson(JsonObject* obj);
};

#endif
