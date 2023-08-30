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
    CRGB color = CRGB(0, 255, 0);
public:

    CRGB getColor() {
        return this->color;
    }

    void setColor(CRGB newColor) {
        this->color = newColor;
    }

    void setRandColor() {
        CRGB color(0, 0, 0);

        while (color == CRGB(0, 0, 0)) {
            byte r = 0,
                    g = 0,
                    b = 0;
            if (random(0, 2)) {
                r = random(25, 255);
            }
            if (random(0, 2)) {
                g = random(25, 255);
            }
            if (random(0, 2)) {
                b = random(25, 255);
            }
            color = CRGB(r, g, b);
        }
        this->setColor(color);
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
