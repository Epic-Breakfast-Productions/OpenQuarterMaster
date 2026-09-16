// ============================================================
//  OQM Barcode Scanner — Main Sketch
//  Hardware: Adafruit ESP32 Feather V2 + HX8357 TFT + M5 QRCode2
// ============================================================

#include <Adafruit_TSC2007.h>
#include <SdFat_Adafruit_Fork.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <base64.h>
#include <Arduino.h>
#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"
#include "color.h"
#include "M5UnitQRCode.h"

// ── TFT ──────────────────────────────────────────────────────
#define TFT_CS   15
#define TFT_DC   33
#define TFT_RST  32
Adafruit_HX8357 tft = Adafruit_HX8357(TFT_CS, TFT_DC, TFT_RST);
Adafruit_TSC2007 touch;

// ── Scanner UART ─────────────────────────────────────────────
// M5 QRCode2 connected to ESP32 Serial1 (GPIO 7 RX, 8 TX)
#define FEATHER_RX 7
#define FEATHER_TX 8
M5UnitQRCodeUART qrcode;
#define UART_AUTO_SCAN_MODE

// ── Screen state ─────────────────────────────────────────────
enum Screen { SCREEN_SETTINGS, SCREEN_QUICK, SCREEN_DETAIL };
Screen currentScreen  = SCREEN_SETTINGS;
Screen previousScreen = SCREEN_SETTINGS;

// ── UI state (shared with scannerUI.ino via extern) ──────────
String currentBarcode  = "";
String currentItemName = "None";
int    currentItemCount = 0;
int    adjustedCount    = 0;

// String actionNames[] = {"Add", "Sub", "Out", "In", "Set"};
String actionNames[] = {"Add", "Sub", "Out", "", "Set"};
int    selectedAction = 0;   // 0-4 → transaction types 1-5

bool showingDBList    = false;
bool showingBlockList = false;

// DB list overlay
String dbList[10];
int    dbCount = 0;

// Block list overlay — label + ID kept in sync
String blockList[10];
String blockListIDs[10];
int    blockCount = 0;

// ── API / session state ───────────────────────────────────────
String db_name       = "";
String storage_block = "";   // active block ID
String block_name    = "";   // active block label (display)
String item_ID       = "";
String item_name     = "";

// Quick-mode add/subtract toggle (true = add)
bool quickModeAdd = true;

// Pending scan flag — set by UART ISR context, consumed by loop()
bool   newScanPending = false;
bool   apiInFlight    = false;

// ── Credentials / endpoints ───────────────────────────────────
struct MyProjectSettings {
  String my_ssid;
  String my_pass;
  String remote_host;
  const int   remote_port = 443;
  const char* db_path     = "/core/api/api/v1/inventory/manage/db";
  const char* def_path    = "/core/api/api/v1/db/";
  const char* stor_path   = "/inventory/storage-block";
  const char* get_path    = "/inventory/item?identifier=";
  const char* auth_path   = "/infra/keycloak/realms/oqm/protocol/openid-connect/token";
  String auth_user;
  String auth_pass;
} settings;

// ── JSON documents ────────────────────────────────────────────
JsonDocument authdoc;
JsonDocument dbdoc;
JsonDocument stblockdoc;
JsonDocument getdoc;
JsonDocument secretdoc;

// ── Timing ────────────────────────────────────────────────────
unsigned long time_at_auth  = 0;
unsigned long expiry        = 0;
unsigned long lastTouchTime = 0;

// ── SD ────────────────────────────────────────────────────────
SdFat32 SD;
bool gotSD = false;

// ── UART buffer ───────────────────────────────────────────────
String serialBuffer = "";

// ── Forward declarations ──────────────────────────────────────
void requestAuth();
void GetDB();
void GetStorageBlocks();
void GetItem(String identifier);
void UpdateCount(String stblock, String itemID, int transaction, int value);
void HandleNewScan();
void processTouch(int x, int y);
void populateDBList();
void populateBlockList(bool filteredByItem);
void handleDBListTouch(int x, int y);
void handleBlockListTouch(int x, int y);
void onDBSelected(int index);
void onBlockSelected(int index);
void refreshQuickCard();
void refreshDetailCard();
void drawSettingsScreen();
void drawQuickScreen();
void drawDetailScreen();
void drawDBListOverlay();
void drawBlockListOverlay();
void drawButton(int x, int y, int w, int h, String label, uint16_t bgColor);
void drawRadioButton(int x, int y, String label, bool selected);

// ============================================================
//  SETUP
// ============================================================
void setup() {
  Serial.begin(115200);
  // M5 QRCode2 UART
    // Scanner initialization
  if (!qrcode.begin(&Serial1, 115200, FEATHER_RX, FEATHER_TX)) {
    if (!qrcode.begin(&Serial1, 9600, FEATHER_RX, FEATHER_TX)) {
      // removed while(1) so it keeps going even without scanner
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

  // Touch controller
  Wire.begin();
  if (!touch.begin()) {
    Serial.println("TSC2007 not found!");
  }

  const int SD_CS = 14;
  pinMode(SD_CS, OUTPUT);
  digitalWrite(SD_CS, HIGH);
  pinMode(TFT_RST, INPUT);

  SPI.begin();

  // TFT init
  tft.begin(HX8357D);
  tft.setRotation(1);  // Landscape

  // SD config load
  Serial.println("Initializing SD...");
  if (SD.begin(SD_CS, SD_SCK_MHZ(10))) {
    File file = SD.open("/sdsetup.json");
    if (file) {
      DeserializationError error = deserializeJson(secretdoc, file);
      if (!error) {
        settings.my_ssid      = secretdoc["ssid"].as<String>();
        settings.my_pass      = secretdoc["password"].as<String>();
        settings.auth_user    = secretdoc["oqm-user"].as<String>();
        settings.auth_pass    = secretdoc["oqm-secret"].as<String>();
        settings.remote_host  = secretdoc["oqm-address"].as<String>();
        gotSD = true;
        Serial.println("SD Config loaded.");
      }
      file.close();
    }
    secretdoc.clear();
  } else {
    Serial.println("SD failed — using defaults.");
  }

  // WiFi
  Serial.printf("Connecting to %s", settings.my_ssid.c_str());
  WiFi.begin(settings.my_ssid, settings.my_pass);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected!");

  requestAuth();

  // Draw initial settings screen — DB not yet chosen, overlay will open
  drawSettingsScreen();

  // Immediately show the DB chooser so the user picks one at startup
  populateDBList();
  showingDBList = true;
  drawDBListOverlay();
}

// ============================================================
//  LOOP
// ============================================================
void loop() {

  // 1. Token refresh (non-blocking)
  if (time_at_auth == 0 ||
      (millis() - time_at_auth) > ((unsigned long)expiry * 750UL)) {
    requestAuth();
  }

  // 2. Touch input
  uint16_t x, y, z1, z2;
  if (touch.read_touch(&x, &y, &z1, &z2) && z1 > 100) {
    int touchX = constrain(map(y, 239, 3780, 0, 480), 0, 479);
    int touchY = constrain(map(x, 3640, 367, 0, 320), 0, 319);

    if (millis() - lastTouchTime > 300) {
      lastTouchTime = millis();

      // Overlays take priority
      if (showingDBList) {
        handleDBListTouch(touchX, touchY);
      } else if (showingBlockList) {
        handleBlockListTouch(touchX, touchY);
      } else {
        processTouch(touchX, touchY);
      }
    }
  }

  // 3. UART scanner (non-blocking)
  if (qrcode.available()) {
      currentBarcode = qrcode.getDecodeData();
      currentBarcode.trim();
      newScanPending = true;
  }

  // 4. Process pending scan (only when no API call is in flight
  //    and no overlay is open)
  if (newScanPending && !apiInFlight &&
      !showingDBList && !showingBlockList) {
    newScanPending = false;
    HandleNewScan();
  }
}

// ============================================================
//  SCAN HANDLER
// ============================================================
void HandleNewScan() {
  if (currentScreen == SCREEN_QUICK) {
    // Fetch current count, then immediately update ±1
    apiInFlight = true;
    GetItem(currentBarcode);
    apiInFlight = false;

    if (getdoc["results"][0]["id"].isNull()) {
      // Item not found — show briefly on card then return
      currentItemName  = "Not found";
      currentItemCount = 0;
      adjustedCount    = 0;
      refreshQuickCard();
      return;
    }

    item_ID          = getdoc["results"][0]["id"].as<String>();
    currentItemName  = getdoc["results"][0]["name"].as<String>();
    currentItemCount = getdoc["results"][0]["stats"]["total"]["value"].as<int>();
    adjustedCount    = currentItemCount + (quickModeAdd ? 1 : -1);

    refreshQuickCard();

    apiInFlight = true;
    UpdateCount(storage_block, item_ID, quickModeAdd ? 1 : 2, 1);
    apiInFlight = false;

  } else if (currentScreen == SCREEN_DETAIL) {
    // Fetch item info
    apiInFlight = true;
    GetItem(currentBarcode);
    apiInFlight = false;

    if (getdoc["results"][0]["id"].isNull()) {
      currentItemName  = "Not found";
      currentItemCount = 0;
      adjustedCount    = 0;
      refreshDetailCard();
      return;
    }

    item_ID          = getdoc["results"][0]["id"].as<String>();
    currentItemName  = getdoc["results"][0]["name"].as<String>();
    currentItemCount = getdoc["results"][0]["stats"]["total"]["value"].as<int>();
    adjustedCount    = currentItemCount;

    refreshDetailCard();

    // Determine storage block for this item
    int blocksinItem = getdoc["results"][0]["storageBlocks"].size();

    if (blocksinItem == 0) {
      storage_block = "";
      block_name    = "None";
    } else if (blocksinItem == 1) {
      // Auto-select the only block
      storage_block = getdoc["results"][0]["storageBlocks"][0].as<String>();
      // Look up its label in stblockdoc
      block_name = storage_block; // fallback
      for (int i = 0; i < (int)stblockdoc["results"].size(); i++) {
        if (stblockdoc["results"][i]["id"].as<String>() == storage_block) {
          block_name = stblockdoc["results"][i]["label"].as<String>();
          break;
        }
      }
    } else {
      // Multiple blocks — show filtered chooser
      populateBlockList(true);  // true = filter to item's blocks only
      showingBlockList = true;
      drawBlockListOverlay();
    }
  }
}

// ============================================================
//  TOUCH ROUTING
// ============================================================
void processTouch(int x, int y) {
  switch (currentScreen) {

    // ── Settings screen ─────────────────────────────────────
    case SCREEN_SETTINGS: {
      // "Change DB" button: x 280-450, y 55-100
      if (x >= 280 && x <= 450 && y >= 55 && y <= 100) {
        populateDBList();
        showingDBList = true;
        drawDBListOverlay();
        return;
      }
      // Quick "+" button: x 30-150, y 148-208
      if (x >= 30 && x <= 150 && y >= 148 && y <= 208) {
        if (db_name == "") return;
        quickModeAdd = true;
        GetStorageBlocks();
        populateBlockList(false);
        showingBlockList = true;
        drawBlockListOverlay();
        previousScreen = SCREEN_QUICK;
        return;
      }
      // Quick "-" button: x 160-280, y 148-208
      if (x >= 160 && x <= 280 && y >= 148 && y <= 208) {
        if (db_name == "") return;
        quickModeAdd = false;
        GetStorageBlocks();
        populateBlockList(false);
        showingBlockList = true;
        drawBlockListOverlay();
        previousScreen = SCREEN_QUICK;
        return;
      }
      // Detail "Open ->" button: x 155-325, y 248-298
      if (x >= 155 && x <= 325 && y >= 248 && y <= 298) {
        if (db_name == "") return;
        GetStorageBlocks();
        previousScreen = currentScreen;
        currentScreen  = SCREEN_DETAIL;
        drawDetailScreen();
        return;
      }
      break;
    }

    // ── Quick screen ─────────────────────────────────────────
    case SCREEN_QUICK: {
      // "+" button: x 20-200, y 197-277
      if (x >= 20 && x <= 200 && y >= 197 && y <= 277) {
        quickModeAdd = true;
        refreshQuickCard();
        return;
      }
      // "-" button: x 260-440, y 197-277
      if (x >= 260 && x <= 440 && y >= 197 && y <= 277) {
        quickModeAdd = false;
        refreshQuickCard();
        return;
      }
      // "< Back" button: x 30-150, y 268-318
      if (x >= 30 && x <= 150 && y >= 268 && y <= 318) {
        previousScreen = currentScreen;
        currentScreen  = SCREEN_SETTINGS;
        drawSettingsScreen();
        return;
      }
      // "Chg Block" button: x 160-320, y 268-318
      if (x >= 160 && x <= 320 && y >= 268 && y <= 318) {
        GetStorageBlocks();
        populateBlockList(false);
        showingBlockList = true;
        drawBlockListOverlay();
        previousScreen = SCREEN_QUICK;
        return;
      }
      // "Detail >" button: x 330-450, y 268-318
      if (x >= 330 && x <= 450 && y >= 268 && y <= 318) {
        previousScreen = currentScreen;
        currentScreen  = SCREEN_DETAIL;
        drawDetailScreen();
        return;
      }
      break;
    }

    // ── Detail screen ────────────────────────────────────────
    case SCREEN_DETAIL: {
      // Count "^" up arrow: x 390-445, y 162-207
      if (x >= 390 && x <= 445 && y >= 162 && y <= 207) {
        adjustedCount++;
        // Redraw just the count area
        tft.fillRoundRect(10, 158, 160, 35, 6, COLOR_CARD);
        tft.drawRoundRect(10, 158, 160, 35, 6, COLOR_DIVIDER);
        tft.setCursor(20, 167);
        tft.setTextColor(COLOR_TEXT);
        tft.setTextSize(2);
        tft.print("Count: ");
        tft.setTextColor(COLOR_HEADER);
        tft.println(adjustedCount);
        return;
      }
      // Count "v" down arrow: x 390-445, y 215-260
      if (x >= 390 && x <= 445 && y >= 215 && y <= 260) {
        if (adjustedCount > 0) adjustedCount--;
        tft.fillRoundRect(10, 158, 160, 35, 6, COLOR_CARD);
        tft.drawRoundRect(10, 158, 160, 35, 6, COLOR_DIVIDER);
        tft.setCursor(20, 167);
        tft.setTextColor(COLOR_TEXT);
        tft.setTextSize(2);
        tft.print("Count: ");
        tft.setTextColor(COLOR_HEADER);
        tft.println(adjustedCount);
        return;
      }
      // Radio buttons: x 230-460, y 162-302 (5 rows × 28px)
      if (x >= 230 && x <= 460 && y >= 162 && y <= 302) {
        int newAction = (y - 162) / 28;
        if (newAction >= 0 && newAction < 5) {
          selectedAction = newAction;
          // Redraw all 5 radio buttons
          for (int i = 0; i < 5; i++) {
            drawRadioButton(230, 159 + i * 26, actionNames[i], selectedAction == i);
          }
        }
        return;
      }
      // "< Back" button: x 30-150, y 265-315
      if (x >= 30 && x <= 150 && y >= 265 && y <= 315) {
        previousScreen = currentScreen;
        currentScreen  = SCREEN_SETTINGS;
        drawSettingsScreen();
        return;
      }
      // "Submit ->" button: x 290-450, y 265-315
      if (x >= 290 && x <= 450 && y >= 265 && y <= 315) {
        if (item_ID == "" || storage_block == "") return;
        apiInFlight = true;
        UpdateCount(storage_block, item_ID, selectedAction + 1, adjustedCount);
        apiInFlight = false;
        GetItem(currentBarcode);
        currentItemCount = getdoc["results"][0]["stats"]["total"]["value"].as<int>();
        adjustedCount    = currentItemCount;
        refreshDetailCard();
        return;
      }
      break;
    }
  }
}

// ============================================================
//  OVERLAY TOUCH HANDLERS
// ============================================================

// DB list overlay touch
// Overlay bounds: x 40-440, y 55-320
// Header: y 56-96 (skip)
// Rows: y 97+, 40px each
// Cancel button: x 160-280, y 275-310
void handleDBListTouch(int x, int y) {
  // Cancel
  if (x >= 160 && x <= 280 && y >= 275 && y <= 310) {
    showingDBList = false;
    // Redraw underlying screen to remove overlay
    if (currentScreen == SCREEN_SETTINGS) drawSettingsScreen();
    return;
  }
  // Row tap
  if (x >= 41 && x <= 439 && y >= 97 && y < 275) {
    int idx = (y - 97) / 40;
    if (idx >= 0 && idx < dbCount) {
      onDBSelected(idx);
    }
  }
}

// Block list overlay touch
void handleBlockListTouch(int x, int y) {
  // Cancel
  if (x >= 160 && x <= 280 && y >= 275 && y <= 310) {
    showingBlockList = false;
    if (currentScreen == SCREEN_SETTINGS) drawSettingsScreen();
    else if (currentScreen == SCREEN_QUICK) drawQuickScreen();
    else if (currentScreen == SCREEN_DETAIL) drawDetailScreen();
    return;
  }
  // Row tap
  if (x >= 41 && x <= 439 && y >= 97 && y < 275) {
    int idx = (y - 97) / 40;
    if (idx >= 0 && idx < blockCount) {
      onBlockSelected(idx);
    }
  }
}

// ── Called when user selects a DB from the overlay ───────────
void onDBSelected(int index) {
  showingDBList = false;
  db_name = dbList[index];
  Serial.printf("DB selected: %s\n", db_name.c_str());

  // Refresh settings screen with new DB name shown
  drawSettingsScreen();
}

// ── Called when user selects a block from the overlay ────────
void onBlockSelected(int index) {
  showingBlockList = false;
  storage_block = blockListIDs[index];
  block_name    = blockList[index];
  Serial.printf("Block selected: %s (%s)\n",
                block_name.c_str(), storage_block.c_str());

  // Navigate if we came from a "+" or "-" button on settings
  if (previousScreen == SCREEN_QUICK) {
    currentScreen = SCREEN_QUICK;
    drawQuickScreen();
  } else if (currentScreen == SCREEN_DETAIL) {
    // Block was chosen post-scan in detail mode — just redraw detail
    drawDetailScreen();
  } else {
    drawSettingsScreen();
  }
}

// ============================================================
//  LIST POPULATION HELPERS
// ============================================================

void populateDBList() {
  GetDB();
  dbCount = 0;
  for (int i = 0; i < (int)dbdoc.size() && i < 10; i++) {
    dbList[i] = dbdoc[i]["name"].as<String>();
    dbCount++;
  }
}

// filteredByItem=true  → only blocks that contain getdoc item
// filteredByItem=false → all blocks from stblockdoc
void populateBlockList(bool filteredByItem) {
  blockCount = 0;

  if (!filteredByItem) {
    for (int i = 0; i < (int)stblockdoc["results"].size() && i < 10; i++) {
      blockList[i]    = stblockdoc["results"][i]["label"].as<String>();
      blockListIDs[i] = stblockdoc["results"][i]["id"].as<String>();
      blockCount++;
    }
    return;
  }

  // Filtered: cross-reference item's storageBlocks with stblockdoc
  int itemBlockCount = getdoc["results"][0]["storageBlocks"].size();
  for (int i = 0; i < (int)stblockdoc["results"].size() && blockCount < 10; i++) {
    String bid = stblockdoc["results"][i]["id"].as<String>();
    for (int j = 0; j < itemBlockCount; j++) {
      if (getdoc["results"][0]["storageBlocks"][j].as<String>() == bid) {
        blockList[blockCount]    = stblockdoc["results"][i]["label"].as<String>();
        blockListIDs[blockCount] = bid;
        blockCount++;
        break;
      }
    }
  }
}

// ============================================================
//  PARTIAL REDRAWS (avoid full screen flicker)
// ============================================================

// Redraws only the item info card and +/- buttons on Quick screen
void refreshQuickCard() {
  // Clear card area
  tft.fillRoundRect(10, 58, 460, 130, 8, COLOR_CARD);
  tft.drawRoundRect(10, 58, 460, 130, 8, COLOR_DIVIDER);

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

  // Redraw +/- with active state highlighted
  uint16_t plusColor  = quickModeAdd  ? COLOR_SELECTED : COLOR_BTN;
  uint16_t minusColor = !quickModeAdd ? COLOR_SELECTED : COLOR_BTN_NEG;
  drawButton(20,  200, 180, 50, "+", plusColor);
  drawButton(260, 200, 180, 50, "-", minusColor);
}

// Redraws only the item info card on Detail screen
void refreshDetailCard() {
  tft.fillRoundRect(10, 58, 460, 95, 8, COLOR_CARD);
  tft.drawRoundRect(10, 58, 460, 95, 8, COLOR_DIVIDER);

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

  // Refresh count display
  tft.fillRoundRect(10, 158, 160, 35, 6, COLOR_CARD);
  tft.drawRoundRect(10, 158, 160, 35, 6, COLOR_DIVIDER);
  tft.setCursor(20, 167);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("Count: ");
  tft.setTextColor(COLOR_HEADER);
  tft.println(adjustedCount);
}

// ============================================================
//  API FUNCTIONS
// ============================================================

//Gets JWT key with username and secret for OQM instance, sends to authdoc
void requestAuth() { 
  if (WiFi.status() != WL_CONNECTED) return;

  WiFiClientSecure secureClient;
  secureClient.setInsecure();
  HTTPClient http;

  String url = "https://" + settings.remote_host + ":" +
               settings.remote_port + settings.auth_path;
  http.begin(secureClient, url);

  String authRaw    = settings.auth_user + ":" + settings.auth_pass;
  String authHeader = "Basic " + base64::encode(authRaw);
  http.addHeader("Content-Type", "application/x-www-form-urlencoded");
  http.addHeader("Authorization", authHeader);

  int code = http.POST("grant_type=client_credentials");
  if (code == HTTP_CODE_OK) {
    authdoc.clear();
    deserializeJson(authdoc, http.getStream());
    expiry       = authdoc["expires_in"].as<unsigned long>();
    time_at_auth = millis();
    Serial.println("Token refreshed.");
  } else {
    Serial.printf("Auth failed (%d)\n", code);
  }
  http.end();
}

//Gets a list of available databases, sends to dbdoc
void GetDB() {
  WiFiClientSecure secureClient;
  secureClient.setInsecure();
  HTTPClient http;

  String url = "https://" + settings.remote_host + ":" +
               settings.remote_port + settings.db_path;
  http.begin(secureClient, url);
  http.addHeader("Authorization",
                 "Bearer " + authdoc["access_token"].as<String>());
  http.addHeader("Accept", "application/json");

  int code = http.GET();
  if (code == HTTP_CODE_OK) {
    dbdoc.clear();
    deserializeJson(dbdoc, http.getStream());
  } else {
    Serial.printf("GetDB failed (%d)\n", code);
  }
  http.end();
}

//Gets a list of available storage blocks, sends to stblockdoc
void GetStorageBlocks() {
  WiFiClientSecure secureClient;
  secureClient.setInsecure();
  HTTPClient http;

  String url = "https://" + settings.remote_host + ":" +
               settings.remote_port +
               settings.def_path + db_name + settings.stor_path;
  http.begin(secureClient, url);
  http.addHeader("Authorization",
                 "Bearer " + authdoc["access_token"].as<String>());
  http.addHeader("Accept", "application/json");

  int code = http.GET();
  if (code == HTTP_CODE_OK) {
    stblockdoc.clear();
    deserializeJson(stblockdoc, http.getStream());
  } else {
    Serial.printf("GetStorageBlocks failed (%d)\n", code);
  }
  http.end();
}

//Gets details of item scanned, sends to getdoc
void GetItem(String identifier) {
  WiFiClientSecure secureClient;
  secureClient.setInsecure();
  HTTPClient http;

  String url = "https://" + settings.remote_host + ":" +
               settings.remote_port +
               settings.def_path + db_name +
               settings.get_path + identifier;
  http.begin(secureClient, url);
  http.addHeader("Authorization",
                 "Bearer " + authdoc["access_token"].as<String>());
  http.addHeader("Accept", "application/json");

  unsigned long start = millis();
  bool success = false;
  while (!success && millis() - start < 10000) {
    int code = http.GET();
    if (code == HTTP_CODE_OK) {
      getdoc.clear();
      DeserializationError err = deserializeJson(getdoc, http.getStream());
      if (!err) success = true;
    } else {
      Serial.printf("GetItem failed (%d) — retrying\n", code);
      delay(2000);
    }
  }
  http.end();
}

//Given storage block, item id, transaction type, and value, updates count of an item
void UpdateCount(String stblock, String itemID,
                 int transaction, int value) {
  if (WiFi.status() != WL_CONNECTED) return;

  WiFiClientSecure secureClient;
  secureClient.setInsecure();
  HTTPClient http;

  String url = "https://" + settings.remote_host + ":" +
               settings.remote_port +
               settings.def_path + db_name +
               "/inventory/item/" + itemID + "/stored/transaction";
  http.begin(secureClient, url);
  http.addHeader("Authorization",
                 "Bearer " + authdoc["access_token"].as<String>());
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Accept", "application/json");

  JsonDocument doc;
  switch (transaction) {
    case 1: {
      doc["type"] = "ADD_AMOUNT";     
      doc["toBlock"]   = stblock; 
      } 
      break;
    case 2: {
      doc["type"] = "SUBTRACT_AMOUNT"; 
      doc["fromBlock"] = stblock; 
      }
      break;
    case 3: {
      doc["type"] = "CHECKOUT_AMOUNT"; 
      doc["fromBlock"] = stblock; 
      JsonObject checkout = doc["checkoutDetails"].to<JsonObject>();
      checkout["reason"] = "";
      checkout["notes"] = "";
      JsonObject checkdet = checkout["checedkOutFor"].to<JsonObject>();
      checkdet["entity"] = "6973dbc978a22b6009670776";
      checkdet["type"] = "OQM_ENTITY";
      doc["all"] = false;
      }
      break;
    // case 4: 
    //   // doc["type"] = "CHECKIN_AMOUNT";  
    //   // doc["toBlock"]   = stblock; 
    //   break;
    case 5: {
      doc["type"] = "SET_AMOUNT";      
      doc["block"]     = stblock;
      } 
      break;
  }
  JsonObject amount = doc["amount"].to<JsonObject>();
  amount["value"] = value;
  amount["scale"] = "ABSOLUTE";
  JsonObject unit = amount["unit"].to<JsonObject>();
  unit["string"]  = "units";

  String body;
  serializeJson(doc, body);

  int code = http.POST(body);
  if (code == HTTP_CODE_OK || code == HTTP_CODE_CREATED) {
    Serial.println("Transaction success.");
  } else {
    Serial.printf("Transaction failed (%d): %s\n",
                  code, http.getString().c_str());
  }
  http.end();
}
