package org.talend.dataquality.semantic.api;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.model.DQCategory;

public class DictionaryContextTest {

    private static final Logger LOGGER = Logger.getLogger(DictionaryContextTest.class);

    @Test
    public void test() throws URISyntaxException, IOException {

        DQCategory animalCategory = new DQCategory(SemanticCategoryEnum.ANIMAL.getTechnicalId());
        animalCategory.setName(SemanticCategoryEnum.ANIMAL.name());

        DQCategory answerCategory = new DQCategory(SemanticCategoryEnum.ANSWER.getTechnicalId());
        answerCategory.setName(SemanticCategoryEnum.ANSWER.name());

        CategoryRegistryManager.setLocalRegistryPath("target/local_registry/DictionaryContextTest");

        CategoryRegistryManager crm1 = CategoryRegistryManager.getInstance("Tenant_001");

        CategoryRegistryManager crm2 = CategoryRegistryManager.getInstance("Tenant_002");

        Map<String, DQCategory> map1 = null, map2 = null;
        try (LocalCategoryIndexAccess access1 = new LocalCategoryIndexAccess(crm1.getMetadataURI());
                LocalCategoryIndexAccess access2 = new LocalCategoryIndexAccess(crm2.getMetadataURI());) {

            access1.deleteCategory(animalCategory);
            access1.commitChanges();
            access2.deleteCategory(animalCategory);
            access2.deleteCategory(answerCategory);
            access2.commitChanges();

            map1 = crm1.getCategoryMetadataMap();
            map2 = crm2.getCategoryMetadataMap();
            System.out.println("tenant 1: " + map1.keySet().size() + "   tenant 2: " + map2.keySet().size());

            crm1.reloadCategoriesFromRegistry();
            map1 = crm1.getCategoryMetadataMap();
            map2 = crm2.getCategoryMetadataMap();
            System.out.println("tenant 1: " + map1.keySet().size() + "   tenant 2: " + map2.keySet().size());

            crm2.reloadCategoriesFromRegistry();
            map1 = crm1.getCategoryMetadataMap();
            map2 = crm2.getCategoryMetadataMap();
            System.out.println("tenant 1: " + map1.keySet().size() + "   tenant 2: " + map2.keySet().size());
        } catch (Exception e) {

        }
    }
}
