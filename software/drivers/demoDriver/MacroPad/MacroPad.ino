#include <Adafruit_SH110X.h>
#include <Adafruit_NeoPixel.h>
#include <RotaryEncoder.h>

// Create the neopixel strip with the built in definitions NUM_NEOPIXEL and PIN_NEOPIXEL
Adafruit_NeoPixel pixels = Adafruit_NeoPixel(NUM_NEOPIXEL, PIN_NEOPIXEL, NEO_GRB + NEO_KHZ800);

// Create the OLED display
Adafruit_SH1106G display = Adafruit_SH1106G(128, 64, &SPI1, OLED_DC, OLED_RST, OLED_CS);

// Create the rotary encoder
RotaryEncoder encoder(PIN_ROTA, PIN_ROTB, RotaryEncoder::LatchMode::FOUR3);
void checkPosition() {  encoder.tick(); } // just call tick() to check the state.
// our encoder position state
int encoder_pos = 0;

void setup() {
  Serial.begin(115200);
  //while (!Serial) { delay(10); }     // wait till serial port is opened
  delay(100);  // RP2040 delay is not a bad idea

  Serial.println("Adafruit Macropad with RP2040");

  // start pixels!
  pixels.begin();
  pixels.setBrightness(255);
  pixels.show(); // Initialize all pixels to 'off'

  // Start OLED
  display.begin(0, true); // we dont use the i2c address but we will reset!
  display.display();
  
  // set all mechanical keys to inputs
  for (uint8_t i=0; i<=12; i++) {
    pinMode(i, INPUT_PULLUP);
  }



  // set rotary encoder inputs and interrupts
  pinMode(PIN_ROTA, INPUT_PULLUP);
  pinMode(PIN_ROTB, INPUT_PULLUP);
  attachInterrupt(digitalPinToInterrupt(PIN_ROTA), checkPosition, CHANGE);
  attachInterrupt(digitalPinToInterrupt(PIN_ROTB), checkPosition, CHANGE);  

  // text display tests
  display.setTextSize(1);
  display.setTextWrap(false);
  display.setTextColor(SH110X_WHITE, SH110X_BLACK); // white text, black background

  pinMode(PIN_LED, OUTPUT);
  digitalWrite(PIN_LED, HIGH);

  pinMode(PIN_SPEAKER, OUTPUT);
  digitalWrite(PIN_SPEAKER, LOW);
//  tone(PIN_SPEAKER, 988, 100);  // tone1 - B5
//  delay(100);
//  tone(PIN_SPEAKER, 1319, 200); // tone2 - E6
//  delay(200);

  Serial.println("Done with setup.");
}

uint8_t j = 0;
String dataFromSerial = "";

void loop() {
  display.clearDisplay();
  display.setCursor(0,0);
  display.println("** Greg's Macropad **");

  while( Serial.available()){  
    dataFromSerial = Serial.readString();  
    Serial.println("Got: " + dataFromSerial);
  }  
  display.setCursor(0, 16);
  display.print("M: " + dataFromSerial);
  
  
  encoder.tick();          // check the encoder
  int newPos = encoder.getPosition();
  if (encoder_pos != newPos) {
    Serial.print("Encoder:");
    Serial.print(newPos);
    Serial.print(" Direction:");
    Serial.println((int)(encoder.getDirection()));
    encoder_pos = newPos;
  }
  display.setCursor(0, 8);
  display.print("Rotary encoder: ");
  display.print(encoder_pos);
  
  // check encoder press
  display.setCursor(0, 24);
  if (!digitalRead(PIN_SWITCH)) {
    Serial.println("Encoder button");
    display.print("Encoder pressed ");
    pixels.setBrightness(255);     // bright!
//    tone(PIN_SPEAKER, 1319, 200);
//    delay(250);
//    noTone(PIN_SPEAKER);
  } else {
    pixels.setBrightness(25);
  }

  for(int i=0; i< pixels.numPixels(); i++) {
    pixels.setPixelColor(i, Wheel(((i * 256 / pixels.numPixels()) + j) & 255));
  }
  
  for (int i=1; i<=12; i++) {
    if (!digitalRead(i)) { // switch pressed!
      Serial.print("Switch "); Serial.println(i);
      pixels.setPixelColor(i-1, 0xFFFFFF);  // make white
      // move the text into a 3x4 grid
      display.setCursor(((i-1) % 3)*48, 32 + ((i-1)/3)*8);
      display.print("KEY");
      display.print(i);
    }
  }

  // show neopixels, incredment swirl
  pixels.show();
  j++;

  // display oled
  display.display();
}





// Input a value 0 to 255 to get a color value.
// The colours are a transition r - g - b - back to r.
uint32_t Wheel(byte WheelPos) {
  if(WheelPos < 85) {
   return pixels.Color(255 - WheelPos * 3, 0, WheelPos * 3);
  } else if(WheelPos < 170) {
   WheelPos -= 85;
   return pixels.Color(0, WheelPos * 3, 255 - WheelPos * 3);
  } else {
   WheelPos -= 170;
   return pixels.Color(WheelPos * 3, 255 - WheelPos * 3, 0);
  }
}
