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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * DOC zshen class global comment. Detailled comment
 */
public class GenerateFromRegexTest {

    /**
     * DOC zshen Comment method "setUpBeforeClass".
     * 
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * DOC zshen Comment method "tearDownAfterClass".
     * 
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * DOC zshen Comment method "setUp".
     * 
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * DOC zshen Comment method "tearDown".
     * 
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#setRandom(java.util.Random)}.
     */
    @Test
    public void testSetRandom() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.setRandom(null);
        Assert.assertTrue("setRandom method will not modify the value of regexFunction", regexFunction.generex == null); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#parse(java.lang.String, boolean, java.util.Random)}.
     * case 1 normal case
     */
    @Test
    public void testParseCase1() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}", true, new Random(100l));
        Assert.assertTrue("regexFunction.generex should not be null", regexFunction.generex != null);
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#parse(java.lang.String, boolean, java.util.Random)}.
     * case 2 extraParameter is null
     */
    @Test
    public void testParseCase2() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse(null, true, new Random(100l));
        Assert.assertTrue("regexFunction.generex should be null", regexFunction.generex == null); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 1 keepNull is true and inputValue is null
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase1() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}", true, new Random(100l)); //$NON-NLS-1$
        String maskResult = regexFunction.doGenerateMaskedField(null);
        Assert.assertEquals("maskResult should be null", null, maskResult); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 2 keepNull is true and inputValue is empty
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase2() {
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        regexFunction.parse("(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}", true, new Random(100l)); //$NON-NLS-1$
        String maskResult = regexFunction.doGenerateMaskedField(StringUtils.EMPTY);
        Assert.assertEquals("maskResult should be EMPTY", StringUtils.EMPTY, maskResult); //$NON-NLS-1$
    }

    /**
     * Test method for
     * {@link org.talend.dataquality.datamasking.semantic.GenerateFromRegex#doGenerateMaskedField(java.lang.String)}.
     * case 3 normal case
     */
    @Test
    public void testDoGenerateMaskedFieldStringCase3() {
        Long seed = 100l;
        GenerateFromRegex regexFunction = new GenerateFromRegex();
        String regexStr = "(0033 ?|\\+33 ?|0)[1-9]([-. ]?[0-9]{2}){4}"; //$NON-NLS-1$
        regexFunction.parse(regexStr, true, new Random(seed));
        String maskResult = regexFunction.doGenerateMaskedField("any not empty value");
        Pattern compile = java.util.regex.Pattern.compile(regexStr);
        Matcher matcher = compile.matcher(maskResult);
        Assert.assertTrue("maskResult is correct result", matcher.matches()); //$NON-NLS-1$

        // use same seed should get same result
        regexFunction = new GenerateFromRegex();
        regexFunction.parse(regexStr, true, new Random(seed));
        String secondMaskResult = regexFunction.doGenerateMaskedField("any not empty value");
        Assert.assertEquals("maskResult is correct result", maskResult, secondMaskResult); //$NON-NLS-1$
    }

}
