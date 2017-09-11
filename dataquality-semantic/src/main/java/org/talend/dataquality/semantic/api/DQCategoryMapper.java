package org.talend.dataquality.semantic.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.talend.dataquality.semantic.api.DictionaryConstants;
import org.talend.dataquality.semantic.model.DQCategory;

/**
 * Created by jteuladedenantes on 29/06/17.
 */
public class DQCategoryMapper {

    private static final String EMPTY_STRING = "";

    private DQCategoryMapper() {
    }

    public static List<Field> fromDQCategorytoFields(DQCategory category) {
        List<Field> fields = new ArrayList<>();
        fields.add(new StringField(DictionaryConstants.ID, category.getId(), Field.Store.YES));
        fields.add(new StringField(DictionaryConstants.NAME, category.getName(), Field.Store.YES));
        fields.add(new TextField(DictionaryConstants.LABEL,
                category.getLabel() == null ? category.getName() : category.getLabel(), Field.Store.YES));
        fields.add(new StringField(DictionaryConstants.TYPE, category.getType().name(), Field.Store.YES));
        fields.add(new TextField(DictionaryConstants.DESCRIPTION,
                category.getDescription() == null ? EMPTY_STRING : category.getDescription(), Field.Store.YES));
        fields.add(new StringField(DictionaryConstants.COMPLETENESS, String.valueOf(category.getCompleteness().booleanValue()),
                Field.Store.YES));
        if (category.getValidationMode() != null)
            fields.add(
                    new StringField(DictionaryConstants.VALIDATION_MODE, category.getValidationMode().name(), Field.Store.YES));
        return fields;

    }

    public static Document fromDQCategoryToLuceneDocument(DQCategory category) {
        Document luceneDoc = new Document();

        luceneDoc.add(new StringField(DictionaryConstants.ID, category.getId(), Field.Store.YES));
        luceneDoc.add(new StringField(DictionaryConstants.NAME, category.getName(), Field.Store.YES));
        luceneDoc.add(new TextField(DictionaryConstants.LABEL,
                category.getLabel() == null ? category.getName() : category.getLabel(), Field.Store.YES));
        luceneDoc.add(new StringField(DictionaryConstants.TYPE, category.getType().name(), Field.Store.YES));
        luceneDoc.add(new TextField(DictionaryConstants.DESCRIPTION,
                category.getDescription() == null ? EMPTY_STRING : category.getDescription(), Field.Store.YES));
        luceneDoc.add(new StringField(DictionaryConstants.COMPLETENESS, String.valueOf(category.getCompleteness().booleanValue()),
                Field.Store.YES));
        if (category.getValidationMode() != null)
            luceneDoc.add(
                    new StringField(DictionaryConstants.VALIDATION_MODE, category.getValidationMode().name(), Field.Store.YES));
        return luceneDoc;
    }
}
