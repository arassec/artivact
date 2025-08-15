package com.arassec.artivact.adapter.in.rest.controller;

import com.arassec.artivact.adapter.in.rest.model.ImageSet;
import com.arassec.artivact.adapter.in.rest.model.ItemDetails;
import com.arassec.artivact.adapter.in.rest.model.ModelSet;
import com.arassec.artivact.application.port.in.item.*;
import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.*;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

/**
 * REST-Controller for item management.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/item")
public class ItemController extends BaseController {

    private final CreateItemUseCase createItemUseCase;
    private final SaveItemUseCase saveItemUseCase;
    private final LoadItemUseCase loadItemUseCase;
    private final ManageItemImagesUseCase manageItemImagesUseCase;
    private final ManageItemModelsUseCase manageItemModelsUseCase;

    private final ExportItemUseCase exportItemUseCase;
    private final DeleteItemUseCase deleteItemUseCase;
    private final UploadItemUseCase uploadItemUseCase;

    private final UseProjectDirsUseCase getProjectRootUseCase;

    private final FileRepository fileRepository;

    /**
     * Creates a new item.
     *
     * @return The item's ID.
     */
    @PostMapping
    public ResponseEntity<String> create() {
        Item item = createItemUseCase.create();
        item = saveItemUseCase.save(item);
        return ResponseEntity.ok(item.getId());
    }

    /**
     * Returns the item with the given ID.
     *
     * @param itemId The item's ID.
     * @return The item details.
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetails> load(@PathVariable String itemId) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemId);

        if (item == null) {
            throw new IllegalArgumentException("No item found with ID " + itemId);
        }

        return ResponseEntity.ok(convertItem(item));
    }

    /**
     * Updates an item.
     *
     * @param itemDetails The item details to save.
     */
    @PutMapping
    public ResponseEntity<ItemDetails> save(@RequestBody ItemDetails itemDetails) {
        Item item = loadItemUseCase.loadTranslatedRestricted(itemDetails.getId());

        if (item == null) {
            throw new IllegalArgumentException("No item found with ID " + itemDetails.getId());
        }

        MediaContent mediaContent = new MediaContent();
        mediaContent.getImages().addAll(itemDetails.getImages().stream()
                .map(Asset::getFileName)
                .toList());
        mediaContent.getModels().addAll(itemDetails.getModels().stream()
                .map(Asset::getFileName)
                .toList());

        MediaCreationContent mediaCreationContent = new MediaCreationContent();
        mediaCreationContent.getImageSets().addAll(itemDetails.getCreationImageSets().stream()
                .map(imageSet -> CreationImageSet.builder()
                        .modelInput(imageSet.isModelInput())
                        .backgroundRemoved(imageSet.getBackgroundRemoved())
                        .files(imageSet.getImages().stream()
                                .map(Asset::getFileName)
                                .toList())
                        .build())
                .toList());
        mediaCreationContent.getModelSets().addAll(itemDetails.getCreationModelSets().stream()
                .map(modelSet -> CreationModelSet.builder()
                        .directory(modelSet.getDirectory())
                        .comment(modelSet.getComment())
                        .build())
                .toList());

        item.setVersion(itemDetails.getVersion());
        item.setRestrictions(new HashSet<>(itemDetails.getRestrictions()));
        item.setTitle(itemDetails.getTitle());
        item.setDescription(itemDetails.getDescription());
        item.setMediaContent(mediaContent);
        item.setMediaCreationContent(mediaCreationContent);
        item.setProperties(itemDetails.getProperties());
        item.setTags(itemDetails.getTags());

        Item savedItem = saveItemUseCase.save(item);

        return ResponseEntity.ok(convertItem(savedItem));
    }

    /**
     * Deletes an item.
     *
     * @param itemId The item's ID.
     */
    @DeleteMapping("/{itemId}")
    public void deleteItem(@PathVariable String itemId) {
        deleteItemUseCase.delete(itemId);
    }

    /**
     * Returns an item's image in the requested size.
     *
     * @param itemId    The item's ID.
     * @param filename  The image's filename.
     * @param imageSize The target {@link ImageSize} of the image.
     * @return The image as byte array.
     */
    @GetMapping(value = "/{itemId}/image/{filename}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> getImage(@PathVariable String itemId, @PathVariable String filename,
                                       @RequestParam(required = false) ImageSize imageSize) {
        if (imageSize == null) {
            imageSize = ImageSize.ORIGINAL;
        }

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(filename)));

        byte[] image = manageItemImagesUseCase.loadImage(itemId, filename, imageSize);

        return new HttpEntity<>(image, headers);
    }

    /**
     * Returns an item's model.
     *
     * @param itemId   The item's ID.
     * @param filename The model's filename.
     * @return The model as byte array.
     */
    @GetMapping(value = "/{itemId}/model/{filename}", produces = "model/gltf-binary")
    public HttpEntity<byte[]> getModel(@PathVariable String itemId, @PathVariable String filename) {
        var contentDisposition = ContentDisposition.builder("inline")
                .filename(filename)
                .build();

        var headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        byte[] model = manageItemModelsUseCase.loadModel(itemId, filename);

        return new HttpEntity<>(model, headers);
    }

    /**
     * Saves an image to an item. Used to maintain an item's media files and image-sets for media creation.
     *
     * @param itemId     The item's ID.
     * @param file       The image file to save.
     * @param uploadOnly Set to {@code true}, if the image should only be uploaded.
     */
    @PostMapping("/{itemId}/image")
    public void uploadImage(@PathVariable String itemId,
                            @RequestPart(value = "file") final MultipartFile file,
                            @RequestParam(defaultValue = "false", required = false) boolean uploadOnly) {
        synchronized (this) {
            if (uploadOnly) {
                try {
                    manageItemImagesUseCase.saveImage(itemId, file.getOriginalFilename(), file.getInputStream(), false);
                } catch (IOException e) {
                    throw new ArtivactException("Could not save uploaded image!", e);
                }
            } else {
                manageItemImagesUseCase.addImage(itemId, file);
            }
        }
    }

    /**
     * Saves a model to an item.
     *
     * @param itemId The item's ID.
     * @param file   The model file.
     */
    @PostMapping("/{itemId}/model")
    public void uploadModel(@PathVariable String itemId,
                            @RequestPart(value = "file") final MultipartFile file) {
        synchronized (this) {
            manageItemModelsUseCase.addModel(itemId, file);
        }
    }

    /**
     * Exports an item into a ZIP file containing the data as JSON file and all media assets.
     *
     * @param response The HTTP stream to write the exported JSON file to.
     * @param itemId   The item's ID.
     * @return The file as {@link StreamingResponseBody}.
     */
    @GetMapping(value = "/{itemId}/export")
    public ResponseEntity<StreamingResponseBody> exportItem(HttpServletResponse response,
                                                            @PathVariable String itemId) {

        Path exportFile = exportItemUseCase.exportItem(itemId);

        response.setContentType(TYPE_ZIP);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ATTACHMENT_PREFIX
                + LocalDate.now() + "." + itemId + "." + ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_ZIP);
        response.addHeader(HttpHeaders.PRAGMA, NO_CACHE);
        response.addHeader(HttpHeaders.EXPIRES, EXPIRES_IMMEDIATELY);

        return ResponseEntity.ok(outputStream -> exportItemUseCase.copyExportAndDelete(exportFile, outputStream));
    }

    /**
     * Called from the UI to start syncing an item with a remote application instance.
     *
     * @param itemId The item's ID.
     */
    @PostMapping(value = "/{itemId}/upload")
    public void uploadItemToRemoteInstance(@PathVariable String itemId) {
        uploadItemUseCase.uploadItemToRemoteInstance(itemId, true);
    }

    /**
     * Converts the given item into item details for the frontend.
     *
     * @param item The item to convert.
     * @return The converted {@link ItemDetails}.
     */
    private ItemDetails convertItem(Item item) {
        return ItemDetails.builder()
                .id(item.getId())
                .version(item.getVersion())
                .restrictions(item.getRestrictions().stream().toList())
                .title(item.getTitle())
                .description(item.getDescription())
                .images(item.getMediaContent().getImages().stream()
                        .map(fileName -> Asset.builder()
                                .fileName(fileName)
                                .url(createImageUrl(item.getId(), fileName))
                                .transferable(false)
                                .build())
                        .toList())
                .models(item.getMediaContent().getModels().stream()
                        .map(fileName -> Asset.builder()
                                .fileName(fileName)
                                .url(createModelUrl(item.getId(), fileName))
                                .transferable(false)
                                .build())
                        .toList())
                .creationImageSets(item.getMediaCreationContent().getImageSets().stream()
                        .map(creationImageSet ->
                                ImageSet.builder()
                                        .backgroundRemoved(creationImageSet.getBackgroundRemoved())
                                        .modelInput(creationImageSet.isModelInput())
                                        .images(creationImageSet.getFiles().stream()
                                                .map(fileName -> Asset.builder()
                                                        .fileName(fileName)
                                                        .url(createImageUrl(item.getId(), fileName))
                                                        .transferable(true)
                                                        .build())
                                                .toList())
                                        .build())
                        .toList())
                .creationModelSets(item.getMediaCreationContent().getModelSets().stream()
                        .map(creationModelSet ->
                                ModelSet.builder()
                                        .directory(creationModelSet.getDirectory())
                                        .comment(creationModelSet.getComment())
                                        .modelSetImage(createModelSetImageUrl(getProjectRootUseCase.getProjectRoot().resolve(creationModelSet.getDirectory())))
                                        .build())
                        .toList()
                )
                .properties(item.getProperties())
                .tags(item.getTags())
                .build();
    }

    /**
     * Creates the image for a given model-set, based on the files available in the set.
     *
     * @param modelSetDir The directory of the model-set.
     * @return The (relative) URL as string.
     */
    private String createModelSetImageUrl(Path modelSetDir) {
        List<String> availableExtensions = fileRepository.listNamesWithoutScaledImages(modelSetDir).stream()
                .filter(f -> f.contains("."))
                .map(fileName -> fileName.substring(fileName.lastIndexOf(".") + 1))
                .toList();

        if (availableExtensions.contains("glb") || availableExtensions.contains("gltf")) {
            return "gltf-logo.png";
        } else if (availableExtensions.contains("blend")) {
            return "blender-logo.png";
        } else if (availableExtensions.contains("obj")) {
            return "obj-logo.png";
        }

        return "unknown-file-logo.png";
    }

    /**
     * Creates the URL to an item's model.
     *
     * @param itemId   The item's ID.
     * @param filename The model's filename
     * @return The (relative) URL as string.
     */
    private String createModelUrl(String itemId, String filename) {
        return createUrl(itemId, filename, "model");
    }

}
