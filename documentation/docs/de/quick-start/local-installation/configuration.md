# Konfiguration der Anwendung

## Das Projektverzeichnis

Artivact speichert alle Projektdateien im Dateisystem.
Das Standardprojektverzeichnis ist ```.avdata``` im Home-Verzeichnis des Nutzers.

Um dies zu ändern, kann die Anwendung mit folgendem Parameter gestartet werden (Beispiel für Windows-Systeme):

``
Artivact.exe --artivact.project.root=C:\Artivact-Projects\sample-project
``

## Anwendungen von Drittanbietern

Alle Anwendungen von Drittanbietern, die innerhalb von Artivact verwendet werden sollen, müssen bereits auf dem System
installiert worden sein.

Sie können dann in der Anwendung konfiguriert werden, über ``Settings`` -> ``Peripherals``.