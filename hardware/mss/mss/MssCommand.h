#ifndef MSS_COMMAND_H
#define MSS_COMMAND_H

#include <Arduino.h>
#include <ArduinoJson.h>
#include "BlockLightSetting.h"


enum CommandType {
    GET_MODULE_INFO,
    GET_MODULE_STATE,
    GET_BLOCK_STATE,
    HIGHLIGHT_BLOCKS,

    SET_BLOCK_STATE
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

    const __FlashStringHelper *getDetail() {
        return this->detail;
    }

    static Command *parse(JsonDocument &doc);

    /**
     * Parses a command from a stream.
     * @param stream
     * @return
     */
    static Command *parse(Stream &stream);
};

/**
 * Example:
 * <pre>{"command":"GET_MODULE_INFO"}</pre>
 */
class GetModuleInfoCommand : public Command {
public:
    GetModuleInfoCommand() : Command(CommandType::GET_MODULE_INFO) {

    }
};

class GetModuleStateCommand : public Command {
public:
    GetModuleStateCommand() : Command(CommandType::GET_MODULE_STATE) {

    }
};

class HighlightBlocksCommandLightSetting {
private:
    unsigned int blockNum = 0;
    PowerState powerState = OFF;
    CRGB color = CRGB(0, 0, 0);
public:
    HighlightBlocksCommandLightSetting() {
    }

    HighlightBlocksCommandLightSetting(
            unsigned int blockNum,
            PowerState powerState,
            CRGB color
    ) {
        this->blockNum = blockNum;
        this->powerState = powerState;
        this->color = color;
    }
};

/**
 * Example:
 * <pre>{"command":"HIGHLIGHT_BLOCKS", "duration": 5, "carry":false, "storageBlocks": [{"blockNum":1, "lightPowerState": "ON", "lightColor": "RAND"}]}</pre>
 */
class HighlightBlocksCommand : public Command {
private:
    int duration = 30;
    bool carry = false;
    int numSettings;
    const HighlightBlocksCommandLightSetting *blockLightSettings;
public:
    HighlightBlocksCommand(
            int duration,
            bool carry,
            int numSettings,
            const HighlightBlocksCommandLightSetting *blockLightSettings
    ) : Command(CommandType::HIGHLIGHT_BLOCKS) {
        this->duration = duration;
        this->carry = carry;
        this->blockLightSettings = blockLightSettings;
        this->numSettings = numSettings;
    }

    static HighlightBlocksCommand* parse(JsonDocument &commandJson){
        JsonArray blockArr = commandJson[F("storageBlocks")].as<JsonArray>();
        int numSettings = blockArr.size();
        HighlightBlocksCommandLightSetting settings[numSettings];

        //TODO:: fails, presumably due to running out of memory?
        for (int i = 0; i < numSettings; i++) {
            JsonObject v = blockArr[i].as<JsonObject>();

            PowerState powerState = PowerState::ON;
            {
                if (strcmp_P(v[F("lightPowerState")], (PGM_P) F("FLASHING")) == 0) {
                    powerState = PowerState::FLASHING;
                }
            }

            settings[i] = HighlightBlocksCommandLightSetting(
                    v[F("blockNum")].as<uint16_t>(),
                    powerState,
                    ColorUtils::getColorFromString(v[F("lightColor")].as<const char *>())
            );

        }

        return new HighlightBlocksCommand(
                commandJson[F("duration")].as<int16_t>(),
                commandJson[F("carry")].as<bool>(),
                numSettings,
                settings
        );
    }
};

static Command *Command::parse(JsonDocument &commandJson) {
//            Serial.print(F("DEBUG:: input json: "));
//            serializeJson(commandJson, Serial);
//            Serial.println();

    const char *commandStr = commandJson[F("command")];
//            Serial.print(F("DEBUG:: "));
//            Serial.println(commandStr);

    if (strcmp_P(commandStr, (PGM_P) F("GET_MODULE_INFO")) == 0) {
//                Serial.println(F("DEBUG:: was info command"));
        return new GetModuleInfoCommand();
    } else if (strcmp_P(commandStr, (PGM_P) F("GET_MODULE_STATE")) == 0) {
        return new GetModuleStateCommand();
    } else if (strcmp_P(commandStr, (PGM_P) F("HIGHLIGHT_BLOCKS")) == 0) {
        return HighlightBlocksCommand::parse(commandJson);
    }
    return nullptr;
}

#endif
