# Fotos Aufnehmen

## Fotoqualität

Bilder von Ausstellungsstücken für die Verwendung mit Fotogrammetrie aufzunehmen ist einfach, es gibt aber einige
Punkte, die beachtet werden sollten.

Gute Fotos für Fotogrammetrie erfüllen die folgenden Bedingungen:

- Gute, konstante Beleuchtung des kompletten Objekts: ![good](/assets/tutorials/artivact-as-scanner/image-capture_good.jpg)
- Harte Schatten sollten vermieden werden: ![hard shadows](/assets/tutorials/artivact-as-scanner/image-capture_bad_shadows.jpg)
- Eine geringe Schärfentiefe muss vermieden werden: ![low depth of sharpness](/assets/tutorials/artivact-as-scanner/image-capture_bad_sharpness.jpg)

Weiterhin sollten die Bilder eine hohe Überlappung haben, damit die Fotogrammetriesoftware diese richtig zuordnen kann.
Eine Rotation des fotografierten Objekts um 10° zwischen den Aufnahmen ist optimal.
D.h. als Minimum sollten 36 Bilder (360° / 10° Rotation) des Objekts erstellt werden.

## Automatische Fotoaufnahmen

Bilder von Objekten können manuell in Artivact importiert werden, wie im [Benutzerhandbuch](/de/user-manual/introduction/about) beschrieben.

Aber der wirkliche Vorteil bei der Nutzung von Artivact ist die Automation bei der Erstellung der Aufnahmen.

Hierfür muss an den Computer, auf dem Artivact läuft, eine Kamera per USB angeschlossen sein.
Weiterhin wird Software zur Steuerung der Kamera benötigt, die installiert sein muss.

Aktuell wird hierfür auf Windows-Systemen [DigiCamControl](https://digicamcontrol.com/) verwendet, unter Linux kann [gphoto2](http://gphoto.org/) genutzt werden.

Nach der Installation der Software kann sie konfiguriert werden:

``Einstellungen`` -> ``Peripheriegeräten`` -> ``Kamera-Konfiguration``

### DigiCamControl

::: tip Unterstütze Version
Die DigiCamControl-Integration wurde mit der Version 2.1 von DigiCamControl getestet.
:::

| Auswahl | Konfigurationswert | Beispiel |
| :---: | :---: | :---: |
| ``DigiCamControl`` | Die exe-Datei des Programms. | ``C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe``|
| ``DigiCamControl (Remote)`` | Der Web-Endpunkt der Anwendung, wenn das [web interface](https://digicamcontrol.com/doc/userguide/web) aktiviert ist.  | ``http://localhost:5513/``|

"DigiCamControl (Remote)" bietet eine bessere Geschwindigkeit bei der Fotoerstellung, muss allerdings durch den Nutzer 
in DigiCamControl aktiviert werden: [DigiCamControl Webserver Configuration](https://digicamcontrol.com/doc/userguide/settings#webserver)

### gphoto2

::: tip Unterstütze Version
Die gphoto2-Integration wurde mit Version 2.5 von gphoto2 getestet.
:::

| Auswahl | Konfigurationswert | Beispiel |
| :---: | :---: | :---: |
| ``gphoto2`` | Das Anwendungs-Binary. | ``/usr/bin/gphoto2``|
