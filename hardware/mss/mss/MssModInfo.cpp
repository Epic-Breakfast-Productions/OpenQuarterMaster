#include "MssModInfo.h"

MssModInfo::MssModInfo(String specVersion, String serialId, unsigned int numBlocks, MssModCapabilities* capabilities) {
    this->specVersion = specVersion;
    this->serialId = serialId;
    this->numBlocks = numBlocks;
    this->capabilities = capabilities;
}

JsonDocument *MssModInfo::toJson() {
    DynamicJsonDocument doc(128);

    doc["specVersion"] = this->specVersion;
    doc["serialId"] = this->serialId;
    doc["numBlocks"] = this->numBlocks;

    JsonObject capabilities = doc.createNestedObject("capabilities");
    this->capabilities->addToJson(&capabilities);

    return &doc;
}

