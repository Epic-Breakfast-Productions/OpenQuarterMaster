#include <SdFat_Adafruit_Fork.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <base64.h>

struct MyProjectSettings {
  String my_ssid  = "speedscanner";
  String my_pass  = "Admin1$$$";
  String remote_host = "10.1.6.27";
  const int   remote_port = 443; 
  const char* db_path = "/core/api/api/v1/inventory/manage/db";
  const char* def_path = "/core/api/api/v1/db/";
  const char* stor_path = "/inventory/storage-block";
  const char* get_path = "/inventory/item?identifier=";
  const char* auth_path = "/infra/keycloak/realms/oqm/protocol/openid-connect/token";
  String auth_user = "scanner";
  String auth_pass = "h5j9inH-cx4LbQKnYmG6wvrvMUi7TEAv";
} settings;

String db_name;
String storage_block;
String block_name;
String item_ID;
String item_name;
SdFat32 SD;
bool gotSD = false;
uint choice = 100000;

unsigned long time_at_auth = 0;
JsonDocument authdoc;
JsonDocument dbdoc;
JsonDocument stblockdoc;
JsonDocument getdoc;
JsonDocument secretdoc;

void requestAuth();
JsonDocument GetDB();
String SetDB();
JsonDocument GetStorageBlocks();
JsonDocument GetCount(String identifier);
void UpdateCount(String stblock, String itemID, int transaction, int value);
void DetailedUpdate(JsonDocument item, JsonDocument stblocks);
void QuickUpdate(JsonDocument item, JsonDocument stblocks, bool add);

void setup() {
  Serial.begin(115200);
  delay(1000);

  pinMode(BUTTON, INPUT_PULLUP);

  // Define Pins
  const int SD_CS = 14;
  const int TFT_CS = 15;
  const int TOUCH_CS = 21;

  // Silence the bus
  pinMode(TFT_CS, OUTPUT); digitalWrite(TFT_CS, HIGH);
  pinMode(TOUCH_CS, OUTPUT); digitalWrite(TOUCH_CS, HIGH);
  pinMode(SD_CS, OUTPUT); digitalWrite(SD_CS, HIGH);

  SPI.begin(); 

  Serial.println("Initializing SD...");

  // Use the standard SD.begin
  if (SD.begin(SD_CS, SD_SCK_MHZ(10))) {
    Serial.println("SD Found!");
    File file = SD.open("/sdsetup.json");
    if (file) {
      DeserializationError error = deserializeJson(secretdoc, file);
      if (!error) {
        //Load into settings struct
        settings.my_ssid = secretdoc["ssid"].as<String>();
        settings.my_pass = secretdoc["password"].as<String>();
        settings.auth_user = secretdoc["oqm-user"].as<String>();
        settings.auth_pass = secretdoc["oqm-secret"].as<String>();
        settings.remote_host = secretdoc["oqm-address"].as<String>();

        gotSD = true;
        Serial.println("SD Config Loaded.");
      }
      file.close();
    }
    serializeJson(secretdoc, Serial);
    secretdoc.clear(); // Free up memory immediately after use
  } else {
    Serial.println("SD Fail. Proceeding with defaults.");
  }
  
  // ... rest of your WiFi setup

  Serial.printf("\nConnecting to %s", settings.my_ssid);
  WiFi.begin(settings.my_ssid, settings.my_pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");

  requestAuth();

  //Select a database, can choose another with SetDB()
  db_name = SetDB();

}

void loop() {
  // Check if token needs refreshing
  unsigned long expiry = authdoc["expires_in"].as<unsigned long>();
  if (time_at_auth == 0 || (millis() - time_at_auth) > (expiry * 1000 * 0.75)) {
    requestAuth();
  }

  Serial.println("--- Fetching Item Count ---");
  DetailedUpdate(GetCount("X004WAJ2H7"), GetStorageBlocks());
  QuickUpdate(GetCount("X004WAJ2H7"), GetStorageBlocks(), true);
  
  


  delay(10000); 
}

void requestAuth() {
  if (WiFi.status() != WL_CONNECTED) return;

  // 1. Create the secure client and set it to insecure
  WiFiClientSecure secureClient;
  secureClient.setInsecure(); 

  HTTPClient http;
  String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) + settings.auth_path;
  
  // 2. Pass the secureClient to http.begin
  http.begin(secureClient, url);

  String authRaw = String(settings.auth_user) + ":" + String(settings.auth_pass);
  String authHeader = "Basic " + base64::encode(authRaw);

  http.addHeader("Content-Type", "application/x-www-form-urlencoded");
  http.addHeader("Authorization", authHeader);

  int httpCode = http.POST("grant_type=client_credentials");

  if (httpCode == HTTP_CODE_OK) {
    authdoc.clear();
    deserializeJson(authdoc, http.getStream());
    time_at_auth = millis();
    Serial.println("Token updated.");
  } else {
    Serial.printf("Auth failed (%d)\n", httpCode);
  }
  http.end();
}

JsonDocument GetDB(){
  JsonDocument doc;

  WiFiClientSecure secureClient;
  secureClient.setInsecure(); // Handles the -k flag

  HTTPClient http;
  
  // Construct the management URL
  String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) 
               + settings.db_path;

  http.begin(secureClient, url);

  // Set Headers
  String bearerToken = "Bearer " + authdoc["access_token"].as<String>();
  http.addHeader("Authorization", bearerToken);
  http.addHeader("Accept", "application/json");

  Serial.println(">>> Fetching Database Management Info...");
  int httpCode = http.GET();

  if (httpCode == HTTP_CODE_OK) {
    doc.clear();
    // Parse directly from the stream to save RAM
    DeserializationError error = deserializeJson(doc, http.getStream());
    
    if (!error) {
      Serial.println("Database info retrieved successfully.");
    } else {
      Serial.print("JSON Parse Error: ");
      Serial.println(error.f_str());
    }
  } else {
    Serial.printf("Database GET failed (%d): %s\n", httpCode, http.errorToString(httpCode).c_str());
  }

  http.end();
  return doc;
}

String SetDB(){
  JsonDocument doc;
    Serial.println("--- Fetching DB List ---");
  do {
    doc = GetDB();
  } while (doc[0]["name"].isNull());
  int choice;
  String db;
  for (int i = 0; i < doc.size(); i++){
        Serial.printf("[%i] Found db: %s\n",
          i,
          doc[i]["name"].as<const char*>());
      }
      while(choice > doc.size()){
        while (Serial.available() == 0){
          delay(1);
        }
        choice = Serial.parseInt();
      }
      db = doc[choice]["name"].as<const char*>();
      choice = 100000;
      while(Serial.available() > 0) { Serial.read(); }
      return db;
}

JsonDocument GetStorageBlocks() {
  JsonDocument doc;

  WiFiClientSecure secureClient;
  secureClient.setInsecure(); // Handles the -k (insecure) flag

  HTTPClient http;
  
  // Construct the URL for storage blocks
  String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) 
               + String(settings.def_path)
               + String(db_name)
               + settings.stor_path;

  http.begin(secureClient, url);

  // Set Headers
  String bearerToken = "Bearer " + authdoc["access_token"].as<String>();
  http.addHeader("Authorization", bearerToken);
  http.addHeader("Accept", "application/json");

  Serial.println(">>> Fetching Storage Blocks...");
  int httpCode = http.GET();

  if (httpCode == HTTP_CODE_OK) {
    doc.clear(); // Reusing getdoc to save memory
    DeserializationError error = deserializeJson(doc, http.getStream());
    
    if (!error) {
      Serial.println("Storage blocks retrieved successfully.");
    } else {
      Serial.printf("JSON Parse Error: %s\n", error.f_str());
    }
  } else {
    Serial.printf("Storage GET failed (%d): %s\n", httpCode, http.errorToString(httpCode).c_str());
  }

  http.end();
  return doc;
}

JsonDocument GetCount(String identifier) {
  JsonDocument doc;
  WiFiClientSecure secureClient;
  secureClient.setInsecure();

  HTTPClient http;
  String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) + settings.def_path + String(db_name) + String(settings.get_path) + identifier;

  http.begin(secureClient, url);

  String bearerToken = "Bearer " + authdoc["access_token"].as<String>();
  http.addHeader("Authorization", bearerToken);
  http.addHeader("Accept", "application/json");

  uint32_t starttime = millis();
  bool success = false;

  while (!success && (millis() - starttime < 10000)) {
    int httpCode = http.GET();

    if (httpCode == HTTP_CODE_OK) {
      doc.clear();
      DeserializationError error = deserializeJson(doc, http.getStream());
      if (!error) success = true;
    } else {
      Serial.printf("GET failed (%d)\n", httpCode);
      delay(2000);
    }
  }
  http.end();
  return doc;
}

void UpdateCount(String stblock, String itemID, int transaction, int value) {
  if (WiFi.status() != WL_CONNECTED) return;

  WiFiClientSecure secureClient;
  secureClient.setInsecure(); // Handles the -k (insecure) flag

  HTTPClient http;
  
  // Construct the URL using the Item ID from the scan
  String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) 
               + String(settings.def_path)
               + String(db_name)
               + "/inventory/item/" 
               + String(itemID) + "/stored/transaction";

  http.begin(secureClient, url);

  // Set Headers
  String bearerToken = "Bearer " + authdoc["access_token"].as<String>();
  http.addHeader("Authorization", bearerToken);
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Accept", "application/json");

  // Construct the simplified JSON Body
  JsonDocument doc;
  
  //Check out and check in are not finished
  switch(transaction){
    case 1: {
      doc["type"] = "ADD_AMOUNT";
      doc["toBlock"] = stblock;
      break;
    }

    case 2: {
      doc["type"] = "SUBTRACT_AMOUNT";
      doc["fromBlock"] = stblock;
      break;
    }

    case 3: {
      doc["type"] = "CHECKOUT_AMOUNT";
      doc["fromBlock"] = stblock;
      break;
    }

    case 4: {
      doc["type"] = "CHECKIN_AMOUNT";
      doc["toBlock"] = stblock;
      break;
    }

    case 5: {
      doc["type"] = "SET_AMOUNT";
      doc["block"] = stblock;
      break;
    }
  }

  JsonObject amount = doc["amount"].to<JsonObject>();
  amount["value"] = value;
  amount["scale"] = "ABSOLUTE";

  // Simplified unit structure: {"string": "units"}
  JsonObject unit = amount["unit"].to<JsonObject>();
  unit["string"] = "units";

  // Serialize and POST
  String requestBody;
  serializeJson(doc, requestBody);
  
  Serial.println(">>> Sending Transaction...");
  int httpCode = http.POST(requestBody);

  if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_CREATED) {
    Serial.println("Success: Item count updated.");
  } else {
    Serial.printf("Failed (%d): %s\n", httpCode, http.errorToString(httpCode).c_str());
    // Print server message to see if 'toBlock' or 'toStored' are rejected
    Serial.println("Response: " + http.getString());
  }

  http.end();
}

void DetailedUpdate(JsonDocument item, JsonDocument stblocks){
  uint transaction = 10;
  uint value;
  int choice = 1000;
  int block_found = 0;
  if (!item["results"][0]["name"].isNull()) { //Prints details of item found through barcode
    Serial.printf("Found %d of item %s\n", 
                  item["results"][0]["stats"]["total"]["value"].as<int>(),
                  item["results"][0]["name"].as<const char*>());
    item_ID = item["results"][0]["id"].as<const char*>();
    item_name = item["results"][0]["name"].as<const char*>();

    if (item["results"][0]["storageBlocks"].size() > 1){ //If there are more than one storage block containing the item, allow the user to choose which block to update
    for(int i = 0; i < stblocks["results"].size(); i++){ //Prints storage blocks that contain the item
      for (int j = 0; j < item["results"][0]["storageBlocks"].size(); j++){
        if(item["results"][0]["storageBlocks"][j].as<String>() == stblocks["results"][i]["id"].as<String>()){
          Serial.printf("[%i] Found storage block: %s\n",
            block_found,
            stblocks["results"][i]["label"].as<const char*>());
            block_found++;
        }
      }
    }
      while (choice > item["results"][0]["storageBlocks"].size()){
        while (Serial.available() == 0){
          delay(5);
        }
        choice = Serial.parseInt();
      }
      while(Serial.available() > 0) { Serial.read(); }
      storage_block = item["results"][0]["storageBlocks"][choice].as<const char*>();
      choice = 1000;
    } else {
      storage_block = item["results"][0]["storageBlocks"][0].as<const char*>();
    }
    for(int i = 0; i < stblocks["results"].size(); i++){ //Retrieve label of storage block chosen
      if(stblocks["results"][i]["id"].as<String>() == storage_block){
        block_name = stblocks["results"][i]["label"].as<const char*>();
      }
    }
    Serial.println("Enter transaction type: "); //choose transaction type, fairly obvious
    Serial.println("[1] Add Amount");
    Serial.println("[2] Subtract Amount");
    Serial.println("[3] Check Out Amount");
    Serial.println("[4] Check In Amount");
    Serial.println("[5] Set Amount");

    while (transaction > 5){
        while (Serial.available() == 0){
          delay(5);
        }
        transaction = Serial.parseInt();
      }
    while(Serial.available() > 0) { Serial.read(); }

    Serial.println("Enter desired value: "); //Choose value to update with

    while (Serial.available() == 0){
      delay(5);
    }
    value = Serial.parseInt();
    while(Serial.available() > 0) { Serial.read(); }

    Serial.println("--- Updating ---");
    ////Example, adds 1 to count
    UpdateCount(storage_block, item_ID, transaction, value);
  }
}

void QuickUpdate(JsonDocument stblocks, bool add){  //Constantly looks for a new scanned item, then +- 1 to the count in the chosen storage block
  int block_found = 0;                              //To change storage block or add/subtract bool, will need to restart function
  int choice = 1000;
  Serial.println("Choose a storage block: ");  //Choose a storage block for the duration of the loop
  for (int i = 0; i < stblocks["results"].size(); i++){
    Serial.printf("[%i] Storage block: %s\n",
      i,
      stblocks["results"][i]["label"].as<String>());
  }

  while (choice > stblocks["results"].size()){
    while (Serial.available() == 0){
          delay(5);
        }
        choice = Serial.parseInt();
  }
  while(Serial.available() > 0) { Serial.read(); }

  while(true) {
      if(digitalRead(BUTTON) == LOW) { //interrupt to end loop
        Serial.println("End of update...");
        break;
      }
      Serial.println("Updating..."); //This is where we will add the scanning loop
      if (add){
        UpdateCount(GetCount("P5400E"), item_ID, 1, 1);
      } else {
        UpdateCount(GetCount("P5400E"), item_ID, 1, 2);
      }
      delay(1000);

  }
}
