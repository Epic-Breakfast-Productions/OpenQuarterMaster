#ifndef MSS_MOD_INFO_H
#define MSS_MOD_INFO_H

#include <ArduinoJson.h>
#include "ToJson.h"
#include "MssModCapabilities.h"

class MssModInfo : public ToJson {
private:
    const char * specVersion;
    const char * serialId;
    MssModCapabilities capabilities;
public:
    MssModInfo(
            const char * specVersion,
            const char * serialId,
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

        doc[F("specVersion")] = this->specVersion;
        doc[F("serialId")] = this->serialId;
        doc[F("numBlocks")] = MSS_VAR_NBLOCKS;

        JsonObject capabilities = doc.createNestedObject(F("capabilities"));
        this->capabilities.addToJson(&capabilities);

        return &doc;
    }

};

#endif
