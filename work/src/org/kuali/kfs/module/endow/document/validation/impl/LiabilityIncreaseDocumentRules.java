/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.endow.document.validation.impl;

import org.kuali.kfs.module.endow.businessobject.EndowmentSourceTransactionLine;
import org.kuali.kfs.module.endow.businessobject.EndowmentTransactionLine;
import org.kuali.kfs.module.endow.document.EndowmentTaxLotLinesDocumentBase;
import org.kuali.kfs.module.endow.document.EndowmentTransactionLinesDocument;
import org.kuali.kfs.module.endow.document.LiabilityIncreaseDocument;
import org.kuali.kfs.module.endow.document.service.LiabilityDocumentService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

public class LiabilityIncreaseDocumentRules extends EndowmentTransactionLinesDocumentBaseRules {


    /**
     * @see org.kuali.kfs.module.endow.document.validation.AddTransactionLineRule#processAddTransactionLineRules(org.kuali.kfs.module.endow.document.EndowmentTransactionLinesDocument,
     *      org.kuali.kfs.module.endow.businessobject.EndowmentTransactionLine)
     */
    @Override
    public boolean processAddTransactionLineRules(EndowmentTransactionLinesDocument transLineDocument, EndowmentTransactionLine line) {
        boolean isValid = true;

        String ERROR_PREFIX = getErrorPrefix(line, -1);

        isValid &= validateSecurity(isValid, (LiabilityIncreaseDocument) transLineDocument, false);

        isValid &= validateRegistration(isValid, (LiabilityIncreaseDocument) transLineDocument, false);

        if (isValid) {
            isValid &= super.processAddTransactionLineRules(transLineDocument, line);
        }

        if (!isValid)
            return isValid;

        LiabilityDocumentService taxLotsService = SpringContext.getBean(LiabilityDocumentService.class);
        LiabilityIncreaseDocument liabilityIncreaseDocument = (LiabilityIncreaseDocument) transLineDocument;
        boolean isSource = line instanceof EndowmentSourceTransactionLine ? true : false;
        taxLotsService.updateLiabilityIncreaseTransactionLineTaxLots(isSource, (EndowmentTaxLotLinesDocumentBase) liabilityIncreaseDocument, line);

        return GlobalVariables.getMessageMap().getErrorCount() == 0;
    }

    /**
     * This method validates the Tx line but from Liability Increase perspective.
     * 
     * @param endowmentTransactionLinesDocumentBase
     * @param line
     * @param index
     * @return
     */
    protected boolean validateTransactionLine(EndowmentTransactionLinesDocument endowmentTransactionLinesDocument, EndowmentTransactionLine line, int index) {
        boolean isValid = true;

        isValid &= super.validateTransactionLine(endowmentTransactionLinesDocument, line, index);

        if (isValid) {
            // Obtain Prefix for Error fields in UI.
            String ERROR_PREFIX = getErrorPrefix(line, index);

            // Ensure for cash Tx do not have a Etran.
            isValid &= checkCashTransactionEndowmentCode(endowmentTransactionLinesDocument, line, ERROR_PREFIX);

            if (endowmentTransactionLinesDocument.isErrorCorrectedDocument()) {
                // Validate Amount is Less than Zero.
                isValid &= validateTransactionAmountLessThanZero(line, ERROR_PREFIX);

                // Validate Units is Less than Zero.
                isValid &= validateTransactionUnitsLessThanZero(line, ERROR_PREFIX);
            }
            else {
                // Validate Amount is Greater than Zero.
                isValid &= validateTransactionAmountGreaterThanZero(line, ERROR_PREFIX);

                // Validate Units is Greater than Zero.
                isValid &= validateTransactionUnitsGreaterThanZero(line, ERROR_PREFIX);
            }

            // Validates Units & Amount are equal.
            isValid &= validateTransactionUnitsAmountEqual(line, ERROR_PREFIX);
        }

        return GlobalVariables.getMessageMap().getErrorCount() == 0;
    }

    /**
     * @see org.kuali.rice.krad.rules.DocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.rice.krad.document.Document)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        boolean isValid = super.processCustomRouteDocumentBusinessRules(document);

        LiabilityIncreaseDocument liabilityIncreaseDocument = (LiabilityIncreaseDocument) document;

        if (isValid) {
            // Validate Security
            isValid &= validateSecurity(isValid, liabilityIncreaseDocument, false);

            // Validate Registration code.
            isValid &= validateRegistration(isValid, liabilityIncreaseDocument, false);

            if (!isValid)
                return isValid;

            // Empty out the Source Tx Line in weird case they got entered.
            liabilityIncreaseDocument.getSourceTransactionLines().clear();

            // Validate atleast one Tx was entered.
            if (!transactionLineSizeGreaterThanZero(liabilityIncreaseDocument, false))
                return false;

            isValid &= super.processCustomSaveDocumentBusinessRules(document);
        }

        return GlobalVariables.getMessageMap().getErrorCount() == 0;
    }

}
