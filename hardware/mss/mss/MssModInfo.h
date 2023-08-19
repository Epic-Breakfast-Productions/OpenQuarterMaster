#ifndef MSS_MOD_INFO_H
#define MSS_MOD_INFO_H

#include <ArduinoJson.h>
#include "ToJson.h"
#include "MssModCapabilities.h"

class MssModInfo : public ToJson {
private:
    String specVersion;
    String serialId;
    unsigned int numBlocks;
    MssModCapabilities* capabilities;
public:
    MssModInfo(
            String specVersion,
            String serialId,
            unsigned int numBlocks,
            MssModCapabilities* capabilities
    );

    JsonDocument* toJson();
};

#endif
