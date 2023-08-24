#ifndef MSS_CONNECTOR_H
#define MSS_CONNECTOR_H

#include <ArduinoJson.h>

class MssConnector {
public:
  virtual bool hasCommand() = 0;
  virtual JsonDocument* getCommand() = 0;
  virtual void sendStr(String message) = 0;

  void sendJson(JsonDocument* payload){
    String output;
    serializeJson(*payload, output);
    this->sendStr(output);
  }

  void sendObj(ToJson* payload){
      this->sendJson(payload->toJson());
  }
};

#endif
