# Installation als Web-Server

## Installation

Artivact ist eine Java-Anwendung, die das Open-Source Framework [Spring](https://spring.io/) verwendet.
Als solche wird sie als self-contained JAR-Datei ausgeliefert.

Die aktuellste Version kann von der Github-Projektseite geladen
werden: [Artivact Releases](https://github.com/arassec/artivact/releases/latest).

Die relevante Datei enthält 'server' im Dateinamen, z.B. ``artivact-server-v0.0.0.jar``

## Start

Ein Java JRE oder JDK muss bereits auf dem System installiert sein um die Anwendung zu starten.

Zum start kann dann einfach der folgende Befehl auf der Kommandozeile ausgeführt werden:

```
$> java -jar artivact-server-v0.0.0.jar
```

::: info
In dem Verzeichnis, welches die JAR-Datei einhält, wird das Standard-Projektverzeichnis ``.avdata`` angelegt, falls
nicht anders konfiguriert.
:::

Während des ersten Starts der Anwendung wird das initialize Administratorkonto ``admin`` angelegt.
Das Passwort kann in der Logdatei der Anwendung gefunden werden. 
Dies sieht z.B. folgendermaßen aus:

```
##############################################################
Initial user created: admin / ebcfd5c6
##############################################################
```

## Linux System Daemon

Soll Artivact auf einem Linux-Server betrieben werden, bietet es sich an die Anwendung als systemd-Dienst zu betreiben.

Dafür muss zunächst ein Nutzer, unter dem die Anwendung laufen soll, erzeugt werden.
Danach kann ein Verzeichnis für die Anwendung erstellt und die JAR-Datei dorthin kopiert werden:

```
$> sudo useradd artivact
$> sudo mkdir /opt/artivact-server
$> sudo mv artivact-server-v0.0.0.jar /opt/artivact-server/
$> sudo chown -R artivact /opt/artivact-server/
```

Als Nächstes muss die folgende Datei im System abgelegt werden: ``/etc/systemd/system/artivact-server.service``

```
[Unit]
Description=Artivact Server
After=syslog.target

[Service]
User=artivact
ExecStart=/opt/artivact-server/artivact-server-v0.0.0.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

Jetzt kann Artivact mit systemd-Kommandos verwaltet werden, z.B. mit diesen:

- Start der Anwendung: ``$> sudo systemctl start artivact-server``
- Stop der Anwendung: ``$> sudo systemctl stop artivact-server``
- Bootsicher machen: ``$> sudo systemctl enable artivact-server``
