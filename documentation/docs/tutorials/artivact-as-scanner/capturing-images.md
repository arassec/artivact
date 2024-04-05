# Capturing Images

## Capturing Good Images

Capturing images for photogrammetry is easy, but certain restrictions apply that should receive attention.

Good pictures for photogrammetry for creating virtual items meet the following conditions:

- Achieve good, constant lighting of the complete object, e.g. ![good](/assets/tutorials/artivact-as-scanner/image-capture_good.jpg)
- Avoid hard shadows, e.g. ![hard shadows](/assets/tutorials/artivact-as-scanner/image-capture_bad_shadows.jpg)
- Avoid a low depth of sharpness, e.g. ![low depth of sharpness](/assets/tutorials/artivact-as-scanner/image-capture_bad_sharpness.jpg)

In order to create good results with photogrammetry software, the images of the item should have a high overlap.
A rotation of 10% of the object between taking pictures is optimal.
I.e. a minimum of 36 pictures should be taken (360° / 10% overlap) in order to create a good model.

## Automatic Capturing with Artivact

You can import taken pictures manually into Artivact as described in the [user manual](/user-manual/introduction/about).

But the real benefit of using Artivact on the desktop is by automating the camera usage and import the images
automatically.

For this, you need to attach your camera to your PC using USB and install Software to control it remotely.
Currently, [DigiCamControl](https://digicamcontrol.com/) for Windows systems and [gphoto2](http://gphoto.org/) for Linux systems is supported.

After installation, you can configure the software under:

``Settings`` -> ``Peripherals`` -> ``Camera Configuration``

### DigiCamControl

::: tip Supported Version
DigiCamControl integration has been tested for Version 2.1.
:::

| Selection | Configuration Value | Example |
| :---: | :---: | :---: |
| ``DigiCamControl`` | The application's executable | ``C:/Program Files (x86)/digiCamControl/CameraControlCmd.exe``|
| ``DigiCamControl (Remote)`` | The application's web endpoint, if the [web interface](https://digicamcontrol.com/doc/userguide/web) is enabled.  | ``http://localhost:5513/``|

"DigiCamControl (Remote)" provides better performance, but needs configuration in DigiCamControl to work.
The embedded web server needs to be activated by the user: [DigiCamControl Webserver Configuration](https://digicamcontrol.com/doc/userguide/settings#webserver)

### gphoto2

::: tip Supported Version
gphoto2 integration has been tested for Version 2.5.
:::

| Selection | Configuration Value | Example |
| :---: | :---: | :---: |
| ``gphoto2`` | The application's executable | ``/usr/bin/gphoto2``|
