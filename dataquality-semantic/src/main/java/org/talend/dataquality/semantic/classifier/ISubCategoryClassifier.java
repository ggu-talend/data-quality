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
package org.talend.dataquality.semantic.classifier;

import org.talend.dataquality.semantic.model.DQCategory;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Created by sizhaoliu on 16.03.15.
 */
public interface ISubCategoryClassifier extends Serializable {

    /**
     * Return a set of category IDs
     * 
     * @param data the string that helps to classify
     * @param sharedCategories
     *@param tenantCategories @return the category IDs found for this string
     */
    public Set<String> classify(String data, List<String> sharedCategories, List<String> tenantCategories);

    boolean validCategories(String value, DQCategory semanticType, Set<DQCategory> children);
}
