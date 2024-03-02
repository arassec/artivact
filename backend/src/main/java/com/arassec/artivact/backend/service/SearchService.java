package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ItemEntityRepository;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.util.ProgressMonitor;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService extends BaseService {

    private static final String SEARCH_INDEX_DIR = "sedata";

    private final ExecutorService executorService = Executors.newFixedThreadPool(1);

    private final ItemEntityRepository itemEntityRepository;

    @Getter
    private final ObjectMapper objectMapper;

    private final Path searchIndexDir;

    private IndexWriter indexWriter;

    @Getter
    private ProgressMonitor progressMonitor;

    public SearchService(ItemEntityRepository itemEntityRepository,
                         ObjectMapper objectMapper,
                         ProjectRootProvider projectRootProvider) {
        this.itemEntityRepository = itemEntityRepository;
        this.objectMapper = objectMapper;
        this.searchIndexDir = projectRootProvider.getProjectRoot().resolve(SEARCH_INDEX_DIR);
    }

    public synchronized void recreateIndex() {

        if (progressMonitor != null && progressMonitor.getException() == null) {
            return;
        }

        progressMonitor = new ProgressMonitor(getClass(), "createIndex");

        executorService.submit(() -> {
            try {
                log.info("Recreating search index.");
                prepareIndexing(false);
                itemEntityRepository.findAll()
                        .forEach(vaultItemEntity
                                -> updateIndexInternal(fromJson(vaultItemEntity.getContentJson(), Item.class), false));
                finalizeIndexing();
                log.info("Search index created.");

                progressMonitor = null;

            } catch (Exception e) {
                progressMonitor.updateProgress("createIndexFailed", e);
                log.error("Error during search index (re-)creation!", e);
            }
        });
    }

    public synchronized void updateIndex(Item item) {
        prepareIndexing(true);
        updateIndexInternal(item, true);
        finalizeIndexing();
    }

    @RestrictResult
    @TranslateResult
    @SuppressWarnings("java:S6204") // Result list needs to be modifiable!
    public List<Item> search(String query, int maxResults) {
        if ("*".equals(query)) {
            Pageable limit = PageRequest.of(0, maxResults);
            return itemEntityRepository.findAll(limit).stream()
                    .map(itemEntity -> fromJson(itemEntity.getContentJson(), Item.class))
                    .collect(Collectors.toList());
        }

        try {
            Directory indexDirectory = FSDirectory.open(searchIndexDir);

            DirectoryReader indexReader = DirectoryReader.open(indexDirectory);

            List<String> itemIds = searchItemIds(query, indexReader, maxResults);

            indexReader.close();

            List<Item> result = new LinkedList<>();

            itemEntityRepository.findAllById(itemIds)
                    .forEach(itementity -> result.add(fromJson(itementity.getContentJson(), Item.class)));

            return result;
        } catch (IOException | ParseException e) {
            throw new ArtivactException("Error during item search!", e);
        }
    }

    private void prepareIndexing(boolean append) {
        try {
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

    private void updateIndexInternal(Item item, boolean updateDoc) {
        try {
            Document luceneDocument = new Document();
            StringBuffer fulltext = new StringBuffer();

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
                luceneDocument.add(new TextField(key, value, Field.Store.YES));
                appendField(fulltext, key, value);
            });

            item.getTags().forEach(tag -> {
                luceneDocument.add(new TextField(tag.getId(), tag.getValue(), Field.Store.YES));
                appendField(fulltext, tag.getId(), tag.getValue());
            });

            luceneDocument.add(new TextField("fulltext", fulltext.toString(), Field.Store.YES));

            if (updateDoc) {
                indexWriter.updateDocument(new Term("preparedItemId", preparedItemId), luceneDocument);
            } else {
                indexWriter.addDocument(luceneDocument);
            }

            indexWriter.commit();
        } catch (IOException e) {
            throw new ArtivactException("Could not write to search index!", e);
        }
    }

    private void finalizeIndexing() {
        try {
            indexWriter.close();
            indexWriter = null;
        } catch (IOException e) {
            throw new ArtivactException("Could not close search index!", e);
        }
    }

    private static List<String> searchItemIds(String searchTerm, DirectoryReader indexReader, int maxResults)
            throws IOException, ParseException {
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        var queryParser = new MultiFieldQueryParser(new String[]{"fulltext"}, new StandardAnalyzer());
        queryParser.setAllowLeadingWildcard(true);

        Query query = queryParser.parse(searchTerm);

        TopDocs hits = indexSearcher.search(query, maxResults);
        StoredFields storedFields = indexSearcher.storedFields();

        List<String> itemIds = new LinkedList<>();

        for (ScoreDoc hit : hits.scoreDocs) {
            Document doc = storedFields.document(hit.doc);
            itemIds.add(doc.get("id"));
        }
        return itemIds;
    }

    private void appendField(StringBuffer target, String key, String value) {
        target.append(key);
        target.append("=");
        target.append(value);
        target.append(" ");
    }

}
