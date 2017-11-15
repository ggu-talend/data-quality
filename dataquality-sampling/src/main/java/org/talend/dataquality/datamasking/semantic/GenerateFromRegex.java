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

import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.talend.dataquality.datamasking.functions.Function;

import com.mifmif.common.regex.Generex;

/**
 * Generate masking data from regex str
 */
public class GenerateFromRegex extends Function<String> {

    private static final long serialVersionUID = 2315410175790920472L;

    protected Generex generex = null;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.datamasking.functions.Function#doGenerateMaskedField(java.lang.Object)
     */
    @Override
    protected String doGenerateMaskedField(String inputValue) {
        if (keepNull && inputValue == null) {
            return null;
        }
        if (StringUtils.isEmpty(inputValue)) {
            return EMPTY_STRING;
        }
        return generex.random();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.datamasking.functions.Function#parse(java.lang.String, boolean, java.util.Random)
     */
    @Override
    public void parse(String extraParameter, boolean keepNullValues, Random rand) {
        if (extraParameter != null) {
            generex = new Generex(extraParameter);
            setKeepNull(keepNullValues);
            if (rand != null) {
                setRandom(rand);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.datamasking.functions.Function#setRandom(java.util.Random)
     */
    @Override
    public void setRandom(Random rand) {
        super.setRandom(rand);
        if (generex != null) {
            generex.setSeed(rnd.nextLong());
        }
    }
}
