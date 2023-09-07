//
// Created by gstewart on 9/5/23.
//

#ifndef MSS_COLORUTILS_H
#define MSS_COLORUTILS_H

#include <Arduino.h>
#include <FastLED.h>

#define MSS_MIN_RAND_COLOR_VAL 128

enum PowerState {
    ON, OFF, FLASHING
};

//TODO:: consider how to support hex codes, or: http://fastled.io/docs/struct_c_r_g_b.html#aeb40a08b7cb90c1e21bd408261558b99
enum LightColor {
    RED, GREEN, BLUE,
    YELLOW, MAGENTA, ORANGE, PURPLE,
    WHITE,
    RANDOM
};

class ColorUtils {
public:

    static LightColor getRandColor() {
        //https://stackoverflow.com/a/2999028
        return static_cast<LightColor>(rand() % LightColor::RANDOM);
    }

    static LightColor getColorFromString(const char *colorStr) {
//        Serial.print(F("DEBUG:: Getting color:" ));
//        Serial.println(colorStr);

        if (strcmp_P(colorStr, (PGM_P) F("RED")) == 0) {
            return LightColor::RED;
        }
        if (strcmp_P(colorStr, (PGM_P) F("GREEN")) == 0) {
            return LightColor::GREEN;
        }
        if (strcmp_P(colorStr, (PGM_P) F("BLUE")) == 0) {
            return LightColor::BLUE;
        }
        if (strcmp_P(colorStr, (PGM_P) F("WHITE")) == 0) {
            return LightColor::WHITE;
        }
        if (strcmp_P(colorStr, (PGM_P) F("YELLOW")) == 0) {
            return LightColor::YELLOW;
        }
        if (strcmp_P(colorStr, (PGM_P) F("ORANGE")) == 0) {
            return LightColor::ORANGE;
        }
        if (strcmp_P(colorStr, (PGM_P) F("MAGENTA")) == 0) {
            return LightColor::MAGENTA;
        }
        if (strcmp_P(colorStr, (PGM_P) F("PURPLE")) == 0) {
            return LightColor::PURPLE;
        }

        return ColorUtils::getRandColor();
    }

    static CRGB getCRGBFromColor(LightColor color) {
        switch (color) {
            case LightColor::RED:
                return CRGB(255, 0, 0);
            case LightColor::GREEN:
                return CRGB(0, 255, 0);
            case LightColor::BLUE:
                return CRGB(0, 0, 255);
            case LightColor::WHITE:
                return CRGB(255, 255, 255);
            case LightColor::YELLOW:
                return CRGB(255, 255, 0);
            case LightColor::MAGENTA:
                return CRGB(255, 0, 255);
            case LightColor::ORANGE:
                return CRGB(255, 80, 0);
            case LightColor::PURPLE:
                return CRGB(138,43,226);
            case LightColor::RANDOM:
                return ColorUtils::getCRGBFromColor(ColorUtils::getRandColor());
        }
    }
};

#endif //MSS_COLORUTILS_H
