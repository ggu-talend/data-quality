package org.talend.dataquality.semantic.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.store.Directory;
import org.talend.dataquality.semantic.classifier.custom.UDCategorySerDeser;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.ClassPathDirectory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

public class CategoryRecognizerBuilderFactory {

    private static final Logger LOGGER = Logger.getLogger(CategoryRecognizerBuilderFactory.class);

    private Map<URI, Directory> luceneDirectoryCache = new HashMap<>();

    private CategoryRegistryManager crm;

    /**
     * constructor
     * 
     * @param crm
     */
    public CategoryRecognizerBuilderFactory(CategoryRegistryManager crm) {
        this.crm = crm;
    }

    /**
     * @return
     */
    public CategoryRecognizerBuilder createCategoryRecognizerBuilder() {
        try {
            Directory ddDir = getDirectory(crm.getDictionaryURI());
            Directory kwDir = getDirectory(crm.getKeywordURI());
            UserDefinedClassifier udc = UDCategorySerDeser.readJsonFile(crm.getRegexURI());
            return CategoryRecognizerBuilder.newBuilder().lucene().metadata(crm.getCategoryMetadataMap()) //
                    .ddDirectory(ddDir).kwDirectory(kwDir).regexClassifier(udc);
        } catch (URISyntaxException e) {
            LOGGER.error("Unable to read directory for CategoryRecognizerBuilder", e);
        } catch (IOException e) {
            LOGGER.error("Unable to read regex for CategoryRecognizerBuilder", e);
        }
        return null;
    }

    private Directory getDirectory(URI uri) {
        Directory dir = luceneDirectoryCache.get(uri);
        if (dir == null) {
            dir = ClassPathDirectory.open(uri);
        }
        return dir;
    }

}
