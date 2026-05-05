// Jase Colino
// QR / Barcode scanning device using ESP32, Atomic QRCode2, TFT FeatherWing
// this program tests the scanning of a barcode or QR code and will eventually display the barcode reading on the TFT screen

#include <Arduino.h>
#include <SPI.h>
#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"
#include "M5UnitQRCode.h"

// screen pin definitions for esp32
#define TFT_CS   15
#define TFT_DC   33
#define TFT_RST  32 

Adafruit_HX8357 tft = Adafruit_HX8357(TFT_CS, TFT_DC, TFT_RST);

// pin definitions for scanner
#define FEATHER_RX 20
#define FEATHER_TX 22

M5UnitQRCodeUART qrcode;

#define UART_AUTO_SCAN_MODE 

void setup() {
    Serial.begin(115200);
    delay(2000); 
    Serial.println("ESP32 Feather V2 Atomic QR Scanner Start");

    // initialize tft screen --- *TFT MUST BE SOLDERED OTHERWISE DISPLAY WILL NOT WORK*
    tft.begin(HX8357D); // display type
    tft.setRotation(1); 
    tft.fillScreen(HX8357_BLACK); 
    
    tft.setTextColor(HX8357_WHITE);
    tft.setTextSize(2);
    tft.setCursor(10, 10);

    // scanner initialization    
    // must use 115200 because of wire limitations
    if (!qrcode.begin(&Serial1, 115200, FEATHER_RX, FEATHER_TX)) {
        
        // if 115200 somehow fails uses 9600
        if (!qrcode.begin(&Serial1, 9600, FEATHER_RX, FEATHER_TX)) {
            Serial.println("Scanner Failed"); 

            tft.fillScreen(HX8357_RED);
            tft.setCursor(10, 10);
            tft.setTextColor(HX8357_WHITE);
            tft.println("Scanner Failed");
            tft.setCursor(10, 40);
                        
            while (1) delay(10); // stop safely -- no crashing
        }
    }
    
    Serial.println("Scanner Success");
    
    // update screen to ready state
    tft.fillScreen(HX8357_BLACK);
    tft.setCursor(10, 10);
    tft.setTextColor(HX8357_GREEN);
    tft.println("Scanner Ready");
    tft.setTextColor(HX8357_WHITE);
    tft.setCursor(10, 40);
    tft.println("Waiting for Barcode");

// scan mode
#ifdef UART_AUTO_SCAN_MODE
    Serial.println("Mode: Auto Scan");
    qrcode.setTriggerMode(AUTO_SCAN_MODE);
#else
    Serial.println("Mode: Manual Scan");
    qrcode.setTriggerMode(MANUAL_SCAN_MODE);
#endif
}

void loop() {
    if (qrcode.available()) {
        String data = qrcode.getDecodeData();
        
        Serial.print("Scan Result: ");
        Serial.println(data);
        Serial.printf("Length: %d\n", data.length());
        Serial.println("-------------------------");

        // print to tft screen  *not sure if this works as we cant test since the esp is not soldered to tft*
        tft.fillScreen(HX8357_BLACK); 
        
        tft.setCursor(10, 10);
        tft.setTextColor(HX8357_CYAN);
        tft.setTextSize(2);
        tft.println("Result:");
        
        tft.setCursor(10, 40);
        tft.setTextColor(HX8357_WHITE);
        tft.setTextSize(3); 
        tft.println(data); 
    }


// auto scan
#ifndef UART_AUTO_SCAN_MODE
    static unsigned long lastTrigger = 0;
    if (millis() - lastTrigger > 5000) {
        qrcode.setDecodeTrigger(true); 
        lastTrigger = millis();
        Serial.println("Manual Trigger Sent...");
    }
#endif

    delay(10); 
}