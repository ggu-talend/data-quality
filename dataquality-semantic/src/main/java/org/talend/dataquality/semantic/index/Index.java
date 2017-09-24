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
package org.talend.dataquality.semantic.index;

import java.util.List;
import java.util.Set;

import org.talend.dataquality.semantic.model.DQCategory;

/**
 * created by talend on 2015-07-28 Detailled comment.
 */
public interface Index {

    void initIndex();

    void closeIndex();

    Set<String> findCategories(String data);

    boolean validCategories(String data, DQCategory semanticType, Set<DQCategory> children);

    void setCategoriesToSearch(List<String> categoryIds);
}
