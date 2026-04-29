#include <SdFat_Adafruit_Fork.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <SPI.h>
#include <base64.h>

struct MyProjectSettings {
  const char* my_ssid  = "speedscanner";
  const char* my_pass  = "Admin1$$$";
  const char* remote_host = "10.1.6.27";
  const int   remote_port = 443; 
  const char* db_path = "/core/api/api/v1/inventory/manage/db";
  const char* def_path = "/core/api/api/v1/db/";
  const char* stor_path = "/inventory/storage-block";
  const char* get_path = "/inventory/item?identifier=";
  const char* auth_path = "/infra/keycloak/realms/oqm/protocol/openid-connect/token";
  const char* auth_user = "scanner";
  const char* auth_pass = "h5j9inH-cx4LbQKnYmG6wvrvMUi7TEAv";
} settings;

const char* db_name;
const char* storage_block;
const char* block_name;
const char* item_ID;
const char* item_name;
SdFat32 SD;
bool gotSD = false;

unsigned long time_at_auth = 0;
JsonDocument authdoc;
JsonDocument dbdoc;
JsonDocument stblockdoc;
JsonDocument getdoc;
JsonDocument secretdoc;

void requestAuth();
void GetDB();
void GetStorageBlocks();
void GetCount(const char* identifier);
void UpdateCount(const char* stblock, const char* itemID, int transaction, int value);

void setup() {
  Serial.begin(115200);
  delay(1000);

  //Currently non functional SD card setup
  // SD.begin(5);
  // File file = SD.open("sdsetup.json");
  // if (file) {
  //   Serial.println("Loading from SD card...");
  //   String sdfile;
  //   while(file.available()){
  //     sdfile += file.read();
  //   }
  //   deserializeJson(secretdoc, sdfile);
  //   db_name = secretdoc["database"].as<const char*>();
  //   storage_block = secretdoc["storage-block"].as<const char*>();
  //   settings.my_ssid = secretdoc["ssid"].as<const char*>();
  //   settings.my_pass = secretdoc["password"].as<const char*>();
  //   settings.remote_host = secretdoc["oqm-address"].as<const char*>();
  //   settings.auth_user = secretdoc["oqm-user"].as<const char*>();
  //   settings.auth_pass = secretdoc["oqm-secret"].as<const char*>();
  //   gotSD = true;
  // } else {
  //   Serial.println("No SD card found");
  // }

  Serial.printf("\nConnecting to %s", settings.my_ssid);
  WiFi.begin(settings.my_ssid, settings.my_pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");

  requestAuth();

  //Setup if no SD card found, will remove
  if (!gotSD) {
    Serial.println("--- Fetching DB List ---");
    do {
      GetDB();
    } while (dbdoc[0]["name"].isNull());

    for (int i = 0; i < dbdoc.size(); i++){
      Serial.printf("Found db: %s\n",
        dbdoc[i]["name"].as<const char*>());
    }
    db_name = dbdoc[0]["name"].as<const char*>();

    Serial.println("--- Fetching Storage Block List ---");
    do {
      GetStorageBlocks();
    } while (stblockdoc["results"][0]["label"].isNull());

    for (int i = 0; i < stblockdoc["results"].size(); i++){
      Serial.printf("Found storage block: %s\n",
        stblockdoc["results"][i]["label"].as<const char*>());
    }
    storage_block = stblockdoc["results"][2]["id"].as<const char*>();
    block_name = stblockdoc["results"][2]["label"].as<const char*>();
  }

}

void loop() {
  // Check if token needs refreshing
  unsigned long expiry = authdoc["expires_in"].as<unsigned long>();
  if (time_at_auth == 0 || (millis() - time_at_auth) > (expiry * 1000 * 0.75)) {
    requestAuth();
  }

  Serial.println("--- Fetching Item Count ---");
  GetCount("P5400E");
  
  if (!getdoc["results"][0]["name"].isNull()) {
    Serial.printf("Found %d of item %s\n", 
                  getdoc["results"][0]["stats"]["total"]["value"].as<int>(),
                  getdoc["results"][0]["name"].as<const char*>());
    item_ID = getdoc["results"][0]["id"].as<const char*>();
    item_name = getdoc["results"][0]["name"].as<const char*>();
    
    Serial.println("--- Updating ---");
    //Example, adds 1 to count
    UpdateCount(storage_block, item_ID, 1, 1);
  }


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

void GetDB(){
  if (WiFi.status() != WL_CONNECTED) return;

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
    dbdoc.clear();
    // Parse directly from the stream to save RAM
    DeserializationError error = deserializeJson(dbdoc, http.getStream());
    
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
}

void GetStorageBlocks() {
  if (WiFi.status() != WL_CONNECTED) return;

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
    stblockdoc.clear(); // Reusing getdoc to save memory
    DeserializationError error = deserializeJson(stblockdoc, http.getStream());
    
    if (!error) {
      Serial.println("Storage blocks retrieved successfully.");
    } else {
      Serial.printf("JSON Parse Error: %s\n", error.f_str());
    }
  } else {
    Serial.printf("Storage GET failed (%d): %s\n", httpCode, http.errorToString(httpCode).c_str());
  }

  http.end();
}

void GetCount(const char* identifier) {
  if (WiFi.status() != WL_CONNECTED) return;

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
      getdoc.clear();
      DeserializationError error = deserializeJson(getdoc, http.getStream());
      if (!error) success = true;
    } else {
      Serial.printf("GET failed (%d)\n", httpCode);
      delay(2000);
    }
  }
  http.end();
}

void UpdateCount(const char* stblock, const char* itemID, int transaction, int value) {
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
