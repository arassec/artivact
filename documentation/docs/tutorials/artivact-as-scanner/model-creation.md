# Model Creation

## Example Workflows

Artivact provides example workflows for the supported photogrammetry tools on the first start.
Those can be used to quickly start model creation. 
They limit the size of the resulting 3D model and texture.

The sample workflows can be customized and saved in order to e.g. increase the model's texture size.

::: warning Export Settings
Export configuration in the provided workflows must *not* be changed, since Artivact expects the resulting model to be
in the there configured place!
:::

Details on the configuration can be found in the [User Manual](../../user-manual/settings/peripherals)

### Meshroom Pipeline

When Meshroom is started from within Artivact, it will open with a default pipeline which already includes
mesh reduction and export nodes. 
If warnings about newer Node IDs appear, update the node's UUIDs using Meshroom.

![Meshroom default pipeline](/assets/tutorials/artivact-as-scanner/model-creation-meshroom.png)

### Metashape Workflow

Agisoft Metashape's standard edition does not allow to open any workflow with when opening the program.
The user has to open a batch processing workflow by hand.

A minimal workflow is provided by Artivact under the project root directory in:

```
utils/Metashape/artivact-metashape-workflow.xml
```

After adding the images, it can be opened with in Metashape with ``Workflow`` -> ``Batch Process`` by clicking the 
directory icon:

![Metashape batch process](/assets/tutorials/artivact-as-scanner/model-creation-metashape-one.png)

Then the following workflow will be executed by Metashape when clicking on ``OK``:

![Metashape batch process](/assets/tutorials/artivact-as-scanner/model-creation-metashape-two.png)

