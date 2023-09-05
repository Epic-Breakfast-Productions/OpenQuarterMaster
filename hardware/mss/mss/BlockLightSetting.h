#ifndef MSS_BLOCK_LIGHT_SETTING_H
#define MSS_BLOCK_LIGHT_SETTING_H

#include <ArduinoJson.h>
#include <FastLED.h>
#include "ColorUtils.h"


enum PowerState {
    ON, OFF, FLASHING
};

class BlockLightSetting {
private:
    PowerState powerState = OFF;
    CRGB color = CRGB(0, 255, 0);
public:

    CRGB getColor() {
        return this->color;
    }

    void setColor(CRGB newColor) {
        this->color = newColor;
    }

    void setRandColor() {
        this->color = ColorUtils::getRandColor();
    }

    PowerState getPowerState() {
        return this->powerState;
    }

    void turnOn() {
        this->powerState = PowerState::ON;
    }

    void turnOff() {
        this->powerState = PowerState::OFF;
    }

    void setPowerState(PowerState state) {
        this->powerState = state;
    }

};

#endif
