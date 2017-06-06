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
package org.talend.survivorship.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Create by zshen a sub dataset of original dataset
 */
public class SubDataSet extends DataSet {

    private List<Integer> dataSetIndex;

    private Map<Attribute, FilledAttribute> fillAttributeMap;

    /**
     * Create by zshen Create a new sub dataset.
     * 
     * @param dataSet original dataset
     * @param conflictDataIndexList index list all of conflict data
     */
    public SubDataSet(DataSet dataSet, List<Integer> conflictDataIndexList) {
        super(dataSet.getColumnList(), dataSet.getRecordList());
        copyConflictDataMap(dataSet);
        dataSetIndex = conflictDataIndexList;
        this.survivorIndexMap = dataSet.survivorIndexMap;
        this.setColumnOrder(dataSet.getColumnOrder());
    }

    /**
     * Copy conflict data map from the dataSet
     * 
     * @param dataSet
     */
    private void copyConflictDataMap(DataSet dataSet) {
        HashMap<String, List<Integer>> conflictDataMap = dataSet.getConflictDataMap().get();
        for (String columnName : conflictDataMap.keySet()) {
            for (Integer index : conflictDataMap.get(columnName)) {
                this.addConfDataIndex(columnName, index);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.survivorship.model.DataSet#getAttributesByColumn(java.lang.String)
     */
    @Override
    public Collection<Attribute> getAttributesByColumn(String columnName) {
        for (Column col : this.getColumnList()) {
            if (col.getName().equals(columnName)) {
                return col.getAttributesByFilter(dataSetIndex, this.fillAttributeMap);
            }
        }
        return null;
    }

    /**
     * Getter for dataSetIndex.
     * 
     * @return the dataSetIndex
     */
    public List<Integer> getDataSetIndex() {
        return this.dataSetIndex;
    }

    public void addFillAttributeMap(FilledAttribute filledAttribute) {
        if (fillAttributeMap == null) {
            fillAttributeMap = new HashMap<>();
        }
        fillAttributeMap.put(filledAttribute.getOrignalAttribute(), filledAttribute);
    }

}
