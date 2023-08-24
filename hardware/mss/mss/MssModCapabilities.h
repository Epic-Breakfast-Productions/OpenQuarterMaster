#ifndef MSS_MOD_CAPABILITIES_H
#define MSS_MOD_CAPABILITIES_H

#include "AddToJson.h"

#ifndef MSS_ALLOW_BULK_OPS
#define MSS_ALLOW_BULK_OPS false;
#endif


/**
 * TODO:: add bulk ops capability flag
 */
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
    ){
        this->blockLights = blockLights;
        this->blockLightColor = blockLightColor;
        this->blockLightBrightness = blockLightBrightness;
    }
    /**
     * Required for existance in class elsewhere
     */
    MssModCapabilities(): MssModCapabilities(false, false, false){
    }

    void addToJson(JsonObject* obj){
        (*obj)["bulkOps"] = MSS_ALLOW_BULK_OPS;
        (*obj)["blockLights"] = this->blockLights;
        (*obj)["blockLightColor"] = this->blockLightColor;
        (*obj)["blockLightBrightness"] = this->blockLightBrightness;
    }
};

#endif
