#ifndef MSS_ENGINE_H
#define MSS_ENGINE_H

#include <Arduino.h>
#include <FastLED.h>
#include <ArduinoJson.h>
#include "MssModInfo.h"
#include "MssConnector.h"


class MssEngine {
  private:
    // https://forum.arduino.cc/t/how-to-read-the-id-serial-number-of-an-arduino/45214/8
    MssModInfo* modInfo;
    
    uint16_t numStorageBins;
    uint16_t numLedsInStrip;
    uint16_t ledPin;
    uint16_t ledBrightness;
    MssConnector* connector;
  
  public:
    MssEngine(
      MssModInfo* modInfo,
      MssConnector* connector,
      uint16_t numStorageBins,
      uint16_t numLedsInStrip,
      uint16_t ledPin,
      uint16_t ledBrightness,
      bool hasIndicator
    );

};



#endif
