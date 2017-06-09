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

/**
 * create by zshen rule definition of conflict resolve
 */
public class ConflictRuleDefinition extends RuleDefinition {

    private String fillColumn;

    private boolean duplicateSurCheck;

    private boolean disableRule;

    /**
     * create by zshen constructor of ConflictRuleDefinition .
     * 
     * @param order The type of rule
     * @param ruleName The name of rule
     * @param referenceColumn The name of reference column
     * @param function The action of current rule
     * @param operation The parameter of function
     * @param targetColumn The name of target column
     * @param ignoreBlanks Whether ignore blanks case in the rule
     * @param fillColumn The name of fill column
     * @param duplicateSurCheck Whether deal with duplicate case in the survived value
     */
    public ConflictRuleDefinition(Order order, String ruleName, String referenceColumn, Function function, String operation,
            String targetColumn, boolean ignoreBlanks, String fillColumn, boolean duplicateSurCheck) {
        super(order, ruleName, referenceColumn, function, operation, targetColumn, ignoreBlanks);
        this.fillColumn = fillColumn;
        this.duplicateSurCheck = duplicateSurCheck;
        this.disableRule = false;
    }

    /**
     * create by zshen constructor of ConflictRuleDefinition .
     * 
     * @param order The type of rule
     * @param ruleName The name of rule
     * @param referenceColumn The name of reference column
     * @param function The action of current rule
     * @param operation The parameter of function
     * @param targetColumn The name of target column
     * @param ignoreBlanks Whether ignore blanks case in the rule
     * @param fillColumn The name of fill column
     * @param duplicateSurCheck Whether deal with duplicate case in the survived value
     */
    public ConflictRuleDefinition(Order order, String ruleName, String referenceColumn, Function function, String operation,
            String targetColumn, boolean ignoreBlanks, String fillColumn, boolean duplicateSurCheck, boolean disableRule) {
        super(order, ruleName, referenceColumn, function, operation, targetColumn, ignoreBlanks);
        this.fillColumn = fillColumn;
        this.duplicateSurCheck = duplicateSurCheck;
        this.disableRule = disableRule;
    }

    /**
     * Getter for fillColumn.
     * 
     * @return the fillColumn
     */
    public String getFillColumn() {
        return this.fillColumn;
    }

    /**
     * Getter for duplicateSurCheck.
     * 
     * @return the duplicateSurCheck
     */
    public boolean isDuplicateSurCheck() {
        return this.duplicateSurCheck;
    }

    /**
     * Getter for disableRule.
     * 
     * @return the disableRule
     */
    public boolean isDisableRule() {
        return this.disableRule;
    }

}
