package com.arassec.artivact.adapter.out.database;

import com.arassec.artivact.adapter.out.database.jdbc.JdbcItemRepository;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.ItemEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.ItemEntityRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests the {@link JdbcItemRepository}.
 */
@ExtendWith(MockitoExtension.class)
class JdbcItemRepositoryTest {

    /**
     * The repository under test.
     */
    @InjectMocks
    private JdbcItemRepository jdbcItemRepository;

    /**
     * Spring-Data's item repository mock.
     */
    @Mock
    private ItemEntityRepository itemEntityRepository;

    /**
     * ObjectMapper mock.
     */
    @Mock
    private ObjectMapper objectMapper;

    /**
     * Tests saving a new item.
     */
    @Test
    @SneakyThrows
    void testSaveNewItem() {
        Item item = new Item();
        item.setId("id");
        item.setVersion(42);
        item.setSyncVersion(23);

        when(objectMapper.writeValueAsString(item)).thenReturn("{contentJson}");

        ItemEntity savedItemEntity = new ItemEntity();
        savedItemEntity.setVersion(43);

        when(itemEntityRepository.save(any(ItemEntity.class))).thenReturn(savedItemEntity);

        Item savedItem = jdbcItemRepository.save(item);

        assertEquals(43, savedItem.getVersion());
    }

    /**
     * Tests saving an existing item.
     */
    @Test
    @SneakyThrows
    void testSaveExistingItem() {
        Item item = new Item();
        item.setId("id");
        item.setVersion(42);
        item.setSyncVersion(23);

        when(objectMapper.writeValueAsString(item)).thenReturn("{contentJson}");

        ItemEntity existingItemEntity = new ItemEntity();

        when(itemEntityRepository.findById("id")).thenReturn(Optional.of(existingItemEntity));

        ItemEntity savedItemEntity = new ItemEntity();
        savedItemEntity.setVersion(43);

        when(itemEntityRepository.save(any(ItemEntity.class))).thenReturn(savedItemEntity);

        Item savedItem = jdbcItemRepository.save(item);

        assertEquals(43, savedItem.getVersion());

        ArgumentCaptor<ItemEntity> argCap = ArgumentCaptor.forClass(ItemEntity.class);
        verify(itemEntityRepository, times(1)).save(argCap.capture());

        assertEquals("{contentJson}", argCap.getValue().getContentJson());
    }

    /**
     * Tests deleting an item by its ID.
     */
    @Test
    void testDeleteById() {
        jdbcItemRepository.deleteById("id");
        verify(itemEntityRepository, times(1)).deleteById("id");
    }

    /**
     * Tests finding an item by its ID.
     */
    @Test
    @SneakyThrows
    void testFindById() {
        assertTrue(jdbcItemRepository.findById("invalid").isEmpty());

        Item item = new Item();

        when(objectMapper.readValue(anyString(), eq(Item.class))).thenReturn(item);

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setVersion(42);
        itemEntity.setContentJson("{contentJson}");

        when(itemEntityRepository.findById("id")).thenReturn(Optional.of(itemEntity));

        Optional<Item> itemOptional = jdbcItemRepository.findById("id");

        assertTrue(itemOptional.isPresent());
        assertEquals(42, itemOptional.get().getVersion());
    }

    /**
     * Tests finding items for remote export.
     */
    @Test
    void testFindItemIdsForRemoteExport() {
        List<String> ids = List.of("id1", "id2");
        when(itemEntityRepository.findItemIdsForRemoteExport(anyInt())).thenReturn(ids);
        List<String> itemIds = jdbcItemRepository.findItemIdsForRemoteExport(123);
        assertEquals(ids, itemIds);
    }

    /**
     * Tests finding all items.
     */
    @Test
    @SneakyThrows
    void testFindAll() {
        when(objectMapper.readValue(anyString(), eq(Item.class))).thenReturn(new Item());

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setVersion(23);
        itemEntity.setContentJson("{contentJson}");

        when(itemEntityRepository.findAll()).thenReturn(List.of(itemEntity));

        List<Item> items = jdbcItemRepository.findAll();

        assertEquals(1, items.size());
        assertEquals(23, items.getFirst().getVersion());
    }

    /**
     * Tests finding all items with a limit.
     */
    @Test
    @SneakyThrows
    void testFindAllWithLimit() {
        when(objectMapper.readValue(anyString(), eq(Item.class))).thenReturn(new Item());

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setVersion(23);
        itemEntity.setContentJson("{contentJson}");

        Pageable limit = PageRequest.of(0, 666);

        Page<ItemEntity> itemEntityPage = new PageImpl<>(List.of(itemEntity));

        when(itemEntityRepository.findAll(limit)).thenReturn(itemEntityPage);

        List<Item> items = jdbcItemRepository.findAll(666);

        assertEquals(1, items.size());
        assertEquals(23, items.getFirst().getVersion());
    }

    /**
     * Tests finding all items with certain IDs.
     */
    @Test
    @SneakyThrows
    void testFindAllById() {
        when(objectMapper.readValue(anyString(), eq(Item.class))).thenReturn(new Item());

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setVersion(23);
        itemEntity.setContentJson("{contentJson}");

        List<String> ids = List.of("id1", "id2");

        when(itemEntityRepository.findAllById(ids)).thenReturn(List.of(itemEntity));

        List<Item> items = jdbcItemRepository.findAllById(ids);

        assertEquals(1, items.size());
        assertEquals(23, items.getFirst().getVersion());
    }

}
