package com.arassec.artivact.adapter.out.database.jdbc;

import com.arassec.artivact.adapter.out.database.jdbc.springdata.entity.ItemEntity;
import com.arassec.artivact.adapter.out.database.jdbc.springdata.repository.ItemEntityRepository;
import com.arassec.artivact.application.infrastructure.aspect.PersistEntityAsJson;
import com.arassec.artivact.application.port.out.repository.ItemRepository;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.item.MediaContent;
import com.arassec.artivact.domain.model.item.MediaCreationContent;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
import com.arassec.artivact.domain.model.misc.ExchangeDefinitions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * {@link ItemRepository} implementation that uses JDBC.
 */
@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class JdbcItemRepository extends BaseJdbcRepository implements ItemRepository {

    /**
     * Spring-Data's repository for {@link ItemEntity}s.
     */
    private final ItemEntityRepository itemEntityRepository;

    /**
     * Jackson's ObjectMapper.
     */
    @Getter
    private final JsonMapper jsonMapper;

    /**
     * {@inheritDoc}
     */
    @PersistEntityAsJson(entityDir = DirectoryDefinitions.ITEMS_DIR, entityType = Item.class, filename = ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_JSON)
    @Override
    public Item save(Item item) {
        ItemEntity itemEntity = itemEntityRepository.findById(item.getId()).orElse(new ItemEntity());

        itemEntity.setId(item.getId());
        itemEntity.setContentJson(toJson(item));
        itemEntity.setSyncVersion(item.getSyncVersion());

        ItemEntity savedItemEntity = itemEntityRepository.save(itemEntity);

        item.setVersion(savedItemEntity.getVersion());

        return item;
    }

    /**
     * {@inheritDoc}
     */
    @PersistEntityAsJson(entityDir = DirectoryDefinitions.ITEMS_DIR, entityType = Item.class, delete = true, filename = ExchangeDefinitions.ITEM_EXCHANGE_FILENAME_JSON)
    @Override
    public void deleteById(String itemId) {
        itemEntityRepository.deleteById(itemId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Item> findById(String itemId) {
        Optional<ItemEntity> itemEntityOptional = itemEntityRepository.findById(itemId);

        if (itemEntityOptional.isPresent()) {
            ItemEntity itemEntity = itemEntityOptional.get();
            return Optional.of(toItem(itemEntity));
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> findItemIdsForRemoteExport(int maxItems) {
        return itemEntityRepository.findItemIdsForRemoteExport(maxItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> findAll() {
        List<Item> allItems = new ArrayList<>();
        itemEntityRepository.findAll().forEach(itemEntity -> allItems.add(toItem(itemEntity)));
        return allItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> findAll(int maxResults) {
        List<Item> allItems = new ArrayList<>();
        Pageable limit = PageRequest.of(0, maxResults);
        itemEntityRepository.findAll(limit).forEach(itemEntity -> allItems.add(toItem(itemEntity)));
        return allItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Item> findAllById(List<String> itemIds) {
        List<Item> allItems = new ArrayList<>();
        itemEntityRepository.findAllById(itemIds).forEach(itemEntity -> allItems.add(toItem(itemEntity)));
        return allItems;
    }

    /**
     * Converts the given {@link ItemEntity} into an {@link Item}.
     *
     * @param itemEntity The entity to convert.
     * @return An {@link Item} with the entity's data.
     */
    private Item toItem(ItemEntity itemEntity) {
        Item item = fromJson(itemEntity.getContentJson(), Item.class);
        item.setVersion(itemEntity.getVersion());
        item.setSyncVersion(itemEntity.getSyncVersion());

        if (item.getMediaContent() == null) {
            item.setMediaContent(new MediaContent());
        }

        if (item.getMediaCreationContent() == null) {
            item.setMediaCreationContent(new MediaCreationContent());
        }

        return item;
    }

}
