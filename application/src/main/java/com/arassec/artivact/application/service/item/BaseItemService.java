package com.arassec.artivact.application.service.item;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for item related services.
 */
public abstract class BaseItemService {

    /**
     * Getter for the {@link FileRepository}.
     *
     * @return The application's file repository.
     */
    public abstract FileRepository getFileRepository();

    /**
     * Getter for the {@link UseProjectDirsUseCase}.
     *
     * @return The application's use project dirs use case.
     */
    public abstract UseProjectDirsUseCase getUseProjectDirsUseCase();

    /**
     * Returns a list of the item's images that are not referenced by the item itself, but only exist in the
     * filesystem.
     *
     * @param item The item.
     * @return List of unreferenced images.
     */
    protected List<String> getDanglingImages(Item item) {
        List<String> imagesInItem = new LinkedList<>(item.getMediaContent().getImages());
        item.getMediaCreationContent().getImageSets().forEach(creationImageSet -> imagesInItem.addAll(creationImageSet.getFiles()));

        List<String> allImagesInFolder = getFileRepository().listNamesWithoutScaledImages(
                getFileRepository().getDirFromId(getUseProjectDirsUseCase().getItemsDir(), item.getId())
                        .resolve(DirectoryDefinitions.IMAGES_DIR)
        );

        allImagesInFolder.removeAll(imagesInItem);

        return allImagesInFolder;
    }

}
