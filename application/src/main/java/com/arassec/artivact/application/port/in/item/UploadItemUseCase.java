package com.arassec.artivact.application.port.in.item;

public interface UploadItemUseCase {

    /**
     * Exports the item with the given ID and uploads it to a remote application instance configured in the exchange
     * configuration.
     *
     * @param itemId       The item's ID.
     * @param asynchronous Set to {@code true} to upload the item in a different thread.
     */
    void uploadItemToRemoteInstance(String itemId, boolean asynchronous);

}
