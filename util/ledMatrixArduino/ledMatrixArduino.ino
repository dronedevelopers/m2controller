#include "FastLED.h"
#include "transportLayer.h"
#define NUM_LEDS 256
#define LED_PIN 3
#define ICON_CHANGE_PIN 4
#define BRIGHTNESS_LEVEL_PIN A7
int z=0;
uint8_t lastbrightness = 5;
CRGB leds[NUM_LEDS];

//#define UseMega2560multipleSerial // rx1(pin19) in mega2560 <=> cc2640R2F serial Tx. Serial0 is left available for ino image download and debug

void setup(){
#ifdef UseMega2560multipleSerial
    #define ctrlSerialPort Serial1
    #define debugSerialPort Serial
    debugSerialPort.begin(115200);
#else
    #define ctrlSerialPort Serial
#endif
    ctrlSerialPort.begin(115200);
    pinMode(LED_BUILTIN, OUTPUT);
    digitalWrite(LED_BUILTIN, LOW);
    pinMode(BRIGHTNESS_LEVEL_PIN, INPUT);
    pinMode(ICON_CHANGE_PIN, INPUT);    // set pin to input
    digitalWrite(ICON_CHANGE_PIN, HIGH);// turn on pullup resistors
    FastLED.addLeds<WS2812B, LED_PIN, GRB>(leds, NUM_LEDS);
    FastLED.setBrightness(lastbrightness);
    FastLED.clear();
#ifdef UseMega2560multipleSerial
    debugSerialPort.println("Program start");
#endif
}
void loop() {
    int brightness = analogRead(BRIGHTNESS_LEVEL_PIN)/8+10; // 0-1023=>0-128, which is 50% of the range of available brightness settings. Save the power bank!
    // 115200 baud, 115200/10=> 11520 byte/sec => ~87us => ~1389 instructions
    while(ctrlSerialPort.available() > 0) {
        uint8_t serialRdByte = ctrlSerialPort.read();
        //debugSerialPort.print((char)serialRdByte);
        VarLenProtocolParserResult res = onProtocolByte(serialRdByte);
        switch (res)
        {
            case SVTELEMPARSER_ERROR:
            {
#ifdef UseMega2560multipleSerial
                debugSerialPort.println("NACK");
#endif
            }
            break;
            case SVTELEMPARSER_PAYLOADLENVIOLATION:
            {
#ifdef UseMega2560multipleSerial
                debugSerialPort.println("bad PAYLOAD len");
#endif                
            }
            break;
            case SVTELEMPARSER_INCOMPLETE:
            {
                //digitalWrite(LED_BUILTIN, LOW);
                // do nothing
            }
            break;
            case SVTELEMPARSER_COMPLETE:
            {
#ifdef UseMega2560multipleSerial
                //debugSerialPort.println(millis());
                //digitalWrite(LED_BUILTIN, HIGH);
#endif                    
                uint16_t totalBytes = m_sParserStatus.ByteExpected;
                if (m_pOneMsgBuf[0] == 0 &&
                    m_pOneMsgBuf[1] == 0 &&
                    m_pOneMsgBuf[2] == 0 &&
                    m_pOneMsgBuf[3] == 0 &&
                    m_pOneMsgBuf[4] == 0){
                    FastLED.show();
                    digitalWrite(LED_BUILTIN, HIGH);
                }
                else{
                    uint8_t colorR = m_pOneMsgBuf[0];
                    uint8_t colorG = m_pOneMsgBuf[1];
                    uint8_t colorB = m_pOneMsgBuf[2];
                    for(int ii = 3; ii < totalBytes; ii++){
                        leds[m_pOneMsgBuf[ii]] = CRGB(colorR,colorG,colorB);
#ifdef UseMega2560multipleSerial
                        //char buf[20];
                        //sprintf(buf,"%d:%d,%d,%d",m_pOneMsgBuf[ii],colorR,colorG,colorB);
                        //debugSerialPort.println(buf);
#endif                
                    }
                }
            }
            break;
        }
    }
    if (abs(lastbrightness - brightness)>10)
    {
        FastLED.setBrightness(brightness);
        lastbrightness = brightness;
    }
}
