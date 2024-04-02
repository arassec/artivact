# Bilder Freistellen

## Warum den Bildhintergrund Entfernen?

Um vollständige, aus jeder Perspektive korrekte 3D-Modelle erstellen zu können, müssen alle Bereiche des Objekts durch
Fotos abgedeckt sein.
Insbesondere von der Unterseite des Objekts müssen ausreichend Fotos vorhanden sein.
Falls diese nicht vorliegen, werden die 3D-Modelle mit Artefakten oder "Löchern" generiert:

![model with hole](/assets/desktop/models/background-removal_hole.png)

Da Fotoaufnahmen von der Unterseite schwierig und mühsam zu erstellen sind, kann das Objekt mit der Unterseite nach
oben fotografiert, und der Bildhintergrund aus den Fotoaufnahmen entfernt werden. 

![image with and without background](/assets/desktop/models/background-removal.png)

Durch das Freistellen der Bilder behandelt die Fotogrammetriesoftware diese, als hätte die Kamera das Objekt von der
Unterseite fotografiert.

![tricked photogrammetry software](/assets/desktop/models/background-removal_tricked.jpg)

Artivact kann die automatisch erstellten Aufnahmen direkt freistellen, und das Ergebnis zur Modellerstellung bereitstellen.

::: warning Nachteile
Der Nachteil der Manipulation der Bilder ist, dass Metainformationen der Kamera aus den freigestellten Bildern entfernt
werden. Dies kann dazu führen, dass einige Fotogrammetrie-Anwendungen diese langsamer oder ggf. gar nicht mehr
verarbeiten können. Meshroom und Metashape haben diesbezüglich bisher keine Probleme gezeigt.
:::

## rembg

::: tip Unterstütze Version
Die rembg-Integration wurde mit der Version 2.0 von rembg getestet.
:::

Artivact kann für das automatische Freistellen das Open-Source-Tool [rembg](https://github.com/danielgatis/rembg) von Daniel Gatis verwenden.

Die Software muss mit dem bereitgestellten Docker-Image als Web-Endpunkt installiert werden:

```
docker run -d -p 7000:7000 --name=rembg --restart=always danielgatis/rembg s
```

Die offizielle [Docker-Dokumentation](https://docs.docker.com/manuals/) bietet weitere Informationen, wie das rembg-Docker-Image auf verschiedenen
Systemen installiert werden kann.

Nach der Installation kann das Tool in Artivact konfiguriert werden:

``Einstellungen`` -> ``Peripheriegeräte`` -> ``Automatisches Freistellen``

|     Selection      |                Configuration Value                 |               Example                |
|:------------------:|:--------------------------------------------------:|:------------------------------------:|
| ``rembg (Remote)`` | Der Web-API-Endpunkt des Docker-Images. | ``http://localhost:7000/api/remove`` |
