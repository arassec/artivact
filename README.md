# Artivact

Artivact is a tool suite of applications to create and manage virtual artifacts.

Currently in development are the following applications:

* Artivact-Creator - A tool to create virtual artifacts.
* Artivact-Vault - A Web-Application to manage and present virtual artifacts.

## Artivact-Creator

A tool to create and organize virtual artifacts for websites or virtual reality experiences.

It can be used to perform the following tasks:

- Automatically capture images via camera and artivact-turntable as input for model generation
- Automatically remove backgrounds from captured images using [rembg](https://github.com/danielgatis/rembg)
- Generate 3D models using photogrammetry for single objects or in batch
  with [Meshroom](https://github.com/alicevision/Meshroom)
- Open generated 3D models in [Blender3D](https://www.blender.org/) to manually finalize them

### Configuration

The following configuration parameters can be set by placing an `application.properties` file next to the program's
JAR-file.

```
# possible values: 'ArtivactTurntable'
adapter.implementation.turntable=fallback
adapter.implementation.turntable.delay=300

# possible values: 'DigiCamControl' or 'gphoto2'
adapter.implementation.camera=fallback
adapter.implementation.camera.executable=

# possible values: 'fallback', 'RemBg' or 'RemBgRemote'
adapter.implementation.background=fallback
adapter.implementation.background.executable=

# possible values: 'Meshroom'
adapter.implementation.model-creator=fallback
adapter.implementation.model-creator.executable=

# possible values: 'Blender'
adapter.implementation.model-editor=fallback
adapter.implementation.model-editor.executable=
```

The adapters to various tools can always contain the value 'fallback', which means the respective tool will not be used
at all.

If a software is configured, e.g. 'Meshroom' or 'Blender', the respective tool must already be installed on the system.

### Usage

Save the provided JAR-file from the [releases page](https://github.com/arassec/artivact-creator/releases/) and run it
with a JRE17 or later.

#### Using the Artivact-Turntable

Linux users have to be in the "dialout" group to use the artivact turntable!

The turntable delay might be configured in case commands are sent to quickly to the turntable and it stops working.

#### Using rembg

[rembg](https://github.com/danielgatis/rembg) can be installed locally and must than be configured as follows:

```
adapter.implementation.background=RemBg
adapter.implementation.background.executable=/path/to/rembg
```

Alternatively you can run rembg in a Docker container and call it remotely via HTTP. For this the rembg service has to
be started in docker, e.g. like this:
``docker run -d -p 5000:5000 --name=rembg --restart=always danielgatis/rembg s``

The artivact-creator configuration is then as follows:

```
adapter.implementation.background=RemBgRemote
adapter.implementation.background.executable=http://localhost:5000
```

## Artivact-Vault

A Spring-Boot application with Quasar frontend to manage and present virtual artifacts.