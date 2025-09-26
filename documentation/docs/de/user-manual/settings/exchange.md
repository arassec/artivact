# Exchange-Konfiguration <Badge type="warning" text="desktop"/>

## Synchronisation

Zwei Konfigurationsoptionen müssen gesetzt werden, um Sammlungsobjekte von einer lokalen Installation auf eine entfernte
Instanz
von
Artivact hochzuladen:

- Remote-Artivact-Server
    - HTTP(S)-URL der entfernten Artivact-Instanz, z.B. ``https://www.beispiel.com/``
- API-Token
    - Ein Sicherheitstoken (String), das in den Kontoeinstellungen der entfernten Instanz konfiguriert wurde. Dieses
      Benutzerkonto
      wird für den Upload verwendet.

## Content-Exporte

Content-Exporte von Teilen oder sogar der gesamten Sammlung können hier verwaltet werden.

Basis für den Content-Export ist immer ein Menü. Die zugehörige Seite und alle darauf angezeigten Sammlungsobjekte
werden im Export enthalten sein.

Zusätzlich bietet die Export-Konfiguration folgende Optionen:

| Option                       | Beschreibung                                                                                                                                                                    |
|:-----------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------| 
| Einschränkungen              | Content-Exporte können auf Administratoren und/oder Benutzer beschränkt werden.                                                                                                 | 
| Titel                        | Ein kurzer Titel des Exports, der im Export selbst enthalten sein wird.                                                                                                         | 
| Kurzbeschreibung             | Eine kurze Zusammenfassung des Inhalts des Content-Exports.                                                                                                                     | 
| Inhalt                       | Eine längere, detailliertere Beschreibung des Inhalts des Exports.                                                                                                              | 
| Exportgröße optimieren       | Optimierte Exporte enthalten, falls verfügbar, ein einziges 3D-Modell für jedes Sammlungsobjekt oder als Fallback ein einzelnes Bild des Objekts, anstelle aller Mediendateien. | 
| Einschränkungen ausschließen | Schließt alle eingeschränkten Sammlungsobjekte, Widgets oder Items vom Export aus.                                                                                              | 

## Content-Importe

Früher erstellte Content-Exporte können in Artivact importiert werden. Für den Content-Import stehen zwei Optionen zur
Verfügung:

- Vollständiger Import: Der Content wird einschließlich aller enthaltenen Sammlungsobjekte, Seiten und Menüs importiert.
- Import zur Verteilung: Nur die Content-Export-Datei wird für die weitere Verteilung importiert, ohne die enthaltenen
  Sammlungsobjekte, Seiten und Menüs zu importieren.
