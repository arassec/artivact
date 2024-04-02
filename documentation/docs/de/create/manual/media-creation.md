# Media Creation

## Overview

In der Desktop-Variante der Anwendung ist im Bearbeitungsmodus eines Ausstellsungsstückes der zusätzliche
Menupunkt ``Gestaltung`` verfügbar.

![media creation menu](/assets/create/manual/de/media-creation-menu.png)

Er öffnet die Übersicht zur Medien-Gestaltung:

![media creation overview](/assets/create/manual/de/media-creation-overview.png)

Die Kernfunktionen, um Artivact als 3D-Scanner zu verwenden, können über folgende Buttons aufgerufen werden:

- Kamera
    - Der Kamera-Knopf öffnet den Dialog, um die Aufnahme von Serienfotos für die 3D-Modell-Erstellung zu öffnen.
      Aufgenommene Fotos werden als Bilder-Sets in der dieser Übersicht angezeigt.
- Schere
    - Der Schere-Knopf eines Bilder-Sets startet das automatische Freistellen aller enthaltenen Bilder.
- Plus
    - Der Plus-Knopf unter ``3D-Modelle`` öffnet die konfigurierte Fotogrammetrie-Software und ein Dateiexplorer-Fenster
      mit den relevanten Bildern.
    - Bilder werden über den Knopf ``Model Input`` als relevant für die Modellerstellung markiert.
- Stift
    - Sobald ein 3D-Modell im OBJ-Format vorliegt, kann übe den Stift-Knopf der konfigurierte 3D-Editor gestartet werden.

## Aufnahmeparameter

Wenn der Kamera-Knopf gedrückt wird öffnet sich ein Dialog zur Konfiguration der Aufnahmeparameter:

![media creation capture params](/assets/create/manual/de/media-creation-image-capture-params.png)

Hier kann die Anzahl der aufzunehmenden Fotos sowie die Nutzung eines Drehtellers konfiguriert werden.
Die Zeitverzögerung bei der Verwendung des Drehtellers wird in Millisekunden angegeben.
Nach der Rotation des automatischen Drehtellers wird erst nach dieser Verzögerung das nächste Foto erstellt.

::: tip Manuelle Drehteller
Falls kein automatischer Drehteller verwendet werden soll, kann die konfigurierte Verzögerung genutzt werden, um einen
manuellen Drehteller von Hand zu rotieren.

Eine Konfiguration von 5000 z.B. sorgt für eine verzögerte Fotoaufnahme von 5 Sekunden, bevor das nächste Foto erstellt wird.
:::

Das automatische Freistellen kann ebenfalls angehakt werden, um schon während er Aufnahmen die Bildhintergründe der
erstellten Fotos automatisch zu entfernen, und so den Gesamtprozess weiter zu beschleunigen.

## Medienübertragung

Nachdem ein 3D-Modell erstellt und als GLTF/GLB exportiert wurde, kann es über die Model-Set-Details für das virtuelle
Ausstellungsstück ausgewählt werden:

![media creation model set details](/assets/create/manual/de/media-creation-model-set-details.png)

Ein Klick auf den Übertragen-Button kopiert das ausgewählte 3D-Modell in die ``Media`` Übersicht des Objekts.

::: tip Bilderübertragung
Derselbe Button kann in den Bilder-Set-Details verwendet werden, um aus den Aufnahmen geeignete Bilder für die Medienübersicht des Objekts auszuwählen.
Dies kann verwendet werden, um schnell ein Objektbild für die Übersicht zu erzeugen, bevor später Detailreichere "Produktbilder" erstellt weden.
:::
