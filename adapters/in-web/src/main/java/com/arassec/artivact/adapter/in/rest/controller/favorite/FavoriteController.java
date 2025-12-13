package com.arassec.artivact.adapter.in.rest.controller.favorite;

import com.arassec.artivact.adapter.in.rest.controller.BaseController;
import com.arassec.artivact.adapter.in.rest.model.FavoriteItemData;
import com.arassec.artivact.application.port.in.favorite.IsItemFavoriteUseCase;
import com.arassec.artivact.application.port.in.favorite.ListFavoriteItemsUseCase;
import com.arassec.artivact.application.port.in.favorite.MarkItemAsFavoriteUseCase;
import com.arassec.artivact.application.port.in.favorite.UnmarkItemAsFavoriteUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.domain.model.favorite.Favorite;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing favorites.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController extends BaseController {

    private final MarkItemAsFavoriteUseCase markItemAsFavoriteUseCase;
    private final UnmarkItemAsFavoriteUseCase unmarkItemAsFavoriteUseCase;
    private final IsItemFavoriteUseCase isItemFavoriteUseCase;
    private final ListFavoriteItemsUseCase listFavoriteItemsUseCase;
    private final LoadItemUseCase loadItemUseCase;

    /**
     * Marks an item as favorite for the current user.
     *
     * @param itemId The item ID.
     * @return Response entity.
     */
    @PostMapping("/{itemId}")
    public ResponseEntity<Void> markAsFavorite(@PathVariable String itemId) {
        markItemAsFavoriteUseCase.markAsFavorite(itemId);
        return ResponseEntity.ok().build();
    }

    /**
     * Removes an item from favorites for the current user.
     *
     * @param itemId The item ID.
     * @return Response entity.
     */
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> unmarkAsFavorite(@PathVariable String itemId) {
        unmarkItemAsFavoriteUseCase.unmarkAsFavorite(itemId);
        return ResponseEntity.ok().build();
    }

    /**
     * Checks if an item is marked as favorite for the current user.
     *
     * @param itemId The item ID.
     * @return True if the item is a favorite, false otherwise.
     */
    @GetMapping("/{itemId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable String itemId) {
        boolean isFavorite = isItemFavoriteUseCase.isFavorite(itemId);
        return ResponseEntity.ok(isFavorite);
    }

    /**
     * Lists all favorite items for the current user.
     *
     * @return List of favorite items.
     */
    @GetMapping
    public ResponseEntity<List<FavoriteItemData>> listFavorites() {
        List<Favorite> favorites = listFavoriteItemsUseCase.listFavorites();

        List<FavoriteItemData> favoriteItemDataList = favorites.stream()
                .flatMap(favorite -> loadItemUseCase.load(favorite.getItemId())
                        .map(item -> {
                            String thumbnailUrl = null;
                            if (item.getMediaContent() != null && !item.getMediaContent().getImages().isEmpty()) {
                                String firstImage = item.getMediaContent().getImages().getFirst();
                                thumbnailUrl = createUrl(item.getId(), firstImage, "image");
                            }
                            return FavoriteItemData.builder()
                                    .itemId(item.getId())
                                    .title(item.getTitle() != null ? item.getTitle().getTranslatedValue() : "")
                                    .thumbnailUrl(thumbnailUrl)
                                    .build();
                        })
                        .stream())
                .toList();

        return ResponseEntity.ok(favoriteItemDataList);
    }

}
