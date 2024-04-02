# Media Creation

## Overview

In the desktop variant the additional menu entry ``Media Creation`` is available in the item edit mode:

![media creation menu](/assets/desktop/manual/en/media-creation-menu.png)

It opens the media creation overview upon clicking:

![media creation overview](/assets/desktop/manual/en/media-creation-overview.png)

The key buttons to use Artivact as 3D scanner application are:

- Camera
    - The camera button opens the dialog to capture images for model creation. Captured images are shown as image-set in
      the images overview.
- Scissors
    - The scissors button on an image-set starts the automatic removal of image backgrounds if clicked.
- Plus
    - The plus button under ``3D Models`` opens the configured photogrammetry software and a file explorer window
      containing the relevant images.
    - Images are selected as relevant if ``Model Input`` in the respective image-set is selected.
- Pencil
    - Once an OBJ file has been created with photogrammetry, the pencil button of a model-set will open the configured
      3D modelling tool.

## Photo-Capture Parameters

When the camera button is clicked, a dialog with configuration parameters for image capturing will appear:

![media creation capture params](/assets/desktop/manual/en/media-creation-image-capture-params.png)

Here the number of photos that should be taken can be configured as well as the usage of an automatic turntable.

::: tip Manual Turntables
If no automatic turntable is used, the configurable delay in milliseconds can be used to manually rotate the object.

E.g. a delay configuration value of 5000 will give you five seconds to rotate the object, before the next image is
captured.
:::

Automatic image background removal can be checked to remove the image backgrounds during image capturing to speed up
the process.

## Media Transfer

After finishing model creation and exporting it as GLTF/GLB file, it can be transferred to the item's media section
using the model-set details view:

![media creation model set details](/assets/desktop/manual/en/media-creation-model-set-details.png)

A click on the transfer button will copy the model to the items media section.
From then on the model is visible on the item.

::: tip Image Transfer
The same button is available in the image-set details view. This is a quick way to create a first image for the item
before capturing better "product" images later.
:::
