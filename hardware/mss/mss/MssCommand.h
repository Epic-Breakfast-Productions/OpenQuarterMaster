#ifndef MSS_COMMAND_H
#define MSS_COMMAND_H

#include <Arduino.h>
#include <ArduinoJson.h>

enum CommandType {
    GET_MODULE_INFO,
    GET_MODULE_STATE,
    GET_BLOCK_STATE,
    SET_BLOCK_STATE,
    ERR
};

class Command {
private:
    CommandType commandType;
protected:
    Command(CommandType command){
        this->commandType = command;
    }
public:
    CommandType getCommand(){
        return this->commandType;
    }

    /**
     *
     * @param doc
     * @return
     */
    static Command parse(JsonDocument& doc){
        const char* commandStr = doc[F("command")];

        if(strcmp_P((PGM_P)F("GET_MODULE_INFO"), commandStr) == 0){
            return Command(GET_MODULE_INFO);
        } else if(strcmp_P((PGM_P)F("GET_MODULE_STATE"), commandStr) == 0){
            return Command(GET_MODULE_STATE);
        }

        return Command(ERR);
    }

    /**
     * Parses a command from a stream.
     * @param stream
     * @return
     */
    static Command parse(Stream& stream){
        DynamicJsonDocument doc(1024);
        deserializeJson(doc, stream);
        return parse(doc);
    }
};

#endif
