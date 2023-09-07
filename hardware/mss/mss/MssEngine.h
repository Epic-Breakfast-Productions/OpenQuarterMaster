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

    bool curHighlighting = false;
    unsigned long highlightStart = 0;
    unsigned long highlightDuration = 0;
    int highlightTone = 262; // C4
    int highlightToneDuration = 250;

    bool lightsNeedUpdated = false;
public:
    MssEngine(
            MssModInfo modInfo,
            MssConnector *connector
    ) {
        this->modInfo = modInfo;
        this->connector = connector;
    }

    MssConnector *getConnector() {
        return this->connector;
    }

    /**
     * Call this to init the engine.
     */
    void init() {
        //Prevent too many leds lighting up and damaging things
        // http://fastled.io/docs/group___power.html
        pinMode(4, OUTPUT);
        set_max_power_in_volts_and_milliamps(5, 2000);
        set_max_power_indicator_LED(4);


        pinMode(MSS_SPKR_PIN, OUTPUT);
        for (int i = 1; i <= MSS_VAR_NBLOCKS; i++) {
            BlockState newState(i);
            this->blockStateArr[i] = newState;
        }

        FastLED.addLeds<WS2812B, MSS_LED_PIN, GRB>(0, 0);

//        FastLED.addLeds<WS2812B, MSS_LED_PIN, GRB>(this->leds, MSS_NUM_LEDS);
//        this->submitLedState();

        this->connector->init();

//
//        Serial.print(F("DEBUG:: start. Free ram:"));
//        Serial.println(MssEngine::freeRam());
//
//        Serial.print(F("DEBUG:: size of block state array:"));
//        Serial.println(sizeof blockStateArr);
//
//        Serial.print(F("DEBUG:: size of mod info:"));
//        Serial.println(sizeof this->modInfo);
//
//        Serial.print(F("DEBUG:: size of led array:"));
//        Serial.println(sizeof this->leds);

        tone(MSS_SPKR_PIN, 2093, 250);
    }

    void processCommand() {
        Command *command;
        { // Needs to happen here due to the limitations of returning abstract classes in C++
            StaticJsonDocument<256> commandJson;
            DeserializationError error = this->connector->getCommand(commandJson);

//            Serial.print(F("DEBUG:: after parse json. Free ram:"));
//            Serial.println(MssEngine::freeRam());

            if (error) {
//                Serial.print(F("DEBUG:: deserializeJson() failed: "));
//                Serial.println(error.f_str());
                if (error == DeserializationError::EmptyInput) {
                    //nothing to do
                    return;
                } else {
                    this->connector->send(ResponseType::R_ERR, F("Could not parse Json."));
                    return;
                }
            }

            command = Command::parse(commandJson);
//            commandJson.~StaticJsonDocument();
        }

//        Serial.print(F("DEBUG:: after parse command. Free ram:"));
//        Serial.println(MssEngine::freeRam());

        if (command == nullptr) {
            this->connector->send(ResponseType::R_ERR, F("Could not parse command."));
            return;
        }

        switch (command->getCommand()) {
            case CommandType::GET_MODULE_INFO:
                this->sendModInfo();
                break;
            case CommandType::CLEAR_HIGHLIGHT:
                this->clearHighlight();
                break;
            case CommandType::HIGHLIGHT_BLOCKS:
                this->highlightBlocks(command);
                break;
            default:
                this->connector->send(ResponseType::ERR, F("Unsupported operation."));
        }
        delete command;
    }

    void loop() {
        if (this->connector->hasCommand()) {
//            Serial.print(F("DEBUG:: before process. Free ram:"));
//            Serial.println(MssEngine::freeRam());
            this->processCommand();
        }

        if(this->curHighlighting){
            if(millis() - this->highlightStart >= this->highlightDuration) {
                this->clearHighlight();
            }
        }

        if(this->lightsNeedUpdated){
            this->submitLedState();
            this->lightsNeedUpdated = false;
        }
    }


    BlockState *getBlock(unsigned int blockNum) {
        return &(this->blockStateArr[blockNum - 1]);
    }

    /**
     * Submits the LED state that is setup in the BlockState array.
     *
     * !Creates the array required for this, can be large! call when memory is relatively clear.
     */
    void submitLedState() {
        CRGB tempLeds[MSS_NUM_LEDS];
        FastLED[0].setLeds(tempLeds, MSS_NUM_LEDS);
        FastLED.clear();

        for (int i = 1; i <= MSS_VAR_NBLOCKS; i++) {
            CRGB curColor = ColorUtils::getCRGBFromColor(this->getBlock(i)->getLightSetting()->getColor());

            if (this->getBlock(i)->getLightSetting()->getPowerState() == PowerState::OFF) {
                curColor = CRGB(0, 0, 0);
            }

            unsigned int ledStartInd = (i - 1) * 3;
            for (int j = ledStartInd; j < (ledStartInd + MSS_VAR_NLEDS_PER_BLOCK); j++) {
                tempLeds[j] = curColor;
            }
        }
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

    void clearHighlight(){
        this->curHighlighting = false;
        this->resetLightPowerState();
        this->lightsNeedUpdated = true;
        this->connector->send(ResponseType::OK);
    }

    void highlightBlocks(HighlightBlocksCommand *command) {
        this->curHighlighting = true;
        this->highlightStart = millis();
        this->highlightDuration = command->getDuration();

        if(!command->isCarry()){
            this->resetLightPowerState();
        }
//        Serial.println(F("DEBUG:: Got command to highlight blocks"));

        //TODO:: set timer for highlight duration
//        Serial.print(F("DEBUG:: num settings:" ));
//        Serial.println(command->getNumSettings());


        for(int i = 0; i < command->getNumSettings(); i++){
//            Serial.print(F("DEBUG:: cur blockNum:" ));
//            Serial.println(command->getSettings()[i].getBlockNum());
//            Serial.print(F("DEBUG:: cur blockColor:" ));
//            Serial.println(command->getSettings()[i].getColor());

            BlockState *curBlock = this->getBlock(command->getSettings()[i].getBlockNum());
            curBlock->getLightSetting()->turnOn();
            curBlock->getLightSetting()->setColor(command->getSettings()[i].getColor());
        }

        this->lightsNeedUpdated = true;
        this->connector->send(ResponseType::OK);
        if(command->doBeep()){
            tone(MSS_SPKR_PIN, this->highlightTone, this->highlightToneDuration);
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
        this->getBlock(1)->getLightSetting()->setColor(LightColor::RED);
        this->getBlock(MSS_VAR_NBLOCKS)->getLightSetting()->turnOn();
        this->getBlock(MSS_VAR_NBLOCKS)->getLightSetting()->setColor(LightColor::RED);
        this->submitLedState();

        tone(MSS_SPKR_PIN, 130 * 4, DELAY);
        delay(DELAY);
        tone(MSS_SPKR_PIN, 130 * 4, DELAY);
        delay(DELAY);
        delay(DELAY);

        //to see how many lights can be on at once, how things behave at high numbers of lights on
//        this->getBlock(MSS_VAR_NBLOCKS)->getLightSetting()->setRandColor();
//        this->getBlock(MSS_VAR_NBLOCKS)->getLightSetting()->turnOn();
//        for (int i = MSS_VAR_NBLOCKS; i >= 1; i--) {
//            this->getBlock(i)->getLightSetting()->turnOn();
//            this->submitLedState();
//            tone(MSS_SPKR_PIN, 130 * i, DELAY / 4);
//            delay(DELAY * 4);
//        }
//
//        tone(MSS_SPKR_PIN, 130 * 4, DELAY);
//        delay(DELAY);
//        tone(MSS_SPKR_PIN, 130 * 4, DELAY);
//        delay(DELAY);
//        delay(DELAY);

    }

    static int freeRam() {
        int size = 2048;
        byte *buf;
        while ((buf = (byte *) malloc(--size)) == NULL);
        free(buf);
        return size;
    }
};

#endif
