package com.arassec.artivact.backend.service;

import com.arassec.artivact.backend.persistence.ItemEntityRepository;
import com.arassec.artivact.backend.service.aop.RestrictResult;
import com.arassec.artivact.backend.service.aop.TranslateResult;
import com.arassec.artivact.backend.service.exception.ArtivactException;
import com.arassec.artivact.backend.service.model.item.Item;
import com.arassec.artivact.backend.service.util.ProjectRootProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
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

@Slf4j
@Service
public class SearchService extends BaseService {

    private static final String SEARCH_INDEX_DIR = "sedata";

    private final ItemEntityRepository itemEntityRepository;

    @Getter
    private final ObjectMapper objectMapper;

    private final Path searchIndexDir;

    private IndexWriter indexWriter;

    public SearchService(ItemEntityRepository itemEntityRepository,
                         ObjectMapper objectMapper,
                         ProjectRootProvider projectRootProvider) {
        this.itemEntityRepository = itemEntityRepository;
        this.objectMapper = objectMapper;
        this.searchIndexDir = projectRootProvider.getProjectRoot().resolve(SEARCH_INDEX_DIR);
    }

    public synchronized void recreateIndex() {
        log.info("Recreating search index.");
        prepareIndexing(false);
        itemEntityRepository.findAll()
                .forEach(vaultItemEntity -> updateIndexInternal(fromJson(vaultItemEntity.getContentJson(), Item.class)));
        finalizeIndexing();
        log.info("Search index created.");
    }

    public synchronized void updateIndex(Item item) {
        prepareIndexing(true);
        updateIndexInternal(item);
        finalizeIndexing();
    }

    @RestrictResult
    @TranslateResult
    public List<Item> search(String query, int maxResults) {
        try {
            Directory indexDirectory = FSDirectory.open(searchIndexDir);

            DirectoryReader indexReader = DirectoryReader.open(indexDirectory);

            List<String> itemIds = searchItemIds(query, indexReader, maxResults);

            indexReader.close();

            List<Item> result = new LinkedList<>();

            itemEntityRepository.findAllById(itemIds)
                    .forEach(vaultItemEntity -> result.add(fromJson(vaultItemEntity.getContentJson(), Item.class)));

            return result;
        } catch (IOException | QueryNodeException e) {
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

    private void updateIndexInternal(Item item) {
        try {
            Document luceneDocument = new Document();

            luceneDocument.add(new StoredField("id", item.getId()));

            if (StringUtils.hasText(item.getTitle().getValue())) {
                luceneDocument.add(new TextField("title", item.getTitle().getValue(), Field.Store.YES));
            }

            if (StringUtils.hasText(item.getDescription().getValue())) {
                luceneDocument.add(new TextField("description", item.getDescription().getValue(), Field.Store.YES));
            }

            item.getProperties().forEach((key, value) -> luceneDocument.add(new TextField(key, value, Field.Store.YES)));

            item.getTags().forEach(tag -> luceneDocument.add(new TextField(tag.getId(), tag.getValue(), Field.Store.YES)));

            indexWriter.addDocument(luceneDocument);
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
            throws QueryNodeException, IOException {
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        StandardQueryParser standardQueryParser = new StandardQueryParser(new StandardAnalyzer());
        standardQueryParser.setAllowLeadingWildcard(true);

        Query query = standardQueryParser.parse(searchTerm, "title");

        TopDocs hits = indexSearcher.search(query, maxResults);
        StoredFields storedFields = indexSearcher.storedFields();

        List<String> itemIds = new LinkedList<>();

        for (ScoreDoc hit : hits.scoreDocs) {
            Document doc = storedFields.document(hit.doc);
            itemIds.add(doc.get("id"));
        }
        return itemIds;
    }

}
