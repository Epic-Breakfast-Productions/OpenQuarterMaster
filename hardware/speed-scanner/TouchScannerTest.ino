
// test for: touch ui, command barcodes, and axis corrected touch

#include <Arduino.h>
#include <SPI.h>
#include <map>
#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"
#include "Adafruit_TSC2007.h" 

//touch pin definitions
#define TFT_CS   15
#define TFT_DC   33
#define TFT_RST  -1 

Adafruit_HX8357 tft = Adafruit_HX8357(TFT_CS, TFT_DC, TFT_RST);
Adafruit_TSC2007 touch; 

//scanner pin definitions
#define FEATHER_RX 7  // tx on scanner - yellow 
#define FEATHER_TX 8  // rx on scanner - blue
#define STEMMA_PWR 2  // stemma power for touch screen

bool isAddMode = true; 
std::map<String, int> addCounts;
std::map<String, int> removeCounts;

//command barcode definitions
const String CMD_IN = "CMD_MODE_IN";
const String CMD_OUT = "CMD_MODE_OUT";

//ui helper functions
void drawButtons() {
    tft.setTextSize(3);
    
    if (isAddMode) {
        tft.fillRect(10, 50, 225, 140, HX8357_GREEN); 
        tft.fillRect(245, 50, 225, 140, 0x8000); 
        
        tft.setTextColor(HX8357_BLACK);
        tft.setCursor(50, 105);
        tft.print("CHECK IN");
        
        tft.setTextColor(HX8357_WHITE); 
        tft.setCursor(275, 105);
        tft.print("CHECK OUT");
    } else {
        tft.fillRect(10, 50, 225, 140, 0x0400); 
        tft.fillRect(245, 50, 225, 140, HX8357_RED); 
        
        tft.setTextColor(HX8357_WHITE); 
        tft.setCursor(50, 105);
        tft.print("CHECK IN");
        
        tft.setTextColor(HX8357_BLACK);
        tft.setCursor(275, 105);
        tft.print("CHECK OUT");
    }
}

void drawUI() {
    tft.fillScreen(HX8357_BLACK);
    
    tft.fillRect(0, 0, 480, 40, HX8357_BLUE);
    tft.setTextColor(HX8357_WHITE);
    tft.setTextSize(2);
    tft.setCursor(10, 12);
    tft.print("Open QuarterMaster | Status: Local");

    drawButtons();

    tft.drawRect(0, 210, 480, 110, HX8357_WHITE);
    tft.setCursor(10, 220);
    tft.setTextColor(HX8357_YELLOW);
    tft.setTextSize(2);
    tft.print("System Ready. Scan to begin...");
}

void updateLog(String barcode) {
    tft.fillRect(2, 212, 476, 106, HX8357_BLACK);
    tft.setTextSize(3);
    tft.setCursor(10, 230);
    
    if (isAddMode) {
        addCounts[barcode]++; 
        tft.setTextColor(HX8357_GREEN);
        tft.print("ADDED (+");
        tft.print(addCounts[barcode]); 
        tft.print("): ");
    } else {
        removeCounts[barcode]++; 
        tft.setTextColor(HX8357_RED);
        tft.print("REMOVED (-");
        tft.print(removeCounts[barcode]); 
        tft.print("): ");
    }
    
    tft.setTextColor(HX8357_WHITE);
    tft.setCursor(10, 270);
    tft.setTextSize(4);
    tft.print(barcode);
}

void showModeSwitchMessage(String modeName, uint16_t color) {
    tft.fillRect(2, 212, 476, 106, HX8357_BLACK);
    tft.setTextSize(3);
    tft.setTextColor(color);
    tft.setCursor(10, 240);
    tft.print(">> SWITCHED TO ");
    tft.print(modeName);
    tft.print(" <<");
}

void setup() {
    Serial.begin(115200);
    
    //turn on stemma power
    pinMode(STEMMA_PWR, OUTPUT);
    digitalWrite(STEMMA_PWR, HIGH);
    delay(500);

    Serial.println("--- ESP32 QuarterMaster Terminal Start ---");

    // initialize the display
    tft.begin(HX8357D); 
    tft.setRotation(1); 
    drawUI();

    //initialize touch screen
    if (!touch.begin()) {
        Serial.println("Touch Controller Failed!");
    } else {
        Serial.println("Touch Controller Initialized.");
    }

    //initialize scanner connection 
    Serial1.begin(115200, SERIAL_8N1, FEATHER_RX, FEATHER_TX);
    Serial.println("Scanner Connection Opened.");
}

void loop() {
    //check touch screen first
    TS_Point p = touch.getPoint();
    
    // check if pressure is above 100
    if (p.z > 100) { 
        
        //debug to print where tap is taking place
        Serial.printf("TAP! Raw X: %d | Raw Y: %d | Pressure: %d\n", p.x, p.y, p.z);
        
        if (p.y < 2000) { 
            if (!isAddMode) {
                isAddMode = true;
                drawButtons();
                Serial.println("Mode Switched: CHECK IN (Via Touch)");
            }
        } else {
            if (isAddMode) {
                isAddMode = false;
                drawButtons();
                Serial.println("Mode Switched: CHECK OUT (Via Touch)");
            }
        }
        delay(300); //delay to stop repeated taps
    }

    //check scanner
    if (Serial1.available()) {
        String data = "";
        
        // read characters until new line
        while (Serial1.available()) {
            char c = Serial1.read();
            if (c != '\r' && c != '\n') {
                data += c;
            }
            delay(2); 
        }
        
        if (data.length() > 0) {
            if (data == CMD_IN) {
                isAddMode = true;
                drawButtons();
                showModeSwitchMessage("CHECK IN", HX8357_GREEN);
                Serial.println(">>> MODE SWITCHED: CHECK IN (Via Barcode)");
                delay(1500); 
                tft.fillRect(2, 212, 476, 106, HX8357_BLACK); 
            } 
            else if (data == CMD_OUT) {
                isAddMode = false;
                drawButtons();
                showModeSwitchMessage("CHECK OUT", HX8357_RED);
                Serial.println(">>> MODE SWITCHED: CHECK OUT (Via Barcode)");
                delay(1500); 
                tft.fillRect(2, 212, 476, 106, HX8357_BLACK); 
            } 
            else {
                updateLog(data);
                
                Serial.println("-------------------------");
                if (isAddMode) {
                    Serial.printf("[CHECK IN] Scanned: %s | Total Adds: %d\n", data.c_str(), addCounts[data]);
                } else {
                    Serial.printf("[CHECK OUT] Scanned: %s | Total Removes: %d\n", data.c_str(), removeCounts[data]);
                }
                Serial.println("-------------------------");
                delay(1000); 
            }
        }
    }
    delay(10); 
}