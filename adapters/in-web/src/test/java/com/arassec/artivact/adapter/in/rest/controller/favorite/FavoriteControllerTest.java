package com.arassec.artivact.adapter.in.rest.controller.favorite;

import com.arassec.artivact.adapter.in.rest.model.FavoriteItemData;
import com.arassec.artivact.application.port.in.favorite.IsItemFavoriteUseCase;
import com.arassec.artivact.application.port.in.favorite.ListFavoriteItemsUseCase;
import com.arassec.artivact.application.port.in.favorite.MarkItemAsFavoriteUseCase;
import com.arassec.artivact.application.port.in.favorite.UnmarkItemAsFavoriteUseCase;
import com.arassec.artivact.application.port.in.item.LoadItemUseCase;
import com.arassec.artivact.domain.model.TranslatableString;
import com.arassec.artivact.domain.model.favorite.Favorite;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteControllerTest {

    @Mock
    private MarkItemAsFavoriteUseCase markItemAsFavoriteUseCase;

    @Mock
    private UnmarkItemAsFavoriteUseCase unmarkItemAsFavoriteUseCase;

    @Mock
    private IsItemFavoriteUseCase isItemFavoriteUseCase;

    @Mock
    private ListFavoriteItemsUseCase listFavoriteItemsUseCase;

    @Mock
    private LoadItemUseCase loadItemUseCase;

    @InjectMocks
    private FavoriteController favoriteController;

    private static final String ITEM_ID = "item-123";

    @Test
    @DisplayName("Should mark item as favorite successfully")
    void shouldMarkItemAsFavorite() {
        // Given
        doNothing().when(markItemAsFavoriteUseCase).markAsFavorite(ITEM_ID);

        // When
        ResponseEntity<Void> response = favoriteController.markAsFavorite(ITEM_ID);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(markItemAsFavoriteUseCase, times(1)).markAsFavorite(ITEM_ID);
    }

    @Test
    @DisplayName("Should unmark item as favorite successfully")
    void shouldUnmarkItemAsFavorite() {
        // Given
        doNothing().when(unmarkItemAsFavoriteUseCase).unmarkAsFavorite(ITEM_ID);

        // When
        ResponseEntity<Void> response = favoriteController.unmarkAsFavorite(ITEM_ID);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
        verify(unmarkItemAsFavoriteUseCase, times(1)).unmarkAsFavorite(ITEM_ID);
    }

    @Test
    @DisplayName("Should return true when item is favorite")
    void shouldReturnTrueWhenItemIsFavorite() {
        // Given
        when(isItemFavoriteUseCase.isFavorite(ITEM_ID)).thenReturn(true);

        // When
        ResponseEntity<Boolean> response = favoriteController.isFavorite(ITEM_ID);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
        verify(isItemFavoriteUseCase, times(1)).isFavorite(ITEM_ID);
    }

    @Test
    @DisplayName("Should return false when item is not favorite")
    void shouldReturnFalseWhenItemIsNotFavorite() {
        // Given
        when(isItemFavoriteUseCase.isFavorite(ITEM_ID)).thenReturn(false);

        // When
        ResponseEntity<Boolean> response = favoriteController.isFavorite(ITEM_ID);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();
        verify(isItemFavoriteUseCase, times(1)).isFavorite(ITEM_ID);
    }

    @Test
    @DisplayName("Should return empty list when no favorites exist")
    void shouldReturnEmptyListWhenNoFavorites() {
        // Given
        when(listFavoriteItemsUseCase.listFavorites()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<FavoriteItemData>> response = favoriteController.listFavorites();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
        verify(listFavoriteItemsUseCase, times(1)).listFavorites();
        verifyNoInteractions(loadItemUseCase);
    }

    @Test
    @DisplayName("Should return favorite items with thumbnail URLs")
    void shouldReturnFavoriteItemsWithThumbnails() {
        // Given
        Favorite favorite1 = new Favorite();
        favorite1.setItemId("item-1");

        Favorite favorite2 = new Favorite();
        favorite2.setItemId("item-2");

        List<Favorite> favorites = List.of(favorite1, favorite2);

        Item item1 = createItemWithImage("item-1", "Item 1", "image1.jpg");
        Item item2 = createItemWithImage("item-2", "Item 2", "image2.jpg");

        when(listFavoriteItemsUseCase.listFavorites()).thenReturn(favorites);
        when(loadItemUseCase.loadTranslated("item-1")).thenReturn(item1);
        when(loadItemUseCase.loadTranslated("item-2")).thenReturn(item2);

        // When
        ResponseEntity<List<FavoriteItemData>> response = favoriteController.listFavorites();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);

        FavoriteItemData data1 = response.getBody().getFirst();
        assertThat(data1.getItemId()).isEqualTo("item-1");
        assertThat(data1.getTitle()).isEqualTo("Item 1");
        assertThat(data1.getThumbnailUrl()).contains("item-1").contains("image1.jpg");

        FavoriteItemData data2 = response.getBody().get(1);
        assertThat(data2.getItemId()).isEqualTo("item-2");
        assertThat(data2.getTitle()).isEqualTo("Item 2");
        assertThat(data2.getThumbnailUrl()).contains("item-2").contains("image2.jpg");

        verify(listFavoriteItemsUseCase, times(1)).listFavorites();
        verify(loadItemUseCase, times(1)).loadTranslated("item-1");
        verify(loadItemUseCase, times(1)).loadTranslated("item-2");
    }

    @Test
    @DisplayName("Should return favorite items without thumbnail when no images exist")
    void shouldReturnFavoriteItemsWithoutThumbnailWhenNoImages() {
        // Given
        Favorite favorite = new Favorite();
        favorite.setItemId("item-no-image");

        Item item = new Item();
        item.setId("item-no-image");
        TranslatableString title = new TranslatableString();
        title.setTranslatedValue("Item Without Image");
        item.setTitle(title);

        when(listFavoriteItemsUseCase.listFavorites()).thenReturn(List.of(favorite));
        when(loadItemUseCase.loadTranslated("item-no-image")).thenReturn(item);

        // When
        ResponseEntity<List<FavoriteItemData>> response = favoriteController.listFavorites();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);

        FavoriteItemData data = response.getBody().getFirst();
        assertThat(data.getItemId()).isEqualTo("item-no-image");
        assertThat(data.getTitle()).isEqualTo("Item Without Image");
        assertThat(data.getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("Should return favorite items with empty title when title is null")
    void shouldReturnFavoriteItemsWithEmptyTitleWhenTitleIsNull() {
        // Given
        Favorite favorite = new Favorite();
        favorite.setItemId("item-no-title");

        Item item = new Item();
        item.setId("item-no-title");
        item.setTitle(null);

        when(listFavoriteItemsUseCase.listFavorites()).thenReturn(List.of(favorite));
        when(loadItemUseCase.loadTranslated("item-no-title")).thenReturn(item);

        // When
        ResponseEntity<List<FavoriteItemData>> response = favoriteController.listFavorites();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);

        FavoriteItemData data = response.getBody().getFirst();
        assertThat(data.getItemId()).isEqualTo("item-no-title");
        assertThat(data.getTitle()).isEmpty();
        assertThat(data.getThumbnailUrl()).isNull();
    }

    @Test
    @DisplayName("Should return favorite items with null thumbnail when media content is null")
    void shouldReturnFavoriteItemsWithNullThumbnailWhenMediaContentIsNull() {
        // Given
        Favorite favorite = new Favorite();
        favorite.setItemId("item-no-media");

        Item item = new Item();
        item.setId("item-no-media");
        TranslatableString title = new TranslatableString();
        title.setTranslatedValue("Item Without Media");
        item.setTitle(title);
        item.setMediaContent(null);

        when(listFavoriteItemsUseCase.listFavorites()).thenReturn(List.of(favorite));
        when(loadItemUseCase.loadTranslated("item-no-media")).thenReturn(item);

        // When
        ResponseEntity<List<FavoriteItemData>> response = favoriteController.listFavorites();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);

        FavoriteItemData data = response.getBody().getFirst();
        assertThat(data.getItemId()).isEqualTo("item-no-media");
        assertThat(data.getTitle()).isEqualTo("Item Without Media");
        assertThat(data.getThumbnailUrl()).isNull();
    }

    private Item createItemWithImage(String id, String title, String imageName) {
        Item item = new Item();
        item.setId(id);

        TranslatableString titleString = new TranslatableString();
        titleString.setTranslatedValue(title);
        item.setTitle(titleString);

        MediaContent mediaContent = new MediaContent();
        mediaContent.setImages(List.of(imageName));
        item.setMediaContent(mediaContent);

        return item;
    }
}

