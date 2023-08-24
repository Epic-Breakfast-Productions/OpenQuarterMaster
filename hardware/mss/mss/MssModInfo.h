#ifndef MSS_MOD_INFO_H
#define MSS_MOD_INFO_H

#include <ArduinoJson.h>
#include "ToJson.h"
#include "MssModCapabilities.h"

class MssModInfo : public ToJson {
private:
    String specVersion;
    String serialId;
    MssModCapabilities capabilities;
public:
    MssModInfo(
            String specVersion,
            String serialId,
            MssModCapabilities capabilities
    ) {
        this->specVersion = specVersion;
        this->serialId = serialId;
        this->capabilities = capabilities;
    }

    MssModInfo(){
    }

    JsonDocument *toJson() {
        DynamicJsonDocument doc(128);

        doc["specVersion"] = this->specVersion;
        doc["serialId"] = this->serialId;
        doc["numBlocks"] = MSS_VAR_NBLOCKS;

        JsonObject capabilities = doc.createNestedObject("capabilities");
        this->capabilities.addToJson(&capabilities);

        return &doc;
    }

};

#endif
