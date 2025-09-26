# Konfiguration der Server-Anwendung

## Allgemein

Artivact ist in Java geschrieben und nutzt das beliebte Open-Source-[Spring Framework](https://spring.io/).

Daher kann es wie jede andere Spring-Boot-Anwendung konfiguriert werden – sei es über JVM-Parameter, eine
`application.properties`-Datei im Stammverzeichnis oder jede andere von Spring unterstützte Methode.

## Das Projektverzeichnis

Das Stammverzeichnis des Projekts kann mit folgendem Parameter konfiguriert werden:

::: code-group

```[Command line parameter]
$> java -jar artivact-server-v##VERSION##.jar \
        -Dartivact.project.root=/opt/artivact-server/project-root
```

```[application.properties]
artivact.project.root=/opt/artivact-server/project-root
```
:::

## Datenbank-Konfiguration

Artivact unterstützt H2 und PostgreSQL Datenbanken.
Standardmäßig wird eine eingebettete H2-Datenbank verwendet, die im Verzeichnis ``dbdata`` im Projektordner gespeichert
wird.
Du kannst die zu verwendende Datenbank über die standardmäßigen Spring-Mechanismen konfigurieren.

::: code-group

```[Command line parameter]
$> java -jar artivact-server-v##VERSION##.jar \
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

Folgendes (unter anderem) kann direkt in der Anwendung konfiguriert werden:

- Administrator- und Benutzerkonten.
- Erscheinungsbild der Anwendung wie Farbthema, Titel, Favicons etc.
- Unterstützte Sprachen für die Internationalisierung (I18N).
- Eigenschaften von Objekten, Tags und Lizenzinformationen für Mediendateien.
- 