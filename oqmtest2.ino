#include <WiFi.h>
#include <ArduinoJson.h>
#include <WiFiClientSecure.h>
#include <ArduinoHttpClient.h>
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

unsigned long time_at_auth;

// Global objects
WiFiClientSecure secureClient;
HttpClient client = HttpClient(secureClient, settings.remote_host, settings.remote_port);

JsonDocument authdoc;
JsonDocument getdoc;
String iden;
String iden_str;
String key_str;

void setup() {
  // Start serial for debugging
  Serial.begin(115200);
  delay(1000); 
  time_at_auth = 0;

  // Connect to WiFi
  Serial.print("Connecting to: ");
  Serial.println(settings.my_ssid);
  WiFi.begin(settings.my_ssid, settings.my_pass);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi Connected!");

  // Equivalent to curl -k (ignores SSL certificate validation)
  secureClient.setInsecure();
}

void loop() {
  //Timing for actual key
  if ((millis() - time_at_auth) > (authdoc["expires_in"].as<unsigned long>() * 1000 * .75)){
    requestAuth();
    time_at_auth = millis();
    delay(1000);
  } else {
    Serial.print("Key still valid, time is: ");
    Serial.println(time_at_auth);
    Serial.print("time to expire: ");
    Serial.println(authdoc["expires_in"].as<unsigned long>());

    delay(1000);
    Serial.println("getting count...");
    GetCount("P5400E");
    
    Serial.print("Found ");
    Serial.print(getdoc["results"][0]["stats"]["total"]["value"].as<int>()); 
    Serial.print(" of item ");
    Serial.println(getdoc["results"][0]["name"].as<String>());
    Serial.println();

  }

  delay(1000);
}

void requestAuth() {
  authdoc.clear();

  // Only run if WiFi is still connected
  if (WiFi.status() == WL_CONNECTED) {
    
    // 1. Manually create the Basic Auth string
    String authRaw = String(settings.auth_user) + ":" + String(settings.auth_pass);
    String authHeader = "Basic " + base64::encode(authRaw);

    // 2. The POST body
    String bodyData = "grant_type=client_credentials";

    Serial.println(">>> Requesting Token...");

    // 3. Start the request
    client.beginRequest();
    client.post(settings.auth_path);
    
    // 4. Send the required headers
    client.sendHeader("Content-Type", "application/x-www-form-urlencoded");
    client.sendHeader("Authorization", authHeader);
    client.sendHeader("Content-Length", bodyData.length());
    
    // 5. Send the body and finish
    client.beginBody();
    client.print(bodyData);
    client.endRequest();

    // 6. Read and print the results
    int statusCode = client.responseStatusCode();
    String responseBody = client.responseBody();
    deserializeJson(authdoc, responseBody);

    Serial.print("HTTP Status: ");
    Serial.println(statusCode);
    Serial.println("Response:");
    Serial.println(responseBody);
    Serial.println("key:");
    Serial.println(authdoc["access_token"].as<String>());
    Serial.println("-----------------------------------");

  } else {
    Serial.println("WiFi Disconnected. Reconnecting...");
    WiFi.begin(settings.my_ssid, settings.my_pass);
  }

}

void GetCount(String Identifier){
  if (WiFi.status() == WL_CONNECTED) {
    //creates path string
    iden_str = settings.get_path + Identifier;
    //creates authstring with key from getauth()
    key_str = "Bearer " + authdoc["access_token"].as<String>();

    //Begins request, sends response format and auth as headers
    client.beginRequest();
    client.get(iden_str);
    client.sendHeader("accept", "application/json");
    client.sendHeader("Authorization", key_str);
    client.endRequest();

    String responseBody = client.responseBody();

    deserializeJson(getdoc, responseBody);
  } else {
    Serial.println("WiFi Disconnected. Reconnecting...");
    WiFi.begin(settings.my_ssid, settings.my_pass);
  }
}
