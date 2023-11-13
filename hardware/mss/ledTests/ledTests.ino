#include <FastLED.h>

#define MSS_VAR_NBLOCKS 64
#define MSS_VAR_NLEDS_PER_BLOCK 3
#define MSS_LED_PIN     2
#define MSS_NUM_LEDS MSS_VAR_NBLOCKS * MSS_VAR_NLEDS_PER_BLOCK
#define MSS_SPKR_PIN    9
#define NOTE_C4  262


#define DELAY    500

CRGB leds[MSS_NUM_LEDS];

void singleStripTest() {//Single strip test
    for (int i = 0; i <= (MSS_NUM_LEDS - 1); i += 3) {
        leds[i] = CRGB(255, 0, 0);
        leds[i + 1] = CRGB(255, 0, 0);
        leds[i + 2] = CRGB(255, 0, 0);
        FastLED.show();
        delay(DELAY);
    }

    for (int i = 0; i <= (MSS_NUM_LEDS - 1); i++) {
        leds[i] = CRGB(0, 0, 0);
    }
    FastLED.show();

    for (int i = (MSS_NUM_LEDS - 1); i >= 0; i -= 3) {
        leds[i] = CRGB(0, 255, 0);
        leds[i - 1] = CRGB(0, 255, 0);
        leds[i - 2] = CRGB(0, 255, 0);
        FastLED.show();
        delay(DELAY);
    }
    for (int i = 0; i <= (MSS_NUM_LEDS - 1); i += 3) {
        leds[i] = CRGB(0, 0, 255);
        leds[i + 1] = CRGB(0, 0, 255);
        leds[i + 2] = CRGB(0, 0, 255);
        FastLED.show();
        delay(DELAY);
    }
    for (int i = (MSS_NUM_LEDS - 1); i >= 0; i -= 3) {
        leds[i] = CRGB(255, 0, 255);
        leds[i - 1] = CRGB(255, 0, 255);
        leds[i - 2] = CRGB(255, 0, 255);
        FastLED.show();
        delay(DELAY);
    }
}

void wholeThingTest() {
    for (int i = 0; i <= (MSS_NUM_LEDS - 1); i += 3) {
        for (int j = 0; j <= (MSS_NUM_LEDS - 1); j++) {
            leds[j] = CRGB(0, 0, 0);
        }
        leds[i] = CRGB(255, 0, 0);
        leds[i + 1] = CRGB(255, 0, 0);
        leds[i + 2] = CRGB(255, 0, 0);
        FastLED.show();
        delay(DELAY);
    }
}

void setup() {
    pinMode(MSS_SPKR_PIN, OUTPUT);
	FastLED.addLeds<WS2812B, MSS_LED_PIN, GRB>(leds, MSS_NUM_LEDS);
    tone(MSS_SPKR_PIN, 2093, 250);
}

void loop() {
	wholeThingTest();
    tone(MSS_SPKR_PIN, 130, 125);
}
