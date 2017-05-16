package org.talend.dataquality.semantic.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.talend.dataquality.common.inference.Analyzer;
import org.talend.dataquality.common.inference.Analyzers;
import org.talend.dataquality.common.inference.ValueQualityStatistics;
import org.talend.dataquality.common.inference.Analyzers.Result;
import org.talend.dataquality.semantic.index.utils.DictionaryGenerationSpec;
import org.talend.dataquality.semantic.index.utils.SemanticDictionaryGenerator;
import org.talend.dataquality.semantic.recognizer.CategoryRecognizerBuilder;

public class SemanticQualityAnalyzerPerformanceTest {

    private static CategoryRecognizerBuilder builder;

    private static int RECORD_LINES_NUMBER = 500000;

    private static final String BIG_FILE_PATH = "src/test/resources/org/talend/dataquality/semantic/statistics/validation_big_file.csv";

    private static final List<String[]> RECORDS_CRM_CUST = getRecords("frCities.csv");

    private static final String[] EXPECTED_CATEGORIES_DICT = new String[] { //
            "CITY_FR_COMPOUND", //CITY_FR_COMPOUND or FR_COMMUNE
            DictionaryGenerationSpec.CIVILITY.getCategoryName(), //
            DictionaryGenerationSpec.CONTINENT.getCategoryName(), //
            DictionaryGenerationSpec.COUNTRY.getCategoryName(), //
            DictionaryGenerationSpec.COUNTRY_CODE_ISO3.getCategoryName(), //
            DictionaryGenerationSpec.MONTH.getCategoryName(), //
            DictionaryGenerationSpec.US_COUNTY.getCategoryName(), //
            DictionaryGenerationSpec.FR_COMMUNE.getCategoryName(), //
            DictionaryGenerationSpec.FR_DEPARTEMENT.getCategoryName(), //
            DictionaryGenerationSpec.LANGUAGE.getCategoryName() //
    };

    private static final long[][] EXPECTED_VALIDITY_COUNT_DICT = new long[][] { //
            new long[] { 9944, 0, 0 }, //
            new long[] { 9944, 0, 0 }, //
            new long[] { 9944, 0, 0 }, //
            new long[] { 9944, 0, 0 }, //
            new long[] { 9944, 0, 0 }, //
            new long[] { 9944, 0, 0 }, //
            new long[] { 9943, 1, 0 }, //
            new long[] { 9943, 0, 0 }, //
            new long[] { 9943, 0, 0 }, //
            new long[] { 9943, 0, 0 }, //
    };

    @BeforeClass
    public static void setupBuilder() throws URISyntaxException {
        final URI ddPath = SemanticQualityAnalyzerPerformanceTest.class.getResource(CategoryRecognizerBuilder.DEFAULT_DD_PATH)
                .toURI();
        final URI kwPath = SemanticQualityAnalyzerPerformanceTest.class.getResource(CategoryRecognizerBuilder.DEFAULT_KW_PATH)
                .toURI();
        builder = CategoryRecognizerBuilder.newBuilder() //
                .ddPath(ddPath) //
                .kwPath(kwPath) //
                .lucene();
    }

    @Test
    @Ignore
    public void testSemanticQualityAnalyzerWithDictionaryCategory() {
        testAnalysis(RECORDS_CRM_CUST, EXPECTED_CATEGORIES_DICT, EXPECTED_VALIDITY_COUNT_DICT);
    }

    public void testAnalysis(List<String[]> records, String[] expectedCategories, long[][] expectedValidityCount) {
        Analyzer<Result> analyzers = Analyzers.with(//
                new SemanticQualityAnalyzer(builder, expectedCategories)//
        );

        long time = System.currentTimeMillis();
        for(int i=0;i<10;i++)
        for (String[] record : records) {
            analyzers.analyze(record);
        }
        System.out.println("Result = " + (System.currentTimeMillis() - time) + " ms");
    }

    private static List<String[]> getRecords(String path) {
        List<String[]> records = new ArrayList<String[]>();
        try {
            Reader reader = new FileReader(SemanticQualityAnalyzerPerformanceTest.class.getResource(path).getPath());
            CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(';');
            Iterable<CSVRecord> csvRecords = csvFormat.parse(reader);

            for (CSVRecord csvRecord : csvRecords) {
                String[] values = new String[csvRecord.size()];
                for (int i = 0; i < csvRecord.size(); i++) {
                    values[i] = csvRecord.get(i);
                }
                records.add(values);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }


    public static List<String> getFile(DictionaryGenerationSpec spec) throws IOException {

        Reader reader = new FileReader(
                SemanticQualityAnalyzerPerformanceTest.class.getResource("../index/utils/" + spec.getSourceFile()).getPath());
        CSVFormat csvFormat = CSVFormat.DEFAULT.withDelimiter(spec.getCsvConfig().getDelimiter());
        if (spec.getCsvConfig().isWithHeader()) {
            csvFormat = csvFormat.withFirstRecordAsHeader();
        }

        // collect values
        Iterable<CSVRecord> records = csvFormat.parse(reader);
        List<Set<String>> valueSetList = SemanticDictionaryGenerator.getDictionaryForCategory(records, spec);

        List<String> result = new ArrayList<>();
        for (Set<String> valueSet : valueSetList) {
            if (valueSet.iterator().hasNext())
                result.add(valueSet.iterator().next());
        }
        return result;
    }

}
