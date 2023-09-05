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

};

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
};

#endif
