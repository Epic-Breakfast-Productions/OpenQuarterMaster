#ifndef MSS_CONNECTOR_H
#define MSS_CONNECTOR_H

#include <ArduinoJson.h>

class MssConnector {
public:
  virtual bool hasCommand() = 0;
  virtual String getCommand() = 0;
  virtual void sendStr(String message) = 0;

  void sendJson(JsonObject payload){
    String output;
    serializeJson(payload, output);
    this->sendStr(output);
  }
};

#endif
