//
// Created by gstewart on 9/5/23.
//

#ifndef MSS_COLORUTILS_H
#define MSS_COLORUTILS_H

#include <Arduino.h>
#include <FastLED.h>

#define MSS_MIN_RAND_COLOR_VAL 128

class ColorUtils{
public:

    static CRGB getRandColor(){
        CRGB color(0, 0, 0);

        while (color == CRGB(0, 0, 0)) {
            byte r = 0,
                    g = 0,
                    b = 0;
            if (random(0, 2)) {
                r = random(MSS_MIN_RAND_COLOR_VAL, 255);
            }
            if (random(0, 2)) {
                g = random(MSS_MIN_RAND_COLOR_VAL, 255);
            }
            if (random(0, 2)) {
                b = random(MSS_MIN_RAND_COLOR_VAL, 255);
            }
            color = CRGB(r, g, b);
        }
        return color;
    }

    static CRGB getColorFromString(const char* colorStr){
        //TODO:: this
        return ColorUtils::getRandColor();
    }
};

#endif //MSS_COLORUTILS_H
