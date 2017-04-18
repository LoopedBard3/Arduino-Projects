#include "FastLED.h"
FASTLED_USING_NAMESPACE
#if FASTLED_VERSION < 3001000
#error "Requires FastLED 3.1 or later; check github for latest code."
#endif

#define DATA_PIN1   8                                                                                                                                            
#define LED_TYPE    WS2811
#define COLOR_ORDER GRB
#define NUM_LEDS1    254
CRGB leds[NUM_LEDS1];
int ledsOn = 1;
int FPS = 300;
float vol = 0.0;
void setup() {
  delay(3000); 
  Serial.begin(115200);
  Serial.setTimeout(5000);
  FastLED.addLeds<LED_TYPE, DATA_PIN1, COLOR_ORDER>(leds, NUM_LEDS1).setCorrection(TypicalLEDStrip);
  Serial.println("Connection is found (Arduino)");
}

void loop() {
  if (Serial.available() > 0){
    ledsOn = (int) (Serial.parseInt());
    Serial.println(ledsOn);
    Serial.flush();
  }
  
  fadeToBlackBy(leds, NUM_LEDS1, 30);
  fill_solid(leds, ledsOn, CRGB::Blue);
  //FastLED.delay(1000/FPS);
  FastLED.show();
}
