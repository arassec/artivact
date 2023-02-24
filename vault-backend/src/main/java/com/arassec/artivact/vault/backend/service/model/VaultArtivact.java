package com.arassec.artivact.vault.backend.service.model;

import com.arassec.artivact.common.model.Artivact;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.nio.file.Path;
import java.util.*;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class VaultArtivact extends Artivact {

    public static final String SCALED_IMAGES_DIR = "scaled-images";

    private String id;

    private Integer version;

    private TranslatableItem title;

    private Path projectRoot;

    @Builder.Default
    private Set<String> restrictions = new HashSet<>();

    private TranslatableItem description;

    @Builder.Default
    private Map<String, String> properties = new HashMap<>();

    private MediaContent mediaContent;

    @Builder.Default
    private List<Tag> tags = new LinkedList<>();

    public int getNextAssetNumber(Path assetDir) {
        return getNextAssetNumber(assetDir, List.of());
    }

    public Path getScaledImagesDir(boolean includeProjectRoot) {
        if (includeProjectRoot) {
            return getAssetDir(getProjectRoot(), SCALED_IMAGES_DIR);
        }
        return getAssetDir(null, SCALED_IMAGES_DIR);
    }

}
