# Model Editing

## Why Edit the Model?

Photogrammetry software does not always generate accurate 3D models from captured images.
Artifacts might get generated into the resulting 3D model, which might not be wanted.

![Model with artifact](/assets/create/models/model-creation-artifact.png)

For those cases it might be useful to edit the 3D model manually with a 3D modelling tool.

Currently, Artivact has integration for the free and open-source tool Blender 3D implemented.

## Blender 3D

::: tip Supported Version
Blender integration has been tested for Versions 3 and 4.
:::

For model editing, Blender 3D can be opened from within Artivact.
The selected model-set, containing an OBJ 3D model from the previous steps, will automatically be imported.

The final 3D model should be exported as GLTF/GLB file into the default folder suggested by Blender.

It can then finally be transferred to the virtual items media section and used from there on.
