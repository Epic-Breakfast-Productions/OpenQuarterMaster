#include <FastLED.h>
#include "MssEngine.h"

#define LED_PIN     2
#define NUM_LEDS    24
#define DELAY    500

CRGB leds[NUM_LEDS];
MssModCapabilities capabilities(
        true,
        true,
        true
);
MssModInfo info(
        "1.0.0",
        "test-01",
        64,
        &capabilities
);


void setup() {

    FastLED.addLeds<WS2812B, LED_PIN, GRB>(leds, NUM_LEDS);



}

void loop() {

    for (int i = 0; i <= (NUM_LEDS - 1); i += 3) {
        leds[i] = CRGB(255, 0, 0);
        leds[i + 1] = CRGB(255, 0, 0);
        leds[i + 2] = CRGB(255, 0, 0);
        FastLED.show();
        delay(DELAY);
    }

//    for (int i = 0; i <= (NUM_LEDS - 1); i++) {
//        leds[i] = CRGB(0, 0, 0);
//    }
//    FastLED.show();

    for (int i = (NUM_LEDS - 1); i >= 0; i -= 3) {
        leds[i] = CRGB(0, 255, 0);
        leds[i - 1] = CRGB(0, 255, 0);
        leds[i - 2] = CRGB(0, 255, 0);
        FastLED.show();
        delay(DELAY);
    }
    for (int i = 0; i <= (NUM_LEDS - 1); i += 3) {
        leds[i] = CRGB(0, 0, 255);
        leds[i + 1] = CRGB(0, 0, 255);
        leds[i + 2] = CRGB(0, 0, 255);
        FastLED.show();
        delay(DELAY);
    }
    for (int i = (NUM_LEDS - 1); i >= 0; i -= 3) {
        leds[i] = CRGB(255, 0, 255);
        leds[i - 1] = CRGB(255, 0, 255);
        leds[i - 2] = CRGB(255, 0, 255);
        FastLED.show();
        delay(DELAY);
    }
}
