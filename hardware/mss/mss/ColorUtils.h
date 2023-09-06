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
enum LightColor {
    RED, GREEN, BLUE
};

class ColorUtils{
public:

    static LightColor getRandColor(){
        //https://stackoverflow.com/a/2999028
        return static_cast<LightColor>(rand() % BLUE);
//
//
//        CRGB color(0, 0, 0);
//
//        while (color == CRGB(0, 0, 0)) {
//            byte r = 0,
//                    g = 0,
//                    b = 0;
//            if (random(0, 2)) {
//                r = random(MSS_MIN_RAND_COLOR_VAL, 255);
//            }
//            if (random(0, 2)) {
//                g = random(MSS_MIN_RAND_COLOR_VAL, 255);
//            }
//            if (random(0, 2)) {
//                b = random(MSS_MIN_RAND_COLOR_VAL, 255);
//            }
//            color = CRGB(r, g, b);
//        }
//        return color;
    }

    static LightColor getColorFromString(const char* colorStr){
        //TODO:: this
        return ColorUtils::getRandColor();
    }

    static CRGB getCRGBFromColor(LightColor color){
        switch (color) {
            case LightColor::RED:
                return CRGB(255, 0, 0);
            case LightColor::GREEN:
                return CRGB(0, 255, 0);
            case LightColor::BLUE:
                return CRGB(0, 0, 255);
        }
    }
};

#endif //MSS_COLORUTILS_H
