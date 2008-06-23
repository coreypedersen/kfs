/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.module.cams.web.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.core.service.DocumentService;
import org.kuali.core.web.struts.action.KualiTransactionalDocumentActionBase;
import org.kuali.kfs.KFSConstants;
import org.kuali.module.cams.bo.BarcodeInventoryErrorDetail;
import org.kuali.module.cams.document.BarcodeInventoryErrorDocument;
import org.kuali.module.cams.web.struts.form.BarcodeInventoryErrorForm;
import org.kuali.rice.KNSServiceLocator;

public class BarcodeInventoryErrorAction extends KualiTransactionalDocumentActionBase {
    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BarcodeInventoryErrorAction.class);
    /**
     * @see org.kuali.core.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.core.web.struts.form.KualiDocumentFormBase)
     *
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
    }*/

    /**
     * Adds handling for cash control detail amount updates.
     * 
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        BarcodeInventoryErrorForm apForm = (BarcodeInventoryErrorForm) form;
        String command = ((BarcodeInventoryErrorForm) form).getCommand();
        String docID = ((BarcodeInventoryErrorForm) form).getDocId();
        
        LOG.info("***BarcodeInventoryErrorAction.execute() - menthodToCall: " + apForm.getMethodToCall() + " - Command:" + command + " - DocId:" + docID);
        return super.execute(mapping, form, request, response);
    }



    /**
     * This method deletes a  detail
     * 
     * @param mapping action mapping
     * @param form action form
     * @param request
     * @param response
     * @return action forward
     * @throws Exception
     */
    public ActionForward deleteBarcodeInventoryErrorDetail(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        BarcodeInventoryErrorForm barcodeInventoryErrorForm = (BarcodeInventoryErrorForm) form;
        BarcodeInventoryErrorDocument inventoryUploadErrorDocument = barcodeInventoryErrorForm.getBarcodeInventoryErrorDocument();
        
        int indexOfLineToDelete = getLineToDelete(request);
        
        BarcodeInventoryErrorDetail inventoryUploadErrorDetail = inventoryUploadErrorDocument.getBarcodeInventoryErrorDetail(indexOfLineToDelete);
        DocumentService documentService = KNSServiceLocator.getDocumentService();

        inventoryUploadErrorDocument.deleteBarcodeInventoryErrorDetail(indexOfLineToDelete);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
}
