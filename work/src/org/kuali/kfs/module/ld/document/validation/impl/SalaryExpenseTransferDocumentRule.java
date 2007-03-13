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
package org.kuali.module.labor.rules;

import static org.kuali.Constants.BALANCE_TYPE_A21;
import static org.kuali.Constants.BALANCE_TYPE_ACTUAL;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.Constants;
import org.kuali.KeyConstants;
import org.kuali.PropertyConstants;
import org.kuali.core.document.Document;
import org.kuali.core.exceptions.ReferentialIntegrityException;
import org.kuali.core.util.ErrorMap;
import org.kuali.core.util.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.core.util.GlobalVariables;
import org.kuali.core.util.KualiDecimal;
import org.kuali.core.util.ObjectUtils;
import org.kuali.core.util.SpringServiceLocator;
import org.kuali.kfs.bo.AccountingLine;
import org.kuali.kfs.document.AccountingDocument;
import org.kuali.kfs.rules.AccountingDocumentRuleBase;
import org.kuali.module.chart.bo.AccountingPeriod;
import org.kuali.module.chart.bo.ObjectCode;
import org.kuali.module.financial.bo.OffsetAccount;
import org.kuali.module.labor.bo.BenefitsCalculation;
import org.kuali.module.labor.bo.BenefitsType;
import org.kuali.module.labor.bo.ExpenseTransferAccountingLine;
import org.kuali.module.labor.bo.LaborObject;
import org.kuali.module.labor.bo.PendingLedgerEntry;
import org.kuali.module.labor.bo.PositionObjectBenefit;
import org.kuali.module.labor.document.SalaryExpenseTransferDocument;
import org.kuali.module.labor.rule.GenerateLaborLedgerBenefitClearingPendingEntriesRule;
import org.kuali.module.labor.rule.GenerateLaborLedgerPendingEntriesRule;

/**
 * Business rule(s) applicable to Salary Expense Transfer documents.
 * 
 * 
 */
public class SalaryExpenseTransferDocumentRule extends AccountingDocumentRuleBase implements GenerateLaborLedgerPendingEntriesRule<AccountingDocument>, GenerateLaborLedgerBenefitClearingPendingEntriesRule<AccountingDocument>{

    // LLPE Constants
    public static final class LABOR_LEDGER_PENDING_ENTRY_CODE {
        public static final String NO = "N";
        public static final String YES = "Y";
        public static final String BLANK_PROJECT_STRING = "----------"; // Max length is 10 for this field
        public static final String BLANK_SUB_OBJECT_CODE = "---"; // Max length is 3 for this field
        public static final String BLANK_SUB_ACCOUNT_NUMBER = "-----"; // Max length is 5 for this field
        public static final String BLANK_OBJECT_CODE = "----"; // Max length is 4 for this field
        public static final String BLANK_OBJECT_TYPE_CODE = "--"; // Max length is 4 for this field
        public static final String BLANK_POSITION_NUMBER = "--------"; // Max length is 8 for this field
        public static final String BLANK_EMPL_ID = "-----------"; // Max length is 11 for this field
        public static final String LL_PE_OFFSET_STRING = "TP Generated Offset";
        public static final int LLPE_DESCRIPTION_MAX_LENGTH = 40;
    }

    public static final String LABOR_LEDGER_SALARY_CODE = "S";
    public static final String LABOR_LEDGER_CHART_OF_ACCOUNT_CODE = "UA";
    public static final String LABOR_LEDGER_ACCOUNT_NUMBER = "9712700";
        
    public SalaryExpenseTransferDocumentRule() {
        super();        
    }   
    
    protected boolean AddAccountingLineBusinessRules(AccountingDocument accountingDocument, AccountingLine accountingLine) {
        return processCustomAddAccountingLineBusinessRules(accountingDocument, accountingLine);
    }
    
    /** 
     * The following criteria will be validated here:
     * Account must be valid.
     * Object code must be valid.
     * Object code must be a labor object code.
            Object code must exist in the ld_labor_obj_t table.
            The field finobj_frngslry_cd for the object code in the ld_labor_obj_t table must have a value of "S".
     * Sub-account, if specified, must be valid for account.
     * Sub-object, if specified, must be valid for account and object code.
     * Enforce the A21-report-related business rules for the "SAVE" action.
     * Position must be valid for fiscal year. FIS enforces this by a direct lookup of the PeopleSoft HRMS position data table. Kuali cannot do this. (See issue 12.)
     * Employee ID exists.
     * Employee does not have pending salary transfers.
     * Amount must not be zero. 
     * 
     * @see org.kuali.module.financial.rules.TransactionalDocumentRuleBase#processCustomAddAccountingLineBusinessRules(org.kuali.core.document.TransactionalDocument,
     *      org.kuali.core.bo.AccountingLine)
     *      
     * @param TransactionalDocument
     * @param AccountingLine
     * @return
   */
    @Override
    protected boolean processCustomAddAccountingLineBusinessRules(AccountingDocument accountingDocument, AccountingLine accountingLine) {

        // Retrieve the Fringe or Salary Code for the object code in the ld_labor_obj_t table. 
        // It must have a value of "S".
        
        ErrorMap errorMap = GlobalVariables.getErrorMap();
        Map fieldValues = new HashMap();
        fieldValues.put("financialObjectCode", accountingLine.getFinancialObjectCode().toString());
        ArrayList laborObjects = (ArrayList) SpringServiceLocator.getBusinessObjectService().findMatching(LaborObject.class, fieldValues);
        if (laborObjects.size() == 0) {
            reportError(PropertyConstants.ACCOUNT, KeyConstants.Labor.LABOR_OBJECT_MISSING_OBJECT_CODE_ERROR, accountingLine.getAccountNumber());
            return false;
        }
        LaborObject laborObject = (LaborObject) laborObjects.get(0);    
        String FringeOrSalaryCode = laborObject.getFinancialObjectFringeOrSalaryCode();

        if (!FringeOrSalaryCode.equals("S")) {
            LOG.info("FringeOrSalaryCode not equal S");
              reportError(PropertyConstants.ACCOUNT, KeyConstants.Labor.INVALID_SALARY_OBJECT_CODE_ERROR, accountingLine.getAccountNumber());
            return false;
        }            
            
        // Validate that an employee ID is enterred.
        SalaryExpenseTransferDocument salaryExpenseTransferDocument = (SalaryExpenseTransferDocument)accountingDocument;
        String emplid = salaryExpenseTransferDocument.getEmplid();
        if ((emplid == null) || (emplid.trim().length() == 0)) {
            reportError(Constants.EMPLOYEE_LOOKUP_ERRORS,KeyConstants.Labor.MISSING_EMPLOYEE_ID, emplid);
            return false;
        }
        
        // Make sure the employee does not have any pending salary transfers
        if (!validatePendingSalaryTransfer(emplid))
            return false;
        
        // Save the employee ID in all accounting related lines       
        ExpenseTransferAccountingLine salaryExpenseTransferAccountingLine = (ExpenseTransferAccountingLine)accountingLine;
        salaryExpenseTransferAccountingLine.setEmplid(emplid); 

        // Validate the accounting year
        fieldValues.clear();
        fieldValues.put("universityFiscalYear", salaryExpenseTransferAccountingLine.getPayrollEndDateFiscalYear());
        AccountingPeriod accountingPeriod = new AccountingPeriod();        
        if (SpringServiceLocator.getBusinessObjectService().countMatching(AccountingPeriod.class, fieldValues) == 0) {
            reportError(PropertyConstants.ACCOUNT,KeyConstants.Labor.INVALID_PAY_YEAR, emplid);
            return false;
        }
        
        // Validate the accounting period code
        fieldValues.clear();
        fieldValues.put("universityFiscalPeriodCode", salaryExpenseTransferAccountingLine.getPayrollEndDateFiscalPeriodCode());
        accountingPeriod = new AccountingPeriod();        
        if (SpringServiceLocator.getBusinessObjectService().countMatching(AccountingPeriod.class, fieldValues) == 0) {
            reportError(PropertyConstants.ACCOUNT,KeyConstants.Labor.INVALID_PAY_PERIOD_CODE, emplid);
            return false;
        }
        return true;
    }
    
    /**
     * This document specific routing business rule check calls the check that makes sure that the budget year is consistent for all
     * accounting lines.
     * 
     * @see org.kuali.core.rule.DocumentRuleBase#processCustomRouteDocumentBusinessRules(org.kuali.core.document.Document)
     */
    @Override
    protected boolean processCustomRouteDocumentBusinessRules(Document document) {
        
        boolean isValid = super.processCustomRouteDocumentBusinessRules(document);

        SalaryExpenseTransferDocument setDoc = (SalaryExpenseTransferDocument) document;

        List sourceLines = new ArrayList();
        List targetLines = new ArrayList();

        //set source and target accounting lines
        sourceLines.addAll(setDoc.getSourceAccountingLines());
        targetLines.addAll(setDoc.getTargetAccountingLines());

        //check to ensure totals of accounting lines in source and target sections match
        if (isValid) {
            isValid = isAccountingLineTotalsMatch(sourceLines, targetLines);            
        }

        //check to ensure totals of accounting lines in source and target sections match by pay FY + pay period
        if (isValid) {
            isValid = isAccountingLineTotalsMatchByPayFYAndPayPeriod(sourceLines, targetLines);
        }
        
        return isValid;        
    }

    /** 
     * This method checks if the total sum amount of the source accounting line
     * matches the total sum amount of the target accounting line, return true if
     * the totals match, false otherwise.
     * 
     * @param sourceLines
     * @param targetLines
     * @return
     */
    private boolean isAccountingLineTotalsMatch(List sourceLines, List targetLines){
        
        boolean isValid = true;
        
        AccountingLine line = null; 
        
        // totals for the from and to lines.
        KualiDecimal sourceLinesAmount = new KualiDecimal(0);
        KualiDecimal targetLinesAmount = new KualiDecimal(0);

        //sum source lines
        for (Iterator i = sourceLines.iterator(); i.hasNext();) {
            line = (ExpenseTransferAccountingLine) i.next();            
            sourceLinesAmount = sourceLinesAmount.add(line.getAmount());            
        }

        //sum target lines
        for (Iterator i = targetLines.iterator(); i.hasNext();) {
            line = (ExpenseTransferAccountingLine) i.next();            
            targetLinesAmount = targetLinesAmount.add(line.getAmount());            
        }
        
        //if totals don't match, then add error message
        if (sourceLinesAmount.compareTo(targetLinesAmount) != 0) {
            isValid = false;
            reportError(PropertyConstants.SOURCE_ACCOUNTING_LINES, KeyConstants.Labor.ACCOUNTING_LINE_TOTALS_MISMATCH_ERROR);            
        }

        return isValid;        
    }
    
    /**
     * This method calls other methods to check if all source and target accounting lines match between each set
     * by pay fiscal year and pay period, returning true if the totals match, false otherwise.
     * 
     * @param sourceLines
     * @param targetLines
     * @return
     */
    private boolean isAccountingLineTotalsMatchByPayFYAndPayPeriod(List sourceLines, List targetLines){
        
        boolean isValid = true;
                
        Map sourceLinesMap = new HashMap();
        Map targetLinesMap = new HashMap();                       

        //sum source lines by pay fy and pay period, store in map by key PayFY+PayPeriod
        sourceLinesMap = sumAccountingLineAmountsByPayFYAndPayPeriod(sourceLines);

        //sum source lines by pay fy and pay period, store in map by key PayFY+PayPeriod
        targetLinesMap = sumAccountingLineAmountsByPayFYAndPayPeriod(targetLines);
        
        //if totals don't match by PayFY+PayPeriod categories, then add error message
        if ( compareAccountingLineTotalsByPayFYAndPayPeriod(sourceLinesMap, targetLinesMap) == false ) {
            isValid = false;
            reportError(PropertyConstants.SOURCE_ACCOUNTING_LINES, KeyConstants.Labor.ACCOUNTING_LINE_TOTALS_BY_PAYFY_PAYPERIOD_MISMATCH_ERROR);            
        }

        return isValid;        
    }

    /**
     * This method returns a String that is a concatenation of pay fiscal year and pay period code.
     * 
     * @param payFiscalYear
     * @param payPeriodCode
     * @return
     */
    private String createPayFYPeriodKey(Integer payFiscalYear, String payPeriodCode){
    
        StringBuffer payFYPeriodKey = new StringBuffer();
        
        payFYPeriodKey.append(payFiscalYear);
        payFYPeriodKey.append(payPeriodCode);
        
        return payFYPeriodKey.toString();
    }
    
    /**
     * This method sums the totals of each accounting line, making an entry in a map
     * for each unique pay fiscal year and pay period.
     * 
     * @param accountingLines
     * @return
     */
    private Map sumAccountingLineAmountsByPayFYAndPayPeriod(List accountingLines){
        
        ExpenseTransferAccountingLine line = null; 
        KualiDecimal linesAmount = new KualiDecimal(0);
        Map linesMap = new HashMap();
        String payFYPeriodKey = null;
        
        //go through source lines adding amounts to appropriate place in map
        for (Iterator i = accountingLines.iterator(); i.hasNext();) {
            //initialize
            line = (ExpenseTransferAccountingLine) i.next();
            linesAmount = new KualiDecimal(0);
            
            //create hash key
            payFYPeriodKey = createPayFYPeriodKey(
                    line.getPayrollEndDateFiscalYear(), line.getPayrollEndDateFiscalPeriodCode()); 
            
            //if entry exists, pull from hash
            if ( linesMap.containsKey(payFYPeriodKey) ){
                linesAmount = (KualiDecimal)linesMap.get(payFYPeriodKey);                
            }
                        
            //update and store
            linesAmount = linesAmount.add(line.getAmount());            
            linesMap.put(payFYPeriodKey, linesAmount);            
        }
        
        return linesMap;        
    }
    
    /**
     * This method checks that the total amount of labor ledger accounting lines
     * in the document's FROM section is equal to the total amount on the labor ledger
     * accounting lines TO section for each unique combination of pay fiscal year and pay period.
     * A value of true is returned if all amounts for each unique combination between source and target
     * accounting lines match, false otherwise. 
     *  
     * @param sourceLinesMap
     * @param targetLinesMap
     * @return
     */
    private boolean compareAccountingLineTotalsByPayFYAndPayPeriod(Map sourceLinesMap, Map targetLinesMap){
    
        boolean isValid = true;
        Map.Entry entry = null;
        String currentKey = null;
        KualiDecimal sourceLinesAmount = new KualiDecimal(0);
        KualiDecimal targetLinesAmount = new KualiDecimal(0);

        
        //Loop through source lines comparing against target lines
        for(Iterator i=sourceLinesMap.entrySet().iterator(); i.hasNext() && isValid;){
            //initialize
            entry = (Map.Entry)i.next();
            currentKey = (String)entry.getKey();
            sourceLinesAmount = (KualiDecimal)entry.getValue();
            
            if( targetLinesMap.containsKey( currentKey ) ){
                targetLinesAmount = (KualiDecimal)targetLinesMap.get(currentKey);

                //return false if the matching key values do not total each other
                if ( sourceLinesAmount.compareTo(targetLinesAmount) != 0 ) {
                    isValid = false;                
                }

            }else{
                isValid = false;                
            }            
        }
        
        /* Now loop through target lines comparing against source lines.
         * This finds missing entries from either direction (source or target)
         */
        for(Iterator i=targetLinesMap.entrySet().iterator(); i.hasNext() && isValid;){
            //initialize
            entry = (Map.Entry)i.next();
            currentKey = (String)entry.getKey();
            targetLinesAmount = (KualiDecimal)entry.getValue();
            
            if( sourceLinesMap.containsKey( currentKey ) ){
                sourceLinesAmount = (KualiDecimal)sourceLinesMap.get(currentKey);

                //return false if the matching key values do not total each other
                if ( targetLinesAmount.compareTo(sourceLinesAmount) != 0 ) {
                    isValid = false;                                
                }
                
            }else{
                isValid = false;                
            }            
        }
        
        
        return isValid;    
    }
        
    /**
     * Overriding hook into generate general ledger pending entries, so no GL pending entries are created.
     * 
     * @see org.kuali.core.rule.GenerateGeneralLedgerPendingEntriesRule#processGenerateGeneralLedgerPendingEntries(org.kuali.core.document.AccountingDocument, org.kuali.core.bo.AccountingLine, org.kuali.core.util.GeneralLedgerPendingEntrySequenceHelper)
     */
    @Override
    public boolean processGenerateGeneralLedgerPendingEntries(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {        
        return true;
    }

    /**
     * This method is the starting point for creating labor ledger pending entries.
     * The logic used to create the LLPEs resides in this method.
     *  
     * @param accountingDocument
     * @param accountingLine
     * @param sequenceHelper
     * @return
     */
    public boolean processGenerateLaborLedgerPendingEntries(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper){
        boolean success = true;

        LOG.info("started processGenerateLaborLedgerPendingEntries");
                
        ExpenseTransferAccountingLine al = (ExpenseTransferAccountingLine)accountingLine;
        Collection<PositionObjectBenefit> positionObjectBenefits;
        
        //setup default values, so they don't have to be set multiple times
        PendingLedgerEntry defaultEntry = new PendingLedgerEntry();        
        populateDefaultLaborLedgerPendingEntry(accountingDocument, accountingLine, defaultEntry);

        //Generate orig entry
        PendingLedgerEntry originalEntry = (PendingLedgerEntry) ObjectUtils.deepCopy(defaultEntry);
        success &= processOriginalLaborLedgerPendingEntry(accountingDocument, sequenceHelper, accountingLine, originalEntry);
            
        //if the AL's pay FY and period do not match the University fiscal year and period
        if( isAccountingLinePayFYPeriodMatchesUniversityPayFYPeriod(accountingDocument, accountingLine) ){    
            //Generate A21
            PendingLedgerEntry a21Entry = (PendingLedgerEntry) ObjectUtils.deepCopy(defaultEntry);
            success &= processA21LaborLedgerPendingEntry(accountingDocument, sequenceHelper, accountingLine, a21Entry);            
        }
        
        //Generate A21 rev
        PendingLedgerEntry a21RevEntry = (PendingLedgerEntry) ObjectUtils.deepCopy(defaultEntry);
        success &= processA21RevLaborLedgerPendingEntry(accountingDocument, sequenceHelper, accountingLine, a21RevEntry);
        
        //retrieve the labor object if null
        if( ObjectUtils.isNull(al.getLaborObject()) ){
            al.refreshReferenceObject("laborObject");    
        }
        
        //if AL object code is a salary object code
        if( StringUtils.equals(al.getLaborObject().getFinancialObjectFringeOrSalaryCode(), LABOR_LEDGER_SALARY_CODE) ){
            //get benefits
            positionObjectBenefits = SpringServiceLocator.getLaborPositionObjectBenefitService().getPositionObjectBenefits(al.getPayrollEndDateFiscalYear(), al.getChartOfAccountsCode(), al.getFinancialObjectCode());            
            
            //for each row in the ld_lbr_obj_bene_t table for the labor ledger AL's pay FY, chart and object code            
            for (PositionObjectBenefit pob : positionObjectBenefits){

                //fringe benefit code
                String fringeBenefitObjectCode = pob.getBenefitsCalculation().getPositionFringeBenefitObjectCode();
                
                //calculate the benefit amount (ledger amt * (benfit pct/100) )
                KualiDecimal benefitAmount = pob.getBenefitsCalculation().getPositionFringeBenefitPercent();                
                benefitAmount = benefitAmount.divide(new KualiDecimal(100));
                benefitAmount = benefitAmount.multiply(al.getAmount());
                
                //Generate Benefit
                PendingLedgerEntry benefitEntry = (PendingLedgerEntry) ObjectUtils.deepCopy(defaultEntry);
                success &= processBenefitLaborLedgerPendingEntry(accountingDocument, sequenceHelper, accountingLine, benefitEntry, benefitAmount, fringeBenefitObjectCode);                    
                
                //if the AL's pay FY and period do not match the University fiscal year and period
                if( isAccountingLinePayFYPeriodMatchesUniversityPayFYPeriod(accountingDocument, accountingLine) ){
                    //Generate Benefit A21
                    PendingLedgerEntry benefitA21Entry = (PendingLedgerEntry) ObjectUtils.deepCopy(defaultEntry);
                    success &= processBenefitA21LaborLedgerPendingEntry(accountingDocument, sequenceHelper, accountingLine, benefitA21Entry, benefitAmount, fringeBenefitObjectCode);                    
                }
                
                //Generate Benefit A21 rev
                PendingLedgerEntry benefitA21RevEntry = (PendingLedgerEntry) ObjectUtils.deepCopy(defaultEntry);
                success &= processBenefitA21RevLaborLedgerPendingEntry(accountingDocument, sequenceHelper, accountingLine, benefitA21RevEntry, benefitAmount, fringeBenefitObjectCode);                
            }
            
        }                            
                                        
        LOG.info("completed processGenerateLaborLedgerPendingEntries");
        
        return true;
    }
    
    /**
     * This method generates benefit clearing and pending entries when the sum of the amount for the source accounting lines by benefit type
     * does not match the sum of the amount for the target accountine lines by benefit type. 
     * 
     * @param AccountingDocument
     * @param sequenceHelper
     * @return
     */
    public boolean processGenerateLaborLedgerBenefitClearingPendingEntries(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper) {

        LOG.info("started processGenerateLaborLedgerBenefitClearingPendingEntries");
        
        Collection<PositionObjectBenefit> positionObjectBenefits;
        
        Map sourceBenefitAmountSumByBenefitType = new HashMap();
        Map targetBenefitAmountSumByBenefitType = new HashMap();

        ExpenseTransferAccountingLine sourceAL = null;
        ExpenseTransferAccountingLine targetAL = null;
        
        List sourceLines = new ArrayList();
        List targetLines = new ArrayList();

        //set source and target accounting lines
        sourceLines.addAll(accountingDocument.getSourceAccountingLines());
        targetLines.addAll(accountingDocument.getTargetAccountingLines());

        Collection<BenefitsType> benefitsType;
                
        //retrieve all benefits type
        benefitsType = SpringServiceLocator.getLaborBenefitsTypeService().getBenefitsType();
        
        KualiDecimal amount = new KualiDecimal(0);
        
        //loop through all source lines, and add to array where benefit type matches
        for (BenefitsType bt : benefitsType){

            for(int i = 0; i < sourceLines.size(); i++){
               
                sourceAL = (ExpenseTransferAccountingLine)sourceLines.get(i);
                
                //get related benefit objects
                positionObjectBenefits = SpringServiceLocator.getLaborPositionObjectBenefitService().getPositionObjectBenefits(sourceAL.getPayrollEndDateFiscalYear(), sourceAL.getChartOfAccountsCode(), sourceAL.getFinancialObjectCode());
            
                //loop through all of this accounting lines benefit type objects, matching with the outer benefit object
                for (PositionObjectBenefit pob : positionObjectBenefits){
                    
                    if( StringUtils.equals(pob.getFinancialObjectBenefitsTypeCode(), bt.getPositionBenefitTypeCode()) ){
                        
                        //take out existing amount and add to it, or store amount if not in the map yet
                        if( sourceBenefitAmountSumByBenefitType.containsKey(pob.getFinancialObjectBenefitsTypeCode()) ){
                            amount = (KualiDecimal)sourceBenefitAmountSumByBenefitType.get(pob.getFinancialObjectBenefitsTypeCode());
                            amount = amount.add(sourceAL.getAmount());
                        }else{
                            amount = sourceAL.getAmount();
                        }
                        
                        //add amount with object code key back into map
                        sourceBenefitAmountSumByBenefitType.put(pob.getFinancialObjectBenefitsTypeCode(), amount);
                    }
                }
            }
             
            for(int i = 0; i < targetLines.size(); i++){
                    
                targetAL = (ExpenseTransferAccountingLine)targetLines.get(i);                                        

                //get related benefit objects
                positionObjectBenefits = SpringServiceLocator.getLaborPositionObjectBenefitService().getPositionObjectBenefits(targetAL.getPayrollEndDateFiscalYear(), targetAL.getChartOfAccountsCode(), targetAL.getFinancialObjectCode());
            
                //loop through all of this accounting lines benefit type objects, matching with the outer benefit object
                for (PositionObjectBenefit pob : positionObjectBenefits){
               
                        if( StringUtils.equals(pob.getFinancialObjectBenefitsTypeCode(), bt.getPositionBenefitTypeCode()) ){

                            //take out existing amount and add to it, or store amount if not in the map yet
                            if( targetBenefitAmountSumByBenefitType.containsKey(pob.getFinancialObjectBenefitsTypeCode()) ){
                                amount = (KualiDecimal)targetBenefitAmountSumByBenefitType.get(pob.getFinancialObjectBenefitsTypeCode());
                                amount = amount.add(targetAL.getAmount());                                
                            }else{
                                amount = targetAL.getAmount();
                            }
                            
                            //add amount with object code key back into map
                            targetBenefitAmountSumByBenefitType.put(pob.getFinancialObjectBenefitsTypeCode(), amount);
                        }
               }
            }
            
            
        }
                
        /*
         * with arrays filled with amounts by benefit type, 
         * generate benefit clearing entries for each benefit type with the amounts from target and source
         */
        KualiDecimal sourceBenefitAmount = new KualiDecimal(0);
        KualiDecimal targetBenefitAmount = new KualiDecimal(0);
        String currentKey = "";
        Map.Entry entry = null;
        
        //Loop through source amounts
        for(Iterator i=sourceBenefitAmountSumByBenefitType.entrySet().iterator(); i.hasNext();){
            //initialize
            entry = (Map.Entry)i.next();
            currentKey = (String)entry.getKey();
            sourceBenefitAmount = (KualiDecimal)entry.getValue();
            
            //if the target map has an entry for the current benefit type, process both amounts
            if( targetBenefitAmountSumByBenefitType.containsKey( currentKey ) ){                
                targetBenefitAmount = (KualiDecimal)targetBenefitAmountSumByBenefitType.get(currentKey);
            }else{
                targetBenefitAmount = new KualiDecimal(0);                
            }            
            
            //only process if amounts are not the same
            if(sourceBenefitAmount.equals(targetBenefitAmount) == false){
                //process for each source amount and possibly a target amount
                processBenefitClearingLaborLedgerPendingEntry(accountingDocument, sequenceHelper, currentKey, sourceBenefitAmount, targetBenefitAmount );
            }
        }
        
        //Loop through target amounts
        for(Iterator i=targetBenefitAmountSumByBenefitType.entrySet().iterator(); i.hasNext();){
            //initialize
            entry = (Map.Entry)i.next();
            currentKey = (String)entry.getKey();
            targetBenefitAmount = (KualiDecimal)entry.getValue();
            
            //if the source map has an entry for the current benefit type, process both amounts
            if( sourceBenefitAmountSumByBenefitType.containsKey( currentKey ) ){                
                //Do nothing, we've already processed the case of both matching
            }else{
                sourceBenefitAmount = new KualiDecimal(0);
                
                //only process if amounts are not the same
                if(sourceBenefitAmount.equals(targetBenefitAmount) == false){
                    //process only the target amounts that don't match a source for completeness
                    processBenefitClearingLaborLedgerPendingEntry(accountingDocument, sequenceHelper, currentKey, sourceBenefitAmount, targetBenefitAmount );
                }
            }                        
        }                

        LOG.info("completed processGenerateLaborLedgerBenefitClearingPendingEntries");
        
        return true;
    }

    /**
     * This method compares the pay fiscal year and period from the
     * accounting line and the university values.  A true is returned
     * if the values match.
     *      
     * @param transactionalDocument
     * @param accountingLine
     * @return
     */
    private boolean isAccountingLinePayFYPeriodMatchesUniversityPayFYPeriod(AccountingDocument accountingDocument, AccountingLine accountingLine){
        boolean success = true;
        
        AccountingPeriod ap = accountingDocument.getAccountingPeriod();
        ExpenseTransferAccountingLine al = (ExpenseTransferAccountingLine)accountingLine;
        
        //if the AL's pay FY and period do not match the University fiscal year and period
        if( !(ap.getUniversityFiscalYear().equals(al.getPayrollEndDateFiscalYear()) &&
             ap.getUniversityFiscalPeriodCode().equals(al.getPayrollEndDateFiscalPeriodCode()) ) ){
            success = false;
        }
        
        return success;
    }

    /**
     * This method returns the accounting line's chart code if it accepts fringe benefits,
     * otherwise the report to chart is returned.
     *   
     * @param accountingLine
     * @return
     */
    private String getLaborLedgerPendingEntryBenefitChart(AccountingLine accountingLine){
        String chart = null;
        
        if(accountingLine.getAccount().isAccountsFringesBnftIndicator()){
            chart = accountingLine.getChartOfAccountsCode();
        }else{
            chart = accountingLine.getAccount().getReportsToChartOfAccountsCode();
        }
        
        return chart;
    }

    /**
     * This method returns the accounting line's account number if it accepts fringe benefits,
     *   
     * @param accountingLine
     * @return
     */
    private String getLaborLedgerPendingEntryBenefitAccount(AccountingLine accountingLine){
        String accountNumber = null;

        if(accountingLine.getAccount().isAccountsFringesBnftIndicator()){
            accountNumber = accountingLine.getAccountNumber();
        }else{
            accountNumber = accountingLine.getAccount().getReportsToAccountNumber();
        }

        return accountNumber;
    }

    /**
     * This method populates common fields amongst the different LLPE use cases.
     *      
     * @param transactionalDocument
     * @param accountingLine
     * @param sequenceHelper
     * @param originalEntry
     */
    private void populateDefaultLaborLedgerPendingEntry(AccountingDocument transactionalDocument, AccountingLine accountingLine, PendingLedgerEntry defaultEntry){

        //the same across all types
        ObjectCode objectCode = accountingLine.getObjectCode();
        if (ObjectUtils.isNull(objectCode)) {
            accountingLine.refreshReferenceObject("objectCode");
        }
        defaultEntry.setFinancialObjectTypeCode(accountingLine.getObjectCode().getFinancialObjectTypeCode());
        defaultEntry.setFinancialDocumentTypeCode(SpringServiceLocator.getDocumentTypeService().getDocumentTypeCodeByClass(transactionalDocument.getClass()));
        defaultEntry.setFinancialSystemOriginationCode(SpringServiceLocator.getHomeOriginationService().getHomeOrigination().getFinSystemHomeOriginationCode());
        defaultEntry.setDocumentNumber(accountingLine.getDocumentNumber());
        defaultEntry.setTransactionLedgerEntryDescription(getEntryValue(accountingLine.getFinancialDocumentLineDescription(), transactionalDocument.getDocumentHeader().getFinancialDocumentDescription()));                
        defaultEntry.setOrganizationDocumentNumber(transactionalDocument.getDocumentHeader().getOrganizationDocumentNumber());
        defaultEntry.setFinancialDocumentReversalDate(null);
        defaultEntry.setReferenceFinancialSystemOriginationCode(null);
        defaultEntry.setReferenceFinancialDocumentNumber(null);
        defaultEntry.setReferenceFinancialDocumentTypeCode(null);
                              
    }

    protected boolean processOriginalLaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, AccountingLine accountingLine, PendingLedgerEntry originalEntry) {        
        
        boolean success = true;
        
        // populate the entry
        populateOriginalLaborLedgerPendingEntry(accountingDocument, accountingLine, sequenceHelper, originalEntry);

        // hook for children documents to implement document specific LLPE field mappings
        customizeOriginalLaborLedgerPendingEntry(accountingDocument, accountingLine, originalEntry);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(originalEntry);

        return success;
    }

    protected boolean customizeOriginalLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, PendingLedgerEntry originalEntry) {
        return true;
    }

    /**
     * 
     * This method gets the next sequence number and increments.
     * 
     * @param sequenceHelper
     * @return
     */
    private Integer getNextSequenceNumber(GeneralLedgerPendingEntrySequenceHelper sequenceHelper){

         //get sequence number and increment
        Integer next = sequenceHelper.getSequenceCounter();
        sequenceHelper.increment();
        
        return next;
    }
    
    protected void populateOriginalLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry originalEntry) {        

        originalEntry.setUniversityFiscalYear(null);
        originalEntry.setUniversityFiscalPeriodCode(null);
        originalEntry.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
        originalEntry.setAccountNumber(accountingLine.getAccountNumber());
        originalEntry.setSubAccountNumber(getEntryValue(accountingLine.getSubAccountNumber(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER));
        originalEntry.setFinancialObjectCode(accountingLine.getFinancialObjectCode());
        originalEntry.setFinancialSubObjectCode(getEntryValue(accountingLine.getFinancialSubObjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE));
        originalEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_ACTUAL); // this is the default that most documents use
        originalEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));
        originalEntry.setTransactionLedgerEntryAmount(getGeneralLedgerPendingEntryAmountForAccountingLine(accountingLine));
        originalEntry.setTransactionDebitCreditCode( accountingLine.isSourceAccountingLine() ? Constants.GL_CREDIT_CODE : Constants.GL_DEBIT_CODE);
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        originalEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        originalEntry.setProjectCode(getEntryValue(accountingLine.getProjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING));
        originalEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
        originalEntry.setPositionNumber( ((ExpenseTransferAccountingLine)accountingLine).getPositionNumber() );
        originalEntry.setEmplid( ((ExpenseTransferAccountingLine)accountingLine).getEmplid() );
        originalEntry.setPayrollEndDateFiscalYear( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalYear() );
        originalEntry.setPayrollEndDateFiscalPeriodCode( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalPeriodCode() );
        originalEntry.setTransactionTotalHours( ((ExpenseTransferAccountingLine)accountingLine).getPayrollTotalHours() );
        
        originalEntry.setReferenceFinancialSystemOriginationCode(null);
        originalEntry.setReferenceFinancialDocumentNumber(null);
        originalEntry.setReferenceFinancialDocumentTypeCode(null);
                      
        // TODO wait for core budget year data structures to be put in place
        // originalEntry.setBudgetYear(accountingLine.getBudgetYear());
        // originalEntry.setBudgetYearFundingSourceCode(budgetYearFundingSourceCode);
    }
    
    protected boolean processA21LaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, AccountingLine accountingLine, PendingLedgerEntry a21Entry) {        

        boolean success = true;
        
        // populate the entry
        populateA21LaborLedgerPendingEntry(accountingDocument, accountingLine, sequenceHelper, a21Entry);

        // hook for children documents to implement document specific LLPE field mappings
        customizeA21LaborLedgerPendingEntry(accountingDocument, accountingLine, a21Entry);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(a21Entry);

        return success;
    }

    protected boolean customizeA21LaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, PendingLedgerEntry a21Entry) {
        return true;
    }

    protected void populateA21LaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry a21Entry) {        
        a21Entry.setUniversityFiscalYear(null);
        a21Entry.setUniversityFiscalPeriodCode(null);
        a21Entry.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
        a21Entry.setAccountNumber(accountingLine.getAccountNumber());
        a21Entry.setSubAccountNumber(getEntryValue(accountingLine.getSubAccountNumber(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER));
        a21Entry.setFinancialObjectCode(accountingLine.getFinancialObjectCode());
        a21Entry.setFinancialSubObjectCode(getEntryValue(accountingLine.getFinancialSubObjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE));
        a21Entry.setFinancialBalanceTypeCode(BALANCE_TYPE_A21);
        a21Entry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));
        a21Entry.setTransactionLedgerEntryAmount(getGeneralLedgerPendingEntryAmountForAccountingLine(accountingLine));
        a21Entry.setTransactionDebitCreditCode( accountingLine.isSourceAccountingLine() ? Constants.GL_DEBIT_CODE : Constants.GL_CREDIT_CODE);
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        a21Entry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        a21Entry.setProjectCode(getEntryValue(accountingLine.getProjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING));
        a21Entry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
        a21Entry.setPositionNumber( ((ExpenseTransferAccountingLine)accountingLine).getPositionNumber() );
        a21Entry.setEmplid( ((ExpenseTransferAccountingLine)accountingLine).getEmplid() );
        a21Entry.setPayrollEndDateFiscalYear( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalYear() );
        a21Entry.setPayrollEndDateFiscalPeriodCode( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalPeriodCode() );
        a21Entry.setTransactionTotalHours( ((ExpenseTransferAccountingLine)accountingLine).getPayrollTotalHours() );
        
        a21Entry.setReferenceFinancialSystemOriginationCode(null);
        a21Entry.setReferenceFinancialDocumentNumber(null);
        a21Entry.setReferenceFinancialDocumentTypeCode(null);
    }

    protected boolean processA21RevLaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, AccountingLine accountingLine, PendingLedgerEntry a21RevEntry) {        

        boolean success = true;
        
        // populate the entry
        populateA21RevLaborLedgerPendingEntry(accountingDocument, accountingLine, sequenceHelper, a21RevEntry);

        // hook for children documents to implement document specific LLPE field mappings
        customizeA21RevLaborLedgerPendingEntry(accountingDocument, accountingLine, a21RevEntry);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(a21RevEntry);
        
        return success;
    }

    protected boolean customizeA21RevLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, PendingLedgerEntry a21RevEntry) {
        return true;
    }

    protected void populateA21RevLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry a21RevEntry) {
        
        ExpenseTransferAccountingLine al = (ExpenseTransferAccountingLine)accountingLine;
        
        a21RevEntry.setUniversityFiscalYear(al.getPayrollEndDateFiscalYear());
        a21RevEntry.setUniversityFiscalPeriodCode(al.getPayrollEndDateFiscalPeriodCode());
        a21RevEntry.setChartOfAccountsCode(accountingLine.getChartOfAccountsCode());
        a21RevEntry.setAccountNumber(accountingLine.getAccountNumber());
        a21RevEntry.setSubAccountNumber(getEntryValue(accountingLine.getSubAccountNumber(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER));
        a21RevEntry.setFinancialObjectCode(accountingLine.getFinancialObjectCode());
        a21RevEntry.setFinancialSubObjectCode(getEntryValue(accountingLine.getFinancialSubObjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE));
        a21RevEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_A21);
        a21RevEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));
        a21RevEntry.setTransactionLedgerEntryAmount(getGeneralLedgerPendingEntryAmountForAccountingLine(accountingLine));
        a21RevEntry.setTransactionDebitCreditCode( accountingLine.isSourceAccountingLine() ? Constants.GL_CREDIT_CODE : Constants.GL_DEBIT_CODE);
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        a21RevEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        a21RevEntry.setProjectCode(getEntryValue(accountingLine.getProjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING));
        a21RevEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
        a21RevEntry.setPositionNumber( ((ExpenseTransferAccountingLine)accountingLine).getPositionNumber() );
        a21RevEntry.setEmplid( ((ExpenseTransferAccountingLine)accountingLine).getEmplid() );
        a21RevEntry.setPayrollEndDateFiscalYear( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalYear() );
        a21RevEntry.setPayrollEndDateFiscalPeriodCode( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalPeriodCode() );
        a21RevEntry.setTransactionTotalHours( ((ExpenseTransferAccountingLine)accountingLine).getPayrollTotalHours() );
        
        a21RevEntry.setReferenceFinancialSystemOriginationCode(null);
        a21RevEntry.setReferenceFinancialDocumentNumber(null);
        a21RevEntry.setReferenceFinancialDocumentTypeCode(null);
    }

    protected boolean processBenefitLaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, AccountingLine accountingLine, PendingLedgerEntry benefitEntry, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {        

        boolean success = true;
        
        // populate the entry
        populateBenefitLaborLedgerPendingEntry(accountingDocument, accountingLine, sequenceHelper, benefitEntry, benefitAmount, fringeBenefitObjectCode);

        // hook for children documents to implement document specific LLPE field mappings
        customizeBenefitLaborLedgerPendingEntry(accountingDocument, accountingLine, benefitEntry);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(benefitEntry);

        return success;
    }

    protected boolean customizeBenefitLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, PendingLedgerEntry benefitEntry) {
        return true;
    }

    protected void populateBenefitLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry benefitEntry, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {       
        benefitEntry.setUniversityFiscalYear(null);
        benefitEntry.setUniversityFiscalPeriodCode(null);
        
        //special handling
        benefitEntry.setChartOfAccountsCode( getLaborLedgerPendingEntryBenefitChart(accountingLine) );
        benefitEntry.setAccountNumber( getLaborLedgerPendingEntryBenefitAccount(accountingLine) );

        //set benefit amount and fringe object code
        benefitEntry.setTransactionLedgerEntryAmount(benefitAmount);
        benefitEntry.setFinancialObjectCode(fringeBenefitObjectCode);        

        benefitEntry.setSubAccountNumber(getEntryValue(accountingLine.getSubAccountNumber(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER));        
        benefitEntry.setFinancialSubObjectCode(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE);
        benefitEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_ACTUAL);
        benefitEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));            
        benefitEntry.setTransactionDebitCreditCode( accountingLine.isSourceAccountingLine() ? Constants.GL_CREDIT_CODE : Constants.GL_DEBIT_CODE);
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        benefitEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        benefitEntry.setProjectCode(getEntryValue(accountingLine.getProjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING));
        benefitEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
        benefitEntry.setPositionNumber(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_POSITION_NUMBER);
        benefitEntry.setEmplid(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_EMPL_ID);
        benefitEntry.setPayrollEndDateFiscalYear( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalYear() );
        benefitEntry.setPayrollEndDateFiscalPeriodCode( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalPeriodCode() );
        benefitEntry.setTransactionTotalHours( ((ExpenseTransferAccountingLine)accountingLine).getPayrollTotalHours() );
        
        benefitEntry.setReferenceFinancialSystemOriginationCode(null);
        benefitEntry.setReferenceFinancialDocumentNumber(null);
        benefitEntry.setReferenceFinancialDocumentTypeCode(null);        
    }

    protected boolean processBenefitA21LaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, AccountingLine accountingLine, PendingLedgerEntry benefitA21Entry, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {        

        boolean success = true;
        
        // populate the entry
        populateBenefitA21LaborLedgerPendingEntry(accountingDocument, accountingLine, sequenceHelper, benefitA21Entry, benefitAmount, fringeBenefitObjectCode);

        // hook for children documents to implement document specific LLPE field mappings
        customizeBenefitA21LaborLedgerPendingEntry(accountingDocument, accountingLine, benefitA21Entry);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(benefitA21Entry);

        return success;
    }

    protected boolean customizeBenefitA21LaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, PendingLedgerEntry benefitA21Entry) {
        return true;
    }

    protected void populateBenefitA21LaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry benefitA21Entry, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {        
        benefitA21Entry.setUniversityFiscalYear(null);
        benefitA21Entry.setUniversityFiscalPeriodCode(null);
        
        //special handling
        benefitA21Entry.setChartOfAccountsCode( getLaborLedgerPendingEntryBenefitChart(accountingLine) );
        benefitA21Entry.setAccountNumber( getLaborLedgerPendingEntryBenefitAccount(accountingLine) );

        //set benefit amount and fringe object code
        benefitA21Entry.setTransactionLedgerEntryAmount(benefitAmount);
        benefitA21Entry.setFinancialObjectCode(fringeBenefitObjectCode);        

        benefitA21Entry.setSubAccountNumber(getEntryValue(accountingLine.getSubAccountNumber(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER));        
        benefitA21Entry.setFinancialSubObjectCode(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE);
        benefitA21Entry.setFinancialBalanceTypeCode(BALANCE_TYPE_A21);
        benefitA21Entry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));            
        benefitA21Entry.setTransactionDebitCreditCode( accountingLine.isSourceAccountingLine() ? Constants.GL_DEBIT_CODE : Constants.GL_CREDIT_CODE);
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        benefitA21Entry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        benefitA21Entry.setProjectCode(getEntryValue(accountingLine.getProjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING));
        benefitA21Entry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
        benefitA21Entry.setPositionNumber(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_POSITION_NUMBER);
        benefitA21Entry.setEmplid(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_EMPL_ID);
        benefitA21Entry.setPayrollEndDateFiscalYear( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalYear() );
        benefitA21Entry.setPayrollEndDateFiscalPeriodCode( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalPeriodCode() );
        benefitA21Entry.setTransactionTotalHours( ((ExpenseTransferAccountingLine)accountingLine).getPayrollTotalHours() );
        
        benefitA21Entry.setReferenceFinancialSystemOriginationCode(null);
        benefitA21Entry.setReferenceFinancialDocumentNumber(null);
        benefitA21Entry.setReferenceFinancialDocumentTypeCode(null);        
    }

    protected boolean processBenefitA21RevLaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, AccountingLine accountingLine, PendingLedgerEntry benefitA21RevEntry, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {        

        boolean success = true;
        
        // populate the entry
        populateBenefitA21RevLaborLedgerPendingEntry(accountingDocument, accountingLine, sequenceHelper, benefitA21RevEntry, benefitAmount, fringeBenefitObjectCode);

        // hook for children documents to implement document specific LLPE field mappings
        customizeBenefitA21RevLaborLedgerPendingEntry(accountingDocument, accountingLine, benefitA21RevEntry);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(benefitA21RevEntry);

        return success;
    }

    protected boolean customizeBenefitA21RevLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, PendingLedgerEntry benefitA21RevEntry) {
        return true;
    }

    protected void populateBenefitA21RevLaborLedgerPendingEntry(AccountingDocument accountingDocument, AccountingLine accountingLine, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry benefitA21RevEntry, KualiDecimal benefitAmount, String fringeBenefitObjectCode) {        
        ExpenseTransferAccountingLine al = (ExpenseTransferAccountingLine)accountingLine;

        benefitA21RevEntry.setUniversityFiscalYear(al.getPayrollEndDateFiscalYear());
        benefitA21RevEntry.setUniversityFiscalPeriodCode(al.getPayrollEndDateFiscalPeriodCode());
        
        //special handling
        benefitA21RevEntry.setChartOfAccountsCode( getLaborLedgerPendingEntryBenefitChart(accountingLine) );
        benefitA21RevEntry.setAccountNumber( getLaborLedgerPendingEntryBenefitAccount(accountingLine) );

        //set benefit amount and fringe object code
        benefitA21RevEntry.setTransactionLedgerEntryAmount(benefitAmount);
        benefitA21RevEntry.setFinancialObjectCode(fringeBenefitObjectCode);        

        benefitA21RevEntry.setSubAccountNumber(getEntryValue(accountingLine.getSubAccountNumber(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER));        
        benefitA21RevEntry.setFinancialSubObjectCode(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE);
        benefitA21RevEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_A21);
        benefitA21RevEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));            
        benefitA21RevEntry.setTransactionDebitCreditCode( accountingLine.isSourceAccountingLine() ? Constants.GL_DEBIT_CODE : Constants.GL_CREDIT_CODE);
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        benefitA21RevEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        benefitA21RevEntry.setProjectCode(getEntryValue(accountingLine.getProjectCode(), LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING));
        benefitA21RevEntry.setOrganizationReferenceId(accountingLine.getOrganizationReferenceId());
        benefitA21RevEntry.setPositionNumber(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_POSITION_NUMBER);
        benefitA21RevEntry.setEmplid(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_EMPL_ID);
        benefitA21RevEntry.setPayrollEndDateFiscalYear( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalYear() );
        benefitA21RevEntry.setPayrollEndDateFiscalPeriodCode( ((ExpenseTransferAccountingLine)accountingLine).getPayrollEndDateFiscalPeriodCode() );
        benefitA21RevEntry.setTransactionTotalHours( ((ExpenseTransferAccountingLine)accountingLine).getPayrollTotalHours() );
        
        benefitA21RevEntry.setReferenceFinancialSystemOriginationCode(null);
        benefitA21RevEntry.setReferenceFinancialDocumentNumber(null);
        benefitA21RevEntry.setReferenceFinancialDocumentTypeCode(null);        
    }

    protected boolean processBenefitClearingLaborLedgerPendingEntry(AccountingDocument accountingDocument, GeneralLedgerPendingEntrySequenceHelper sequenceHelper, String benefitTypeCode, KualiDecimal fromAmount, KualiDecimal toAmount) {        

        boolean success = true;
        
        PendingLedgerEntry benefitClearingEntry = new PendingLedgerEntry();
        
        // populate the entry
        populateBenefitClearingLaborLedgerPendingEntry(accountingDocument, sequenceHelper, benefitClearingEntry, benefitTypeCode, fromAmount, toAmount);

        // hook for children documents to implement document specific LLPE field mappings
        customizeBenefitClearingLaborLedgerPendingEntry(accountingDocument, benefitClearingEntry, fromAmount, toAmount);

        // add the new entry to the document now
        ((SalaryExpenseTransferDocument)accountingDocument).getLaborLedgerPendingEntries().add(benefitClearingEntry);

        return success;
    }

    protected boolean customizeBenefitClearingLaborLedgerPendingEntry(AccountingDocument accountingDocument, PendingLedgerEntry benefitClearingEntry, KualiDecimal fromAmount, KualiDecimal toAmount) {
        return true;
    }

    protected void populateBenefitClearingLaborLedgerPendingEntry(AccountingDocument accountingDocument,  GeneralLedgerPendingEntrySequenceHelper sequenceHelper, PendingLedgerEntry benefitClearingEntry, String benefitTypeCode, KualiDecimal fromAmount, KualiDecimal toAmount) {        

        benefitClearingEntry.setUniversityFiscalYear(null);
        benefitClearingEntry.setUniversityFiscalPeriodCode(null);
        
        //special handling
        benefitClearingEntry.setChartOfAccountsCode( LABOR_LEDGER_CHART_OF_ACCOUNT_CODE );
        benefitClearingEntry.setAccountNumber( LABOR_LEDGER_ACCOUNT_NUMBER );
                
        benefitClearingEntry.setSubAccountNumber(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_ACCOUNT_NUMBER);        

        //special handling
        AccountingPeriod ap = accountingDocument.getAccountingPeriod();
        BenefitsCalculation bc = SpringServiceLocator.getLaborBenefitsCalculationService().getBenefitsCalculation(ap.getUniversityFiscalYear(), "UA", benefitTypeCode);
        benefitClearingEntry.setFinancialObjectCode(bc.getPositionFringeBenefitObjectCode());        
        
        benefitClearingEntry.setFinancialSubObjectCode(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_SUB_OBJECT_CODE);
        benefitClearingEntry.setFinancialBalanceTypeCode(BALANCE_TYPE_ACTUAL);
        benefitClearingEntry.setTransactionLedgerEntrySequenceNumber(getNextSequenceNumber(sequenceHelper));            
        
        //special handling, set the transaction amount to the absolute value of the from minus the to amount
        KualiDecimal amount = fromAmount.subtract(toAmount);
        benefitClearingEntry.setTransactionLedgerEntryAmount( amount.abs() );

        //special handling
        String debitCreditCode = Constants.GL_CREDIT_CODE;
        if( fromAmount.isGreaterThan(toAmount) ){
            debitCreditCode = Constants.GL_DEBIT_CODE;
        }
        benefitClearingEntry.setTransactionDebitCreditCode( debitCreditCode );
        
        Timestamp transactionTimestamp = new Timestamp(SpringServiceLocator.getDateTimeService().getCurrentDate().getTime());
        benefitClearingEntry.setTransactionDate(new java.sql.Date(transactionTimestamp.getTime()));
        benefitClearingEntry.setProjectCode(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_PROJECT_STRING);
        benefitClearingEntry.setOrganizationReferenceId(null);
        benefitClearingEntry.setPositionNumber(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_POSITION_NUMBER);
        benefitClearingEntry.setEmplid(LABOR_LEDGER_PENDING_ENTRY_CODE.BLANK_EMPL_ID);
        benefitClearingEntry.setPayrollEndDateFiscalYear( ap.getUniversityFiscalYear() );
        benefitClearingEntry.setPayrollEndDateFiscalPeriodCode( ap.getUniversityFiscalPeriodCode() );
        benefitClearingEntry.setTransactionTotalHours( null );                     
        benefitClearingEntry.setReferenceFinancialSystemOriginationCode(null);
        benefitClearingEntry.setReferenceFinancialDocumentNumber(null);
        benefitClearingEntry.setReferenceFinancialDocumentTypeCode(null);                
                
        //special handling
        ObjectCode oc = SpringServiceLocator.getObjectCodeService().getByPrimaryId(ap.getUniversityFiscalYear(), LABOR_LEDGER_CHART_OF_ACCOUNT_CODE, bc.getPositionFringeBenefitObjectCode());
        benefitClearingEntry.setFinancialObjectTypeCode(oc.getFinancialObjectTypeCode());
        
        //defaults
        benefitClearingEntry.setFinancialDocumentTypeCode(SpringServiceLocator.getDocumentTypeService().getDocumentTypeCodeByClass(accountingDocument.getClass()));
        benefitClearingEntry.setFinancialSystemOriginationCode(SpringServiceLocator.getHomeOriginationService().getHomeOrigination().getFinSystemHomeOriginationCode());
        benefitClearingEntry.setDocumentNumber(accountingDocument.getDocumentNumber());
        benefitClearingEntry.setTransactionLedgerEntryDescription(accountingDocument.getDocumentHeader().getFinancialDocumentDescription());                
        benefitClearingEntry.setOrganizationDocumentNumber(accountingDocument.getDocumentHeader().getOrganizationDocumentNumber());
        benefitClearingEntry.setFinancialDocumentReversalDate(null); 
    }
    
    /**
     * This is responsible for properly negating the sign on an accounting line's amount when its associated document is an error
     * correction.
     * 
     * @param accountingDocument
     * @param accountingLine
     */
    private final void handleDocumentErrorCorrection(AccountingDocument accountingDocument, AccountingLine accountingLine) {
        // If the document corrects another document, make sure the accounting line has the correct sign.
        if ((null == accountingDocument.getDocumentHeader().getFinancialDocumentInErrorNumber() && accountingLine.getAmount().isNegative()) || (null != accountingDocument.getDocumentHeader().getFinancialDocumentInErrorNumber() && accountingLine.getAmount().isPositive())) {
            accountingLine.setAmount(accountingLine.getAmount().multiply(new KualiDecimal(1)));
        }
    }

    /**
     * Applies the given flexible offset account to the given offset entry. Does nothing if flexibleOffsetAccount is null or its COA
     * and account number are the same as the offset entry's.
     * 
     * @param flexibleOffsetAccount may be null
     * @param offsetEntry may be modified
     */
    private static void flexOffsetAccountIfNecessary(OffsetAccount flexibleOffsetAccount, PendingLedgerEntry offsetEntry) {
        if (flexibleOffsetAccount == null) {
            return; // They are not required and may also be disabled.
        }
        String flexCoa = flexibleOffsetAccount.getFinancialOffsetChartOfAccountCode();
        String flexAccountNumber = flexibleOffsetAccount.getFinancialOffsetAccountNumber();
        if (flexCoa.equals(offsetEntry.getChartOfAccountsCode()) && flexAccountNumber.equals(offsetEntry.getAccountNumber())) {
            return; // no change, so leave sub-account as is
        }
        if (ObjectUtils.isNull(flexibleOffsetAccount.getFinancialOffsetAccount())) {
            throw new ReferentialIntegrityException("flexible offset account " + flexCoa + "-" + flexAccountNumber);
        }
        offsetEntry.setChartOfAccountsCode(flexCoa);
        offsetEntry.setAccountNumber(flexAccountNumber);
        // COA and account number are part of the sub-account's key, so the original sub-account would be invalid.
        offsetEntry.setSubAccountNumber(Constants.DASHES_SUB_ACCOUNT_NUMBER);
    }
    

    public boolean isDebit(AccountingDocument accountingDocument, AccountingLine accountingLine) {
        boolean isDebit = false;
        if (accountingLine.isSourceAccountingLine()) {
            isDebit = IsDebitUtils.isDebitConsideringNothingPositiveOnly(this, accountingDocument, accountingLine);
        }
        else if (accountingLine.isTargetAccountingLine()) {
            isDebit = !IsDebitUtils.isDebitConsideringNothingPositiveOnly(this, accountingDocument, accountingLine);
        }
        else {
            throw new IllegalStateException(IsDebitUtils.isInvalidLineTypeIllegalArgumentExceptionMessage);
        }

        return isDebit;
    }

    public boolean isCredit(AccountingLine accountingLine, AccountingDocument accountingDocument) {
        return false;
    }
 
    /**
     * util class that contains common algorithms for determining debit amounts
     * 
     * 
     */
    protected static class IsDebitUtils {
        protected static final String isDebitCalculationIllegalStateExceptionMessage = "an invalid debit/credit check state was detected";
        protected static final String isErrorCorrectionIllegalStateExceptionMessage = "invalid (error correction) document not allowed";
        protected static final String isInvalidLineTypeIllegalArgumentExceptionMessage = "invalid accounting line type";

        /**
         * 
         * @param debitCreditCode
         * @return true if debitCreditCode equals the the debit constant
         */
        static boolean isDebitCode(String debitCreditCode) {
            return StringUtils.equals(Constants.GL_DEBIT_CODE, debitCreditCode);
        }

        /**
         * <ol>
         * <li>object type is included in determining if a line is debit or credit.
         * </ol>
         * the following are credits (return false)
         * <ol>
         * <li> (isIncome || isLiability) && (lineAmount > 0)
         * <li> (isExpense || isAsset) && (lineAmount < 0)
         * </ol>
         * the following are debits (return true)
         * <ol>
         * <li> (isIncome || isLiability) && (lineAmount < 0)
         * <li> (isExpense || isAsset) && (lineAmount > 0)
         * </ol>
         * the following are invalid ( throws an <code>IllegalStateException</code>)
         * <ol>
         * <li> document isErrorCorrection
         * <li> lineAmount == 0
         * <li> ! (isIncome || isLiability || isExpense || isAsset)
         * </ol>
         * 
         * 
         * @param rule
         * @param accountingDocument
         * @param accountingLine
         * @return boolean
         * 
         */
        static boolean isDebitConsideringType(AccountingDocumentRuleBase rule, AccountingDocument accountingDocument, AccountingLine accountingLine) {

            KualiDecimal amount = accountingLine.getAmount();
            // zero amounts are not allowed
            if (amount.isZero()) {
                throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
            }
            boolean isDebit = false;
            boolean isPositiveAmount = accountingLine.getAmount().isPositive();

            // income/liability
            if (rule.isIncomeOrLiability(accountingLine)) {
                isDebit = !isPositiveAmount;
            }
            // expense/asset
            else {
                if (rule.isExpenseOrAsset(accountingLine)) {
                    isDebit = isPositiveAmount;
                }
                else {
                    throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                }
            }
            return isDebit;
        }

        /**
         * <ol>
         * <li>object type is not included in determining if a line is debit or credit.
         * <li>accounting line section (source/target) is not included in determining if a line is debit or credit.
         * </ol>
         * the following are credits (return false)
         * <ol>
         * <li> none
         * </ol>
         * the following are debits (return true)
         * <ol>
         * <li> (isIncome || isLiability || isExpense || isAsset) && (lineAmount > 0)
         * </ol>
         * the following are invalid ( throws an <code>IllegalStateException</code>)
         * <ol>
         * <li> lineAmount <= 0
         * <li> ! (isIncome || isLiability || isExpense || isAsset)
         * </ol>
         * 
         * @param rule
         * @param accountingDocument
         * @param accountingLine
         * @return boolean
         */
        static boolean isDebitConsideringNothingPositiveOnly(AccountingDocumentRuleBase rule, AccountingDocument accountingDocument, AccountingLine accountingLine) {

            boolean isDebit = false;
            KualiDecimal amount = accountingLine.getAmount();
            // non error correction
            if (!isErrorCorrection(accountingDocument)) {
                boolean isPositiveAmount = amount.isPositive();
                // isDebit if income/liability/expense/asset and line amount is positive
                if (isPositiveAmount && (rule.isIncomeOrLiability(accountingLine) || rule.isExpenseOrAsset(accountingLine))) {
                    isDebit = true;
                }
                else {
                    throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                }
            }
            // error correction
            else {
                boolean isNegativeAmount = amount.isNegative();
                // isDebit if income/liability/expense/asset and line amount is negative
                if (isNegativeAmount && (rule.isIncomeOrLiability(accountingLine) || rule.isExpenseOrAsset(accountingLine))) {
                    isDebit = false;
                }
                else {
                    throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                }

            }
            return isDebit;
        }

        /**
         * <ol>
         * <li>accounting line section (source/target) type is included in determining if a line is debit or credit.
         * <li> zero line amounts are never allowed
         * </ol>
         * the following are credits (return false)
         * <ol>
         * <li> isSourceLine && (isIncome || isExpense || isAsset || isLiability) && (lineAmount > 0)
         * <li> isTargetLine && (isIncome || isExpense || isAsset || isLiability) && (lineAmount < 0)
         * </ol>
         * the following are debits (return true)
         * <ol>
         * <li> isSourceLine && (isIncome || isExpense || isAsset || isLiability) && (lineAmount < 0)
         * <li> isTargetLine && (isIncome || isExpense || isAsset || isLiability) && (lineAmount > 0)
         * </ol>
         * the following are invalid ( throws an <code>IllegalStateException</code>)
         * <ol>
         * <li> lineAmount == 0
         * <li> ! (isIncome || isLiability || isExpense || isAsset)
         * </ol>
         * 
         * 
         * @param rule
         * @param accountingDocument
         * @param accountingLine
         * @return boolean
         * 
         */
        static boolean isDebitConsideringSection(AccountingDocumentRuleBase rule, AccountingDocument accountingDocument, AccountingLine accountingLine) {

            KualiDecimal amount = accountingLine.getAmount();
            // zero amounts are not allowed
            if (amount.isZero()) {
                throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
            }
            boolean isDebit = false;
            boolean isPositiveAmount = accountingLine.getAmount().isPositive();
            // source line
            if (accountingLine.isSourceAccountingLine()) {
                // income/liability/expense/asset
                if (rule.isIncomeOrLiability(accountingLine) || rule.isExpenseOrAsset(accountingLine)) {
                    isDebit = !isPositiveAmount;
                }
                else {
                    throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                }
            }
            // target line
            else {
                if (accountingLine.isTargetAccountingLine()) {
                    if (rule.isIncomeOrLiability(accountingLine) || rule.isExpenseOrAsset(accountingLine)) {
                        isDebit = isPositiveAmount;
                    }
                    else {
                        throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                    }
                }
                else {
                    throw new IllegalArgumentException(isInvalidLineTypeIllegalArgumentExceptionMessage);
                }
            }
            return isDebit;
        }

        /**
         * <ol>
         * <li>accounting line section (source/target) and object type is included in determining if a line is debit or credit.
         * <li> negative line amounts are <b>Only</b> allowed during error correction
         * </ol>
         * the following are credits (return false)
         * <ol>
         * <li> isSourceLine && (isExpense || isAsset) && (lineAmount > 0)
         * <li> isTargetLine && (isIncome || isLiability) && (lineAmount > 0)
         * <li> isErrorCorrection && isSourceLine && (isIncome || isLiability) && (lineAmount < 0)
         * <li> isErrorCorrection && isTargetLine && (isExpense || isAsset) && (lineAmount < 0)
         * </ol>
         * the following are debits (return true)
         * <ol>
         * <li> isSourceLine && (isIncome || isLiability) && (lineAmount > 0)
         * <li> isTargetLine && (isExpense || isAsset) && (lineAmount > 0)
         * <li> isErrorCorrection && (isExpense || isAsset) && (lineAmount < 0)
         * <li> isErrorCorrection && (isIncome || isLiability) && (lineAmount < 0)
         * </ol>
         * the following are invalid ( throws an <code>IllegalStateException</code>)
         * <ol>
         * <li> !isErrorCorrection && !(lineAmount > 0)
         * <li> isErrorCorrection && !(lineAmount < 0)
         * </ol>
         * 
         * @param rule
         * @param accountingDocument
         * @param accountingLine
         * @return boolean
         */
        static boolean isDebitConsideringSectionAndTypePositiveOnly(AccountingDocumentRuleBase rule, AccountingDocument accountingDocument, AccountingLine accountingLine) {

            boolean isDebit = false;
            KualiDecimal amount = accountingLine.getAmount();
            // non error correction
            if (!isErrorCorrection(accountingDocument)) {
                boolean isPositiveAmount = amount.isPositive();
                // only allow amount >0
                if (!isPositiveAmount) {
                    throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                }
                // source line
                if (accountingLine.isSourceAccountingLine()) {
                    isDebit = rule.isIncomeOrLiability(accountingLine);
                }
                // target line
                else {
                    if (accountingLine.isTargetAccountingLine()) {
                        isDebit = rule.isExpenseOrAsset(accountingLine);
                    }
                    else {
                        throw new IllegalArgumentException(isInvalidLineTypeIllegalArgumentExceptionMessage);
                    }
                }
            }
            // error correction document
            else {
                boolean isNegativeAmount = amount.isNegative();
                if (!isNegativeAmount) {
                    throw new IllegalStateException(isDebitCalculationIllegalStateExceptionMessage);
                }
                // source line
                if (accountingLine.isSourceAccountingLine()) {
                    isDebit = rule.isExpenseOrAsset(accountingLine);
                }
                // target line
                else {
                    if (accountingLine.isTargetAccountingLine()) {
                        isDebit = rule.isIncomeOrLiability(accountingLine);
                    }
                    else {
                        throw new IllegalArgumentException(isInvalidLineTypeIllegalArgumentExceptionMessage);
                    }
                }
            }

            return isDebit;
        }

        /**
         * throws an <code>IllegalStateException</code> if the document is an error correction. otherwise does nothing
         * 
         * @param rule
         * @param accountingDocument
         */
        static void disallowErrorCorrectionDocumentCheck(AccountingDocumentRuleBase rule, AccountingDocument accountingDocument) {
            if (isErrorCorrection(accountingDocument)) {
                throw new IllegalStateException(isErrorCorrectionIllegalStateExceptionMessage);
            }
        }
        
        /**
         * Convience method for determine if a document is an error correction document.
         * 
         * @param accountingDocument
         * @return true if document is an error correct
         */
        static boolean isErrorCorrection(AccountingDocument accountingDocument) {
            boolean isErrorCorrection = false;

            String correctsDocumentId = accountingDocument.getDocumentHeader().getFinancialDocumentInErrorNumber();
            if (StringUtils.isNotBlank(correctsDocumentId)) {
                isErrorCorrection = true;
            }

            return isErrorCorrection;
        }

    }
    
    /**
     * Verify that the selected employee does not have other pending salary transfers that have
     * not been processed.
     * 
     * @param Employee ID
     * @return true if the employee does not have any pending salary transfers.
     */
    public boolean validatePendingSalaryTransfer(String emplid) {
        
        // We must not have any pending labor ledger entries
        if (SpringServiceLocator.getLaborLedgerPendingEntryService().hasPendingLaborLedgerEntry(emplid)) {
           reportError(Constants.EMPLOYEE_LOOKUP_ERRORS,KeyConstants.Labor.PENDING_SALARY_TRANSFER_ERROR, emplid);
           return false;
        }      
        return true;
 
    }
}