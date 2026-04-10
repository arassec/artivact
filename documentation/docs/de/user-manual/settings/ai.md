# KI-Konfiguration

## Konfiguration

Die KI-Konfigurationsseite ermöglicht die Einrichtung der Integration künstlicher Intelligenz in Artivact für
Funktionen wie automatische Übersetzung und Text-to-Speech (TTS) Audiogenerierung.

Die Seite ist über das Systemeinstellungsmenü erreichbar und kann nur von Administratoren aufgerufen werden.

## Einstellungen

Die folgenden Konfigurationsmöglichkeiten stehen zur Verfügung:

| Einstellung         | Beschreibung                                                                                                                                                                                                           |
|:--------------------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| KI aktivieren       | Aktiviert oder deaktiviert alle KI-Funktionen in der Anwendung.                                                                                                                                                       |
| API-Schlüssel       | Der API-Schlüssel zur Authentifizierung beim KI-Dienst (z.B. OpenAI).                                                                                                                                                 |
| Allgemeiner Kontext | Ein System-Prompt, der der KI Kontext liefert. Der Standardwert weist die KI an, als Museumskurator zu agieren, der sachliche und wissenschaftliche Texte für ein breites Publikum verfasst.                           |
| Übersetzungs-Prompt | Ein Prompt-Template für automatische Übersetzungen. Der Platzhalter ``{locale}`` wird durch die Zielsprache ersetzt. Siehe [I18N](../content-management/internationalization) für weitere Details zu Übersetzungen.    |
| TTS-Prompt          | Ein Prompt-Template für die Text-to-Speech-Generierung. Der Platzhalter ``{locale}`` wird durch die Zielsprache ersetzt.                                                                                              |
| TTS-Stimme          | Die Stimmen-ID, die für die Text-to-Speech-Audiogenerierung verwendet wird (z.B. ``alloy``).                                                                                                                          |

::: warning API-Schlüssel
Der API-Schlüssel ist für alle KI-Funktionen erforderlich. Ohne einen gültigen API-Schlüssel stehen automatische
Übersetzung und Audiogenerierung nicht zur Verfügung.
:::

## Automatische Übersetzung

Wenn die KI aktiviert und konfiguriert ist, kann Artivact Texte von Sammlungsobjekten und Seiteninhalte automatisch in
die konfigurierten Sprachen übersetzen.

Die Übersetzung basiert auf dem **Übersetzungs-Prompt** und dem **Allgemeinen Kontext**. Das Prompt-Template kann
angepasst werden, um zu beeinflussen, wie die KI Übersetzungen durchführt.

Siehe [I18N](../content-management/internationalization) für Details zur Verwaltung mehrsprachiger Inhalte.

## Audiogenerierung (TTS)

Wenn die KI aktiviert und konfiguriert ist, kann Artivact Audioinhalte aus Text mittels Text-to-Speech generieren.

Die Audiogenerierung basiert auf dem **TTS-Prompt**, der **TTS-Stimme** und dem **Allgemeinen Kontext**. Die erzeugten
Audiodateien können z.B. in Content-Exporten verwendet werden.

Die verfügbaren Stimmen hängen vom konfigurierten KI-Anbieter ab.
