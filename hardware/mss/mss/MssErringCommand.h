#ifndef MSS_ERRING_COMMAND_H
#define MSS_ERRING_COMMAND_H

#include "MssCommand.h."

class ErringCommand : public Command {
private:
    CommandType commandType;
protected:
    ErringCommand(CommandType command){
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
//        Serial.print(F("DEBUG:: input json: "));serializeJson(doc, Serial);Serial.println();

        const char* commandStr = doc[F("command")];

//        Serial.print("DEBUG:: ");Serial.println(commandStr);

        if(strcmp_P(commandStr, (PGM_P)F("GET_MODULE_INFO")) == 0){
//            Serial.println(F("DEBUG:: was info command"));
            return Command(GET_MODULE_INFO);
        } else if(strcmp_P((PGM_P)F("GET_MODULE_STATE"), commandStr) == 0){
            return Command(GET_MODULE_STATE);
        }
//        Serial.println(F("DEBUG:: was error"));
        return Command(CommandType::ERROR);
    }

    /**
     * Parses a command from a stream.
     * @param stream
     * @return
     */
    static Command parse(Stream& stream){
        StaticJsonDocument<256> doc;
        DeserializationError error = deserializeJson(doc, stream);

        if (error) {
//            Serial.print(F("DEBUG:: deserializeJson() failed: "));Serial.println(error.f_str());

            if(error == DeserializationError::EmptyInput){
                return Command(CommandType::NULL_OP);
            } else {
                return Command(CommandType::ERROR);
            }
        }

        return parse(doc);
    }
};

#endif
