/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.fp.document.web.struts;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.businessobject.CapitalAccountingLines;
import org.kuali.kfs.fp.businessobject.options.CapitalAccountingLinesComparator;
import org.kuali.kfs.fp.document.CapitalAccountingLinesDocumentBase;
import org.kuali.kfs.fp.document.validation.event.CapitalAccountingLinesSameObjectCodeSubTypeEvent;
import org.kuali.kfs.integration.cab.CapitalAssetBuilderModuleService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kns.document.Document;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.KualiRuleService;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.ObjectUtils;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.kns.web.struts.form.KualiForm;

/**
 * This is the action class for the CapitalAccountingLinesActionBase.
 */
public abstract class CapitalAccountingLinesActionBase extends CapitalAssetInformationActionBase {
    private CapitalAssetBuilderModuleService capitalAssetBuilderModuleService = SpringContext.getBean(CapitalAssetBuilderModuleService.class);

    /**
     * All document-load operations get routed through here
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiDocumentActionBase#loadDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.loadDocument(kualiDocumentFormBase);
        
        CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) kualiDocumentFormBase;
        AccountingDocument tdoc = (AccountingDocument) kualiDocumentFormBase.getDocument();
        List<CapitalAccountingLines> capitalAccountingLines = new ArrayList();
        createCapitalAccountingLines(capitalAccountingLines, tdoc);
        sortCaptitalAccountingLines(capitalAccountingLines);

        CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) tdoc;
        caldb.setCapitalAccountingLines(capitalAccountingLines);
        
        checkCapitalAccountingLinesSelected(capitalAccountingLinesFormBase);
        
        //setup the initial next sequence number column..
        setupIntialNextCapitalAssetLineNumber(kualiDocumentFormBase);
        
        KualiForm kualiForm = (KualiForm) kualiDocumentFormBase;
        //based on the records in capital accounting lines, capital asset information lists
        //set the tabs to open if lists not empty else set to close
        setTabStatesForCapitalAssets(kualiForm);
    }
        
    /**
     * 
     * @see org.kuali.rice.kns.web.struts.action.KualiAction#refresh(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
   // @Override
    public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        super.refresh(mapping, form, request, response);

        CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) form;
        CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();
        
        List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        KualiDocumentFormBase kualiDocumentFormBase = (KualiDocumentFormBase) form;
        AccountingDocument tdoc = (AccountingDocument) kualiDocumentFormBase.getDocument();
        
        capitalAccountingLines = updateCapitalAccountingLines(capitalAccountingLines, tdoc);
        sortCaptitalAccountingLines(capitalAccountingLines);
        caldb.setCapitalAccountingLines(capitalAccountingLines);

        checkCapitalAccountingLinesSelected(capitalAccountingLinesFormBase);
        
        setTabStatesForCapitalAssets(form);
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * When user adds an accounting line to the either source or target, if the object code on
     * that line has capital object type code group then a capital accounting line is created.
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#insertAccountingLine(boolean, org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase, org.kuali.kfs.sys.businessobject.AccountingLine)
     */
    @Override
    protected void insertAccountingLine(boolean isSource, KualiAccountingDocumentFormBase financialDocumentForm, AccountingLine line) {
        super.insertAccountingLine(isSource, financialDocumentForm, line);
        
        CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) financialDocumentForm;
        CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();
        
        List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();

        createCapitalAccountingLine(capitalAccountingLines, line);
        sortCaptitalAccountingLines(capitalAccountingLines);
        
        KualiForm kualiForm = (KualiForm) financialDocumentForm;
        setTabStatesForCapitalAssets(kualiForm);
    }
      
    /**
     * When user deletes an accounting line to the either source or target, if the corresponding line in
     * capital accounting line is deleted.
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#deleteAccountingLine(boolean, org.kuali.kfs.sys.web.struts.KualiAccountingDocumentFormBase, int)
     */
    @Override
    protected void deleteAccountingLine(boolean isSource, KualiAccountingDocumentFormBase financialDocumentForm, int deleteIndex) {

        CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) financialDocumentForm;
        CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();
        
        List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        
        if (isSource) {
            // remove from capital accounting lines for this source line
            AccountingLine line = (AccountingLine) financialDocumentForm.getFinancialDocument().getSourceAccountingLines().get(deleteIndex);
            deleteCapitalAccountingLine(capitalAccountingLines, line);
            deleteCapitalAssetLines(financialDocumentForm, line);
        }
        else {
            // remove from document
            AccountingLine line = (AccountingLine) financialDocumentForm.getFinancialDocument().getTargetAccountingLines().get(deleteIndex);
            deleteCapitalAccountingLine(capitalAccountingLines, line);
            deleteCapitalAssetLines(financialDocumentForm, line);
        }
        
        super.deleteAccountingLine(isSource, financialDocumentForm, deleteIndex);
        sortCaptitalAccountingLines(capitalAccountingLines);
        
        KualiForm kualiForm = (KualiForm) financialDocumentForm;
        setTabStatesForCapitalAssets(kualiForm);
    }
    
    /**
     * After uploading the accounting lines, the capital accounting lines will be created from these.
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#uploadAccountingLines(boolean, org.apache.struts.action.ActionForm)
     */
    @Override
    protected void uploadAccountingLines(boolean isSource, ActionForm form) throws FileNotFoundException, IOException {
        super.uploadAccountingLines(isSource, form);
        
        KualiAccountingDocumentFormBase kualiAccountingDocumentFormBase = (KualiAccountingDocumentFormBase) form;
        CapitalAccountingLinesFormBase capitalAccountingLinesFormBase = (CapitalAccountingLinesFormBase) form;
        CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) capitalAccountingLinesFormBase.getFinancialDocument();
                
        List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        AccountingDocument tdoc = (AccountingDocument) kualiAccountingDocumentFormBase.getDocument();
        
        createCapitalAccountingLines(capitalAccountingLines, tdoc);
        sortCaptitalAccountingLines(capitalAccountingLines);
        
        KualiForm kualiForm = (KualiForm) form;
        setTabStatesForCapitalAssets(kualiForm);
    }
    
    /**
     * creates the capital accounting lines looking at source and/or target accounting lines.
     * 
     * @param capitalAccountingLines
     * @param tdoc
     */
    protected void createCapitalAccountingLines(List<CapitalAccountingLines> capitalAccountingLines, AccountingDocument tdoc) {
        List<SourceAccountingLine> sourceAccountLines = tdoc.getSourceAccountingLines();

        for (SourceAccountingLine line : sourceAccountLines) {
            createCapitalAccountingLine(capitalAccountingLines, line);
        }
        
        List<TargetAccountingLine> targetAccountLines = tdoc.getTargetAccountingLines();

        for (TargetAccountingLine line : targetAccountLines) {
            createCapitalAccountingLine(capitalAccountingLines, line);
        }
        
        //sort the capital accounting lines collection
        sortCaptitalAccountingLines(capitalAccountingLines);
    }
    
    /**
     * updates the capital accounting lines looking at source and/or target accounting lines.
     * 
     * @param capitalAccountingLines
     * @param tdoc
     */
    protected List<CapitalAccountingLines> updateCapitalAccountingLines(List<CapitalAccountingLines> capitalAccountingLines, AccountingDocument tdoc) {
        List<SourceAccountingLine> sourceAccountLines = tdoc.getSourceAccountingLines();

        for (SourceAccountingLine line : sourceAccountLines) {
            updateCapitalAccountingLine(capitalAccountingLines, line);
        }
        
        List<TargetAccountingLine> targetAccountLines = tdoc.getTargetAccountingLines();

        for (TargetAccountingLine line : targetAccountLines) {
            updateCapitalAccountingLine(capitalAccountingLines, line);
        }
        
        //remove the orphan capital accounting lines that does not have corresponding
        //accounting line from either source or target.
       return removeOrphanCapitalAccountingLines(capitalAccountingLines, tdoc);
    }
    
    /**
     * updates the capital accounting lines looking at source and/or target accounting lines.
     * 
     * @param capitalAccountingLines
     * @param tdoc
     */
    protected List<CapitalAccountingLines> removeOrphanCapitalAccountingLines(List<CapitalAccountingLines> capitalAccountingLines, AccountingDocument tdoc) {
        List<CapitalAccountingLines> newCapitalAccountingLines = new ArrayList<CapitalAccountingLines>();
        
        List<AccountingLine> sourceAccountLines = tdoc.getSourceAccountingLines();
        for (CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (removeOrphanCapitalAccountingLine(sourceAccountLines, capitalAccountingLine)) {
                newCapitalAccountingLines.add(capitalAccountingLine);
            }
        }
        
        List<AccountingLine> targetAccountLines = tdoc.getTargetAccountingLines();
        for (CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (removeOrphanCapitalAccountingLine(targetAccountLines, capitalAccountingLine)) {
                newCapitalAccountingLines.add(capitalAccountingLine);
            }
        }
        
        return newCapitalAccountingLines;
    }
    
    /**
     * If the line exists in capital accounting lines, and that line does not exist in 
     * accounting lines (source or target) then remove the line from capital accounting lines.
     * 
     * @param capitalAccountingLines
     * @param line to remove
     * @return true if the capital accounting line to be removed is found in accounting lines, else false
     */
    protected boolean removeOrphanCapitalAccountingLine(List<AccountingLine> accountLines, CapitalAccountingLines capitalAccountingLine) {
        boolean found = false;
        
        for (AccountingLine accountingLine : accountLines) {
            if (capitalAccountingLine.getChartOfAccountsCode().equals(accountingLine.getChartOfAccountsCode()) && 
                    capitalAccountingLine.getAccountNumber().equals(accountingLine.getAccountNumber()) &&
                    capitalAccountingLine.getFinancialObjectCode().equals(accountingLine.getFinancialObjectCode()) &&
                    capitalAccountingLine.getLineType().equalsIgnoreCase(accountingLine instanceof SourceAccountingLine ? KFSConstants.SOURCE : KFSConstants.TARGET)) {
                found = true;
            }
        }
        
        return found;
    }
    
    /**
     * Checks if the accounting line has an object code that belongs to object sub type group codes and
     * if so, creates a capital accounting line that will be displayed on the jsp.
     * 
     * @param capitalAccountingLines
     * @param line
     * @return List of capitalAccountingLines
     */
    protected List<CapitalAccountingLines> createCapitalAccountingLine(List<CapitalAccountingLines> capitalAccountingLines, AccountingLine line) {
        Integer sequenceNumber = capitalAccountingLines.size() + 1;

        if (capitalAssetBuilderModuleService.hasCapitalAssetObjectSubType(line)) {
            //capital object code so we need to build the capital accounting line...
            CapitalAccountingLines cal = addCapitalAccountingLine(capitalAccountingLines, line);
            capitalAccountingLines.add(cal);
        }
        
        return capitalAccountingLines;
    }

    /**
     * Checks if the accounting line exits in the capital accounting lines
     * and if so, updates the other information.
     * else inserts a new record into the collection
     * 
     * @param capitalAccountingLines
     * @param line
     * @return List of capitalAccountingLines
     */
    protected void updateCapitalAccountingLine(List<CapitalAccountingLines> capitalAccountingLines, AccountingLine line) {
        boolean found = false;
        
        for (CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.getChartOfAccountsCode().equals(line.getChartOfAccountsCode()) && 
                    capitalAccountingLine.getAccountNumber().equals(line.getAccountNumber()) &&
                    capitalAccountingLine.getFinancialObjectCode().equals(line.getFinancialObjectCode()) &&
                    capitalAccountingLine.getLineType().equalsIgnoreCase(line instanceof SourceAccountingLine ? KFSConstants.SOURCE : KFSConstants.TARGET)) {
                capitalAccountingLine.setFinancialSubObjectCode(line.getFinancialSubObjectCode());
                capitalAccountingLine.setSubAccountNumber(line.getSubAccountNumber());
                capitalAccountingLine.setProjectCode(line.getProjectCode());
                capitalAccountingLine.setAmount(line.getAmount());
                capitalAccountingLine.setFinancialDocumentLineDescription(line.getFinancialDocumentLineDescription());
                found = true;
                break;
            }
        }
        
        if (!found) {
            //capital object code so we need to build the capital accounting line...
            if (capitalAssetBuilderModuleService.hasCapitalAssetObjectSubType(line)) {
                CapitalAccountingLines cal = addCapitalAccountingLine(capitalAccountingLines, line);
                capitalAccountingLines.add(cal);
            }
        }
    }
    
    /**
     * convenience method to add a new capital accounting line to the collection of capital 
     * accounting lines.
     * 
     * @param capitalAccountingLines
     * @param line
     * @return cal newly created capital accounting line
     */
    protected CapitalAccountingLines addCapitalAccountingLine(List<CapitalAccountingLines> capitalAccountingLines, AccountingLine line) {
        CapitalAccountingLines cal = new CapitalAccountingLines();

        if (line instanceof SourceAccountingLine) {
            cal.setLineType(KFSConstants.SOURCE);
        }
        else {
            cal.setLineType(KFSConstants.TARGET);
        }
        cal.setSequenceNumber(line.getSequenceNumber());
        cal.setChartOfAccountsCode(line.getChartOfAccountsCode());
        cal.setAccountNumber(line.getAccountNumber());
        cal.setSubAccountNumber(line.getSubAccountNumber());
        cal.setFinancialObjectCode(line.getFinancialObjectCode());
        cal.setFinancialSubObjectCode(line.getFinancialSubObjectCode());
        cal.setProjectCode(line.getProjectCode());
        cal.setFinancialDocumentLineDescription(line.getFinancialDocumentLineDescription());
        cal.setAmount(line.getAmount());
        cal.setSelectLine(false);
        
        return cal;
    }
    
    /**
     * If the line exists in capital accounting lines, that will be deleted.
     * 
     * @param capitalAccountingLines
     * @param line to remove
     * @return List of capitalAccountingLines
     */
    protected List<CapitalAccountingLines> deleteCapitalAccountingLine(List<CapitalAccountingLines> capitalAccountingLines, AccountingLine line) {
        CapitalAccountingLines cal = null;
        
        for (CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.getChartOfAccountsCode().equals(line.getChartOfAccountsCode()) && 
                    capitalAccountingLine.getAccountNumber().equals(line.getAccountNumber()) &&
                    capitalAccountingLine.getFinancialObjectCode().equals(line.getFinancialObjectCode()) &&
                    capitalAccountingLine.getLineType().equalsIgnoreCase(line instanceof SourceAccountingLine ? KFSConstants.SOURCE : KFSConstants.TARGET)) {
                cal = capitalAccountingLine;
                break;
            }
        }
        
        if (ObjectUtils.isNotNull(cal)) {
            capitalAccountingLines.remove(cal);
        }
        
        return capitalAccountingLines;
    }
    
    /**
     * sort the capital accounting lines collection based on financial object code and account number.
     * @param capitalAccountingLines List of capital accounting lines
     */
    protected void sortCaptitalAccountingLines(List<CapitalAccountingLines> capitalAccountingLines) {
        CapitalAccountingLinesComparator calComparator = new CapitalAccountingLinesComparator();
        
        //sort the collection based on financial object code and account number
        Collections.sort(capitalAccountingLines, calComparator);
    }
    
    /**
     * creates asset for the accounting line to the document
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward createAsset(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
        String distributionCode = calfb.getCapitalAccountingLine().getDistributionCode();
        if (AMOUNT_EQUAL_DISTRIBUTION_CODE.equals(distributionCode)) {
           calfb.setDistributeEqualAmount(true);
        }
        else {
            calfb.setDistributeEqualAmount(false);
        }
        
        boolean createAction = calfb.getCapitalAccountingLine().isCanCreateAsset();
        calfb.setEditCreateOrModify(false);

        GlobalVariables.getMessageMap().clearErrorMessages();
        if (!capitalAccountingLinesSelected(calfb)) {
            GlobalVariables.getMessageMap().putError(KFSConstants.EDIT_ACCOUNTING_LINES_FOR_CAPITALIZATION_ERRORS, KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_FOR_CAPITALIZATAION_REQUIRED_CREATE);
        }
        else {
            calfb.setEditCreateOrModify(false);
        }
            
        Document document = calfb.getFinancialDocument();
        
        //if same object subtypes then continue creating capital assets....
        if (checkObjecSubTypeCrossingCapitalAccountingLines(document)) {
            //question the user if to continue....
            ActionForward forward = performQuestionPrompt(mapping, form, request, response, KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR);
            if (forward != null) {
                return forward;
            }
        }
        else {
            createCapitalAssetInformation(calfb, KFSConstants.CapitalAssets.CAPITAL_ASSET_CREATE_ACTION_INDICATOR);
            checkCapitalAccountingLinesSelected(calfb);
            
            KualiForm kualiForm = (KualiForm) form;
            setTabStatesForCapitalAssets(kualiForm);
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);        
    }
    
    /**
     * modify asset for the accounting line to the document
     * 
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward modifyAsset(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
        String distributionCode = calfb.getCapitalAccountingLine().getDistributionCode();
        
        if (AMOUNT_EQUAL_DISTRIBUTION_CODE.equals(distributionCode)) {
            calfb.setDistributeEqualAmount(true);
         }
        else {
            calfb.setDistributeEqualAmount(false);
        }
        
        GlobalVariables.getMessageMap().clearErrorMessages();
        if (!capitalAccountingLinesSelected(calfb)) {
            GlobalVariables.getMessageMap().putError(KFSConstants.EDIT_ACCOUNTING_LINES_FOR_CAPITALIZATION_ERRORS, KFSKeyConstants.ERROR_DOCUMENT_ACCOUNTING_LINE_FOR_CAPITALIZATAION_REQUIRED_MODIFY);
        }
        else {
            calfb.setEditCreateOrModify(false);
        }
        
        Document document = calfb.getFinancialDocument();
        
        //if same object subtypes then continue creating capital assets....
        if (checkObjecSubTypeCrossingCapitalAccountingLines(document)) {
            //question the user if to continue....
            ActionForward forward = performQuestionPrompt(mapping, form, request, response, KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR);
            if (forward != null) {
                return forward;
            }
        }
        else {
            createCapitalAssetInformation(calfb, KFSConstants.CapitalAssets.CAPITAL_ASSET_MODIFY_ACTION_INDICATOR);
            checkCapitalAccountingLinesSelected(calfb);
            
            KualiForm kualiForm = (KualiForm) form;
            setTabStatesForCapitalAssets(kualiForm);
        }
        
        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }
    
    /**
     * @param calfb
     * @return
     */
    protected boolean capitalAccountingLinesSelected(CapitalAccountingLinesFormBase calfb) {
        boolean selected = false;
        CapitalAccountingLinesDocumentBase caldb = (CapitalAccountingLinesDocumentBase) calfb.getFinancialDocument();
        List<CapitalAccountingLines> capitalAccountingLines = caldb.getCapitalAccountingLines();
        
        for (CapitalAccountingLines capitalAccountingLine : capitalAccountingLines) {
            if (capitalAccountingLine.isSelectLine()) {
                selected = true;
                break;
            }
        }
        
        return selected;
    }
    
   /**
    * runs the validation to check if object subtypes crosses groups on
    * selected capital accounting lines.
    * 
    * @param form
    * @return true if rule passed else false
    */
    protected boolean checkObjecSubTypeCrossingCapitalAccountingLines(Document document) {
        boolean differentObjecSubtypes = true;
        differentObjecSubtypes &= getRuleService().applyRules(new CapitalAccountingLinesSameObjectCodeSubTypeEvent(document));

        return differentObjecSubtypes;
    }
    
    /**
     * 
     * @param mapping An ActionMapping
     * @param form An ActionForm
     * @param request The HttpServletRequest
     * @param response The HttpServletResponse
     * @throws Exception
     * @return An ActionForward
     */
    protected ActionForward performQuestionPrompt(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response, String actionTypeCode) throws Exception {
        ActionForward forward = null;
        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        
        if (question == null) {
            String questionText = SpringContext.getBean(KualiConfigurationService.class).getPropertyString(KFSKeyConstants.WARNING_NOT_SAME_OBJECT_SUB_TYPES);
            return this.performQuestionWithoutInput(mapping, form, request, response, KFSConstants.OBJECT_SUB_TYPES_DIFFERENT_QUESTION, questionText, KFSConstants.CONFIRMATION_QUESTION, KFSConstants.ROUTE_METHOD, "");
        }
        else {
            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            // If the user replies 'Yes' the question, proceed..
            if (KFSConstants.OBJECT_SUB_TYPES_DIFFERENT_QUESTION.equals(question) && ConfirmationQuestion.YES.equals(buttonClicked)) {
                CapitalAccountingLinesFormBase calfb = (CapitalAccountingLinesFormBase) form;
                createCapitalAssetInformation(calfb, actionTypeCode);
                checkCapitalAccountingLinesSelected(calfb);
                
                KualiForm kualiForm = (KualiForm) form;
                setTabStatesForCapitalAssets(kualiForm);
                
                return mapping.findForward(KFSConstants.MAPPING_BASIC);
                
            }
            // If the user replies 'No' to either of the questions
            else {
                forward = mapping.findForward(KFSConstants.MAPPING_BASIC);
            }
        }

        return forward;
    }
    
    /**
     * Get the rule service
     * 
     * @return ruleService
     */
    protected KualiRuleService getRuleService() {
        return SpringContext.getBean(KualiRuleService.class);
    }
}
