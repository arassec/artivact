# Bilder Freistellen

## Warum den Bildhintergrund Entfernen?

Um vollständige, aus jeder Perspektive korrekte 3D-Modelle erstellen zu können, müssen alle Bereiche des Objekts durch
Fotos abgedeckt sein.
Insbesondere von der Unterseite des Objekts müssen ausreichend Fotos vorhanden sein.
Falls diese nicht vorliegen, werden die 3D-Modelle mit Artefakten oder "Löchern" generiert:

![model with hole](/assets/tutorials/artivact-as-scanner/background-removal_hole.png)

Da Fotoaufnahmen von der Unterseite schwierig und mühsam zu erstellen sind, kann das Objekt mit der Unterseite nach
oben fotografiert, und der Bildhintergrund aus den Fotoaufnahmen entfernt werden. 

![image with and without background](/assets/tutorials/artivact-as-scanner/background-removal.png)

Durch das Freistellen der Bilder behandelt die Fotogrammetriesoftware diese, als hätte die Kamera das Objekt von der
Unterseite fotografiert.

![tricked photogrammetry software](/assets/tutorials/artivact-as-scanner/background-removal_tricked.jpg)

Artivact kann die automatisch erstellten Aufnahmen direkt freistellen, und das Ergebnis zur Modellerstellung bereitstellen.

::: warning Nachteile
Der Nachteil der Manipulation der Bilder ist, dass Metainformationen der Kamera aus den freigestellten Bildern entfernt
werden. Dies kann dazu führen, dass einige Fotogrammetrie-Anwendungen diese langsamer oder ggf. gar nicht mehr
verarbeiten können. Meshroom und Metashape haben diesbezüglich bisher keine Probleme gezeigt.
:::

Details zur Konfiguration sind im [Benutzerhandbuch](../../user-manual/settings/peripherals) beschrieben.