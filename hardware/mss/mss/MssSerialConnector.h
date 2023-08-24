#ifndef MSS_SERIAL_CONNECTOR_H
#define MSS_SERIAL_CONNECTOR_H

#include "MssConnector.h"

class MssSerialConnector : public MssConnector {
  public:
    MssSerialConnector(){}

    bool hasCommand(){
        //TODO
        return false;
    }

    JsonDocument* getCommand(){
        //TODO
        return nullptr;
    }

    void sendStr(String str){
        //TODO
    }
};
#endif
