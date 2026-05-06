//OpenQuarterMaster Speed Scanner
//ESP32 Feather V2 + Atomic QRCode2 + TFT FeatherWing (HX8357)

#include <Arduino.h>
#include <SPI.h>
#include <Wire.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <WiFiClientSecure.h>
#include <ArduinoHttpClient.h>
#include <base64.h>
#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"
#include "Adafruit_TSC2007.h"
#include "M5UnitQRCode.h"

// TFT pin definitions
#define TFT_CS   15
#define TFT_DC   33
#define TFT_RST  -1
Adafruit_HX8357 tft = Adafruit_HX8357(TFT_CS, TFT_DC, TFT_RST);

// Touch
Adafruit_TSC2007 touch;
#define TS_MINX 300
#define TS_MAXX 3800
#define TS_MINY 185
#define TS_MAXY 3700

// Scanner pin definitions
#define FEATHER_RX 7
#define FEATHER_TX 8
M5UnitQRCodeUART qrcode;
#define UART_AUTO_SCAN_MODE

// Color scheme - white, sage green, light grey
#define COLOR_BG        0xFFFF  // white
#define COLOR_HEADER    0x5D8A  // sage green
#define COLOR_CARD      0xFFFF  // white
#define COLOR_TEXT      0x2104  // dark grey text
#define COLOR_BTN       0x5D8A  // sage green buttons
#define COLOR_BTN_NEG   0xC618  // light grey for subtract/cancel
#define COLOR_DIVIDER   0xC618  // light grey divider
#define COLOR_SELECTED  0x3C67  // darker sage selected
#define COLOR_WHITE     0xFFFF  // white text on buttons

// Settings
struct MyProjectSettings {
  const char* my_ssid     = "speedscanner";
  const char* my_pass     = "Admin1$$$";
  const char* remote_host = "10.1.6.27";
  const int   remote_port = 443;
  const char* get_path    = "/core/api/api/v1/db/speedscanner/inventory/item?identifier=";
  const char* auth_path   = "/infra/keycloak/realms/oqm/protocol/openid-connect/token";
  const char* auth_user   = "scanner";
  const char* auth_pass   = "h5j9inH-cx4LbQKnYmG6wvrvMUi7TEAv";
  const char* db_path     = "/core/api/api/v1/db";
  const char* block_path  = "/core/api/api/v1/db/speedscanner/storage/block";
} settings;

unsigned long time_at_auth = 0;

WiFiClientSecure secureClient;
HttpClient client = HttpClient(secureClient, settings.remote_host, settings.remote_port);

JsonDocument authdoc;
JsonDocument getdoc;
String key_str;

// UI state
enum Screen { SCREEN_SETTINGS, SCREEN_QUICK, SCREEN_DETAIL };
Screen currentScreen  = SCREEN_SETTINGS;
Screen previousScreen = SCREEN_SETTINGS;

// Item state
String currentBarcode   = "";
String currentItemName  = "";
int    currentItemCount = 0;
int    adjustedCount    = 0;

// DB/Block selection
String dbList[10];
String blockList[10];
String selectedDB    = "none";
String selectedBlock = "none";
int    dbCount       = 0;
int    blockCount    = 0;
bool   showingDBList    = false;
bool   showingBlockList = false;

// Detail mode
int    selectedAction = 0;
String actionNames[]  = {"Add", "Sub", "Checkout", "Checkin", "Set"};

// Forward declarations
void requestAuth();
void GetCount(String Identifier);
void fetchDBList();
void fetchBlockList();
void drawSettingsScreen();
void drawQuickScreen();
void drawDetailScreen();
void drawDBListOverlay();
void drawBlockListOverlay();
void drawButton(int x, int y, int w, int h, String label, uint16_t bgColor);
void drawRadioButton(int x, int y, String label, bool selected);
void checkTouch();

// ══════════════════════════════════════════════════════
void setup() {
  Serial.begin(115200);
  delay(2000);
  time_at_auth = 0;

  SPI.begin();
  Wire.begin(21, 22);

  // TFT initialization
  tft.begin(HX8357D);
  tft.setRotation(1);
  tft.fillScreen(COLOR_BG);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.setCursor(10, 10);
  tft.println("Initializing...");

  // Touch initialization
  if (!touch.begin(0x48)) {
    Serial.println("Touch not found!");
  } else {
    Serial.println("Touch Ready");
  }

  // Scanner initialization
  if (!qrcode.begin(&Serial1, 115200, FEATHER_RX, FEATHER_TX)) {
    if (!qrcode.begin(&Serial1, 9600, FEATHER_RX, FEATHER_TX)) {
      Serial.println("Scanner Failed");
      tft.fillScreen(HX8357_RED);
      tft.setCursor(10, 10);
      tft.setTextColor(HX8357_WHITE);
      tft.println("Scanner Failed");
    }
  }
  Serial.println("Scanner Success");

#ifdef UART_AUTO_SCAN_MODE
  Serial.println("Mode: Auto Scan");
  qrcode.setTriggerMode(AUTO_SCAN_MODE);
#else
  Serial.println("Mode: Manual Scan");
  qrcode.setTriggerMode(MANUAL_SCAN_MODE);
#endif

  // WiFi initialization
  tft.fillScreen(COLOR_BG);
  tft.setCursor(10, 10);
  tft.setTextColor(COLOR_HEADER);
  tft.println("Connecting to WiFi...");

  Serial.print("Connecting to: ");
  Serial.println(settings.my_ssid);
  WiFi.begin(settings.my_ssid, settings.my_pass);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");

  secureClient.setInsecure();

  requestAuth();
  time_at_auth = millis();

  Serial.println("Drawing screen...");
  currentScreen = SCREEN_QUICK;
  drawQuickScreen();
} 

// ══════════════════════════════════════════════════════
void loop() {
  if ((millis() - time_at_auth) > (authdoc["expires_in"].as<unsigned long>() * 1000 * .75)) {
    requestAuth();
    time_at_auth = millis();
    delay(1000);
  } else {
    if (qrcode.available()) {
      String data = qrcode.getDecodeData();
      data.trim();
      Serial.print("Scan Result: ");
      Serial.println(data);
      currentBarcode = data;
      GetCount(data);
      while (qrcode.available()) { qrcode.getDecodeData(); }
      delay(2000);
    }
  }
  if (Serial.available()) {
    char cmd = Serial.read();
    if (cmd == 'q') { currentScreen = SCREEN_QUICK; drawQuickScreen(); }
    if (cmd == 'd') { currentScreen = SCREEN_DETAIL; drawDetailScreen(); }
    if (cmd == 's') { currentScreen = SCREEN_SETTINGS; drawSettingsScreen(); }
  }

#ifndef UART_AUTO_SCAN_MODE
  static unsigned long lastTrigger = 0;
  if (millis() - lastTrigger > 5000) {
    qrcode.setDecodeTrigger(true);
    lastTrigger = millis();
    Serial.println("Manual Trigger Sent...");
  }
#endif

  checkTouch();
  delay(10);
}

// ══════════════════════════════════════════════════════

void checkTouch() {
  uint16_t x, y, z1, z2;
  if (!touch.read_touch(&x, &y, &z1, &z2)) return;
  if (z1 == 0) return;

  int sx = map(x, TS_MINX, TS_MAXX, 0, 480);
  int sy = map(y, TS_MINY, TS_MAXY, 0, 320);

  Serial.print("Touch: ");
  Serial.print(sx);
  Serial.print(", ");
  Serial.println(sy);

  if (showingDBList) {
    for (int i = 0; i < dbCount; i++) {
      if (sx > 60 && sx < 420 && sy > (80 + i * 40) && sy < (115 + i * 40)) {
        selectedDB = dbList[i];
        showingDBList = false;
        drawSettingsScreen();
      }
    }
    if (sx > 60 && sx < 420 && sy > 270 && sy < 310) {
      showingDBList = false;
      drawSettingsScreen();
    }
    return;
  }

  if (showingBlockList) {
    for (int i = 0; i < blockCount; i++) {
      if (sx > 60 && sx < 420 && sy > (80 + i * 40) && sy < (115 + i * 40)) {
        selectedBlock = blockList[i];
        showingBlockList = false;
        drawSettingsScreen();
      }
    }
    if (sx > 60 && sx < 420 && sy > 270 && sy < 310) {
      showingBlockList = false;
      drawSettingsScreen();
    }
    return;
  }

  if (currentScreen == SCREEN_SETTINGS) {
    if (sx > 300 && sx < 460 && sy > 55 && sy < 90) {
      showingDBList = true;
      drawDBListOverlay();
    }
    if (sx > 300 && sx < 460 && sy > 100 && sy < 135) {
      showingBlockList = true;
      drawBlockListOverlay();
    }
    if (sx > 10 && sx < 90 && sy > 185 && sy < 245) {
      adjustedCount++;
      currentScreen = SCREEN_QUICK;
      drawQuickScreen();
    }
    if (sx > 100 && sx < 180 && sy > 185 && sy < 245) {
      if (adjustedCount > 0) adjustedCount--;
      currentScreen = SCREEN_QUICK;
      drawQuickScreen();
    }
    if (sx > 200 && sx < 300 && sy > 252 && sy < 287) {
      previousScreen = SCREEN_SETTINGS;
      currentScreen  = SCREEN_DETAIL;
      drawDetailScreen();
    }
  }

  else if (currentScreen == SCREEN_QUICK) {
    if (sx > 20 && sx < 200 && sy > 200 && sy < 290) {
      adjustedCount++;
      drawQuickScreen();
    }
    if (sx > 260 && sx < 440 && sy > 200 && sy < 290) {
      if (adjustedCount > 0) adjustedCount--;
      drawQuickScreen();
    }
    if (sx > 10 && sx < 130 && sy > 288 && sy < 320) {
      currentScreen = SCREEN_SETTINGS;
      drawSettingsScreen();
    }
    if (sx > 350 && sx < 470 && sy > 288 && sy < 320) {
      previousScreen = SCREEN_QUICK;
      currentScreen  = SCREEN_DETAIL;
      drawDetailScreen();
    }
  }

  else if (currentScreen == SCREEN_DETAIL) {
    for (int i = 0; i < 5; i++) {
      if (sx > 10 && sx < 220 && sy > (162 + i * 28) && sy < (187 + i * 28)) {
        selectedAction = i;
        drawDetailScreen();
      }
    }
    if (sx > 390 && sx < 445 && sy > 162 && sy < 207) {
      adjustedCount++;
      drawDetailScreen();
    }
    if (sx > 390 && sx < 445 && sy > 215 && sy < 260) {
      if (adjustedCount > 0) adjustedCount--;
      drawDetailScreen();
    }
    if (sx > 10 && sx < 130 && sy > 288 && sy < 320) {
      currentScreen = previousScreen;
      if (currentScreen == SCREEN_SETTINGS) drawSettingsScreen();
      else drawQuickScreen();
    }
  }
}

// ══════════════════════════════════════════════════════
void drawSettingsScreen() {
  tft.fillScreen(COLOR_BG);
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("OQM Barcode Scanner");

  tft.fillRoundRect(10, 58, 460, 35, 6, COLOR_CARD);
  tft.setCursor(20, 67);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("DB: ");
  tft.print(selectedDB);
  drawButton(305, 58, 155, 35, "choose", COLOR_BTN);

  tft.fillRoundRect(10, 100, 460, 35, 6, COLOR_CARD);
  tft.setCursor(20, 109);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("Block: ");
  tft.print(selectedBlock);
  drawButton(305, 100, 155, 35, "choose", COLOR_BTN);

  tft.drawFastHLine(10, 148, 460, COLOR_DIVIDER);

  tft.setCursor(10, 158);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Quick Mode:");
  drawButton(10, 183, 80, 55, "+", COLOR_BTN);
  drawButton(100, 183, 80, 55, "-", COLOR_BTN_NEG);

  tft.drawFastHLine(10, 248, 460, COLOR_DIVIDER);

  tft.setCursor(10, 258);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Detail Mode:");
  drawButton(200, 252, 110, 35, "  ->", COLOR_BTN);
}

// ══════════════════════════════════════════════════════
void drawQuickScreen() {
  tft.fillScreen(COLOR_BG);
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("Quick Mode");

  tft.fillRoundRect(10, 58, 460, 130, 8, COLOR_CARD);
  tft.setCursor(20, 68);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Last Item:");
  tft.setCursor(20, 93);
  tft.setTextColor(COLOR_TEXT);
  tft.print("Barcode: ");
  tft.println(currentBarcode);
  tft.setCursor(20, 118);
  tft.print("Name:    ");
  tft.println(currentItemName);
  tft.setCursor(20, 143);
  tft.print("Count:   ");
  tft.setTextColor(COLOR_HEADER);
  tft.println(adjustedCount);

  drawButton(20, 200, 180, 82, "+", COLOR_BTN);
  drawButton(260, 200, 180, 82, "-", COLOR_BTN_NEG);
  drawButton(10, 288, 120, 30, "< Back", COLOR_CARD);
  drawButton(350, 288, 120, 30, "Detail >", COLOR_BTN);
  tft.setTextColor(COLOR_TEXT);
}

// ══════════════════════════════════════════════════════
void drawDetailScreen() {
  tft.fillScreen(COLOR_BG);
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("Detail Mode");

  tft.fillRoundRect(10, 58, 460, 95, 8, COLOR_CARD);
  tft.setCursor(20, 68);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Selected Item:");
  tft.setCursor(20, 93);
  tft.setTextColor(COLOR_TEXT);
  tft.print("Barcode: ");
  tft.println(currentBarcode);
  tft.setCursor(20, 118);
  tft.print("Name:    ");
  tft.println(currentItemName);

  tft.fillRoundRect(10, 158, 160, 35, 6, COLOR_CARD);
  tft.setCursor(20, 167);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("Count: ");
  tft.setTextColor(COLOR_HEADER);
  tft.println(adjustedCount);

  for (int i = 0; i < 5; i++) {
    drawRadioButton(230, 162 + i * 28, actionNames[i], selectedAction == i);
  }

  drawButton(390, 162, 55, 45, "^", COLOR_BTN);
  drawButton(390, 215, 55, 45, "v", COLOR_BTN);
  drawButton(10, 288, 120, 30, "< Back", COLOR_CARD);
}

// ══════════════════════════════════════════════════════
void drawDBListOverlay() {
  tft.fillRoundRect(40, 55, 400, 265, 8, COLOR_BG);
  tft.drawRoundRect(40, 55, 400, 265, 8, COLOR_HEADER);
  tft.fillRect(41, 56, 398, 40, COLOR_HEADER);
  tft.setCursor(55, 65);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(2);
  tft.println("Select Database:");
  for (int i = 0; i < dbCount; i++) {
    if (i % 2 == 0) tft.fillRect(41, 97 + i * 40, 398, 40, COLOR_CARD);
    tft.setCursor(55, 105 + i * 40);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    tft.println(dbList[i]);
  }
  drawButton(160, 275, 120, 35, "Cancel", COLOR_BTN_NEG);
}

// ══════════════════════════════════════════════════════
void drawBlockListOverlay() {
  tft.fillRoundRect(40, 55, 400, 265, 8, COLOR_BG);
  tft.drawRoundRect(40, 55, 400, 265, 8, COLOR_HEADER);
  tft.fillRect(41, 56, 398, 40, COLOR_HEADER);
  tft.setCursor(55, 65);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(2);
  tft.println("Select Block:");
  for (int i = 0; i < blockCount; i++) {
    if (i % 2 == 0) tft.fillRect(41, 97 + i * 40, 398, 40, COLOR_CARD);
    tft.setCursor(55, 105 + i * 40);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    tft.println(blockList[i]);
  }
  drawButton(160, 275, 120, 35, "Cancel", COLOR_BTN_NEG);
}

// ══════════════════════════════════════════════════════
void drawButton(int x, int y, int w, int h, String label, uint16_t color) {
  tft.fillRoundRect(x, y, w, h, 6, color);
  tft.setTextColor(color == COLOR_CARD ? COLOR_TEXT : COLOR_WHITE);
  tft.setTextSize(2);
  int textX = x + (w / 2) - (label.length() * 6);
  int textY = y + (h / 2) - 8;
  tft.setCursor(textX, textY);
  tft.print(label);
}

// ══════════════════════════════════════════════════════
void drawRadioButton(int x, int y, String label, bool selected) {
  uint16_t fillColor = selected ? COLOR_HEADER : COLOR_BG;
  tft.fillCircle(x + 8, y + 8, 8, fillColor);
  tft.drawCircle(x + 8, y + 8, 8, COLOR_HEADER);
  tft.setCursor(x + 22, y);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.println(label);
}

// ══════════════════════════════════════════════════════
void requestAuth() {
  authdoc.clear();

  if (WiFi.status() == WL_CONNECTED) {
    String authRaw    = String(settings.auth_user) + ":" + String(settings.auth_pass);
    String authHeader = "Basic " + base64::encode(authRaw);
    String bodyData   = "grant_type=client_credentials";

    Serial.println(">>> Requesting Token...");

    client.beginRequest();
    client.post(settings.auth_path);
    client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
    client.sendHeader("Authorization", authHeader);
    client.sendHeader("Content-Length", bodyData.length());
    client.beginBody();
    client.print(bodyData);
    client.endRequest();

    int statusCode      = client.responseStatusCode();
    String responseBody = client.responseBody();
    deserializeJson(authdoc, responseBody);
    key_str = "Bearer " + authdoc["access_token"].as<String>();

    Serial.print("HTTP Status: ");
    Serial.println(statusCode);
    Serial.println("Token received.");
  } else {
    Serial.println("WiFi Disconnected. Reconnecting...");
    WiFi.begin(settings.my_ssid, settings.my_pass);
  }
  client.stop();
}


// ══════════════════════════════════════════════════════
void fetchDBList() {
  dbCount = 0;
  if (WiFi.status() != WL_CONNECTED) return;
  client.stop();
  client.beginRequest();
  client.get(settings.db_path);
  client.sendHeader("accept", "application/json");
  client.sendHeader("Authorization", key_str);
  client.endRequest();
  String responseBody = client.responseBody();
  JsonDocument doc;
  deserializeJson(doc, responseBody);
  JsonArray arr = doc.as<JsonArray>();
  for (JsonVariant v : arr) {
    if (dbCount < 10) dbList[dbCount++] = v["name"].as<String>();
  }
  client.stop();
}

// ══════════════════════════════════════════════════════
void fetchBlockList() {
  blockCount = 0;
  if (WiFi.status() != WL_CONNECTED) return;
  client.stop();
  client.beginRequest();
  client.get(settings.block_path);
  client.sendHeader("accept", "application/json");
  client.sendHeader("Authorization", key_str);
  client.endRequest();
  String responseBody = client.responseBody();
  JsonDocument doc;
  deserializeJson(doc, responseBody);
  JsonArray arr = doc["results"].as<JsonArray>();
  for (JsonVariant v : arr) {
    if (blockCount < 10) blockList[blockCount++] = v["label"].as<String>();
  }
  client.stop();
}

// ══════════════════════════════════════════════════════
void GetCount(String Identifier) {
  getdoc.clear();

  if (WiFi.status() == WL_CONNECTED) {
    String path = String(settings.get_path) + Identifier;

    client.stop();
    client.beginRequest();
    client.get(path);
    client.sendHeader("accept", "application/json");
    client.sendHeader("Authorization", key_str);
    client.endRequest();

    int statusCode      = client.responseStatusCode();
    String responseBody = client.responseBody();
    Serial.print("Get Response: ");
    Serial.println(statusCode);
    Serial.println(responseBody);
    deserializeJson(getdoc, responseBody);

    if (getdoc["numResults"].as<int>() > 0) {
      currentItemName  = getdoc["results"][0]["name"].as<String>();
      currentItemCount = getdoc["results"][0]["stats"]["total"]["value"].as<int>();
      adjustedCount    = currentItemCount;
    } else {
      currentItemName  = "Not Found";
      currentItemCount = 0;
      adjustedCount    = 0;
    }

    Serial.print("Found ");
    Serial.print(currentItemCount);
    Serial.print(" of item ");
    Serial.println(currentItemName);

    if (currentScreen == SCREEN_QUICK) drawQuickScreen();
    else if (currentScreen == SCREEN_DETAIL) drawDetailScreen();
    else { currentScreen = SCREEN_QUICK; drawQuickScreen(); }

  } else {
    Serial.println("WiFi Disconnected. Reconnecting...");
    WiFi.begin(settings.my_ssid, settings.my_pass);
    tft.fillScreen(HX8357_RED);
    tft.setCursor(10, 10);
    tft.setTextColor(HX8357_WHITE);
    tft.setTextSize(2);
    tft.println("No WiFi!");
  }
}