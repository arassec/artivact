# Peripheriegeräte-Konfiguration <Badge type="warning" text="desktop"/>

## Konfiguration

Über die Peripheriegeräte-Konfiguration kann die Drittanbietersoftware konfiguriert werden, um aus Artivact einen
3D-Scanner zu machen.

Diese ist nur in der Desktop-Variante im Einstellungen-Menü verfügbar:

![peripherals menu](/assets/user-manual/settings/peripherals/en/peripherals-menu.png)

Die Seite bietet die Konfigurationsoptionen für die Drittanbietersoftware:

![peripherals overview](/assets/user-manual/settings/peripherals/en/peripherals-overview.png)

## Turntable

::: warning Linux Systems
Linux users have to be in the **dialout** group in order to use the Artivact turntable!
:::

After [assembly of the turntable](/tutorials/artivact-as-scanner/artivact-turntable), you can connect it to your PC via
USB and configure it in Artivact.

|       Selection        |   Configuration Value    |
|:----------------------:|:------------------------:| 
| ``Artivact Turntable`` | Artivact's DIY turntable | 

## Camera

For automatic image capturing, you need to attach your camera to your PC using USB and install Software to control it
remotely. Currently, [DigiCamControl](https://digicamcontrol.com/) for Windows systems and [gphoto2](http://gphoto.org/)
for Linux systems are supported.

### DigiCamControl

::: tip Supported Version
DigiCamControl integration has been tested for Version **2.1**.
:::

|          Selection          | Configuration Value                                                                                              |                            Example                             |
|:---------------------------|:-----------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------|
|     ``DigiCamControl``      | The application's executable                                                                                     | ``C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe`` |
| ``DigiCamControl (Remote)`` | The application's web endpoint, if the [web interface](https://digicamcontrol.com/doc/userguide/web) is enabled. |                   ``http://localhost:5513/``                   |

"DigiCamControl (Remote)" provides better performance, but needs configuration in DigiCamControl to work.
The embedded web server needs to be activated by the
user: [DigiCamControl Webserver Configuration](https://digicamcontrol.com/doc/userguide/settings#webserver)

### gphoto2

::: tip Supported Version
gphoto2 integration has been tested for Version **2.5**.
:::

|  Selection  | Configuration Value          |       Example        |
|:-----------|:-----------------------------|:--------------------|
| ``gphoto2`` | The application's executable | ``/usr/bin/gphoto2`` |

## Background Removal

Artivact can use the free and open-source tool [rembg](https://github.com/danielgatis/rembg) by Daniel Gatis to
automatically remove the backgrounds of images.

### rembg

::: tip Supported Version
rembg integration has been tested for Version **2.0**.
:::

The software has to be installed using the provided Docker image:

```
docker run -d -p 7000:7000 --name=rembg --restart=always danielgatis/rembg s
```

Please refer to the official [Docker documentation](https://docs.docker.com/manuals/) for further information on how to
use docker on your system.

After installation, it can be configured in Artivact.

|     Selection      | Configuration Value                                |               Example                |
|:------------------|:---------------------------------------------------|:------------------------------------|
| ``rembg (Remote)`` | The application's web endpoint, exposed by Docker. | ``http://localhost:7000/api/remove`` |

## Model Creator

Model creation with photogrammetry is supported by Artivact through external programs.

### Meshroom

::: tip Supported Version
Meshroom integration has been tested for Version **2023.3**.
:::

### Metashape

::: tip Supported Version
Metashape integration has been tested for Version **2.1**
:::

## Model Editor

Model editing is supported by Artivact through an external application: Blender 3D.

### Blender 3D

::: tip Supported Version
Blender integration has been tested for Versions **3** and **4**.
:::

For model editing, Blender 3D can be opened from within Artivact.
The selected model-set, containing an OBJ 3D model from the previous steps, will automatically be imported.

The final 3D model should be exported as GLTF/GLB file into the default folder suggested by Blender.

It can then finally be transferred to the virtual items media section and used from there on.
