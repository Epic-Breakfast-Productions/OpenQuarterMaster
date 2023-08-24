#ifndef MSS_BLOCK_STATE_H
#define MSS_BLOCK_STATE_H

#include <ArduinoJson.h>
#include "BlockLightSetting.h"

class BlockState {
    unsigned int blockNum;
    BlockLightSetting lightSetting;
public:
    BlockState(
            unsigned int blockNum
    ) {
        this->blockNum = blockNum;
    }
    BlockState(){
        this->blockNum = 0;
    }

    BlockLightSetting* getLightSetting(){
        return &this->lightSetting;
    }
};

#endif
