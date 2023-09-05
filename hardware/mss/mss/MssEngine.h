#ifndef MSS_ENGINE_H
#define MSS_ENGINE_H

#ifndef MSS_VAR_NBLOCKS
#error "MSS_VAR_NBLOCKS not defined."
#endif
#ifndef MSS_LED_PIN
#error "MSS_LED_PIN not defined."
#endif
#ifndef MSS_VAR_NLEDS_PER_BLOCK
#error "MSS_VAR_NLEDS_PER_BLOCK not defined."
#endif
#ifndef MSS_SPKR_PIN
#error "MSS_SPKR_PIN not defined."
#endif

#ifndef MSS_NUM_LEDS
#define MSS_NUM_LEDS    MSS_VAR_NBLOCKS * MSS_VAR_NLEDS_PER_BLOCK
#endif
#define NOTE_C4  262


#include <Arduino.h>
#include <FastLED.h>
#include <ArduinoJson.h>
#include "MssModInfo.h"
#include "MssConnector.h"
#include "MssCommand.h"
#include "BlockState.h"

/**
 * http://fastled.io/docs/class_c_fast_l_e_d.html
 */
class MssEngine {
private:
    // https://forum.arduino.cc/t/how-to-read-the-id-serial-number-of-an-arduino/45214/8
    MssModInfo modInfo;
    MssConnector *connector;
    BlockState blockStateArr[MSS_VAR_NBLOCKS];
    CRGB leds[MSS_NUM_LEDS];
    byte brightness = 255;

    bool loopDelay = false;
public:
    MssEngine(
            MssModInfo modInfo,
            MssConnector *connector,
            bool loopDelay
    ) {
        this->modInfo = modInfo;
        this->connector = connector;
        this->loopDelay = loopDelay;
    }

    MssConnector *getConnector() {
        return this->connector;
    }

    /**
     * Call this to init the engine.
     */
    void init() {
        pinMode(MSS_SPKR_PIN, OUTPUT);
        for (int i = 1; i <= MSS_VAR_NBLOCKS; i++) {
            BlockState newState(i);
            this->blockStateArr[i] = newState;
        }
        FastLED.addLeds<WS2812B, MSS_LED_PIN, GRB>(this->leds, MSS_NUM_LEDS);
        this->submitLedState();

        this->connector->init();

        tone(MSS_SPKR_PIN, 2093, 250);
    }

    void processCommand() {
        Command *command;
        { // Needs to happen here due to the limitations of returning abstract classes in C++
            StaticJsonDocument<256> commandJson;
            DeserializationError error = this->connector->getCommand(commandJson);

            if (error) {
//            Serial.print(F("DEBUG:: deserializeJson() failed: "));Serial.println(error.f_str());
                if (error == DeserializationError::EmptyInput) {
                    //nothing to do
                    return;
                } else {
                    this->connector->send(ResponseType::R_ERR, F("Could not parse Json."));
                    return;
                }
            }

            Serial.print(F("DEBUG:: input json: "));
            serializeJson(commandJson, Serial);
            Serial.println();

            const char *commandStr = commandJson[F("command")];
            Serial.print("DEBUG:: ");
            Serial.println(commandStr);

            if (strcmp_P(commandStr, (PGM_P) F("GET_MODULE_INFO")) == 0) {
//                Serial.println(F("DEBUG:: was info command"));
                command = new GetModuleInfoCommand();
            } else if (strcmp_P(commandStr, (PGM_P) F("GET_MODULE_STATE")) == 0) {
                command = new GetModuleStateCommand();
            } else if (strcmp_P(commandStr, (PGM_P) F("HIGHLIGHT_BLOCKS")) == 0) {
                JsonArray blockArr = commandJson[F("storageBlocks")].as<JsonArray>();
                int numSettings = blockArr.size();
                HighlightBlocksCommandLightSetting settings[numSettings];

                //TODO:: fails, presumably due to running out of memory?
//                for (int i = 0; i < numSettings; i++) {
//                    JsonObject v = blockArr[i].as<JsonObject>();
//
//                    PowerState powerState = PowerState::ON;
//                    {
//                        const char *lightSettingStr = v[F("lightPowerState")];
//                        if (strcmp_P(commandStr, (PGM_P) F("FLASHING")) == 0) {
//                            powerState = PowerState::FLASHING;
//                        }
//                    }
//
//                    settings[i] = HighlightBlocksCommandLightSetting(
//                        v[F("blockNum")].as<uint16_t>(),
//                        powerState,
//                        ColorUtils::getColorFromString(v[F("lightColor")].as<const char*>())
//                    );
//
//                }

                command = new HighlightBlocksCommand(
                        commandJson[F("duration")].as<int16_t>(),
                        commandJson[F("carry")].as<bool>(),
                        numSettings,
                        settings
                );
            } else {
                this->connector->send(ResponseType::R_ERR, F("Unrecognized command given."));
                return;
            }
        }

        switch (command->getCommand()) {
            case CommandType::GET_MODULE_INFO:
                this->sendModInfo();
                break;
            default:
                this->connector->send(ResponseType::ERR, F("Unsupported operation."));
        }
    }

    void loop() {
        if (this->connector->hasCommand()) {
            this->processCommand();
        }
        if (this->loopDelay) {
            delay(250);
        }
    }


    BlockState *getBlock(unsigned int blockNum) {
        return &(this->blockStateArr[blockNum - 1]);
    }

    void submitLedState() {
        for (int i = 1; i <= MSS_VAR_NBLOCKS; i++) {
            CRGB curColor = this->getBlock(i)->getLightSetting()->getColor();

            if (this->getBlock(i)->getLightSetting()->getPowerState() == PowerState::OFF) {
                curColor = CRGB(0, 0, 0);
            }

            unsigned int ledStartInd = (i - 1) * 3;
            for (int j = ledStartInd; j < (ledStartInd + MSS_VAR_NLEDS_PER_BLOCK); j++) {
                this->leds[j] = curColor;
            }
        }
        FastLED.setBrightness(this->brightness);
        FastLED.show();
    }

    void sendModInfo() {
        this->connector->send(this->modInfo);
    }

    void resetLightPowerState() {
        for (int i = 1; i <= MSS_VAR_NBLOCKS; i++) {
            this->getBlock(i)->getLightSetting()->turnOff();
        }
    }

    void lightTest() {
        for (int i = 1; i <= MSS_VAR_NBLOCKS; i++) {
            this->getBlock(i)->getLightSetting()->setRandColor();

            this->resetLightPowerState();
            this->getBlock(i)->getLightSetting()->turnOn();
            this->submitLedState();
            delay(DELAY);
        }

        for (int i = MSS_VAR_NBLOCKS; i >= 1; i--) {
            this->getBlock(i)->getLightSetting()->setRandColor();

            this->resetLightPowerState();
            this->getBlock(i)->getLightSetting()->turnOn();
            this->submitLedState();
            tone(MSS_SPKR_PIN, 130 * i, DELAY / 4);
            delay(DELAY / 4);
        }

        this->resetLightPowerState();

        this->getBlock(1)->getLightSetting()->turnOn();
        this->getBlock(1)->getLightSetting()->setColor(CRGB(255, 0, 0));
        this->getBlock(MSS_VAR_NBLOCKS)->getLightSetting()->turnOn();
        this->getBlock(MSS_VAR_NBLOCKS)->getLightSetting()->setColor(CRGB(255, 0, 0));
        this->submitLedState();

        tone(MSS_SPKR_PIN, 130 * 4, DELAY);
        delay(DELAY);
        tone(MSS_SPKR_PIN, 130 * 4, DELAY);
        delay(DELAY);
        delay(DELAY);
    }
};

#endif
