// ============================================================
//  OQM Scanner UI — scannerUI.ino  (Arduino IDE tab 2)
//  All drawing functions. No API calls here.
// ============================================================

#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"
#include "color.h"

// ── Externs (defined in main .ino) ───────────────────────────
extern Adafruit_HX8357 tft;

extern String currentBarcode;
extern String currentItemName;
extern int    currentItemCount;
extern int    adjustedCount;
extern String db_name;
extern String storage_block;
extern String block_name;

extern String dbList[];
extern String blockList[];
extern String blockListIDs[];
extern int    dbCount;
extern int    blockCount;
extern bool   showingDBList;
extern bool   showingBlockList;

extern String actionNames[];
extern int    selectedAction;
extern bool   quickModeAdd;

extern Screen currentScreen;
extern Screen previousScreen;

// ── Forward declarations ──────────────────────────────────────
void drawButton(int x, int y, int w, int h,
                String label, uint16_t bgColor);
void drawRadioButton(int x, int y, String label, bool selected);

// ============================================================
//  SETTINGS SCREEN
// ============================================================
void drawSettingsScreen() {
  tft.fillScreen(COLOR_BG);

  // Header bar
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("OQM Scanner");

  // ── DB row ──
  tft.setCursor(20, 67);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("DB: ");
  // Truncate long names so they don't overwrite the button
  String dbDisplay = db_name.length() > 0 ? db_name : "(none)";
  if (dbDisplay.length() > 16) dbDisplay = dbDisplay.substring(0, 15) + "~";
  tft.print(dbDisplay);
  // "Change DB" button — inset from right edge, taller
  drawButton(280, 55, 170, 45, "Change DB", COLOR_BTN);

  // Divider
  tft.drawFastHLine(30, 112, 420, COLOR_DIVIDER);

  // ── Quick mode row ──
  tft.setCursor(30, 122);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Quick Mode  (scan -> update block)");

  // Highlight whichever was last active — inset from edges, taller
  uint16_t plusColor  = quickModeAdd  ? COLOR_SELECTED : COLOR_BTN;
  uint16_t minusColor = !quickModeAdd ? COLOR_SELECTED : COLOR_BTN_NEG;
  drawButton(30,  148, 120, 60, "+  Add", plusColor);
  drawButton(160, 148, 120, 60, "-  Sub", minusColor);

  // Divider
  tft.drawFastHLine(30, 218, 420, COLOR_DIVIDER);

  // ── Detail mode row ──
  tft.setCursor(30, 228);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Detail Mode  (choose action + qty)");

  // Centred, inset, taller
  drawButton(155, 248, 170, 50, "Open  ->", COLOR_BTN);

  // Disabled hint when no DB selected
  if (db_name.length() == 0) {
    tft.setCursor(30, 305);
    tft.setTextColor(COLOR_BTN_NEG);
    tft.setTextSize(1);
    tft.println("Select a database above to begin.");
  }
}

// ============================================================
//  QUICK SCREEN
// ============================================================
void drawQuickScreen() {
  tft.fillScreen(COLOR_BG);

  // Header
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  String modeLabel = quickModeAdd ? "Quick Mode  +" : "Quick Mode  -";
  tft.println(modeLabel);

  // Block label
  tft.setCursor(10, 52);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(1);
  tft.print("Block: ");
  tft.println(block_name.length() > 0 ? block_name : "(none)");

  // Item info card
  tft.drawRoundRect(10, 62, 460, 125, 8, COLOR_DIVIDER);
  tft.setCursor(20, 72);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Last Item:");

  tft.setCursor(20, 97);
  tft.setTextColor(COLOR_TEXT);
  tft.print("Barcode: ");
  tft.println(currentBarcode.length() > 0 ? currentBarcode : "—");

  tft.setCursor(20, 122);
  tft.print("Name:    ");
  tft.println(currentItemName);

  tft.setCursor(20, 147);
  tft.print("Count:   ");
  tft.setTextColor(COLOR_HEADER);
  tft.println(adjustedCount);

  // +/- big buttons — selected one is darker
  uint16_t plusColor  = quickModeAdd  ? COLOR_SELECTED : COLOR_BTN;
  uint16_t minusColor = !quickModeAdd ? COLOR_SELECTED : COLOR_BTN_NEG;
  drawButton(20,  197, 180, 50, "+", plusColor);
  drawButton(260, 197, 180, 50, "-", minusColor);

  // Nav row — inset 30px from each edge, 50px tall
  drawButton(30,  268, 120, 50, "< Back",    COLOR_BTN_NEG);
  drawButton(160, 268, 160, 50, "Chg Block", COLOR_BTN_NEG);
  drawButton(330, 268, 120, 50, "Detail >",  COLOR_BTN);
}

// ============================================================
//  DETAIL SCREEN
// ============================================================
void drawDetailScreen() {
  tft.fillScreen(COLOR_BG);

  // Header
  tft.fillRect(0, 0, 480, 50, COLOR_HEADER);
  tft.setCursor(10, 12);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(3);
  tft.println("Detail Mode");

  // Item info card
  tft.drawRoundRect(10, 55, 460, 95, 8, COLOR_DIVIDER);
  tft.setCursor(20, 65);
  tft.setTextColor(COLOR_HEADER);
  tft.setTextSize(2);
  tft.println("Selected Item:");

  tft.setCursor(20, 90);
  tft.setTextColor(COLOR_TEXT);
  tft.print("Barcode: ");
  tft.println(currentBarcode.length() > 0 ? currentBarcode : "—  (scan to load)");

  tft.setCursor(20, 115);
  tft.print("Name:    ");
  tft.println(currentItemName);

  // Block label (auto-set or chosen from overlay)
  tft.setCursor(20, 135);
  tft.setTextColor(COLOR_DIVIDER);
  tft.setTextSize(1);
  tft.print("Block: ");
  tft.println(block_name.length() > 0 ? block_name : "(scan item first)");

  // Count box
  tft.drawRoundRect(10, 155, 160, 35, 6, COLOR_DIVIDER);
  tft.setCursor(20, 164);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.print("Count: ");
  tft.setTextColor(COLOR_HEADER);
  tft.println(adjustedCount);

  // Action radio buttons
  for (int i = 0; i < 5; i++) {
    drawRadioButton(230, 159 + i * 26, actionNames[i], selectedAction == i);
  }

  // Up/Down arrows (aligned with count box)
  drawButton(390, 155, 55, 45, "^", COLOR_BTN);
  drawButton(390, 208, 55, 45, "v", COLOR_BTN);

  // Nav / submit row — inset 30px from each edge, 50px tall
  drawButton(30,  265, 120, 50, "< Back",   COLOR_BTN_NEG);
  drawButton(290, 265, 160, 50, "Submit ->", COLOR_SUBMIT);
}

// ============================================================
//  DB LIST OVERLAY
// ============================================================
void drawDBListOverlay() {
  // Overlay card
  tft.fillRoundRect(40, 50, 400, 270, 8, COLOR_BG);
  tft.drawRoundRect(40, 50, 400, 270, 8, COLOR_HEADER);

  // Overlay header
  tft.fillRect(41, 51, 398, 40, COLOR_HEADER);
  tft.setCursor(55, 61);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(2);
  tft.println("Select Database:");

  // Rows
  for (int i = 0; i < dbCount; i++) {
    uint16_t rowColor = (i % 2 == 0) ? COLOR_DIVIDER : COLOR_BG;
    tft.fillRect(41, 92 + i * 40, 398, 40, rowColor);
    tft.setCursor(55, 100 + i * 40);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    // Truncate long names
    String label = dbList[i];
    if (label.length() > 22) label = label.substring(0, 21) + "~";
    tft.println(label);
  }

  // Empty state
  if (dbCount == 0) {
    tft.setCursor(55, 140);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    tft.println("No databases found.");
  }

  drawButton(160, 272, 120, 35, "Cancel", COLOR_BTN_NEG);
}

// ============================================================
//  BLOCK LIST OVERLAY
// ============================================================
void drawBlockListOverlay() {
  tft.fillRoundRect(40, 50, 400, 270, 8, COLOR_BG);
  tft.drawRoundRect(40, 50, 400, 270, 8, COLOR_HEADER);

  tft.fillRect(41, 51, 398, 40, COLOR_HEADER);
  tft.setCursor(55, 61);
  tft.setTextColor(COLOR_WHITE);
  tft.setTextSize(2);
  tft.println("Select Block:");

  for (int i = 0; i < blockCount; i++) {
    uint16_t rowColor = (i % 2 == 0) ? COLOR_DIVIDER : COLOR_BG;
    tft.fillRect(41, 92 + i * 40, 398, 40, rowColor);
    tft.setCursor(55, 100 + i * 40);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    String label = blockList[i];
    if (label.length() > 22) label = label.substring(0, 21) + "~";
    tft.println(label);
  }

  if (blockCount == 0) {
    tft.setCursor(55, 140);
    tft.setTextColor(COLOR_TEXT);
    tft.setTextSize(2);
    tft.println("No blocks found.");
  }

  drawButton(160, 272, 120, 35, "Cancel", COLOR_BTN_NEG);
}

// ============================================================
//  SHARED DRAWING HELPERS
// ============================================================

void drawButton(int x, int y, int w, int h,
                String label, uint16_t color) {
  tft.fillRoundRect(x, y, w, h, 6, color);

  // Text color: dark on light buttons, white on colored ones
  uint16_t textColor = (color == COLOR_BTN_NEG) ? COLOR_TEXT : COLOR_WHITE;
  tft.setTextColor(textColor);
  tft.setTextSize(2);

  // Centre the label
  int charW   = 12;  // ~12px per char at size 2
  int textX   = x + (w - (int)label.length() * charW) / 2;
  int textY   = y + (h / 2) - 8;
  tft.setCursor(textX, textY);
  tft.print(label);
}

void drawRadioButton(int x, int y, String label, bool selected) {
  uint16_t fillColor = selected ? COLOR_SELECTED : COLOR_BG;
  tft.fillCircle(x + 8, y + 8, 8, fillColor);
  tft.drawCircle(x + 8, y + 8, 8, COLOR_HEADER);
  tft.setCursor(x + 22, y);
  tft.setTextColor(COLOR_TEXT);
  tft.setTextSize(2);
  tft.println(label);
}
