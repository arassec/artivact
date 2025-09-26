# Konfiguration der lokalen Anwendung

## Das Projektverzeichnis

Artivact speichert alle Projektdaten im Dateisystem.  
Das Standard-Projektverzeichnis ist das Verzeichnis ```.avdata``` im Home-Verzeichnis des Benutzers.

Um das Projektverzeichnis zu ändern, kann die Anwendung mit einem zusätzlichen Parameter gestartet werden.

Beispiel für Windows:

```
Artivact.exe --artivact.project.root=C:\Artivact-Projects\sample-project
```

Beispiel für Linux:

```
./Artivact --artivact.project.root=/opt/Artivact-Projects/sample-project
```

## Drittanbieter-Software

Alle verwendeten Drittanbieter-Anwendungen werden innerhalb der laufenden Anwendung konfiguriert.  
Um die Werkzeuge nutzen zu können, müssen diese bereits installiert sein.

Sie können dann über ``Einstellungen`` -> ``Peripherie`` konfiguriert werden.
