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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.Analyzers.Result;
import org.talend.dataquality.common.inference.Metadata;
import org.talend.dataquality.semantic.api.CategoryRecognizerBuilderFactory;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.broadcast.TdqCategories;
import org.talend.dataquality.semantic.broadcast.TdqCategoriesFactory;
import org.talend.dataquality.semantic.classifier.SemanticCategoryEnum;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

public class SemanticAnalyzerTestWithTdqCategoriesFactory {

    final List<String[]> TEST_RECORDS = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "CHAT" });
            add(new String[] { "United States" });
            add(new String[] { "France" });
        }
    };

    final List<String[]> TEST_RECORDS_TAGADA = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "1", "Lennon", "John", "40", "10/09/1940", "false" });
            add(new String[] { "2", "Bowie", "David", "67", "01/08/1947", "true" });
        }
    };

    final List<String> EXPECTED_CATEGORY_TAGADA = Arrays
            .asList(new String[] { "", SemanticCategoryEnum.LAST_NAME.name(), SemanticCategoryEnum.FIRST_NAME.name(), "", "" });

    final List<String[]> TEST_RECORDS_CITY_METADATA = new ArrayList<String[]>() {

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
            add(new String[] { "9", "New York" });
            add(new String[] { "10", "+33688052266" });
        }
    };

    final List<String> EXPECTED_FR_COMMUNE_CATEGORY_METADATA = Arrays
            .asList(new String[] { "", SemanticCategoryEnum.LAST_NAME.name() });

    final List<String[]> TEST_RECORDS_PHONE_METADATA = new ArrayList<String[]>() {

        private static final long serialVersionUID = 1L;

        {
            add(new String[] { "08 25 01 20 11" });
            add(new String[] { "+33123456789" });
            add(new String[] { "+1 (555) 457-2154" });
            add(new String[] { "+509 7845 2156" });
        }
    };

    final List<String> EXPECTED_PHONE_CATEGORY_METADATA = Arrays.asList(new String[] { SemanticCategoryEnum.PHONE.name() });

    private CategoryRecognizerBuilder builder;

    @Before
    public void setUp() throws Exception {
        CategoryRecognizerBuilderFactory factory = new CategoryRecognizerBuilderFactory(
                CategoryRegistryManager.getInstance("default"));
        // builder = factory.createCategoryRecognizerBuilder();
    }

    @Test
    public void testTagada() {
        Set<String> catNames = new HashSet<>(
                Arrays.asList(new String[] { SemanticCategoryEnum.FIRST_NAME.name(), SemanticCategoryEnum.LAST_NAME.name() }));
        TdqCategories tdqCats = TdqCategoriesFactory.createTdqCategories("default", catNames);
        builder = CategoryRecognizerBuilder.newBuilder().metadata(tdqCats.getCategoryMetadata().getMetadata())
                .ddDirectory(tdqCats.getDictionary().asDirectory())//
                .kwDirectory(tdqCats.getKeyword().asDirectory()).regexClassifier(tdqCats.getRegex().getRegexClassifier());

        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

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
    public void firstNameToFRCommune() {
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(builder);

        Analyzer<Result> analyzer = Analyzers.with(semanticAnalyzer);

        analyzer.init();
        semanticAnalyzer.setMetadata(Metadata.HEADER_NAME, Arrays.asList("", "Last Name"));

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
