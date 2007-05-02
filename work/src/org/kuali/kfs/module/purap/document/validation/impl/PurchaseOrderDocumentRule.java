/*
 * Copyright 2006-2007 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.module.purap.rules;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.core.datadictionary.validation.fieldlevel.ZipcodeValidationPattern;
import org.kuali.core.util.ErrorMap;
import org.kuali.core.util.GlobalVariables;
import org.kuali.core.util.ObjectUtils;
import org.kuali.kfs.KFSConstants;
import org.kuali.kfs.KFSKeyConstants;
import org.kuali.kfs.util.SpringServiceLocator;
import org.kuali.module.purap.PurapConstants;
import org.kuali.module.purap.PurapKeyConstants;
import org.kuali.module.purap.PurapPropertyConstants;
import org.kuali.module.purap.PurapConstants.ItemTypeCodes;
import org.kuali.module.purap.bo.PurchaseOrderItem;
import org.kuali.module.purap.bo.PurchaseOrderVendorStipulation;
import org.kuali.module.purap.bo.PurchasingApItem;
import org.kuali.module.purap.document.PurchaseOrderDocument;
import org.kuali.module.purap.document.PurchasingAccountsPayableDocument;
import org.kuali.module.purap.document.PurchasingDocument;

public class PurchaseOrderDocumentRule extends PurchasingDocumentRuleBase {

    /**
     * Tabs included on Purchase Order Documents are: Stipulation
     * 
     * @see org.kuali.module.purap.rules.PurchasingAccountsPayableDocumentRuleBase#processValidation(org.kuali.module.purap.document.PurchasingAccountsPayableDocument)
     */
    @Override
    public boolean processValidation(PurchasingAccountsPayableDocument purapDocument) {
        boolean valid = super.processValidation(purapDocument);
        valid &= processVendorStipulationValidation((PurchaseOrderDocument) purapDocument);
        return valid;
    }

    /**
     * 
     * @see org.kuali.module.purap.rules.PurchasingDocumentRuleBase#processItemValidation(org.kuali.module.purap.document.PurchasingDocument)
     */
    @Override
    public boolean processItemValidation(PurchasingDocument purDocument) {
        boolean valid = super.processItemValidation(purDocument);
        for (PurchasingApItem item : purDocument.getItems()) {
            valid &= validateEmptyItemWithAccounts((PurchaseOrderItem) item);
            valid &= validateItemWithoutAccounts((PurchaseOrderItem) item);
            valid &= validateItemUnitOfMeasure((PurchaseOrderItem) item);
        }
        valid &= validateTradeInAndDiscountCoexistence(purDocument);
        return valid;
    }

    /**
     * 
     * This method validates that the item detail must not be empty if its account is not empty.
     * @param item
     * @return
     */
    private boolean validateEmptyItemWithAccounts(PurchaseOrderItem item) {
        boolean valid = true;
        if (item.isItemDetailEmpty() && !item.isAccountListEmpty()) {
            valid = false;
            GlobalVariables.getErrorMap().putError("newPurchasingItemLine", PurapKeyConstants.ERROR_ITEM_ACCOUNTING_NOT_ALLOWED, "Item " + item.getItemLineNumber());
        }
        return valid;
    }

    /**
     * 
     * This method validates that the item must contain at least one account
     * @param item
     * @return
     */
    private boolean validateItemWithoutAccounts(PurchaseOrderItem item) {
        boolean valid = true;
        if (item.isAccountListEmpty()) {
            valid = false;
            GlobalVariables.getErrorMap().putError("newPurchasingItemLine", PurapKeyConstants.ERROR_ITEM_ACCOUNTING_INCOMPLETE, "Item " + item.getItemLineNumber());
        }
        return valid;
    }

    /**
     * 
     * This method validates that if the item type is ITEM, the unit of measure field is required.
     * 
     * @param item
     * @return
     */
    private boolean validateItemUnitOfMeasure(PurchaseOrderItem item) {
        boolean valid = true;
        if (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ITEM_CODE) && StringUtils.isEmpty(item.getItemUnitOfMeasureCode())) {
            valid = false;
            GlobalVariables.getErrorMap().putError("newPurchasingItemLine", PurapKeyConstants.ERROR_ITEM_UNIT_OF_MEASURE_REQUIRED, "Item " + item.getItemLineNumber());
        }
        return valid;
    }

    /**
     * 
     * This method validates that the purchase order cannot have both trade in and discount item.
     * 
     * @param purDocument
     * @return
     */
    private boolean validateTradeInAndDiscountCoexistence(PurchasingDocument purDocument) {
        boolean discountExists = false;
        boolean tradeInExists = false;
        
        for (PurchasingApItem item : purDocument.getItems()) {
            if (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_ORDER_DISCOUNT_CODE)) {
                discountExists = true;
                if (tradeInExists) {
                    GlobalVariables.getErrorMap().putError("newPurchasingItemLine", PurapKeyConstants.ERROR_ITEM_TRADEIN_DISCOUNT_COEXISTENCE);
                    return false;
                }
            }
            else if (item.getItemTypeCode().equals(ItemTypeCodes.ITEM_TYPE_TRADE_IN_CODE)) {
                tradeInExists = true;
                if (discountExists) {
                    GlobalVariables.getErrorMap().putError("newPurchasingItemLine", PurapKeyConstants.ERROR_ITEM_TRADEIN_DISCOUNT_COEXISTENCE);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * This method performs any validation for the Stipulation tab.
     * 
     * @param poDocument
     * @return
     */
    public boolean processVendorStipulationValidation(PurchaseOrderDocument poDocument) {
        boolean valid = true;
        List<PurchaseOrderVendorStipulation> stipulations = poDocument.getPurchaseOrderVendorStipulations();
        for (int i = 0; i < stipulations.size(); i++) {
            PurchaseOrderVendorStipulation stipulation = stipulations.get(i);
            if (StringUtils.isBlank(stipulation.getVendorStipulationDescription())) {
                GlobalVariables.getErrorMap().putError(PurapPropertyConstants.VENDOR_STIPULATION + "[" + i + "]." + PurapPropertyConstants.VENDOR_STIPULATION_DESCRIPTION, PurapKeyConstants.ERROR_STIPULATION_DESCRIPTION);
                valid = false;
            }
        }
        return valid;
    }

    @Override
    public boolean processVendorValidation(PurchasingAccountsPayableDocument purapDocument) {
        ErrorMap errorMap = GlobalVariables.getErrorMap();
        boolean valid = super.processVendorValidation(purapDocument);
        PurchaseOrderDocument poDocument = (PurchaseOrderDocument) purapDocument;
        if (StringUtils.isBlank(poDocument.getVendorCountryCode())) {
            // TODO can't this be done by the data dictionary?
            valid = false;
            errorMap.putError(PurapPropertyConstants.VENDOR_COUNTRY_CODE, KFSKeyConstants.ERROR_REQUIRED);
        }
        else if (poDocument.getVendorCountryCode().equals(KFSConstants.COUNTRY_CODE_UNITED_STATES)) {
            if (StringUtils.isBlank(poDocument.getVendorStateCode())) {
                valid = false;
                errorMap.putError(PurapPropertyConstants.VENDOR_STATE_CODE, KFSKeyConstants.ERROR_REQUIRED_FOR_US);
            }
            ZipcodeValidationPattern zipPattern = new ZipcodeValidationPattern();
            if (StringUtils.isBlank(poDocument.getVendorPostalCode())) {
                valid = false;
                errorMap.putError(PurapPropertyConstants.VENDOR_POSTAL_CODE, KFSKeyConstants.ERROR_REQUIRED_FOR_US);
            }
            else if (!zipPattern.matches(poDocument.getVendorPostalCode())) {
                valid = false;
                errorMap.putError(PurapPropertyConstants.VENDOR_POSTAL_CODE, PurapKeyConstants.ERROR_POSTAL_CODE_INVALID);
            }
        }
        return valid;
    }

    @Override
    public boolean processAdditionalValidation(PurchasingDocument purDocument) {
        boolean valid = super.processAdditionalValidation(purDocument);
        valid = validateFaxNumberIfTransmissionTypeIsFax(purDocument);
        return valid;
    }

    // TODO check comments; mentions REQ, but this class performs only PO validation
    /**
     * Validate that if Vendor Id (VendorHeaderGeneratedId) is not empty, and tranmission method is fax, vendor fax number cannot be
     * empty and must be valid. In other words: allow reqs to not force fax # when transmission type is fax if vendor id is empty
     * because it will not be allowed to become an APO and it will be forced on the PO.
     * 
     * @return False if VendorHeaderGeneratedId is not empty, tranmission method is fax, and VendorFaxNumber is empty or invalid.
     *         True otherwise.
     */
    private boolean validateFaxNumberIfTransmissionTypeIsFax(PurchasingDocument purDocument) {
        boolean valid = true;
        if (ObjectUtils.isNotNull(purDocument.getVendorHeaderGeneratedIdentifier()) && purDocument.getPurchaseOrderTransmissionMethodCode().equals(PurapConstants.POTransmissionMethods.FAX)) {
            if (ObjectUtils.isNull(purDocument.getVendorFaxNumber()) || !SpringServiceLocator.getPhoneNumberService().isValidPhoneNumber(purDocument.getVendorFaxNumber())) {
                GlobalVariables.getErrorMap().putError(PurapPropertyConstants.REQUISITION_VENDOR_FAX_NUMBER, PurapKeyConstants.ERROR_FAX_NUMBER_PO_TRANSMISSION_TYPE);
                valid &= false;
            }
        }
        return valid;
    }

}
