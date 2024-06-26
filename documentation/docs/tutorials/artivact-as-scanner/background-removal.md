# Background Removal

## Why Remove the Background?

If you want to create full, 360° 3D models of your objects, you would have to take pictures from all angles around the
whole object.
Especially the bottom side of the object must be included in the image set, or else artifacts or holes will be generated.

![model with hole](/assets/tutorials/artivact-as-scanner/background-removal_hole.png)

Since this is complicated and tedious, Artivact uses automatic background removal from images to "trick" the
photogrammetry software into believing, pictures were taken from the bottom side.

![image with and without background](/assets/tutorials/artivact-as-scanner/background-removal.png)

By removing the background from the images, the photogrammetry software calculates the parameters as if the camera
would have been moved instead of the object, e.g.

![tricked photogrammetry software](/assets/tutorials/artivact-as-scanner/background-removal_tricked.jpg)

::: warning Downside
The downside of manipulating the image with the software below is, that image metadata from the camera taking the
picture is lost during the process. This might lead to problems with different photogrammetry tools, although e.g.
Meshroom or Metashape didn't have any problems so far.
:::

Details on the configuration can be found in the [User Manual](../../user-manual/settings/peripherals)