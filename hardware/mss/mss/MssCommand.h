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
    CLEAR_HIGHLIGHT,

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

/**
 * Example:
 * <pre>{"command":"CLEAR_HIGHLIGHT"}</pre>
 */
class ClearHighlightCommand : public Command {
public:
    ClearHighlightCommand() : Command(CommandType::CLEAR_HIGHLIGHT) {
    }
};

class HighlightBlocksCommandLightSetting {
private:
    unsigned int blockNum = 0;
    PowerState powerState = PowerState::ON;
    LightColor color = LightColor::RED;
public:
    HighlightBlocksCommandLightSetting() {
    }

    HighlightBlocksCommandLightSetting(
            unsigned int blockNum,
            PowerState powerState,
            LightColor color
    ) {
        this->blockNum = blockNum;
        this->powerState = powerState;
        this->color = color;
    }

    unsigned int getBlockNum() {
        return this->blockNum;
    }

    PowerState getPowerState() {
        return this->powerState;
    }

    LightColor getColor() {
        return this->color;
    }

};

/**
 * Example:
 * <pre>{"command":"HIGHLIGHT_BLOCKS", "duration": 5, "carry":false, "beep":true, "storageBlocks": [{"blockNum":1, "lightPowerState": "ON", "lightColor": "RAND"}]}</pre>
 */
class HighlightBlocksCommand : public Command {
private:
    int duration = 30;
    bool carry = false;
    bool beep = false;
    int numSettings;
    const HighlightBlocksCommandLightSetting *blockLightSettings;
public:
    HighlightBlocksCommand(
            int duration,
            bool carry,
            bool beep,
            int numSettings,
            const HighlightBlocksCommandLightSetting *blockLightSettings
    ) : Command(CommandType::HIGHLIGHT_BLOCKS) {
        this->duration = duration;
        this->carry = carry;
        this->beep = beep;
        this->blockLightSettings = blockLightSettings;
        this->numSettings = numSettings;
    }

    int getDuration() {
        return this->duration;
    }

    bool isCarry() {
        return this->carry;
    }

    bool doBeep() {
        return this->beep;
    }

    int getNumSettings() {
        return this->numSettings;
    }

    const HighlightBlocksCommandLightSetting *getSettings() {
        return this->blockLightSettings;
    }

    static HighlightBlocksCommand *parse(JsonDocument &commandJson) {
        JsonArray blockArr = commandJson[F("storageBlocks")].as<JsonArray>();
        int numSettings = blockArr.size();
        HighlightBlocksCommandLightSetting *settings = new HighlightBlocksCommandLightSetting[numSettings];

//        Serial.print(F("DEBUG:: num settings gotten:"));
//        Serial.println(numSettings);

        for (int i = 0; i < numSettings; i++) {
            JsonObject v = blockArr[i].as<JsonObject>();

            PowerState powerState = PowerState::ON;
            if (strcmp_P(v[F("lightPowerState")], (PGM_P) F("FLASHING")) == 0) {
                powerState = PowerState::FLASHING;
            }

            unsigned int blockNum = v[F("blockNum")].as<unsigned int>();

//            Serial.print(F("DEBUG:: block num gotten:" ));
//            Serial.println(numSettings);

            const char *colorStr = v[F("lightColor")].as<const char *>();

//            Serial.print(F("DEBUG:: color str gotten:" ));
//            Serial.println(colorStr);

            LightColor color = ColorUtils::getColorFromString(colorStr);


//            Serial.print(F("DEBUG:: color gotten:" ));
//            Serial.println(color);

            settings[i] = HighlightBlocksCommandLightSetting(
                    blockNum,
                    powerState,
                    color
            );

        }

        return new HighlightBlocksCommand(
                commandJson[F("duration")].as<int>() * 1000,
                commandJson[F("carry")].as<bool>(),
                commandJson[F("beep")].as<bool>(),
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
    } else if (strcmp_P(commandStr, (PGM_P) F("CLEAR_HIGHLIGHT")) == 0) {
        return new ClearHighlightCommand();
    }
    return nullptr;
}

#endif
