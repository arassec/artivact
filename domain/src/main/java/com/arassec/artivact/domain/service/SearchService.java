package com.arassec.artivact.domain.service;

import com.arassec.artivact.core.exception.ArtivactException;
import com.arassec.artivact.core.misc.ProgressMonitor;
import com.arassec.artivact.core.model.item.Item;
import com.arassec.artivact.core.repository.FileRepository;
import com.arassec.artivact.core.repository.ItemRepository;
import com.arassec.artivact.domain.aspect.RestrictResult;
import com.arassec.artivact.domain.aspect.TranslateResult;
import com.arassec.artivact.domain.misc.ProjectDataProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service for search-engine management and search handling.
 */
@Slf4j
@Service
public class SearchService {

    /**
     * Repository for items.
     */
    private final ItemRepository itemRepository;

    /**
     * Repository for file access.
     */
    private final FileRepository fileRepository;

    /**
     * The object mapper.
     */
    @Getter
    private final ObjectMapper objectMapper;

    /**
     * Directory to the search-engine files.
     */
    private final Path searchIndexDir;

    /**
     * Executor service for long-running background tasks.
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    /**
     * The progress monitor for long-running tasks.
     */
    @Getter
    private ProgressMonitor progressMonitor;

    /**
     * Lucene's {@link IndexWriter}.
     */
    private IndexWriter indexWriter;

    /**
     * Creates a new instance.
     *
     * @param itemRepository      Repository for items.
     * @param objectMapper        The object mapper.
     * @param projectDataProvider Provider for project data.
     */
    public SearchService(ItemRepository itemRepository,
                         FileRepository fileRepository,
                         ObjectMapper objectMapper,
                         ProjectDataProvider projectDataProvider) {
        this.itemRepository = itemRepository;
        this.fileRepository  = fileRepository;
        this.objectMapper = objectMapper;
        this.searchIndexDir = projectDataProvider.getProjectRoot().resolve(ProjectDataProvider.SEARCH_INDEX_DIR);
    }

    /**
     * Recreates the search index.
     */
    public synchronized void recreateIndex() {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "createIndex");

        executorService.submit(() -> {
            try {
                log.info("Recreating search index.");
                prepareIndexing(false);
                itemRepository.findAll().forEach(item -> updateIndexInternal(item, false));
                finalizeIndexing();
                log.info("Search index created.");

                progressMonitor = null;

            } catch (Exception e) {
                progressMonitor.updateProgress("createIndexFailed", e);
                log.error("Error during search index (re-)creation!", e);
            }
        });
    }

    /**
     * Updates an item's search index.
     *
     * @param item The item.
     */
    public synchronized void updateIndex(Item item) {
        prepareIndexing(true);
        updateIndexInternal(item, true);
        finalizeIndexing();
    }

    /**
     * Searches for items without restrictions and without translating results.
     *
     * @param query      The search query to use.
     * @param maxResults The maximum number of results.
     * @return The list of found items.
     */
    @SuppressWarnings("java:S6204") // Result list needs to be modifiable!
    public List<Item> search(String query, int maxResults) {
        if (!StringUtils.hasText(query)) {
            return new LinkedList<>();
        }

        if ("*".equals(query)) {
            return itemRepository.findAll(maxResults);
        }

        try {
            Directory indexDirectory = FSDirectory.open(searchIndexDir);

            DirectoryReader indexReader = DirectoryReader.open(indexDirectory);

            List<String> itemIds = searchItemIds(query, indexReader, maxResults);

            indexReader.close();

            return itemRepository.findAllById(itemIds);
        } catch (IOException | ParseException e) {
            throw new ArtivactException("Error during item search!", e);
        }
    }

    /**
     * Searches for items with the given query.
     *
     * @param query      The lucene search query.
     * @param maxResults Maximum number of results to return.
     * @return The list of found items.
     */
    @TranslateResult
    @RestrictResult
    public List<Item> searchTranslatedRestricted(String query, int maxResults) {
        return search(query, maxResults);
    }

    /**
     * Prepares indexing.
     *
     * @param append Set to {@code true} to append to the current search index rather than create a new one.
     */
    private void prepareIndexing(boolean append) {
        try {
            if (!append) {
                fileRepository.delete(searchIndexDir);
                fileRepository.createDirIfRequired(searchIndexDir);
            }

            Directory indexDirectory = FSDirectory.open(searchIndexDir);
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());

            if (append) {
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
            } else {
                config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            }

            indexWriter = new IndexWriter(indexDirectory, config);
        } catch (IOException e) {
            throw new ArtivactException("Could not create search index writer!", e);
        }
    }

    /**
     * Updates the search index for the given item.
     *
     * @param item        The item.
     * @param updateIndex Set to {@code true} to update an existing index instead of creating a new entry for the item.
     */
    private void updateIndexInternal(Item item, boolean updateIndex) {
        try {
            Document luceneDocument = new Document();
            StringBuilder fulltext = new StringBuilder();

            // Lucene doesn't like "-" in the UUIDs, so wie store the ID for search with Lucene without them:
            String preparedItemId = item.getId().replace("-", "");
            luceneDocument.add(new TextField("preparedItemId", preparedItemId, Field.Store.YES));

            luceneDocument.add(new TextField("id", item.getId(), Field.Store.YES));
            appendField(fulltext, "id", item.getId());

            if (StringUtils.hasText(item.getTitle().getValue())) {
                luceneDocument.add(new TextField("title", item.getTitle().getValue(), Field.Store.YES));
                appendField(fulltext, "title", item.getTitle().getValue());
            }

            if (StringUtils.hasText(item.getDescription().getValue())) {
                luceneDocument.add(new TextField("description", item.getDescription().getValue(), Field.Store.YES));
                appendField(fulltext, "description", item.getDescription().getValue());
            }

            item.getProperties().forEach((key, value) -> {
                if (!StringUtils.hasText(value.getValue())) {
                    return;
                }
                luceneDocument.add(new TextField(key, value.getValue(), Field.Store.YES));
                appendField(fulltext, key, value.getValue());
                if (!value.getTranslations().isEmpty()) {
                    value.getTranslations().forEach((translationKey, translationValue)
                            -> appendField(fulltext, key + "_" + translationKey, translationValue)
                    );
                }
            });

            item.getTags().forEach(tag -> {
                luceneDocument.add(new TextField(tag.getId(), tag.getValue(), Field.Store.YES));
                appendField(fulltext, tag.getId(), tag.getValue());
            });

            luceneDocument.add(new TextField("fulltext", fulltext.toString(), Field.Store.YES));

            if (updateIndex) {
                indexWriter.updateDocument(new Term("preparedItemId", preparedItemId), luceneDocument);
            } else {
                indexWriter.addDocument(luceneDocument);
            }

            indexWriter.commit();
        } catch (IOException e) {
            throw new ArtivactException("Could not write to search index!", e);
        }
    }

    /**
     * Finalize indexing.
     */
    private void finalizeIndexing() {
        try {
            indexWriter.close();
            indexWriter = null;
        } catch (IOException e) {
            throw new ArtivactException("Could not close search index!", e);
        }
    }

    /**
     * Search for item IDs with the given query.
     *
     * @param searchQuery The query to use.
     * @param indexReader The index reader.
     * @param maxResults  Maximum number of results to return.
     * @return List of found item IDs.
     * @throws IOException    In case of errors during search index access.
     * @throws ParseException In case of malformed search queries.
     */
    private static List<String> searchItemIds(String searchQuery, DirectoryReader indexReader, int maxResults)
            throws IOException, ParseException {
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        var queryParser = new MultiFieldQueryParser(new String[]{"fulltext"}, new StandardAnalyzer());
        queryParser.setAllowLeadingWildcard(true);

        Query query = queryParser.parse(searchQuery);

        TopDocs hits = indexSearcher.search(query, maxResults);
        StoredFields storedFields = indexSearcher.storedFields();

        List<String> itemIds = new LinkedList<>();

        for (ScoreDoc hit : hits.scoreDocs) {
            Document doc = storedFields.document(hit.doc);
            itemIds.add(doc.get("id"));
        }
        return itemIds;
    }

    /**
     * Appends a key-value pair to the supplied StringBuilder.
     * <p>
     * Used to support fulltext-search over all item properties.
     *
     * @param target The target to put the values to.
     * @param key    The key.
     * @param value  The value.
     */
    private void appendField(StringBuilder target, String key, String value) {
        target.append(key);
        target.append("=");
        target.append(value);
        target.append(" ");
    }

}
