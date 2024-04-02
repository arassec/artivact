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

### Meshroom Pipeline

::: tip Unterstützte Version
Die Meshroom-Integration wurde mit Version 2023.3 von Meshroom getestet.
:::

Wenn Meshroom aus Artivact heraus gestartet wird, öffnet sich direkt die Beispiel-Pipeline, die bereits Knoten für die
Mesh-Reduktion und den Export enthält.
Falls Warnungen über neuere Knoten-IDs angezeigt werden, können die Knoten über Meshrooms UI aktualisiert werden.

![Meshroom default pipeline](/assets/create/models/model-creation-meshroom.png)

### Metashape Workflow

::: tip Unterstützte Version
Die Metashape-Integration wurde mit Version 2.1 von Metashape getestet.
:::

Agisoft Metashape's Standardedition erlaubt nicht, den Beispiel-Workflow direkt beim Programmstart zu öffnen.
Der Artivcat Beispiel-Workflow wird bereitgestellt im Projektverzeichnis unter:

```
utils/Metashape/artivact-metashape-workflow.xml
```

Der Nutzer muss diesen über ``Workflow`` -> ``Batch Process`` von Hand öffnen.

![Metashape batch process](/assets/create/models/model-creation-metashape-one.png)

Danach wird der folgende Workflow bei Klick auf ``OK`` durchgeführt:

![Metashape batch process](/assets/create/models/model-creation-metashape-two.png)
