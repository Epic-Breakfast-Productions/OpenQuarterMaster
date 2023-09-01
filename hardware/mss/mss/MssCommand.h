#ifndef MSS_COMMAND_H
#define MSS_COMMAND_H

#include <Arduino.h>
#include <ArduinoJson.h>


enum CommandType {
    GET_MODULE_INFO,
    GET_MODULE_STATE,
    GET_BLOCK_STATE,
    HIGHLIGHT_BLOCKS,

    SET_BLOCK_STATE,

    REQUEST_ERROR,
    ERROR,
    NULL_OP
};

class Command {
private:
    CommandType commandType;
    const __FlashStringHelper *detail;
protected:
    Command(CommandType command) {
        this->commandType = command;
    }
    Command(CommandType command, const __FlashStringHelper *detail) {
        this->commandType = command;
        this->detail = detail;
    }

public:
    CommandType getCommand() {
        return this->commandType;
    }

    const __FlashStringHelper * getDetail(){
        return this->detail;
    }

    /**
     *
     * @param doc
     * @return
     */
    static Command parse(JsonDocument &doc);

    /**
     * Parses a command from a stream.
     * @param stream
     * @return
     */
    static Command parse(Stream &stream);
};

static Command Command::parse(JsonDocument &doc) {
//        Serial.print(F("DEBUG:: input json: "));serializeJson(doc, Serial);Serial.println();
    const char *commandStr = doc[F("command")];
//        Serial.print("DEBUG:: ");Serial.println(commandStr);
    if (strcmp_P(commandStr, (PGM_P)F("GET_MODULE_INFO")) == 0) {
//            Serial.println(F("DEBUG:: was info command"));
        return Command(GET_MODULE_INFO);
    } else if (strcmp_P((PGM_P) F("GET_MODULE_STATE"), commandStr) == 0) {
        return Command(GET_MODULE_STATE);
    }
//        Serial.println(F("DEBUG:: was error"));
    return Command(CommandType::REQUEST_ERROR, F("Unsupported command given."));
}

static Command Command::parse(Stream &stream) {
    StaticJsonDocument<256> doc;
    DeserializationError error = deserializeJson(doc, stream);

    if (error) {
//            Serial.print(F("DEBUG:: deserializeJson() failed: "));Serial.println(error.f_str());
        if (error == DeserializationError::EmptyInput) {
            return Command(CommandType::NULL_OP);
        } else {
            return Command(CommandType::REQUEST_ERROR, F("Could not parse json."));
        }
    }

    return parse(doc);
}

#endif
