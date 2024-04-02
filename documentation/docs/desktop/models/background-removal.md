# Background Removal

## Why Remove the Background?

If you want to create full, 360° 3D models of your objects, you would have to take pictures from all angles around the
whole object.
Especially the bottom side of the object must be included in the image set, or else artifacts or holes will be generated.

![model with hole](/assets/desktop/models/background-removal_hole.png)

Since this is complicated and tedious, Artivact uses automatic background removal from images to "trick" the
photogrammetry software into believing, pictures were taken from the bottom side.

![image with and without background](/assets/desktop/models/background-removal.png)

By removing the background from the images, the photogrammetry software calculates the parameters as if the camera
would have been moved instead of the object, e.g.

![tricked photogrammetry software](/assets/desktop/models/background-removal_tricked.jpg)

::: warning Downside
The downside of manipulating the image with the software below is, that image metadata from the camera taking the
picture is lost during the process. This might lead to problems with different photogrammetry tools, although e.g.
Meshroom or Metashape didn't have any problems so far.
:::

## rembg

::: tip Supported Version
rembg integration has been tested for Version 2.0.
:::

Artivact can use the free and open-source tool [rembg](https://github.com/danielgatis/rembg) by Daniel Gatis to
automatically remove the backgrounds of images.

The software has to be installed using the provided Docker image:

```
docker run -d -p 7000:7000 --name=rembg --restart=always danielgatis/rembg s
```

Please refer to the official [Docker documentation](https://docs.docker.com/manuals/) for further information on how to
use docker on your system.

After installation, it can be configured as follows under:

``Settings`` -> ``Peripherals`` -> ``Background Removal``

|     Selection      |                Configuration Value                 |               Example                |
|:------------------:|:--------------------------------------------------:|:------------------------------------:|
| ``rembg (Remote)`` | The application's web endpoint, exposed by Docker. | ``http://localhost:7000/api/remove`` |
