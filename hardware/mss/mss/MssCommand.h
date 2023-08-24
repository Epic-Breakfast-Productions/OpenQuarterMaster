#ifndef MSS_COMMAND_H
#define MSS_COMMAND_H

#include <Arduino.h>
#include <ArduinoJson.h>

class Command {
private:
    String command;
protected:
    Command(String command){
        this->command = command;
    }
public:
    String getCommand(){
        return this->command;
    }

    /**
     *
     * @param doc
     * @return
     */
    static Command parse(JsonDocument& doc){
        //TODO:: switch case to do this
        return Command("");
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
