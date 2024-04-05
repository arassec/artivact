# Artivact-Drehteller

## Automatische Rotation von Ausstellungsstücken

Um die Objekterzeugung druch Artivact zu beschleunigen, und da die Kamera bereits automatisch gesteuert werden kann,
lässt sich auch dei Objektrotation automatisieren.

Basierend auf anderen "Do-it-Yourself"-3D-Scannern im Internet, bietet Artivact einen steuerbaren Drehteller zur
Automatisierung.

Die Einzelteile müssen per 3D-Drucker ausgedruckt, sonstige Teile zusammengebaut und programmiert werden.
Anschließend kann der Drehteller per USB an den Computer angeschlossen werden.

## Notwendige Komponenten

Die folgenden Komponenten werden für den Drehteller benötigt:

| Komponente | Beschreibung | Downlad/Shop |
| :---: | :---: | :---: |
| Drehteller Einzelteile | Die Einzelteile des Drehtellers, die per 3D-Drucker gedruckt werden müssen. | [Github](https://github.com/arassec/artivact/blob/main/artivact-tt-v1/artivact-tt-v1.zip) |
| Schrittmotor 28BYJ-48 + Treiberplatine ULN2003 | Schrittmotor um den Drehteller anzutreiben. | [Amazon](https://www.amazon.com/-/de/dp/B09QQLMYWP/) |
| Druckkugellager | 57x57mm Druckkugellager 360 Grad. | [Amazon](https://www.amazon.com/-/de/dp/B0BPYR1S1Y/) |
| Arduino Nano Every | Arduino zur Steuerung des Motors. | [Arduiono Store](https://store.arduino.cc/products/arduino-nano-every) |

::: tip Arduino Sketch
Der Arduino Nano muss mit folgendem Sketch programmiert werden: [sketch_artivact-tt-v1.ino](https://github.com/arassec/artivact/blob/main/artivact-tt-v1/sketch_artivact-tt-v1/sketch_artivact-tt-v1.ino).
Die offizielle Arduino Dokumentation erläutert das notwendige Vorgehen: [Arduino Documentation](https://docs.arduino.cc/learn/starting-guide/getting-started-arduino/).
:::

## Zusammenbau

Die 3D-Teile des Drehtellers müssen ausgedruckt und zusammengesteckt werden.

Den Schrittmotor hinzufügen und mit der Treiberplatine verbinden:

![stepper motor driver wiring](/assets/tutorials/artivact-as-scanner/turntable-driver-wiring.jpg)

Die Treiberplatine mit dem Arduino Nano verbinden und einsetzen:

![driver board arduino wiring](/assets/tutorials/artivact-as-scanner/turntable-arduino-wiring.jpg)

Magnete an der Oberplatte des Drehtellers und dem oberen Zahnrad anbringen und die übrigen Teile zusammensetzen:

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-zero.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-one.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-two.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-three.jpg)

![assemble top plate](/assets/tutorials/artivact-as-scanner/turntable-assembly-final.jpg)

## Konfiguration

::: warning Linux Benutzer
Linux Benutzer müssen in der *dialout* Gruppe sein, um den Artivact-Drehteller benutzen zu können!
:::

Nach dem Zusammenbau kann der Drehteller per USB an den Computer angeschlossen werden und in Artivact aktiviert werden:

``Einstellugen`` -> ``Peripheriegeräte`` -> ``Drehteller-Konfiguration`` -> ``Artivact Turntable``
