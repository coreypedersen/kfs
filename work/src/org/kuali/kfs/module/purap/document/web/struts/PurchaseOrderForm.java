/*
 * Copyright 2006 The Kuali Foundation.
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
package org.kuali.module.purap.web.struts.form;

import org.kuali.core.util.ObjectUtils;
import org.kuali.core.web.uidraw.KeyLabelPair;
import org.kuali.module.purap.bo.PurchasingItem;
import org.kuali.module.purap.bo.PurchaseOrderItem;
import org.kuali.module.purap.document.PurchaseOrderDocument;

/**
 * This class is the form class for the PurchaseOrder document. This method extends the parent KualiTransactionalDocumentFormBase
 * class which contains all of the common form methods and form attributes needed by the PurchaseOrder document.
 * 
 */
public class PurchaseOrderForm extends PurchasingFormBase {

    /**
     * Constructs a PurchaseOrderForm instance and sets up the appropriately casted document. 
     */
    public PurchaseOrderForm() {
        super();
        setDocument(new PurchaseOrderDocument());
        this.setNewPurchasingItemLine(setupNewPurchasingItemLine());
    }

    /**
     * @return Returns the internalBillingDocument.
     */
    public PurchaseOrderDocument getPurchaseOrderDocument() {
        return (PurchaseOrderDocument) getDocument();
    }

    /**
     * @param internalBillingDocument The internalBillingDocument to set.
     */
    public void setPurchaseOrderDocument(PurchaseOrderDocument purchaseOrderDocument) {
        setDocument(purchaseOrderDocument);
    }

    /**
     * @see org.kuali.core.web.struts.form.KualiForm#getAdditionalDocInfo1()
     */
    public KeyLabelPair getAdditionalDocInfo1() {
        if (ObjectUtils.isNotNull(this.getPurchaseOrderDocument().getIdentifier())) {
            return new KeyLabelPair("DataDictionary.KualiPurchaseOrderDocument.attributes.identifier", ((PurchaseOrderDocument)this.getDocument()).getIdentifier().toString());
        } else {
            return new KeyLabelPair("DataDictionary.KualiPurchaseOrderDocument.attributes.identifier", "Not Available");
        }
    }

    /**
     * @see org.kuali.core.web.struts.form.KualiForm#getAdditionalDocInfo2()
     */
    public KeyLabelPair getAdditionalDocInfo2() {
        if (ObjectUtils.isNotNull(this.getPurchaseOrderDocument().getStatus())) {
            return new KeyLabelPair("DataDictionary.KualiPurchaseOrderDocument.attributes.statusCode", ((PurchaseOrderDocument)this.getDocument()).getStatus().getStatusDescription());
        } else {
            return new KeyLabelPair("DataDictionary.KualiPurchaseOrderDocument.attributes.statusCode", "Not Available");
        }
    }

    /**
     * @see org.kuali.module.purap.web.struts.form.PurchasingFormBase#setupNewPurchasingItemLine()
     */
    @Override
    public PurchasingItem setupNewPurchasingItemLine() {
        return new PurchaseOrderItem();
    }
    
    
}