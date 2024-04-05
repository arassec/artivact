# Artivact Turntable

## Automatic Object Rotation

In order to speedup item creation, and since the camera can be automatically controlled, the object can be automatically
rotated by Artivact as well.

Based on other do-it-yourself 3D scanner solutions in the web, Artivact provides a automatic 3D turntable.

The parts have to be 3D printed and assembled by yourself. 
It can afterward be used to rotate the object automatically between image capturing.

## Required Parts

The following parts are required to assemble the turntable.

| Part | Description | Downlad/Example |
| :---: | :---: | :---: |
| Turntable Parts | The parts of the turntable that have to be 3D printed and assembled. | [Github](https://github.com/arassec/artivact/blob/main/artivact-tt-v1/artivact-tt-v1.zip) |
| Stepper Motor 28BYJ-48 + Driver ULN2003 | The stepper motor to rotate the turntable. | [Amazon](https://www.amazon.com/-/de/dp/B09QQLMYWP/) |
| Rotary Bearing Swivel Plate 360° Degree | 4 Inch Square Swivel to support heavy objects. | [Amazon](https://www.amazon.com/-/de/dp/B0BPYR1S1Y/) |
| Arduino Nano Every | Used to control the stepper motor from PC via USB. | [Arduiono Store](https://store.arduino.cc/products/arduino-nano-every) |

::: tip Arduino Sketch
The Arduino Nano needs to have the following sketch installed: [sketch_artivact-tt-v1.ino](https://github.com/arassec/artivact/blob/main/artivact-tt-v1/sketch_artivact-tt-v1/sketch_artivact-tt-v1.ino).
Please refer to the [Arduino Documentation](https://docs.arduino.cc/learn/starting-guide/getting-started-arduino/) for how to do this.
:::

## Assembly

Print out the 3D parts and assemble the base.

Add the stepper motor and wire it to the driver Board:

![stepper motor driver wiring](/assets/tutorials/artivact-as-scanner/turntable-driver-wiring.jpg)

Wire the driver board to the Arduino Nano:

![driver board arduino wiring](/assets/tutorials/artivact-as-scanner/turntable-arduino-wiring.jpg)

Attach magnets to the top plate and assemble all remaining parts:

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-zero.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-one.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-two.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-three.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-final.jpg)

## Configuration

::: warning Linux Users
Linux users have to be in the *dialout* group in order to use the Artivact turntable!
:::

After assembly, you can connect the turntable to your PC via USB and configure it in Artivact:

``Settings`` -> ``Peripherals`` -> ``Turntable Configuration`` -> ``Artivact Turntable``
