# Konfiguration der Anwendung

## Allgemein

Artivact ist eine Java-Anwendung, die das Open-Source Framework [Spring](https://spring.io/) verwendet.

Daher kann die Anwendung wie jede andere Spring-Boot-Anwendung auch konfiguriert werden, z.B. mittels JVM-Parametern
oder einer application.properties Datei im Dateisystem.

## Das Projektverzeichnis

Das Projektverzeichnis, das alle Dateien der Anwendung enthält, kann über folgenden Parameter konfiguriert werden:

::: code-group

```[Kommandozeilenparameter]
$> java -jar artivact-server-v0.0.0.jar \
        -Dartivact.project.root=/opt/artivact-server/project-root
```

```[application.properties]
artivact.project.root=/opt/artivact-server/project-root
```

:::

Das Standardprojektverzeichnis ``.avdata`` wird, ohne Konfigurationsänderung, im Ausführungsverzeichnis der Anwendung
erstellt.

## Datenbankkonfiguration

Artivact unterstützt *H2* und *PostgreSQL* Datenbanken.
Die Anwendung verwendet eine H2 Datenbank im Dateisystem als Standard. 
Diese wird im Ordner ``dbdata`` im Projektverzeichnis beim ersten Anwendungsstart erstellt.
Für größere Installation und den produktiven Betrieb wird PostgreSQL empfohlen.

Die Datenbank kann über die Standard-Spring-Mechanismen konfiguriert werden:

::: code-group

```[Kommandozeilenparameter]
$> java -jar artivact-server-v0.0.0.jar \
        -Dspring.datasource.url=jdbc:postgresql://localhost:5432/postgres \
        -Dspring.datasource.username=artivact \
        -Dspring.datasource.password=artivact \
        -Dspring.datasource.driver-class-name=org.postgresql.Driver \
```

```[application.properties]
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=artivact
spring.datasource.password=artivact
spring.datasource.driver-class-name=org.postgresql.Driver
```

:::

## Weitere Anpassungen

Die folgenden Anpassungen können innerhalb der Anwendung per Weboberfläche vorgenommen werden:

- Administrator- und Benutzerkonten anlegen und pflegen
- Das Erscheinungsbild der Anwendung wie das Farbschema, der Titel, Favicons etc.
- Unterstützte Sprachen für I18N.
- Eigenschaften, Tags sowie Lizenzangaben für Mediendaten von Ausstellungsstücken