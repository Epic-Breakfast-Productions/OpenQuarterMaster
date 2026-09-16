#ifndef MSS_SERIAL_CONNECTOR_H
#define MSS_SERIAL_CONNECTOR_H

#include "MssConnector.h"
#include "MssCommand.h"

/**
 * MssConnector that uses default usb-connected serial port to communicate.
 *
 * TODO:: contemplate: https://docs.arduino.cc/learn/built-in-libraries/software-serial
 */
class MssSerialConnector : public MssConnector {
  public:
    long baud = 115200;

    MssSerialConnector(){}
    MssSerialConnector(long baud){
        this->baud = baud;
    }

    void init(){
        Serial.begin(this->baud);
        while (!Serial) continue;
        Serial.setTimeout(1500);

//        Serial.print(F("DEBUG:: start. Size of crgb:"));
//        Serial.println(sizeof CRGB(0,0,0));
    }

    bool hasCommand(){
        return Serial.available();
    }

    DeserializationError getCommand(JsonDocument &doc){
        return deserializeJson(doc, Serial);
    }

    void send(JsonDocument& payload){
        serializeJson(payload, Serial);
        Serial.println();
    }
};
#endif
