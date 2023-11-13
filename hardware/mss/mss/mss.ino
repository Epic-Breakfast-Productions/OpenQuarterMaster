#define MSS_VAR_NBLOCKS 64
#define MSS_VAR_NLEDS_PER_BLOCK 3
#define MSS_LED_PIN     2
#define MSS_SPKR_PIN    3

#define DELAY    500

#include <FastLED.h>
#include <ArduinoJson.h>
#include "MssEngine.h"
#include "MssSerialConnector.h"

MssEngine mssEngine(
        MssModInfo(
                "1.0.0",
                "test-01",
                "Sep 2023",
                MssModCapabilities(
                        true,
                        true,
                        false
                )
        ),
        new MssSerialConnector()
);

void setup() {
    mssEngine.init();
}


void loop() {
    mssEngine.loop();
//    mssEngine.lightTest();

//    Serial.print(F("DEBUG:: end of loop. Free ram:"));
//    Serial.println(MssEngine::freeRam());

//    if(mssEngine.getConnector()->hasCommand()){
//        StaticJsonDocument<256> doc;
//        DeserializationError error = mssEngine.getConnector()->getCommand(doc);
//
//        if (error) {
//            Serial.print(F("deserializeJson() failed: "));
//            Serial.println(error.f_str());
//        } else {
//            serializeJson(doc, Serial);
//            Serial.println();
//        }
//
//    }
}
