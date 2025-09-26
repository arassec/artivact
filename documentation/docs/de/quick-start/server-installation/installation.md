# Installation von Artivact als Webserver

## Installation

Artivact ist in Java geschrieben und nutzt das beliebte Open-Source-[Spring Framework](https://spring.io/).  
Die Anwendung wird als eigenständige JAR-Datei bereitgestellt.

Die neueste Version kann von der [GitHub-Seite des Projekts](https://github.com/arassec/artivact/releases/latest)
heruntergeladen werden.

Die herunterzuladende Datei enthält „server“ im Namen, z. B.
``artivact-server-v##VERSION##.jar``.

## Start

Um den Artivact-Server zu starten, muss eine Java JRE oder JDK installiert sein.

Die Anwendung wird dann einfach über die Kommandozeile gestartet mit:

```
$> java -jar artivact-server-v##VERSION##.jar
```

::: info
Das Verzeichnis, das die JAR-Datei enthält, wird standardmäßig als ``.avdata`` Projektverzeichnis verwendet, sofern
nicht anders konfiguriert. Siehe [Konfiguration](configuration) für Details.
:::

Beim ersten Start wird das initiale Administratorkonto ``admin`` erstellt.  
Das Passwort kann in der Logdatei der Anwendung gefunden werden, z. B.:

```
##############################################################
Initial user created: admin / ebcfd5c6
##############################################################
```

## Linux-Systemdienst

Wenn Artivact auf einem Linux-Server betrieben werden soll, kann ein systemd-Skript verwendet werden,  
um die Anwendung als Systemdienst zu verwalten.

Zuerst einen neuen Benutzer erstellen, der die Anwendung ausführt.  
Dann ein Verzeichnis erstellen und die JAR-Datei dort ablegen:

```
$> sudo useradd artivact
$> sudo mkdir /opt/artivact-server
$> sudo mv artivact-server-v##VERSION##.jar /opt/artivact-server/
$> sudo chown -R artivact /opt/artivact-server/
```

Anschließend die folgende Datei als Root anlegen: ``/etc/systemd/system/artivact-server.service``

```
[Unit]
Description=Artivact Server
After=syslog.target

[Service]
User=artivact
ExecStart=/opt/artivact-server/artivact-server-v##VERSION##.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

Danach können systemd-Befehle verwendet werden, um den Dienst zu steuern, z. B.:

- Anwendung starten: ``$> sudo systemctl start artivact-server``
- Anwendung stoppen: ``$> sudo systemctl stop artivact-server``
- Autostart aktivieren: ``$> sudo systemctl enable artivact-server``
