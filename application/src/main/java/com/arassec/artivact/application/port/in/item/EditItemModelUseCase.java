package com.arassec.artivact.application.port.in.item;

public interface EditItemModelUseCase {

    /**
     * Opens the item's model in an external 3D editor.
     *
     * @param itemId              The item's ID.
     * @param modelEditorConfigId The ID of the model editor configuration to use.
     * @param modelSetIndex       The model-set index to open the model from.
     */
    void editModel(String itemId, String modelEditorConfigId, int modelSetIndex);

}
