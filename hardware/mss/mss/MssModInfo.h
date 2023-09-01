#ifndef MSS_MOD_INFO_H
#define MSS_MOD_INFO_H

#include <ArduinoJson.h>
#include "AddToJson.h"
#include "MssModCapabilities.h"

class MssModInfo : public AddToJson {
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

    void addToJson(JsonObject& doc){
        doc[F("specVersion")] = this->specVersion;
        doc[F("serialId")] = this->serialId;
        doc[F("numBlocks")] = MSS_VAR_NBLOCKS;

        JsonObject capabilities = doc.createNestedObject(F("capabilities"));
        this->capabilities.addToJson(capabilities);
    }

};

#endif
