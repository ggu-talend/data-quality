// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataquality.semantic.recognizer;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.talend.dataquality.semantic.classifier.custom.UDCategorySerDeser;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.DictionarySearchMode;
import org.talend.dataquality.semantic.index.LuceneIndex;
import org.talend.dataquality.semantic.model.DQCategory;

/**
 * created by talend on 2015-07-28 Detailled comment.
 *
 */
public class CategoryRecognizerBuilder {

    private static final Logger LOGGER = Logger.getLogger(CategoryRecognizerBuilder.class);

    private CategoryRecognizer categoryRecognizerInstance; // do not rebuild once exists

    public static final String DEFAULT_METADATA_PATH = "/category/";

    public static final String DEFAULT_DD_PATH = "/index/dictionary/";

    public static final String DEFAULT_KW_PATH = "/index/keyword/";

    public static final String DEFAULT_RE_PATH = "/org/talend/dataquality/semantic/recognizer/categorizer.json";

    private Mode mode = Mode.LUCENE;

    @Deprecated
    private URI ddPath;

    @Deprecated
    private URI kwPath;

    private URI regexPath;

    @Deprecated
    private LuceneIndex dataDictIndex;

    @Deprecated
    private LuceneIndex sharedDataDictIndex;

    @Deprecated
    private LuceneIndex keywordIndex;

    private Directory ddDirectory;

    private Directory sharedddDirectory;

    private Directory kwDirectory;

    private UserDefinedClassifier regexClassifier;

    private Map<String, DQCategory> metadata;

    public static CategoryRecognizerBuilder newBuilder() {
        System.out.println("New builder !!!");
        return new CategoryRecognizerBuilder();
    }

    public CategoryRecognizerBuilder metadata(Map<String, DQCategory> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @deprecated
     */
    public CategoryRecognizerBuilder ddPath(URI ddPath) {
        this.ddPath = ddPath;
        return this;
    }

    public CategoryRecognizerBuilder ddDirectory(Directory ddDirectory) {
        this.ddDirectory = ddDirectory;
        return this;
    }

    public CategoryRecognizerBuilder dataDictIndex(LuceneIndex dataDictIndex) {
        this.dataDictIndex = dataDictIndex;
        return this;
    }

    /**
     * @deprecated
     */
    public CategoryRecognizerBuilder kwPath(URI kwPath) {
        this.kwPath = kwPath;
        return this;
    }

    public CategoryRecognizerBuilder kwDirectory(Directory kwDirectory) {
        this.kwDirectory = kwDirectory;
        return this;
    }

    /**
     * @deprecated
     */
    public CategoryRecognizerBuilder regexPath(URI regexPath) {
        this.regexPath = regexPath;
        return this;
    }

    public CategoryRecognizerBuilder regexClassifier(UserDefinedClassifier regexClassifier) {
        this.regexClassifier = regexClassifier;
        return this;
    }

    public CategoryRecognizerBuilder lucene() {
        this.mode = Mode.LUCENE;
        return this;
    }

    public CategoryRecognizer build() throws IOException {

        switch (mode) {
        case LUCENE:
            if (categoryRecognizerInstance == null) {
                Map<String, DQCategory> meta = getCategoryMetadata();
                LuceneIndex sharedDict = getDataDictIndex();
                LuceneIndex dict = getDataDictIndex();
                LuceneIndex keyword = getKeywordIndex();
                UserDefinedClassifier regex = getRegexClassifier();
                categoryRecognizerInstance = new DefaultCategoryRecognizer(sharedDict, dict, keyword, regex, meta);
            }
            return categoryRecognizerInstance;
        case ELASTIC_SEARCH:
            throw new IllegalArgumentException("Elasticsearch mode is not supported any more");
        default:
            throw new IllegalArgumentException("no mode specified.");
        }

    }

    public Map<String, DQCategory> getCategoryMetadata() {
        if (metadata == null) {
            throw new NullPointerException("Metadata is not set");
        }
        return metadata;
    }

    private LuceneIndex getDataDictIndex() {
        if (dataDictIndex == null) {
            if (ddDirectory == null) {
                if (ddPath == null) {
                    try {
                        ddPath = CategoryRecognizerBuilder.class.getResource(DEFAULT_DD_PATH).toURI();
                    } catch (URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                dataDictIndex = new LuceneIndex(ddPath, DictionarySearchMode.MATCH_SEMANTIC_DICTIONARY);
            } else {
                if (ddPath == null) {
                    dataDictIndex = new LuceneIndex(ddDirectory, DictionarySearchMode.MATCH_SEMANTIC_DICTIONARY);
                } else {
                    throw new IllegalArgumentException("Please call either ddDirectory() or ddPath() but not both!");
                }
            }
        }
        return dataDictIndex;
    }

    private LuceneIndex getSharedDataDictIndex() {
        if (sharedDataDictIndex == null) {
            if (sharedddDirectory == null) {
                if (ddPath == null) { // FIXME create sharedddPath field or find another way to init the index
                    try {
                        ddPath = CategoryRecognizerBuilder.class.getResource(DEFAULT_DD_PATH).toURI();
                    } catch (URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                sharedDataDictIndex = new LuceneIndex(ddPath, DictionarySearchMode.MATCH_SEMANTIC_DICTIONARY);
            } else {
                if (ddPath == null) {
                    sharedDataDictIndex = new LuceneIndex(sharedddDirectory, DictionarySearchMode.MATCH_SEMANTIC_DICTIONARY);
                } else {
                    throw new IllegalArgumentException("Please call either ddDirectory() or ddPath() but not both!");
                }
            }
        }
        return sharedDataDictIndex;
    }

    private LuceneIndex getKeywordIndex() {
        if (keywordIndex == null) {
            if (kwDirectory == null) {
                if (kwPath == null) {
                    try {
                        kwPath = CategoryRecognizerBuilder.class.getResource(DEFAULT_KW_PATH).toURI();
                    } catch (URISyntaxException e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                keywordIndex = new LuceneIndex(kwPath, DictionarySearchMode.MATCH_SEMANTIC_KEYWORD);
            } else {
                if (kwPath == null) {
                    keywordIndex = new LuceneIndex(kwDirectory, DictionarySearchMode.MATCH_SEMANTIC_KEYWORD);
                } else {
                    throw new IllegalArgumentException("Please call either kwDirectory() or kwPath() but not both!");
                }
            }
        }
        return keywordIndex;
    }

    private UserDefinedClassifier getRegexClassifier() {
        if (regexClassifier == null) {
            if (regexPath == null) {
                throw new NullPointerException("RegexClassifier is not set");
            } else {
                try {
                    regexClassifier = UDCategorySerDeser.readJsonFile(regexPath);
                } catch (IOException e) {
                    LOGGER.error("Failed to load regex classifiers from URI: " + regexPath, e);
                }
            }
        }
        return regexClassifier;
    }

    /**
     * @deprecated
     */
    public enum Mode {
        LUCENE,
        ELASTIC_SEARCH
    }

    /**
     * @deprecated
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * @deprecated
     */
    public URI getDDPath() {
        return ddPath;
    }

    /**
     * @deprecated
     */
    public URI getKWPath() {
        return kwPath;
    }

    public void initIndex() {
        if (dataDictIndex != null) {
            dataDictIndex.initIndex();
        }
        if (keywordIndex != null) {
            keywordIndex.initIndex();
        }
    }

}
