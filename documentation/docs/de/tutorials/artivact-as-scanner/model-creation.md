# 3d-Modell Erzeugung

## Beispiel-Workflows

Artivact stellt Beispiel-Workflows für die unterstützten Fotogrammetrie-Anwendungen bereit.
Diese können verwendet werden, um schnell in die 3D-Modellerstellung einzusteigen.
Die Workflows limitieren die größe und Texturqualität des erstellten Modells, und exportieren dieses im
OBJ-Format in das Projektverzeichnis.

Die Beispiel-Workflows können angepasst und gespeichert werden, z.B. um die Texturqualität zu erhöhen.

::: warning Export-Einstellungen
Die Exportkonfiguration in den bereitgestellten Workflows darf *nicht* angepasst werden, da Artivact die erstellten
OBJ-Dateien an der vorkonfigurierten Stelle erwartet.
:::

Details zur Konfiguration sind im [Benutzerhandbuch](../../user-manual/settings/peripherals) beschrieben.

### Meshroom Pipeline

Wenn Meshroom aus Artivact heraus gestartet wird, öffnet sich direkt die Beispiel-Pipeline, die bereits Knoten für die
Mesh-Reduktion und den Export enthält.
Falls Warnungen über neuere Knoten-IDs angezeigt werden, können die Knoten über Meshrooms UI aktualisiert werden.

![Meshroom default pipeline](/assets/tutorials/artivact-as-scanner/model-creation-meshroom.png)

### Metashape Workflow

Agisoft Metashape's Standardedition erlaubt nicht, den Beispiel-Workflow direkt beim Programmstart zu öffnen.
Der Artivact Beispiel-Workflow wird bereitgestellt im Projektverzeichnis unter:

```
utils/Metashape/artivact-metashape-workflow.xml
```

Der Nutzer muss diesen über ``Workflow`` -> ``Batch Process`` von Hand öffnen.

![Metashape batch process](/assets/tutorials/artivact-as-scanner/model-creation-metashape-one.png)

Danach wird der folgende Workflow bei Klick auf ``OK`` durchgeführt:

![Metashape batch process](/assets/tutorials/artivact-as-scanner/model-creation-metashape-two.png)
