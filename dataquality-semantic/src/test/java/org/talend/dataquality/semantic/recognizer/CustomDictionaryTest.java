package org.talend.dataquality.semantic.recognizer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.talend.dataquality.semantic.broadcast.BroadcastDocumentObject;
import org.talend.dataquality.semantic.broadcast.BroadcastIndexObject;
import org.talend.dataquality.semantic.classifier.custom.UserDefinedClassifier;
import org.talend.dataquality.semantic.model.DQCategory;

public class CustomDictionaryTest {

    private static final String[] DICT_ENTRIES = new String[] { "1", "1234567", "1-2-3-4-5-6-7", "123.4567", "ABC-DEFG",
            "1234567A", "12€", "01", "2²", "1!", "2;", "3%", "4#", "#2", "_____;;;///" };

    private static final String CATEGOTY_ID = "CatId";

    private static final String CATEGOTY_NAME = "CatName";

    @Test
    public void testCustomDict() throws IOException {

        List<BroadcastDocumentObject> bdoList = new ArrayList<>();
        for (String entry : DICT_ENTRIES) {
            bdoList.add(new BroadcastDocumentObject(CATEGOTY_NAME, Collections.singleton(entry)));
        }
        final BroadcastIndexObject ddBio = new BroadcastIndexObject(bdoList);
        final BroadcastIndexObject kwBio = new BroadcastIndexObject(new ArrayList<>());

        final UserDefinedClassifier regexClassifier = new UserDefinedClassifier();

        Map<String, DQCategory> metadata = new HashMap<String, DQCategory>() {

            private static final long serialVersionUID = 1L;

            {
                DQCategory dqCat = new DQCategory(CATEGOTY_ID);
                dqCat.setName(CATEGOTY_NAME);
                put(CATEGOTY_NAME, dqCat);
            }
        };

        CategoryRecognizer catRecognizer = CategoryRecognizerBuilder.newBuilder().lucene().metadata(metadata)
                .ddDirectory(ddBio.asDirectory()).kwDirectory(kwBio.asDirectory()).regexClassifier(regexClassifier).build();

        for (String entry : DICT_ENTRIES) {
            String[] res = catRecognizer.process(entry);
            assertEquals(1, res.length);
            assertEquals(CATEGOTY_NAME, res[0]);
        }
    }

}
