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
package org.talend.dataquality.semantic.statistics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.Analyzers.Result;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.api.CustomDictionaryHolder;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.model.CategoryType;
import org.talend.dataquality.semantic.model.DQCategory;
import org.talend.dataquality.semantic.model.DQDocument;
import org.talend.dataquality.semantic.model.DQRegEx;
import org.talend.dataquality.semantic.model.DQValidator;
import org.talend.dataquality.semantic.model.MainCategory;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

public class SemanticAnalyzerTest {

    private final List<String[]> TEST_RECORDS = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "veau" });
            add(new String[] { "United States" });
            add(new String[] { "France" });
        }
    };

    private final List<String[]> TEST_RECORDS_TAGADA = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "1", "Williams", "John", "40", "10/09/1940", "false" });
            add(new String[] { "2", "Bowie", "David", "67", "01/08/1947", "true" });
        }
    };

    private final List<String> EXPECTED_CATEGORY_TAGADA = Arrays.asList(
            new String[] { "", SemanticCategoryEnum.LAST_NAME.name(), SemanticCategoryEnum.FIRST_NAME.name(), "", "", "" });

    private final List<String[]> TEST_RECORDS_CITY_METADATA = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "1", "Washington" });
            add(new String[] { "2", "Washington" });
            add(new String[] { "3", "Washington" });
            add(new String[] { "4", "Washington" });
            add(new String[] { "5", "Washington" });
            add(new String[] { "6", "Washington" });
            add(new String[] { "7", "Washington" });
            add(new String[] { "8", "Washington" });
            add(new String[] { "9", "Washington" });
            add(new String[] { "10", "Washington" });
            add(new String[] { "11", "Washington" });
            add(new String[] { "12", "Washington" });
            add(new String[] { "13", "Washington" });
            add(new String[] { "14", "Washington" });
            add(new String[] { "15", "Washington" });
            add(new String[] { "16", "Washington" });
            add(new String[] { "17", "Washington" });
            add(new String[] { "18", "New York" });
            add(new String[] { "19", "+33688052266" });
            add(new String[] { "20", "+33688052266" });
        }
    };

    private final List<String> EXPECTED_FR_COMMUNE_CATEGORY_METADATA = Arrays
            .asList(new String[] { "", SemanticCategoryEnum.LAST_NAME.name() });

    private final List<String[]> TEST_RECORDS_PHONE_METADATA = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "08 25 01 20 11" });
            add(new String[] { "+33123456789" });
            add(new String[] { "+1 (555) 457-2154" });
            add(new String[] { "+509 7845 2156" });
        }
    };

    private final List<String> EXPECTED_PHONE_CATEGORY_METADATA = Arrays
            .asList(new String[] { SemanticCategoryEnum.PHONE.name() });

    private CategoryRecognizerBuilder builder;

    @Before
    public void setUp() throws Exception {
        builder = CategoryRecognizerBuilder.newBuilder().lucene();
    }

    @After
    public void reset() {
        CategoryRegistryManager.reset();
    }

    @Test
    public void testTagada() {
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder.getDictionaryConstituents());

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);
        analyzer.init();
        for (String[] record : TEST_RECORDS_TAGADA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        for (int i = 0; i < EXPECTED_CATEGORY_TAGADA.size(); i++) {
            Result result = analyzer.getResult().get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_CATEGORY_TAGADA.get(i), suggestedCategory);
            }
        }
    }

    @Test
    public void testTagadaWithCustomMetadata() {
        CategoryRegistryManager.setLocalRegistryPath("target/test_crm");
        CustomDictionaryHolder holder = CategoryRegistryManager.getInstance().getCustomDictionaryHolder("t_custom_meta");
        builder.tenantID("t_custom_meta");

        DQCategory firstNameCat = holder.getMetadata().get(SemanticCategoryEnum.FIRST_NAME.getTechnicalId());
        firstNameCat.setDeleted(true);
        holder.updateCategory(firstNameCat);

        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);
        analyzer.init();
        for (String[] record : TEST_RECORDS_TAGADA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        final List<String> EXPECTED_CATEGORIES = Arrays.asList(
                new String[] { "", SemanticCategoryEnum.LAST_NAME.name(), SemanticCategoryEnum.LAST_NAME.name(), "", "", "" });

        for (int i = 0; i < EXPECTED_CATEGORIES.size(); i++) {
            Result result = analyzer.getResult().get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_CATEGORIES.get(i), suggestedCategory);
            }
        }
        CategoryRegistryManager.getInstance().removeCustomDictionaryHolder("t_custom_meta");
    }

    @Test
    public void testTagadaWithCustomDataDict() {
        CategoryRegistryManager.setLocalRegistryPath("target/test_crm");
        CustomDictionaryHolder holder = CategoryRegistryManager.getInstance().getCustomDictionaryHolder("t_custom_dd");
        builder.tenantID("t_custom_dd");

        DQCategory answerCategory = holder.getMetadata().get(SemanticCategoryEnum.ANSWER.getTechnicalId());
        DQCategory categoryClone = SerializationUtils.clone(answerCategory); // make a clone instead of modifying the shared
                                                                             // category metadata
        categoryClone.setModified(true);
        holder.updateCategory(categoryClone);

        DQDocument newDoc = new DQDocument();
        newDoc.setCategory(categoryClone);
        newDoc.setId("the_doc_id");
        newDoc.setValues(new HashSet<>(Arrays.asList("true", "false")));
        holder.addDataDictDocument(Collections.singletonList(newDoc));

        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);
        analyzer.init();
        for (String[] record : TEST_RECORDS_TAGADA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        final List<String> EXPECTED_CATEGORIES = Arrays.asList(new String[] { "", SemanticCategoryEnum.LAST_NAME.name(),
                SemanticCategoryEnum.FIRST_NAME.name(), "", "", SemanticCategoryEnum.ANSWER.name() });

        for (int i = 0; i < EXPECTED_CATEGORIES.size(); i++) {
            Result result = analyzer.getResult().get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_CATEGORIES.get(i), suggestedCategory);
            }
        }
        CategoryRegistryManager.getInstance().removeCustomDictionaryHolder("t_custom_dd");
    }

    @Test
    public void testTagadaWithCustomRegex() {
        CategoryRegistryManager.setLocalRegistryPath("target/test_crm");
        CustomDictionaryHolder holder = CategoryRegistryManager.getInstance().getCustomDictionaryHolder("t_custom_re");
        builder.tenantID("t_custom_re");

        DQValidator dqValidator = new DQValidator();
        dqValidator.setPatternString("^(true|false)$");
        DQRegEx dqRegEx = new DQRegEx();
        dqRegEx.setMainCategory(MainCategory.Alpha);
        dqRegEx.setValidator(dqValidator);
        DQCategory dqCat = new DQCategory("the_id");
        dqCat.setName("the_name");
        dqCat.setLabel("the_label");
        dqCat.setDescription("the_description");
        dqCat.setRegEx(dqRegEx);
        dqCat.setType(CategoryType.REGEX);
        dqCat.setCompleteness(Boolean.TRUE);
        dqCat.setModified(Boolean.TRUE);
        holder.addRegexCategory(dqCat);

        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);
        analyzer.init();
        for (String[] record : TEST_RECORDS_TAGADA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        final List<String> EXPECTED_CATEGORIES = Arrays.asList(new String[] { "", SemanticCategoryEnum.LAST_NAME.name(),
                SemanticCategoryEnum.FIRST_NAME.name(), "", "", "the_name" });

        for (int i = 0; i < EXPECTED_CATEGORIES.size(); i++) {
            Result result = analyzer.getResult().get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_CATEGORIES.get(i), suggestedCategory);
            }
        }
        CategoryRegistryManager.getInstance().removeCustomDictionaryHolder("t_custom_re");
    }

    @Test
    public void firstNameToFRCommuneIgnoreCaseAndAccent() {
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);

        analyzer.init();
        semanticAnalyzer.setMetadata(Metadata.HEADER_NAME, Arrays.asList("", "Läst name"));

        // 85% last name
        // 90% city
        // and column name is city
        for (String[] record : TEST_RECORDS_CITY_METADATA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        List<Result> results = analyzer.getResult();
        for (int i = 0; i < EXPECTED_FR_COMMUNE_CATEGORY_METADATA.size(); i++) {
            Result result = results.get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_FR_COMMUNE_CATEGORY_METADATA.get(i), suggestedCategory);
            }
        }
    }

    @Test
    public void firstNameToFRCommune() {
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);

        analyzer.init();
        semanticAnalyzer.setMetadata(Metadata.HEADER_NAME, Arrays.asList("", "Last Name"));

        // 85% last name
        // 90% city
        // and column name is city
        for (String[] record : TEST_RECORDS_CITY_METADATA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        List<Result> results = analyzer.getResult();
        for (int i = 0; i < EXPECTED_FR_COMMUNE_CATEGORY_METADATA.size(); i++) {
            Result result = results.get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_FR_COMMUNE_CATEGORY_METADATA.get(i), suggestedCategory);
            }
        }
    }

    @Test
    public void metadataLastNameWithPhoneNumber() {
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);

        analyzer.init();
        semanticAnalyzer.setMetadata(Metadata.HEADER_NAME, Arrays.asList("Last Name"));

        for (String[] record : TEST_RECORDS_PHONE_METADATA) {
            analyzer.analyze(record);
        }
        analyzer.end();

        List<Result> results = analyzer.getResult();
        for (int i = 0; i < EXPECTED_PHONE_CATEGORY_METADATA.size(); i++) {
            Result result = results.get(i);

            if (result.exist(SemanticType.class)) {
                final SemanticType semanticType = result.get(SemanticType.class);
                final String suggestedCategory = semanticType.getSuggestedCategory();
                assertEquals("Unexpected Category.", EXPECTED_PHONE_CATEGORY_METADATA.get(i), suggestedCategory);
            }
        }
    }

    @Test
    public void testSetLimit() {

        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        semanticAnalyzer.setLimit(0);
        assertEquals("Unexpected Category.", SemanticCategoryEnum.COUNTRY.getId(), getSuggestedCategorys(semanticAnalyzer));

        semanticAnalyzer.setLimit(1);
        assertEquals("Unexpected Category.", SemanticCategoryEnum.ANIMAL.getId(), getSuggestedCategorys(semanticAnalyzer));

        semanticAnalyzer.setLimit(3);
        assertEquals("Unexpected Category.", SemanticCategoryEnum.COUNTRY.getId(), getSuggestedCategorys(semanticAnalyzer));
    }

    private String getSuggestedCategorys(SemanticAnalyzer semanticAnalyzer) {
        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);
        analyzer.init();
        for (String[] record : TEST_RECORDS) {
            analyzer.analyze(record);
        }
        analyzer.end();
        Result result = analyzer.getResult().get(0);

        if (result.exist(SemanticType.class)) {
            final SemanticType semanticType = result.get(SemanticType.class);
            final String suggestedCategory = semanticType.getSuggestedCategory();
            return suggestedCategory;
        }
        return null;
    }

}
