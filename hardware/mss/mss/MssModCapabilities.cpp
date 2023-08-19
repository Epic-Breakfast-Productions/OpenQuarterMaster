#include "MssModCapabilities.h"

MssModCapabilities::MssModCapabilities(bool blockLights, bool blockLightColor, bool blockLightBrightness) {
    this->blockLights = blockLights;
    this->blockLightColor = blockLightColor;
    this->blockLightBrightness = blockLightBrightness;
}

void MssModCapabilities::addToJson(JsonObject* obj) {
    (*obj)["blockLights"] = this->blockLights;
    (*obj)["blockLightColor"] = this->blockLightColor;
    (*obj)["blockLightBrightness"] = this->blockLightBrightness;
}
