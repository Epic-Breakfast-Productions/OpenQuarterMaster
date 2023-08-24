#ifndef MSS_BLOCK_LIGHT_SETTING_H
#define MSS_BLOCK_LIGHT_SETTING_H

#include <ArduinoJson.h>

enum PowerState {
    ON, OFF, FLASHING
};

class BlockLightSetting {
private:
    PowerState powerState = OFF;
    CRGBSet color;
//    byte brightness = 255;
public:

//    CRGB getColor(){
//        return this->color;
//    }

    BlockLightSetting(CRGBSet color) {
        this->color = color;
    }

    void setColor(CRGB newColor) {
        this->color = newColor;
    }

    void setColor(CRGBSet newColorSet) {
        this->color = newColorSet;
    }
};

#endif
