#ifndef MSS_BLOCK_LIGHT_SETTING_H
#define MSS_BLOCK_LIGHT_SETTING_H

#include <ArduinoJson.h>
#include <FastLED.h>

enum PowerState {
    ON, OFF, FLASHING
};

class BlockLightSetting {
private:
    PowerState powerState = OFF;
    CRGB color = CRGB(0,255,0);
//    byte brightness = 255;
public:

    CRGB getColor(){
        return this->color;
    }
    PowerState getPowerState(){
        return this->powerState;
    }

    void setColor(CRGB newColor) {
        this->color = newColor;
    }

    void turnOn(){
        this->powerState = ON;
    }
    void turnOff(){
        this->powerState = OFF;
    }
    void setPowerState(PowerState state){
        this->powerState = state;
    }

};

#endif
