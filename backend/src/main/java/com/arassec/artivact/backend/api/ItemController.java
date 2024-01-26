package com.arassec.artivact.backend.api;

import com.arassec.artivact.backend.api.model.Asset;
import com.arassec.artivact.backend.api.model.ImageSet;
import com.arassec.artivact.backend.api.model.ItemDetails;
import com.arassec.artivact.backend.api.model.ModelSet;
import com.arassec.artivact.backend.service.ItemService;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.*;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/item")
public class ItemController extends BaseController {

    private final ItemService itemService;

    private final ProjectRootProvider projectRootProvider;

    @PostMapping
    public ResponseEntity<String> create() {
        Item item = itemService.create();
        item = itemService.save(item);
        return ResponseEntity.ok(item.getId());
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetails> load(@PathVariable String itemId) {
        Item item = itemService.load(itemId);

        if (item == null) {
            throw new IllegalArgumentException("No item found with ID " + itemId);
        }

        return ResponseEntity.ok(ItemDetails.builder()
                .id(item.getId())
                .version(item.getVersion())
                .restrictions(item.getRestrictions().stream().toList())
                .title(item.getTitle())
                .description(item.getDescription())
                .images(item.getMediaContent().getImages().stream()
                        .map(fileName -> Asset.builder()
                                .fileName(fileName)
                                .url(createImageUrl(itemId, fileName))
                                .transferable(false)
                                .build())
                        .toList())
                .models(item.getMediaContent().getModels().stream()
                        .map(fileName -> Asset.builder()
                                .fileName(fileName)
                                .url(createModelUrl(itemId, fileName))
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
                                                        .url(createImageUrl(itemId, fileName))
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
                                        .modelSetImage(createModelSetImageUrl(projectRootProvider.getProjectRoot().resolve(creationModelSet.getDirectory())))
                                        .build())
                        .toList()
                )
                .properties(item.getProperties())
                .tags(item.getTags())
                .build());
    }

    @PutMapping
    public ResponseEntity<Void> save(@RequestBody ItemDetails itemDetails) {

        Item item = itemService.load(itemDetails.getId());

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

        itemService.save(item);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(@PathVariable String itemId) {
        itemService.delete(itemId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{itemId}/image/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public HttpEntity<byte[]> getImage(@PathVariable String itemId, @PathVariable String fileName,
                                       @RequestParam(required = false) ImageSize imageSize) {
        if (imageSize == null) {
            imageSize = ImageSize.ORIGINAL;
        }

        FileSystemResource image = itemService.loadImage(itemId, fileName, imageSize);

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(URLConnection.guessContentTypeFromName(fileName)));

        try {
            return new HttpEntity<>(Files.readAllBytes(image.getFile().toPath()), headers);
        } catch (IOException e) {
            throw new ArtivactException("Could not read artivact model!", e);
        }
    }

    @GetMapping(value = "/{itemId}/model/{fileName}", produces = "model/gltf-binary")
    public HttpEntity<byte[]> getModel(@PathVariable String itemId, @PathVariable String fileName) {
        var contentDisposition = ContentDisposition.builder("inline")
                .filename(fileName)
                .build();

        var headers = new HttpHeaders();
        headers.setContentDisposition(contentDisposition);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        FileSystemResource model = itemService.loadModel(itemId, fileName);

        try {
            return new HttpEntity<>(Files.readAllBytes(model.getFile().toPath()), headers);
        } catch (IOException e) {
            throw new ArtivactException("Could not read artivact model!", e);
        }
    }

    @GetMapping(value = "/{itemId}/media")
    public ResponseEntity<StreamingResponseBody> downloadMedia(HttpServletResponse response,
                                                               @PathVariable String itemId) {

        List<String> mediaFiles = itemService.getFilesForDownload(itemId);

        StreamingResponseBody streamResponseBody = out -> {
            final ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream());

            ZipEntry zipEntry = null;
            for (String mediaFile : mediaFiles) {
                File file = new File(mediaFile);
                zipEntry = new ZipEntry(file.getName());

                try (var inputStream = new FileInputStream(file)) {
                    zipOutputStream.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = inputStream.read(bytes)) >= 0) {
                        zipOutputStream.write(bytes, 0, length);
                    }
                } catch (IOException e) {
                    log.error("Exception while reading and streaming data!", e);
                }

            }

            zipOutputStream.close();

            response.setContentLength((int) (zipEntry != null ? zipEntry.getSize() : 0));
        };

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=" + itemId + ".zip");
        response.addHeader("Pragma", "no-cache");
        response.addHeader("Expires", "0");

        return ResponseEntity.ok(streamResponseBody);
    }

    @PostMapping("/{itemId}/image")
    public ResponseEntity<String> uploadImage(@PathVariable String itemId,
                                              @RequestPart(value = "file") final MultipartFile file,
                                              @RequestParam(defaultValue = "false", required = false) Boolean uploadOnly) {
        synchronized (this) {
            if (uploadOnly) {
                try {
                    itemService.saveImage(itemId, file.getOriginalFilename(), file.getInputStream(), false);
                } catch (IOException e) {
                    throw new ArtivactException("Could not save uploaded image!", e);
                }
            } else {
                itemService.addImage(itemId, file);
            }
        }
        return ResponseEntity.ok("image uploaded");
    }

    @PostMapping("/{itemId}/model")
    public ResponseEntity<String> uploadModel(@PathVariable String itemId,
                                              @RequestPart(value = "file") final MultipartFile file) {
        synchronized (this) {
            itemService.addModel(itemId, file);
        }
        return ResponseEntity.ok("model uploaded");
    }

}
