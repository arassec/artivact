package com.arassec.artivact.adapter.out.search;

import com.arassec.artivact.application.port.in.project.UseProjectDirsUseCase;
import com.arassec.artivact.application.port.out.gateway.SearchGateway;
import com.arassec.artivact.application.port.out.repository.FileRepository;
import com.arassec.artivact.domain.exception.ArtivactException;
import com.arassec.artivact.domain.model.item.Item;
import com.arassec.artivact.domain.model.misc.DirectoryDefinitions;
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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

@Component
public class LuceneSearchGateway implements SearchGateway {

    /**
     * Repository for file access.
     */
    private final FileRepository fileRepository;

    /**
     * Directory to the search-engine files.
     */
    private final Path searchIndexDir;

    /**
     * Lucene's {@link IndexWriter}.
     */
    private IndexWriter indexWriter;


    public LuceneSearchGateway(FileRepository fileRepository,
                               UseProjectDirsUseCase getProjectRootUseCase) {
        this.fileRepository = fileRepository;
        this.searchIndexDir = getProjectRootUseCase.getProjectRoot()
                .resolve(DirectoryDefinitions.SEARCH_INDEX_DIR);
    }

    /**
     * Prepares indexing.
     *
     * @param append Set to {@code true} to append to the current search index rather than create a new one.
     */
    @Override
    public void prepareIndexing(boolean append) {
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
    @Override
    public void updateIndex(Item item, boolean updateIndex) {
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
     * {@inheritDoc}
     */
    @Override
    public void finalizeIndexing() {
        try {
            indexWriter.close();
            indexWriter = null;
        } catch (IOException e) {
            throw new ArtivactException("Could not close search index!", e);
        }
    }

    @Override
    public List<String> search(String searchQuery, int maxResults) {
        try {
            Directory indexDirectory = FSDirectory.open(searchIndexDir);

            DirectoryReader indexReader = DirectoryReader.open(indexDirectory);

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

            indexReader.close();

            return itemIds;
        } catch (IOException | ParseException e) {
            throw new ArtivactException("Error during item search!", e);
        }
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
