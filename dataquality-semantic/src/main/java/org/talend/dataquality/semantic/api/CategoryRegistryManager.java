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
package org.talend.dataquality.semantic.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.json.JSONArray;
import org.json.JSONObject;
import org.talend.dataquality.semantic.classifier.custom.UDCategorySerDeser;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.index.ClassPathDirectory;
import org.talend.dataquality.semantic.model.CategoryType;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizer;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

/**
 * Singleton class providing API for local category registry management.
 * 
 * A local category registry is composed by the following subfolders:
 * <ul>
 * <li><b>category:</b> lucene index containing metadata of all categories</li>
 * <li><b>index/dictionary:</b> lucene index containing dictionary documents</li>
 * <li><b>index/keyword:</b> lucene index containing keyword documents</li>
 * <li><b>regex:</b> json file containing all categories that can be recognized by regex patterns and eventual subvalidators</li>
 * </ul>
 * In each of the above subfolders, there is still a level of subfolders representing different contexts. The default context name
 * is "default".
 */
public class CategoryRegistryManager {

    private static final Logger LOGGER = Logger.getLogger(CategoryRegistryManager.class);

    /**
     * Map between context names and corresponding instances.
     */
    private static final Map<String, CategoryRegistryManager> instances = new HashMap<>();

    /**
     * Whether the local category registry will be used.
     * Default value is false, which means only initial categories are loaded. This is mostly useful for unit tests.
     * More often, the value is set to true when the localRegistryPath is configured. see
     * {@link CategoryRegistryManager.setLocalRegistryPath()}
     */
    private static boolean usingLocalCategoryRegistry = false;

    private static String localRegistryPath = System.getProperty("user.home") + "/.talend/dataquality/semantic";

    public static final String CATEGORY_SUBFOLDER_NAME = "category";

    public static final String DICTIONARY_SUBFOLDER_NAME = "index/dictionary";

    public static final String KEYWORD_SUBFOLDER_NAME = "index/keyword";

    public static final String REGEX_SUBFOLDER_NAME = "regex";

    public static final String REGEX_CATEGRIZER_FILE_NAME = "categorizer.json";

    /**
     * Map between category ID and the object containing its metadata.
     */
    private Map<String, DQCategory> dqCategories = new LinkedHashMap<>();

    private String contextName;

    private UserDefinedClassifier udc;

    private LocalDictionaryCache localDictionaryCache;

    private static final Object indexExtractionLock = new Object();

    private CategoryRegistryManager() {
        this("default");
    }

    private CategoryRegistryManager(String contextName) {
        this.contextName = contextName;

        if (dqCategories.isEmpty()) {
            try {
                if (usingLocalCategoryRegistry) {
                    loadRegisteredCategories();
                } else {
                    loadInitialCategories();
                }
            } catch (IOException | URISyntaxException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

    public static CategoryRegistryManager getInstance() {
        return getInstance("default");
    }

    public static synchronized CategoryRegistryManager getInstance(String contextName) {
        if (instances.get(contextName) == null) {
            instances.put(contextName, new CategoryRegistryManager(contextName));
        }
        return instances.get(contextName);
    }

    public void reset() {
        setUsingLocalCategoryRegistry(false);
        instances.clear();
    }

    private static void setUsingLocalCategoryRegistry(boolean b) {
        usingLocalCategoryRegistry = b;
    }

    /**
     * Configure the local category registry path.
     * 
     * @param folder the folder to contain the category registry.
     */
    public static void setLocalRegistryPath(String folder) {
        if (folder != null && folder.trim().length() > 0) {
            localRegistryPath = folder;
            usingLocalCategoryRegistry = true;
            getInstance();
        } else {
            LOGGER.warn("Cannot set an empty path as local registy location. Use default one: " + localRegistryPath);
        }
    }

    /**
     * @return the path of local registry.
     */
    public static String getLocalRegistryPath() {
        return localRegistryPath;
    }

    /**
     * @return the {@link LocalDictionaryCache} corresponding to the current context.
     */
    public LocalDictionaryCache getDictionaryCache() {
        if (localDictionaryCache == null) {
            return new LocalDictionaryCache(contextName);
        }
        return localDictionaryCache;
    }

    /**
     * Reload the category from local registry. This method is typically called following category or dictionary enrichments.
     */
    public void reloadCategoriesFromRegistry() {
        LOGGER.info("Reload categories from local registry.");
        File categorySubFolder = new File(
                localRegistryPath + File.separator + contextName + File.separator + CATEGORY_SUBFOLDER_NAME);
        if (categorySubFolder.exists()) {
            dqCategories.clear();
            try {
                final Directory indexDir = FSDirectory.open(categorySubFolder);
                final DirectoryReader reader = DirectoryReader.open(indexDir);

                fillDqCategoriesMap(reader);

                reader.close();
                indexDir.close();
            } catch (IOException e) {
                LOGGER.error("Error while reloading categories from local registry.", e);
            }
        }
    }

    private void loadRegisteredCategories() throws IOException, URISyntaxException {
        // read local DD categories
        LOGGER.info("Loading categories from local registry.");
        final File categorySubFolder = new File(
                localRegistryPath + File.separator + contextName + File.separator + CATEGORY_SUBFOLDER_NAME);
        loadBaseIndex(categorySubFolder, CATEGORY_SUBFOLDER_NAME);
        if (categorySubFolder.exists()) {
            try (final DirectoryReader reader = DirectoryReader.open(FSDirectory.open(categorySubFolder))) {
                fillDqCategoriesMap(reader);

            }
        }

        // extract initial DD categories if not present
        final File dictionarySubFolder = new File(
                localRegistryPath + File.separator + contextName + File.separator + DICTIONARY_SUBFOLDER_NAME);
        loadBaseIndex(dictionarySubFolder, DICTIONARY_SUBFOLDER_NAME);

        // extract initial KW categories if not present
        final File keywordSubFolder = new File(
                localRegistryPath + File.separator + contextName + File.separator + KEYWORD_SUBFOLDER_NAME);
        loadBaseIndex(keywordSubFolder, KEYWORD_SUBFOLDER_NAME);

        // read local RE categories
        final File regexRegistryFolder = new File(
                localRegistryPath + File.separator + contextName + File.separator + REGEX_SUBFOLDER_NAME);
        if (!regexRegistryFolder.exists()) {
            // load provided RE into registry
            InputStream is = CategoryRecognizer.class.getResourceAsStream(REGEX_CATEGRIZER_FILE_NAME);
            StringBuilder sb = new StringBuilder();
            for (String line : IOUtils.readLines(is)) {
                sb.append(line);
            }
            JSONObject obj = new JSONObject(sb.toString());
            JSONArray array = obj.getJSONArray("classifiers");
            regexRegistryFolder.mkdirs();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(regexRegistryFolder + File.separator + REGEX_CATEGRIZER_FILE_NAME);
                IOUtils.write(array.toString(2), fos);
            } finally {
                if (fos != null) {
                    fos.close();
                }
            }
        }
    }

    private void loadInitialCategories() throws IOException, URISyntaxException {
        try (final DirectoryReader reader = DirectoryReader.open(ClassPathDirectory.open(getMetadataURI()))) {
            fillDqCategoriesMap(reader);
        }
    }

    /**
     * Fill dqCategories which contains for each category ID, the metadata with all the children and the parents.
     *
     * @param reader
     * @throws IOException
     */
    private void fillDqCategoriesMap(DirectoryReader reader) throws IOException {
        Map<String, Set<String>> categoryToParents = new HashMap<>();
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        // add the categories
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (liveDocs != null && !liveDocs.get(i)) {
                continue;
            }
            Document doc = reader.document(i);
            DQCategory dqCat = DictionaryUtils.categoryFromDocument(doc);
            dqCategories.put(dqCat.getId(), dqCat);
            if (!CollectionUtils.isEmpty(dqCat.getChildren()))
                fillCategoryToParents(categoryToParents, dqCat);
        }

        // add the parent references in the child
        for (Map.Entry<String, Set<String>> entry : categoryToParents.entrySet())
            if (dqCategories.get(entry.getKey()) != null) {
                List<DQCategory> parents = new ArrayList<>();
                for (String child : entry.getValue())
                    parents.add(new DQCategory(child));
                dqCategories.get(entry.getKey()).setParents(parents);
            }
    }

    /**
     * fill the map child -> parents
     * 
     * @param categoryToParents, the map child -> parents
     * @param dqCat, the category used to create the reference child -> parents
     */
    private void fillCategoryToParents(Map<String, Set<String>> categoryToParents, DQCategory dqCat) {
        for (DQCategory cat : dqCat.getChildren()) {
            if (categoryToParents.get(cat.getId()) == null)
                categoryToParents.put(cat.getId(), new HashSet<String>());
            categoryToParents.get(cat.getId()).add(dqCat.getId());
        }
    }

    private void loadBaseIndex(final File destSubFolder, String sourceSubFolder) throws IOException, URISyntaxException {
        synchronized (indexExtractionLock) {
            if (!destSubFolder.exists()) {
                final URI indexSourceURI = this.getClass().getResource("/" + sourceSubFolder).toURI();
                try (final Directory srcDir = ClassPathDirectory.open(indexSourceURI)) {
                    if (usingLocalCategoryRegistry && !destSubFolder.exists()) {
                        DictionaryUtils.rewriteIndex(srcDir, destSubFolder);
                    }
                }
            }
        }
    }

    /**
     * List all categories.
     * 
     * @return collection of category objects
     */
    public Collection<DQCategory> listCategories() {
        return dqCategories.values();
    }

    /**
     * List all categories.
     * 
     * @param includeOpenCategories whether include incomplete categories
     * @return collection of category objects
     */
    public Collection<DQCategory> listCategories(boolean includeOpenCategories) {
        if (includeOpenCategories) {
            return dqCategories.values();
        } else {
            List<DQCategory> catList = new ArrayList<>();
            for (DQCategory dqCat : dqCategories.values()) {
                if (dqCat.getCompleteness()) {
                    catList.add(dqCat);
                }
            }
            return catList;
        }
    }

    /**
     * List all categories of a given {@link CategoryType}.
     * 
     * @param type the given category type
     * @return collection of category objects of the given type
     */
    public List<DQCategory> listCategories(CategoryType type) {
        List<DQCategory> catList = new ArrayList<>();
        for (DQCategory dqCat : dqCategories.values()) {
            if (type.equals(dqCat.getType())) {
                catList.add(dqCat);
            }
        }
        return catList;
    }

    /**
     * Get the label of a category by its functional ID.
     * 
     * @param catId the category ID
     * @return the category label
     */
    public String getCategoryLabel(String catId) {
        if ("".equals(catId)) {
            return "";
        }
        return getCategoryMetadataByName(catId).getLabel();
    }

    /**
     * Get the full map between category ID and category metadata.
     */
    public Map<String, DQCategory> getCategoryMetadataMap() {
        return dqCategories;
    }

    /**
     * Get the category object by its technical ID.
     * 
     * @param catId the technical ID of the category
     * @return the category object
     */
    public DQCategory getCategoryMetadataById(String catId) {
        return dqCategories.get(catId);
    }

    /**
     * Get the category object by its functional ID (aka. name).
     * 
     * @param catName the functional ID (aka. name)
     * @return the category object
     */
    public DQCategory getCategoryMetadataByName(String catName) {
        for (DQCategory cat : dqCategories.values()) {
            if (cat.getName().equals(catName)) {
                return cat;
            }
        }
        return null;
    }

    /**
     * Get the category IDs.
     *
     * @return the category IDs
     */
    public Set<String> getCategoryIds() {
        return dqCategories.keySet();
    }

    /**
     * get instance of UserDefinedClassifier
     * 
     * @param refresh whether classifiers should be reloaded from local json file
     */
    public UserDefinedClassifier getRegexClassifier(boolean refresh) throws IOException {
        if (!usingLocalCategoryRegistry)
            return UDCategorySerDeser.getRegexClassifier();

        // load regexes from local registry
        if (udc == null || refresh) {
            final File regexRegistryFile = new File(localRegistryPath + File.separator + contextName + File.separator
                    + REGEX_SUBFOLDER_NAME + File.separator + REGEX_CATEGRIZER_FILE_NAME);

            if (!regexRegistryFile.exists()) {
                regexRegistryFile.getParentFile().mkdirs();
                // load provided RE into registry
                InputStream is = CategoryRecognizer.class.getResourceAsStream(REGEX_CATEGRIZER_FILE_NAME);
                StringBuilder sb = new StringBuilder();
                for (String line : IOUtils.readLines(is)) {
                    sb.append(line);
                }
                JSONObject obj = new JSONObject(sb.toString());
                JSONArray array = obj.getJSONArray("classifiers");
                FileOutputStream fos = new FileOutputStream(regexRegistryFile);
                IOUtils.write(array.toString(2), fos);
                fos.close();
            }

            udc = UDCategorySerDeser.readJsonFile(regexRegistryFile.toURI());
        }
        return udc;
    }

    /**
     * get URI of local category metadata
     */
    public URI getMetadataURI() throws URISyntaxException {
        if (usingLocalCategoryRegistry) {
            return Paths.get(localRegistryPath, contextName, CATEGORY_SUBFOLDER_NAME).toUri();
        } else {
            return CategoryRecognizerBuilder.class.getResource(CategoryRecognizerBuilder.DEFAULT_METADATA_PATH).toURI();
        }
    }

    /**
     * get URI of local dictionary index
     */
    public URI getDictionaryURI() throws URISyntaxException {
        if (usingLocalCategoryRegistry) {
            return Paths.get(localRegistryPath, contextName, DICTIONARY_SUBFOLDER_NAME).toUri();
        } else {
            return CategoryRecognizerBuilder.class.getResource(CategoryRecognizerBuilder.DEFAULT_DD_PATH).toURI();
        }
    }

    /**
     * get URI of local keyword index
     */
    public URI getKeywordURI() throws URISyntaxException {
        if (usingLocalCategoryRegistry) {
            return Paths.get(localRegistryPath, contextName, KEYWORD_SUBFOLDER_NAME).toUri();
        } else {
            return CategoryRecognizerBuilder.class.getResource(CategoryRecognizerBuilder.DEFAULT_KW_PATH).toURI();
        }
    }

    /**
     * get URI of local regexes
     */
    public URI getRegexURI() throws URISyntaxException {
        if (usingLocalCategoryRegistry) {
            return Paths.get(localRegistryPath, contextName, REGEX_SUBFOLDER_NAME, REGEX_CATEGRIZER_FILE_NAME).toUri();
        } else {
            return CategoryRecognizerBuilder.class.getResource(CategoryRecognizerBuilder.DEFAULT_RE_PATH).toURI();
        }
    }
}
