//OpenQuarterMaster Speed Scanner
//ESP32 Feather V2 + Atomic QRCode2 + TFT FeatherWing (HX8357)

#include <Arduino.h>
#include <SPI.h>
#include <WiFi.h>
#include <ArduinoJson.h>
#include <WiFiClientSecure.h>
#include <ArduinoHttpClient.h>
#include <base64.h>
#include "Adafruit_GFX.h"
#include "Adafruit_HX8357.h"
#include "M5UnitQRCode.h"

// TFT pin definitions
#define TFT_CS   15
#define TFT_DC   33
#define TFT_RST  32
Adafruit_HX8357 tft = Adafruit_HX8357(TFT_CS, TFT_DC, TFT_RST);

// Scanner pin definitions
#define FEATHER_RX 20
#define FEATHER_TX 22
M5UnitQRCodeUART qrcode;
#define UART_AUTO_SCAN_MODE

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
} settings;

unsigned long time_at_auth;

WiFiClientSecure secureClient;
HttpClient client = HttpClient(secureClient, settings.remote_host, settings.remote_port);

JsonDocument authdoc;
JsonDocument getdoc;
String iden_str;
String key_str;

void requestAuth();
void GetCount(String Identifier);

void setup() {
  Serial.begin(115200);
  delay(2000);
  time_at_auth = 0;

  // TFT initialization
  tft.begin(HX8357D);
  tft.setRotation(1);
  tft.fillScreen(HX8357_BLACK);
  tft.setTextColor(HX8357_WHITE);
  tft.setTextSize(2);
  tft.setCursor(10, 10);
  tft.println("Initializing...");

  // Scanner initialization
  if (!qrcode.begin(&Serial1, 115200, FEATHER_RX, FEATHER_TX)) {
    if (!qrcode.begin(&Serial1, 9600, FEATHER_RX, FEATHER_TX)) {
      Serial.println("Scanner Failed");
      tft.fillScreen(HX8357_RED);
      tft.setCursor(10, 10);
      tft.setTextColor(HX8357_WHITE);
      tft.println("Scanner Failed");
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

  // WiFi initialization
  tft.fillScreen(HX8357_BLACK);
  tft.setCursor(10, 10);
  tft.setTextColor(HX8357_YELLOW);
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

  // Get auth token
  requestAuth();
  
  time_at_auth = millis();

  // Ready screen
  tft.fillScreen(HX8357_BLACK);
  tft.setCursor(10, 10);
  tft.setTextColor(HX8357_GREEN);
  tft.println("Scanner Ready");
  tft.setTextColor(HX8357_WHITE);
  tft.setCursor(10, 40);
  tft.println("Waiting for Barcode");
}

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
      GetCount(data);
      delay(5000); //wait 5 seconds before scanning again
    }
  }

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

    Serial.print("HTTP Status: ");
    Serial.println(statusCode);
    Serial.println("Token recieved.");
  } else {
    Serial.println("WiFi Disconnected. Reconnecting...");
    WiFi.begin(settings.my_ssid, settings.my_pass);
  }
  Serial.println("Token received.");
  client.stop();  // add this
  
}

void GetCount(String Identifier) {
  getdoc.clear();

  if (WiFi.status() == WL_CONNECTED) {
    iden_str = settings.get_path + Identifier;
    key_str  = "Bearer " + authdoc["access_token"].as<String>();

    client.stop();  // close previous connection
    client.beginRequest();
    client.get(iden_str);
    client.sendHeader("accept", "application/json");
    client.sendHeader("Authorization", key_str);
    client.endRequest();

 
    //status code to see if request is succeeding
    client.endRequest();
    int statusCode = client.responseStatusCode();
    Serial.println("Get Response: ");
    Serial.println(statusCode);

    String responseBody = client.responseBody();
    Serial.println("Get Response: ");
    Serial.println(responseBody);
    deserializeJson(getdoc, responseBody);

   String itemName  = getdoc["results"][0]["name"].as<String>();
   int    itemCount = getdoc["results"][0]["stats"]["total"]["value"].as<int>();

    Serial.print("Found ");
    Serial.print(itemCount);
    Serial.print(" of item ");
    Serial.println(itemName);

    tft.fillScreen(HX8357_BLACK);
    tft.setCursor(10, 10);
    tft.setTextColor(HX8357_CYAN);
    tft.setTextSize(2);
    tft.println("Item:");
    tft.setCursor(10, 40);
    tft.setTextColor(HX8357_WHITE);
    tft.setTextSize(2);
    tft.println(itemName);
    tft.setCursor(10, 80);
    tft.setTextColor(HX8357_CYAN);
    tft.setTextSize(2);
    tft.println("Qty:");
    tft.setCursor(10, 110);
    tft.setTextColor(HX8357_GREEN);
    tft.setTextSize(4);
    tft.println(itemCount);
    tft.setCursor(10, 200);
    tft.setTextColor(HX8357_WHITE);
    tft.setTextSize(2);
    tft.println("Scan next item...");

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