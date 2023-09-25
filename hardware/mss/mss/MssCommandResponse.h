#ifndef MSS_COMMAND_RESPONSE_H
#define MSS_COMMAND_RESPONSE_H

#include <Arduino.h>
#include <ArduinoJson.h>

enum ResponseType{
    OK,
    R_ERR,
    ERR
};

static const __FlashStringHelper *responseTypeFromEnum(ResponseType type) {
    if (type == ResponseType::OK) {
        return F("OK");
    } else if (type == ResponseType::R_ERR) {
        return F("R_ERR");
    }
    return F("ERR");
}

//class CommandResponse {
//private:
//    ResponseType responseType;
//    const char* description = char[0];
//    JsonDocument response;
//protected:
//public:
//    CommandResponse(ResponseType responseType){
//        this->responseType = responseType;
//    }
//    CommandResponse(ResponseType responseType, const char* description){
//        this->responseType = responseType;
//        this->description = description;
//    }
//    CommandResponse(ResponseType responseType, JsonDocument response){
//        this->responseType = responseType;
//        this->response = response;
//    }
//    CommandResponse(JsonDocument response){
//        this->responseType = OK;
//        this->response = response;
//    }
//
//
//
//    ResponseType getResponseType(){
//        return this->responseType;
//    }
//
//
//};

#endif
