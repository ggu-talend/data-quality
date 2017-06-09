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
package org.talend.survivorship.action.handler;

/**
 * The class which used to remove duplicate survived value
 */
public class RemDupCRCRHandler extends DataCleanCRCRHandler {

    /**
     * The constructor of RemDupCRCRHandler class.
     * 
     * @param handlerParameter
     */
    public RemDupCRCRHandler(HandlerParameter handlerParameter) {
        super(handlerParameter);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.survivorship.action.handler.DataCleanCRCRHandler#canHandler(java.lang.Object, java.lang.String, int)
     */
    @Override
    protected boolean canHandler(Object inputData, String expression, int rowNum) {
        Object realValue = this.getHandlerParameter().getTarInputData((Object[]) inputData);
        if (!this.getHandlerParameter().getDataset().checkDupSurValue(realValue,
                this.handlerParameter.getRefColumn().getName())) {
            return true;
        }
        return false;
    }

}
