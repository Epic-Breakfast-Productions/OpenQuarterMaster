
// OQM Scanner UI

#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"


//UI Colors
#define COLOR_BG        0xFFFF  // white
#define COLOR_HEADER    0x5D8A  // sage green
#define COLOR_CARD      0xFFFF  // white
#define COLOR_TEXT      0x2104  // dark grey text
#define COLOR_BTN       0x5D8A  // sage green buttons
#define COLOR_BTN_NEG   0xC618  // light grey for subtract/cancel
#define COLOR_DIVIDER   0xC618  // light grey divider
#define COLOR_SELECTED  0x3C67  // darker sage selected
#define COLOR_WHITE     0xFFFF  // white text on buttons

//TFT declaration
extern Adafruit_HX8357 tft;

//UI state
extern String currentBarcode;
extern String currentItemName;
extern int    currentItemCount;
extern int    adjustedCount;
extern String selectedDB;
extern String selectedBlock;
extern String dbList[];
extern String blockList[];
extern int    dbCount;
extern int    blockCount;
extern bool   showingDBList;
extern bool   showingBlockList;


extern String actionNames[];
extern int    selectedAction;

enum Screen { SCREEN_SETTINGS, SCREEN_QUICK, SCREEN_DETAIL };
extern Screen currentScreen;
extern Screen previousScreen;

//Forward declarations
void drawButton(int x, int y, int w, int h, String label, uint16_t bgColor);
void drawRadioButton(int x, int y, String label, bool selected);

//Setting Screen
void drawSettingsScreen() {
  tft.fillScreen(COLOR_BG);

  // Header
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("OQM Barcode Scanner");

  // DB row
  tft.setCursor(20, 67);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("DB: ");
  tft.print(selectedDB);
  drawButton(305, 58, 155, 35, "choose", COLOR_BTN);

  // Block row
  tft.setCursor(20, 109);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("Block: ");
  tft.print(selectedBlock);
  drawButton(305, 100, 155, 35, "choose", COLOR_BTN_NEG);

  // Divider
  tft.drawFastHLine(10, 148, 460, COLOR_DIVIDER);

  // Quick mode
  tft.setCursor(10, 158);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Quick Mode:");
  drawButton(10, 183, 80, 55, "+", COLOR_BTN);
  drawButton(100, 183, 80, 55, "-", COLOR_BTN_NEG);

  // Divider
  tft.drawFastHLine(10, 248, 460, COLOR_DIVIDER);

  // Detail mode
  tft.setCursor(10, 258);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Detail Mode:");
  drawButton(200, 252, 110, 35, "  ->", COLOR_BTN);
}

//Quick Screen 
void drawQuickScreen() {
  tft.fillScreen(COLOR_BG);

  // Header
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("Quick Mode");

  // Item info
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

  // + and - buttons
  drawButton(20, 200, 180, 82, "+", COLOR_BTN);
  drawButton(260, 200, 180, 82, "-", COLOR_BTN_NEG);

  // Nav buttons
  drawButton(10, 288, 120, 30, "< Back", COLOR_BTN_NEG);
  drawButton(350, 288, 120, 30, "Detail >", COLOR_BTN);
}

//Detail Screen
void drawDetailScreen() {
  tft.fillScreen(COLOR_BG);

  // Header
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("Detail Mode");

  // Item info
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

  // Count
  tft.drawRoundRect(10, 158, 160, 35, 6, COLOR_DIVIDER);
  tft.setCursor(20, 167);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("Count: ");
  tft.setTextColor(COLOR_HEADER);
  tft.println(adjustedCount);

  // Radio buttons
  for (int i = 0; i < 5; i++) {
    drawRadioButton(230, 162 + i * 28, actionNames[i], selectedAction == i);
  }

  // Up/down arrows
  drawButton(390, 162, 55, 45, "^", COLOR_BTN);
  drawButton(390, 215, 55, 45, "v", COLOR_BTN);

  // Back button
  drawButton(10, 288, 120, 30, "< Back", COLOR_BTN_NEG);
}

//DB List
void drawDBListOverlay() {
  tft.fillRoundRect(40, 55, 400, 265, 8, COLOR_BG);
  tft.drawRoundRect(40, 55, 400, 265, 8, COLOR_HEADER);
  tft.fillRect(41, 56, 398, 40, COLOR_HEADER);
  tft.setCursor(55, 65);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(2);
  tft.println("Select Database:");
  for (int i = 0; i < dbCount; i++) {
    if (i % 2 == 0) tft.fillRect(41, 97 + i * 40, 398, 40, COLOR_DIVIDER);
    tft.setCursor(55, 105 + i * 40);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    tft.println(dbList[i]);
  }
  drawButton(160, 275, 120, 35, "Cancel", COLOR_BTN_NEG);
}

//Block List
void drawBlockListOverlay() {
  tft.fillRoundRect(40, 55, 400, 265, 8, COLOR_BG);
  tft.drawRoundRect(40, 55, 400, 265, 8, COLOR_HEADER);
  tft.fillRect(41, 56, 398, 40, COLOR_HEADER);
  tft.setCursor(55, 65);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(2);
  tft.println("Select Block:");
  for (int i = 0; i < blockCount; i++) {
    if (i % 2 == 0) tft.fillRect(41, 97 + i * 40, 398, 40, COLOR_DIVIDER);
    tft.setCursor(55, 105 + i * 40);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    tft.println(blockList[i]);
  }
  drawButton(160, 275, 120, 35, "Cancel", COLOR_BTN_NEG);
}


void drawButton(int x, int y, int w, int h, String label, uint16_t color) {
  tft.fillRoundRect(x, y, w, h, 6, color);
  tft.setTextColor(color == COLOR_BTN_NEG ? COLOR_TEXT : COLOR_WHITE);
  tft.setTextSize(2);
  int textX = x + (w / 2) - (label.length() * 6);
  int textY = y + (h / 2) - 8;
  tft.setCursor(textX, textY);
  tft.print(label);
}


void drawRadioButton(int x, int y, String label, bool selected) {
  uint16_t fillColor = selected ? COLOR_HEADER : COLOR_BG;
  tft.fillCircle(x + 8, y + 8, 8, fillColor);
  tft.drawCircle(x + 8, y + 8, 8, COLOR_HEADER);
  tft.setCursor(x + 22, y);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.println(label);
}