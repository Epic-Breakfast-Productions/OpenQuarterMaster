#ifndef MSS_SERIAL_CONNECTOR_H
#define MSS_SERIAL_CONNECTOR_H

#include "MssConnector.h"
#include "MssCommand.h"

class MssSerialConnector : public MssConnector {
  public:
    MssSerialConnector(){}

    bool hasCommand(){
        //TODO
        return false;
    }

    Command getCommand(){
        //TODO:: this for real
        DynamicJsonDocument doc(128);
        doc[F("command")] = F("GET_MODULE_INFO");

        return Command::parse(doc);
    }

    void sendStr(String str){
        //TODO
    }
};
#endif
