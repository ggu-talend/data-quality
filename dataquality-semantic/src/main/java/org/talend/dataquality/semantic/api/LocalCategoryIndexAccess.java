package org.talend.dataquality.semantic.api;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.talend.dataquality.semantic.model.DQCategory;

public class LocalCategoryIndexAccess implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(LocalCategoryIndexAccess.class);

    protected IndexWriter luceneWriter;

    protected DirectoryReader luceneReader;

    protected IndexSearcher luceneSearcher;

    private FSDirectory fsDir;

    public LocalCategoryIndexAccess(URI uri) throws IOException {
        fsDir = FSDirectory.open(new File(uri));
        createSearcher();
        createWriter();
    }

    public void createCategory(DQCategory category) {
        LOGGER.debug("createCategory: " + category);
        try {
            Document luceneDoc = DQCategoryMapper.fromDQCategoryToLuceneDocument(category);

            if (!CollectionUtils.isEmpty(category.getChildren()))
                for (DQCategory child : category.getChildren())
                    luceneDoc.add(new TextField(DictionaryConstants.CHILD, child.getId(), Field.Store.YES));
            luceneWriter.addDocument(luceneDoc);
        } catch (IOException | NullPointerException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void insertOrUpdateCategory(DQCategory category) {
        LOGGER.debug("insertOrUpdateCategory: " + category);
        final Term searchTerm = new Term(DictionaryConstants.ID, category.getId());
        final TermQuery termQuery = new TermQuery(searchTerm);
        try {
            TopDocs result = luceneSearcher.search(termQuery, 1);
            if (result.totalHits == 1) {
                final Term term = new Term(DictionaryConstants.ID, category.getId());
                List<Field> fields = DQCategoryMapper.fromDQCategorytoFields(category);
                if (!CollectionUtils.isEmpty(category.getChildren()))
                    for (DQCategory child : category.getChildren())
                        fields.add(new TextField(DictionaryConstants.CHILD, child.getId(), Field.Store.YES));
                luceneWriter.updateDocument(term, fields);
            } else {
                createCategory(category);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public void deleteAll() {
        LOGGER.debug("delete all content");
        try {
            luceneWriter.deleteAll();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void deleteCategory(DQCategory category) {
        LOGGER.debug("deleteCategory: " + category);
        Term luceneId = new Term(DictionaryConstants.ID, category.getId());
        try {
            luceneWriter.deleteDocuments(luceneId);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Create the index writer
     */
    private void createWriter() {
        if (luceneWriter == null) {
            try {
                final Analyzer analyzer = new StandardAnalyzer();
                final IndexWriterConfig iwc = new IndexWriterConfig(Version.LATEST, analyzer);
                iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
                iwc.setWriteLockTimeout(5000000);
                luceneWriter = new IndexWriter(fsDir, iwc);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Create the index searcher
     */
    private void createSearcher() {
        if (luceneSearcher == null) {
            try {
                luceneReader = DirectoryReader.open(fsDir);
                luceneSearcher = new IndexSearcher(luceneReader);
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (luceneWriter != null) {
            try {
                luceneWriter.commit();
                luceneWriter.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (luceneReader != null) {
            try {
                luceneReader.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (fsDir != null) {
            fsDir.close();
        }
    }

    public void commitChanges() {
        if (luceneWriter != null) {
            try {
                luceneWriter.commit();
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}
