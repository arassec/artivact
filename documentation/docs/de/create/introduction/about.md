# Artivact als Desktop-Anwendung

## Über Artivact

Das Kernstück von Artivact auf dem Desktop ist die Erstellung von 3D-Modellen für virtuelle Ausstellungsstücke.
Hierfür werden Anwendungen von Drittanbietern verwendet, z.B. Software für Fotogrammetrie oder zur Nutzung von
Digitalkameras am PC.

Der normale Ablauf zur Erzeugung von 3D-Modellen ist üblicherweise folgendermaßen:

- Es werden von Hand etliche Fotos des Ausstellungsstückes aus verschiedenen Perspektiven erzeugt.
- Die Bilder müssen von der Kamera auf den PC übertragen werden.
- Mithilfe von Fotogrammetrie-Software wird aus den Fotos ein 3D-Modell erstellt.
- Manchmal muss das Modell mit einem 3D-Editor nachbearbeitet werden.
- Das 3D-Modell muss im gewünschten Format exportiert werden.

Artivact ermöglicht die Konfiguration und "fließende" Nutzung der externen Programme sowie die Verwaltung der
erzeugten Dateien um den Gesamtprozess erheblich zu beschleunigen.

## Features

Um mittels Fotogrammetrie digitale Doppelgänger von Ausstellungsstücken zu erzeugen ist ein hoher manueller
notwendig, der Zeit kostet. Der Prozess führt häufig zu unzähligen Dateien und Verzeichnissen im Dateisystem des PCs,
wodurch man schnell den Überblick über den Arbeitsfortschritt verliert.

Die Desktop-Variante von Artivact adressiert dieses Problem mithilfe der folgenden Funktionen:

- Einfache und schnelle Erzeugung von 3D-Modellen durch die effiziente Kombination verschiedener Tools.
- Flexible Optionen zur Organisation und Verwaltung der erzeugten, digitalen Ausstellungsstücke.
- Export der erzeugten Stücke und Mediendateien in einem standard ZIP-Format.

## Anwendungen von Drittanbietern

Artivact selbst ist keine Fotogrammetrie-Software.
Es stellt eine Verwaltungsoberfläche für verschiedene Tools und deren Arbeitsergebnisse dar.
Der Nutzer hat weiterhin die volle Kontrolle über die Anwendungen, kann dieser aber effizienter nutzen und die
Ergebnisse leichter verwalten.

Die folgenden Anwendungen werden aktuell unterstützt.

### Foto Erzeugung & Bearbeitung

Zur möglichst automatischen Erzeugung von Fotos auf dem PC kann die folgende Software verwendet werden:

| Windows | Linux |
| :-------------: | :-----------: |
| [DigiCamControl](https://digicamcontrol.com/) ![DigiCamControl](/assets/logos/digicamcontrol-logo.png) | [gphoto2](http://gphoto.org/) ![gphoto2](/assets/logos/gphoto2-logo.png)|

Eine, von der jeweiligen Software unterstützte, Kamera muss an den PC angeschlossen sein.

Um ein vollständiges 3D-Modell zu erzeugen kann es notwendig sein, die erzeugten Fotos freizustellen, d.h. den
Bildhintergrund zu entfernen.
Um dies zu automatisieren kann das folgende Tool über Artivact verwendet werden:

| Windows & Linux |
| :-------------: |
| [Rembg](https://github.com/danielgatis/rembg) |

### 3D-Modell Erzeugung

Die folgenden Fotogrammetrie-Anwendungen werden von Artivact unterstützt.
Sie werden innerhalb der Anwendung gestartet, der Nutzer übernimmt dann die Steuerung und kehrt nach Abschluss der
Nutzung wieder zu Artivact zurück.

| Windows & Linux | Windows & Linux |
| :-------------: | :-----------: |
| [Meshroom](https://alicevision.org/#meshroom) ![Meshroom](/assets/logos/meshroom-logo.png) | [Metashape](https://www.agisoft.com/) ![Metashape](/assets/logos/metashape-logo.png)|

### 3D-Modell Bearbeitung

Um die per Fotogrammetrie erzeugten 3D-Modelle zu bearbeiten kann das folgende Tool verwendet werden:

| Windows & Linux |
| :-------------: |
| [Blender 3D](https://www.blender.org/) ![Blender 3D](/assets/logos/blender-logo.png) |
