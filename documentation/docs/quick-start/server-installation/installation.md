# Installing Artivact as Web Server

## Installation

Artivact is written in Java using the popular open source [Spring framework](https://spring.io/).
As such, the application is provided as self-contained JAR file.

The latest release can be downloaded from the
project's [GitHub page](https://github.com/arassec/artivact/releases/latest).

The file to download contains 'server' in its name, e.g. ``artivact-server-v0.0.0.jar``

## Startup

A Java JRE or JDK has to be installed in order to start the Artivact server.

The application is then started simply by calling

```
$> java -jar artivact-server-v0.0.0.jar
```

on the command line.

::: info
The directory containing the JAR file will be used to create the ``.avdata`` project home, unless configured otherwise.
:::

During first start the initial administrator account ``admin`` will be created. 
The password can be found in the application's log file, e.g.:

```
##############################################################
Initial user created: admin / ebcfd5c6
##############################################################
```

## Linux System Daemon

If you want to run Artivact on a linux server, a systemd script can be used to manage the application
as system service.

First, create a new user, which will be used to run the application.
Then create a directory and place the JAR in it:

```
$> sudo useradd artivact
$> sudo mkdir /opt/artivact-server
$> sudo mv artivact-server-v0.0.0.jar /opt/artivact-server/
$> sudo chown -R artivact /opt/artivact-server/
```

Next, add the following file as root: ``/etc/systemd/system/artivact-server.service``

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

You can now use systemd commands to configure the service, e.g.:

- Start the application: ``$> sudo systemctl start artivact-server``
- Stop the application: ``$> sudo systemctl stop artivact-server``
- Make it boot safe: ``$> sudo systemctl enable artivact-server``