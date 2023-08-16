#include <FastLED.h>

#define LED_PIN     2
#define NUM_LEDS    32

CRGB leds[NUM_LEDS];

void setup() {

  FastLED.addLeds<WS2812B, LED_PIN, GRB>(leds, NUM_LEDS);

}

void loop() {

  for (int i = 0; i <= (NUM_LEDS - 1); i++) {
    leds[i] = CRGB ( 0, 0, 255);
    FastLED.show();
    delay(100);
  }
  for (int i = (NUM_LEDS - 1); i >= 0; i--) {
    leds[i] = CRGB ( 255, 0, 0);
    FastLED.show();
    delay(100);
  }
}
