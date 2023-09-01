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
    long baud = 9600;

    MssSerialConnector(){}
    MssSerialConnector(long baud){
        this->baud = baud;
    }

    void init(){
        Serial.begin(this->baud);
        while (!Serial) continue;
    }

    bool hasCommand(){
        return Serial.available();
    }

    Command getCommand(){
        return Command::parse(Serial);
    }

    void send(JsonDocument& payload){
        serializeJson(payload, Serial);
        Serial.println();
    }
};
#endif
