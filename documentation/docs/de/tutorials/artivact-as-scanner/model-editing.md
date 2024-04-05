# 3D-Modell Bearbeitung

## Warum das Modell Bearbeiten?

Fotogrammetriesoftware erstellt nicht immer akkurate 3D-Modell aus den aufgenommenen Fotos.
Es können Artefakte in das Ergebnis generiert werden, was nicht wünschenswert ist.

![Model with artifact](/assets/tutorials/artivact-as-scanner/model-creation-artifact.png)

In diesen Fällen kann es nützlich sein, das 3D-Modell von Hand mittels eines 3D-Editor zu bearbeiten.

Aktuell bietet Artivact eine Integration des freien und Open-Source-Tools Blender 3D für diese Aufgabe.

## Blender 3D

::: tip Unterstützte Version
Die Blender-Integration wurde für die Versionen 3 und 4 von Blender 3D getestet.
:::

Zur Bearbeitung des 3D-Modells kann Blender direkt aus Artivact geöffnet werden, bei Klick auf den Stift-Button des
entsprechenden Model-Sets.
Dadurch werden die OBJ-Dateien des zuvor erzeugten 3D-Modells automatisch in die Szene importiert.

Das Finale 3D-Modell sollte als GLTF/GLB-Datei in das von Blender vorgeschlagene Exportverzeichnis exportiert werden.

Diese kann nun über die Detailansicht des Model-Sets in den Medienbereich des Ausstellungsstückes übertragen und
veröffentlicht werden.
