#include <Arduino.h>
#include <Stepper.h>

const int STEPS_PER_REV = 2048;
const int pin1 = 8;
const int pin2 = 9;
const int pin3 = 10;
const int pin4 = 11;

Stepper stepper(STEPS_PER_REV, pin1, pin3, pin2, pin4);

const char* VERSION = "ARTIVACT_TT_V2";

void setup() {
  Serial.begin(115200);
  stepper.setSpeed(15);
}

void loop() {
  if (!Serial.available()) return;

  String cmd = Serial.readStringUntil('\n');
  cmd.trim();

  // Command: get Version
  if (cmd == "VERSION") {
    Serial.println(VERSION);
    return;
  }

  // Command: MOVE <value>
  // Example: "MOVE 1" -> 360°, "MOVE 36" -> 10°
  if (cmd.startsWith("MOVE")) {
    int spaceIndex = cmd.indexOf(' ');
    if (spaceIndex < 0) {
      Serial.println("ERR");
      return;
    }

    int val = cmd.substring(spaceIndex + 1).toInt();
    long steps = (2048 * 6) / val; // 28BYJ-48 with ULN2003 + cog

    stepper.step(steps);
    Serial.println("OK");
    return;
  }
}