#include <WiFi.h>
#include <HTTPClient.h>
#include <WiFiClientSecure.h>
#include <ArduinoJson.h>
#include <base64.h>

struct MyProjectSettings {
  const char* my_ssid     = "speedscanner";
  const char* my_pass     = "Admin1$$$";
  const char* remote_host = "10.1.6.27";
  const int   remote_port = 443; 
  const char* get_path = "/core/api/api/v1/db/speedscanner/inventory/item?identifier=";
  const char* auth_path = "/infra/keycloak/realms/oqm/protocol/openid-connect/token";
  const char* auth_user   = "scanner";
  const char* auth_pass   = "h5j9inH-cx4LbQKnYmG6wvrvMUi7TEAv";
} settings;

unsigned long time_at_auth = 0;
JsonDocument authdoc;
JsonDocument getdoc;

void requestAuth();
void GetCount(const char* identifier);
void UpdateCount(const char* itemID, int amountValue);

void setup() {
  Serial.begin(115200);
  delay(1000);

  Serial.printf("\nConnecting to %s", settings.my_ssid);
  WiFi.begin(settings.my_ssid, settings.my_pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");
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
                  
    // Serial.println("Updating...");
    // UpdateCount(getdoc["results"][0]["id"].as<const char*>(), 1);
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

void GetCount(const char* identifier) {
  if (WiFi.status() != WL_CONNECTED) return;

  WiFiClientSecure secureClient;
  secureClient.setInsecure();

  HTTPClient http;
  String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) + settings.get_path + identifier;

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

// void UpdateCount(const char* itemID, int amountValue = 1) {
//   if (WiFi.status() != WL_CONNECTED) return;

//   WiFiClientSecure secureClient;
//   secureClient.setInsecure();

//   HTTPClient http;
  
//   // 1. Build the dynamic URL
//   // Note: The path below matches your curl example exactly
//   String url = "https://" + String(settings.remote_host) + ":" + String(settings.remote_port) 
//                + "/api/v1/db/speedscanner/inventory/item/" 
//                + String(itemID) + "/stored/transaction";

//   http.begin(secureClient, url);

//   // 2. Set Headers
//   String bearerToken = "Bearer " + authdoc["access_token"].as<String>();
//   http.addHeader("Authorization", bearerToken);
//   http.addHeader("Content-Type", "application/json");
//   http.addHeader("Accept", "application/json");

//   // 3. Construct the nested JSON Body
//   // We use a larger document size for nested objects
//   JsonDocument doc;
  
//   doc["toBlock"] = "0x00";  // Replace with your actual hex logic
//   doc["toStored"] = "0x00"; 
//   doc["type"] = "ADD_AMOUNT";

//   JsonObject amount = doc["amount"].to<JsonObject>();
//   amount["value"] = amountValue;
//   amount["scale"] = "ABSOLUTE";

//   JsonObject unit = amount["unit"].to<JsonObject>();
//   unit["symbol"] = "string";
//   unit["name"] = "string";
//   unit["systemUnit"] = "string";

//   // Create nested 'dimension' -> 'baseDimensions'
//   JsonObject baseDims = unit["dimension"]["baseDimensions"].to<JsonObject>();
//   baseDims["additionalProp1"] = 0;
//   baseDims["additionalProp2"] = 0;
//   baseDims["additionalProp3"] = 0;

//   // Create 'baseUnits'
//   JsonObject baseUnits = unit["baseUnits"].to<JsonObject>();
//   baseUnits["additionalProp1"] = 0;
//   baseUnits["additionalProp2"] = 0;
//   baseUnits["additionalProp3"] = 0;

//   // 4. Serialize to string and POST
//   String requestBody;
//   serializeJson(doc, requestBody);
  
//   Serial.println(">>> Executing Transaction Update...");
//   int httpCode = http.POST(requestBody);

//   // 5. Handle Response
//   if (httpCode == HTTP_CODE_OK || httpCode == HTTP_CODE_CREATED) {
//     Serial.println("Update Successful!");
//   } else {
//     Serial.printf("Update Failed (%d): %s\n", httpCode, http.errorToString(httpCode).c_str());
//     // Helpful for debugging 400 errors:
//     Serial.println("Server says: " + http.getString());
//   }

//   http.end();
// }
