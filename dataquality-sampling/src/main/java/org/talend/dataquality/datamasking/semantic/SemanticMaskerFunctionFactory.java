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
package org.talend.dataquality.datamasking.semantic;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.talend.dataquality.datamasking.functions.DateVariance;
import org.talend.dataquality.datamasking.functions.Function;
import org.talend.dataquality.semantic.api.CategoryRegistryManager;
import org.talend.dataquality.semantic.model.CategoryType;

public class SemanticMaskerFunctionFactory {

    private static final Logger LOGGER = Logger.getLogger(SemanticMaskerFunctionFactory.class);

    public static Function<String> createMaskerFunctionForSemanticCategory(String semanticCategory, String dataType) {
        return createMaskerFunctionForSemanticCategory(semanticCategory, dataType, null);
    }

    @SuppressWarnings("unchecked")
    public static Function<String> createMaskerFunctionForSemanticCategory(String semanticCategory, String dataType,
            List<String> params) {
        Function<String> function = null;
        final MaskableCategoryEnum cat = MaskableCategoryEnum.getCategoryById(semanticCategory);
        if (cat != null) {
            try {
                function = (Function<String>) cat.getFunctionType().getClazz().newInstance();
                if (cat.getParameter() == null) {
                    function.parse("X", true, null); //$NON-NLS-1$
                } else {
                    function.parse(cat.getParameter(), true, null);
                }
                function.setKeepFormat(true);
            } catch (InstantiationException e) {
                LOGGER.debug(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }

        if ("string".equals(dataType)) {
            org.talend.dataquality.semantic.model.DQCategory category = CategoryRegistryManager.getInstance()
                    .getCategoryMetadataByName(semanticCategory);
            if (category != null && CategoryType.REGEX.equals(category.getType())) {
                function = new GenerateFromRegex();
                function.parse(semanticCategory, true, new Random(100l));
            }
        }

        if (function == null) {
            switch (dataType) {
            case "numeric":
            case "integer":
            case "float":
            case "double":
            case "decimal":
                function = new FluctuateNumericString();
                function.parse("10", true, null);
                break;
            case "date":
                DateVariance df = new DateVariance();
                df.parse("61", true, null);
                function = new DateFunctionAdapter(df, params);
                break;
            case "string":
                function = new ReplaceCharactersWithGeneration();
                function.parse("X", true, null);
                break;
            default:
                break;

            }
        }
        if (function == null) {
            throw new IllegalArgumentException("No masking function available for the current column! SemanticCategory: "
                    + semanticCategory + " DataType: " + dataType);
        }
        return function;
    }

}
