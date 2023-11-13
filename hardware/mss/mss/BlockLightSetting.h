#ifndef MSS_BLOCK_LIGHT_SETTING_H
#define MSS_BLOCK_LIGHT_SETTING_H

#include <ArduinoJson.h>
#include <FastLED.h>
#include "ColorUtils.h"



class BlockLightSetting {
private:
    PowerState powerState = OFF;
    LightColor color = LightColor::GREEN;
public:

    LightColor getColor() {
        return this->color;
    }

    void setColor(LightColor newColor) {
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
