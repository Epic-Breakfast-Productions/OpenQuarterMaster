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

#ifndef MSS_NUM_LEDS
#define MSS_NUM_LEDS    MSS_VAR_NBLOCKS * MSS_VAR_NLEDS_PER_BLOCK
#endif


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
    MssConnector* connector;
    BlockState blockStateArr[MSS_VAR_NBLOCKS];
    CRGBArray<MSS_NUM_LEDS> leds;

  public:
    MssEngine(
      MssModInfo modInfo,
      MssConnector* connector
    ){
        this->modInfo = modInfo;
        this->connector = connector;
    }

    MssConnector* getConnector(){
        return this->connector;
    }

    /**
     * Call this to init the engine.
     */
    void init(){
        for(int i = 1; i <= MSS_VAR_NBLOCKS; i++){
            BlockState newState(i);
            this->blockStateArr[i] = newState;
//            CRGB* curColor = this->blockStateArr[i]->getLightSetting()->getColor();

            int ledStart = i * 3;
            int ledEnd = ledStart + 2;


            CRGBSet curBlockLeds(this->leds(ledStart, ledEnd));

            curBlockLeds = this->blockStateArr[i].getLightSetting()->setColor(curBlockLeds);

//            for(int j = i * 3; j < MSS_VAR_NLEDS_PER_BLOCK; j++){
//
//            }
        }
        FastLED.addLeds<WS2812B, MSS_LED_PIN, GRB>(this->leds, MSS_NUM_LEDS);
        this->submitLedState();
    }

    void loop(){
        //TODO
        this->modInfo.toJson();
    }

    void process(Command* command){
        //TODO
    }

    BlockState getBlock(unsigned int i){
        return this->blockStateArr[i];
    }

    void submitLedState(){
        FastLED.show();
    }

    void test(){
        for(int i = 0; i < MSS_VAR_NBLOCKS; i++){

            if(i > 0){
                CRGB noColor(0,0,0);
                this->getBlock(i - 1).getLightSetting()->setColor(noColor);
            }
            CRGB newColor(255,0,0);
            this->getBlock(i).getLightSetting()->setColor(newColor);
            this->submitLedState();
            delay(DELAY);
        }
    }
};



#endif
