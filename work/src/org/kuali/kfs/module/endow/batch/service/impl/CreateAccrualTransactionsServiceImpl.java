/*
 * Copyright 2010 The Kuali Foundation.
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
package org.kuali.kfs.module.endow.batch.service.impl;

import static org.kuali.kfs.module.endow.EndowConstants.NEW_TARGET_TRAN_LINE_PROPERTY_NAME;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.endow.EndowConstants;
import org.kuali.kfs.module.endow.batch.CreateAccrualTransactionsStep;
import org.kuali.kfs.module.endow.batch.service.CreateAccrualTransactionsService;
import org.kuali.kfs.module.endow.businessobject.EndowmentExceptionReportHeader;
import org.kuali.kfs.module.endow.businessobject.EndowmentTargetTransactionLine;
import org.kuali.kfs.module.endow.businessobject.EndowmentTargetTransactionSecurity;
import org.kuali.kfs.module.endow.businessobject.EndowmentTransactionLine;
import org.kuali.kfs.module.endow.businessobject.EndowmentTransactionSecurity;
import org.kuali.kfs.module.endow.businessobject.HoldingTaxLot;
import org.kuali.kfs.module.endow.businessobject.Security;
import org.kuali.kfs.module.endow.businessobject.TransactionDocumentExceptionReportLine;
import org.kuali.kfs.module.endow.businessobject.TransactionDocumentTotalReportLine;
import org.kuali.kfs.module.endow.dataaccess.SecurityDao;
import org.kuali.kfs.module.endow.document.CashIncreaseDocument;
import org.kuali.kfs.module.endow.document.service.HoldingTaxLotService;
import org.kuali.kfs.module.endow.document.service.KEMService;
import org.kuali.kfs.module.endow.document.validation.event.AddTransactionLineEvent;
import org.kuali.kfs.sys.service.ReportWriterService;
import org.kuali.rice.kew.exception.WorkflowException;
import org.kuali.rice.kns.rule.event.RouteDocumentEvent;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DocumentService;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.service.KualiRuleService;
import org.kuali.rice.kns.service.ParameterService;
import org.kuali.rice.kns.util.ErrorMessage;
import org.kuali.rice.kns.util.GlobalVariables;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.MessageMap;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class...
 */
@Transactional
public class CreateAccrualTransactionsServiceImpl implements CreateAccrualTransactionsService {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CreateAccrualTransactionsServiceImpl.class);

    private BusinessObjectService businessObjectService;
    private KEMService kemService;
    private HoldingTaxLotService holdingTaxLotService;
    private SecurityDao securityDao;
    private DocumentService documentService;
    private KualiConfigurationService configService;
    private KualiRuleService kualiRuleService;
    protected ParameterService parameterService;

    private ReportWriterService accrualTransactionsExceptionReportWriterService;
    private ReportWriterService accrualTransactionsTotalReportWriterService;
 
    private TransactionDocumentExceptionReportLine exceptionReportLine = null;
    private TransactionDocumentTotalReportLine totalReportLine = null;
    
    private boolean isFistTimeForWritingTotalReport = true;
    private boolean isFistTimeForWritingExceptionReport = true;

    /**
     * Constructs a CreateAccrualTransactionsServiceImpl.java.
     */
    public CreateAccrualTransactionsServiceImpl() {

    }

    /**
     * @see org.kuali.kfs.module.endow.batch.service.CreateAccrualTransactionsService#createAccrualTransactions()
     */
    public boolean createAccrualTransactions() {
        boolean success = true;
        
        LOG.debug("createAccrualTransactions() started");

        int maxNumberOfTranLines = kemService.getMaxNumberOfTransactionLinesPerDocument();

        List<Security> securities = getAllSecuritiesWithNextPayDateEqualCurrentDate();

        for (Security security : securities) {

            List<HoldingTaxLot> taxLots = holdingTaxLotService.getAllTaxLotsWithAccruedIncomeGreaterThanZeroPerSecurity(security.getId());

            // a map from registration code to taxlots
            Map<String, List<HoldingTaxLot>> regCodeMap = new HashMap<String, List<HoldingTaxLot>>();

            for (HoldingTaxLot holdingTaxLot : taxLots) {
                String registrationCode = holdingTaxLot.getRegistrationCode();
                if (regCodeMap.containsKey(registrationCode)) {
                    regCodeMap.get(registrationCode).add(holdingTaxLot);
                }
                else {
                    List<HoldingTaxLot> tmpTaxLots = new ArrayList<HoldingTaxLot>();
                    tmpTaxLots.add(holdingTaxLot);
                    regCodeMap.put(registrationCode, tmpTaxLots);
                }
            }

            for (String registrationCode : regCodeMap.keySet()) {

                // 4. create new CashIncreaseDocument
                CashIncreaseDocument cashIncreaseDocument = createNewCashIncreaseDocument(security.getId(), registrationCode);
                      
                //create a new totalReportLine and exceptionReportLine for a new CashIncreaseDocument
                initializeTotalAndExceptionReportLines(cashIncreaseDocument.getDocumentNumber(),security.getId());
         
                // group tax lots by kemid and ip indicator
                Map<String, List<HoldingTaxLot>> kemidIpMap = new HashMap<String, List<HoldingTaxLot>>();

                for (HoldingTaxLot holdingTaxLot : taxLots) {
                    String kemidAndIp = holdingTaxLot.getKemid() + holdingTaxLot.getIncomePrincipalIndicator();
                    if (kemidIpMap.containsKey(kemidAndIp)) {
                        kemidIpMap.get(kemidAndIp).add(holdingTaxLot);
                    }
                    else {
                        List<HoldingTaxLot> tmpTaxLots = new ArrayList<HoldingTaxLot>();
                        tmpTaxLots.add(holdingTaxLot);
                        kemidIpMap.put(kemidAndIp, tmpTaxLots);
                    }
                }

                List<HoldingTaxLot> taxLotsForUpdate = new ArrayList<HoldingTaxLot>();
                // keep a counter to create a new document if there are more that maximum number of allowed transaction lines per
                // document
                int counter = 0;

                for (String kemidIp : kemidIpMap.keySet()) {

                    KualiDecimal totalAmount = KualiDecimal.ZERO;
                    String kemid = null;

                    for (HoldingTaxLot lot : kemidIpMap.get(kemidIp)) {
                        totalAmount = totalAmount.add(new KualiDecimal(lot.getCurrentAccrual()));

                        // initialize kemid
                        if (kemid == null) {
                            kemid = lot.getKemid();
                        }
                    }

                    taxLotsForUpdate.addAll(kemidIpMap.get(kemidIp));
                    // if we have already reached the maximum number of transaction lines on the current document then create a new
                    // document
                    if (counter == maxNumberOfTranLines) {
                        // submit the current ECI doc and update the values in the tax lots used already
                        submitCashIncreaseDocumentAndUpdateTaxLots(cashIncreaseDocument, taxLotsForUpdate);
                                         
                        cashIncreaseDocument = createNewCashIncreaseDocument(security.getId(), registrationCode);
                        
                        //create a new totalReportLine and exceptionReportLine for a new CashIncreaseDocument
                        initializeTotalAndExceptionReportLines(cashIncreaseDocument.getDocumentNumber(),security.getId());
                            
                        counter = 0;
                    }

                    // add a new transaction line
                    if (addTransactionLine(cashIncreaseDocument, security, kemid, totalAmount)) {
                        counter++;
                    }
                }

                // submit the current ECI doc and update the values in the tax lots used already
                submitCashIncreaseDocumentAndUpdateTaxLots(cashIncreaseDocument, taxLotsForUpdate);    

            }

            LOG.debug("createAccrualTransactions() done");
        }

        return success;
    }

    /**
     * Creates and adds a new transaction line to the cash increase document.
     * 
     * @param cashIncreaseDocument
     * @param security
     * @param kemid
     * @param totalAmount
     * @return true if transaction line successfully added, false otherwise
     */
    private boolean addTransactionLine(CashIncreaseDocument cashIncreaseDocument, Security security, String kemid, KualiDecimal totalAmount){
        boolean success = true;

        // Create a new transaction line
        EndowmentTransactionLine endowmentTransactionLine = new EndowmentTargetTransactionLine();
        endowmentTransactionLine.setDocumentNumber(cashIncreaseDocument.getDocumentNumber());
        endowmentTransactionLine.setKemid(kemid);
        endowmentTransactionLine.setEtranCode(security.getClassCode().getSecurityIncomeEndowmentTransactionPostCode());
        endowmentTransactionLine.setTransactionIPIndicatorCode(EndowConstants.IncomePrincipalIndicator.INCOME);
        endowmentTransactionLine.setTransactionAmount(totalAmount);

        boolean rulesPassed = kualiRuleService.applyRules(new AddTransactionLineEvent(NEW_TARGET_TRAN_LINE_PROPERTY_NAME, cashIncreaseDocument, endowmentTransactionLine));

        if (rulesPassed) {
            cashIncreaseDocument.addTargetTransactionLine((EndowmentTargetTransactionLine) endowmentTransactionLine);
            totalReportLine.addIncomeAmount(totalAmount);
        }
        else {
            success = false;
            
            //write an exception line when a transaction line fails to pass the validation.
            exceptionReportLine.setKemid(kemid);
            exceptionReportLine.setIncomeAmount(totalAmount);
            if (isFistTimeForWritingExceptionReport){
                accrualTransactionsExceptionReportWriterService.writeTableHeader(exceptionReportLine);
                isFistTimeForWritingExceptionReport = false;
            }
            accrualTransactionsExceptionReportWriterService.writeTableRow(exceptionReportLine);
            List<String> errorMessages = extractGlobalVariableErrors();
            for (String errorMessage : errorMessages) {
                accrualTransactionsExceptionReportWriterService.writeFormattedMessageLine("Reason:  %s",errorMessage);
                accrualTransactionsExceptionReportWriterService.writeNewLines(1);
            }
        }
        return success;
    }

    /**
     * Creates a new CashIncreaseDocument with source type Automated, transaction sub-type Cash, target security id set to the input
     * security id.
     * 
     * @param securityId
     * @return a new CashIncreaseDocument
     */
    private CashIncreaseDocument createNewCashIncreaseDocument(String securityId, String registrationCode) {
        try {

            CashIncreaseDocument cashIncreaseDocument = (CashIncreaseDocument) documentService.getNewDocument(getCashIncreaseDocumentType());
            String documentDescription = parameterService.getParameterValue(CreateAccrualTransactionsStep.class, EndowConstants.EndowmentSystemParameter.DESCRIPTION);
            cashIncreaseDocument.getDocumentHeader().setDocumentDescription(documentDescription);
            cashIncreaseDocument.setTransactionSourceTypeCode(EndowConstants.TransactionSourceTypeCode.AUTOMATED);
            cashIncreaseDocument.setTransactionSubTypeCode(EndowConstants.TransactionSubTypeCode.CASH);

            // set security details
            EndowmentTransactionSecurity targetTransactionSecurity = new EndowmentTargetTransactionSecurity();
            targetTransactionSecurity.setSecurityID(securityId);
            targetTransactionSecurity.setRegistrationCode(registrationCode);
            cashIncreaseDocument.setTargetTransactionSecurity(targetTransactionSecurity);

            return cashIncreaseDocument;
        }
        catch (WorkflowException ex) {
            if (isFistTimeForWritingExceptionReport){
                if (exceptionReportLine == null){
                    exceptionReportLine = new TransactionDocumentExceptionReportLine(getCashIncreaseDocumentType(), "",securityId); 
                }
                accrualTransactionsExceptionReportWriterService.writeTableHeader(exceptionReportLine);
                isFistTimeForWritingExceptionReport = false;
            }
            accrualTransactionsExceptionReportWriterService.writeTableRow(exceptionReportLine);
            accrualTransactionsExceptionReportWriterService.writeFormattedMessageLine("Reason:  %s",
                        "WorkflowException while creating a CashIncreaseDocument for Accrual Transactions: "+ex.toString());
            accrualTransactionsExceptionReportWriterService.writeNewLines(1);
            throw new RuntimeException("WorkflowException while creating a CashIncreaseDocument for Accrual Transactions.", ex);
        }
    }

    /**
     * Submits the ECI doc and updates the values in the tax lots list.
     * 
     * @param cashIncreaseDocument
     * @param taxLotsForUpdate
     */
    private void submitCashIncreaseDocumentAndUpdateTaxLots(CashIncreaseDocument cashIncreaseDocument, List<HoldingTaxLot> taxLotsForUpdate) {

        boolean rulesPassed = kualiRuleService.applyRules(new RouteDocumentEvent(cashIncreaseDocument));

        if (rulesPassed) {

            String noRouteIndVal = parameterService.getParameterValue(CreateAccrualTransactionsStep.class, EndowConstants.EndowmentSystemParameter.NO_ROUTE_IND);
            boolean noRouteIndicator = EndowConstants.YES.equalsIgnoreCase(noRouteIndVal) ? true : false;

            try {
                cashIncreaseDocument.setNoRouteIndicator(noRouteIndicator);
                // TODO figure out if/how we use the ad hoc recipients list
                documentService.routeDocument(cashIncreaseDocument, "Created by Accrual Transactions Batch process.", null);
                
                //write a total report line for a CashIncreaseDocument
               if(isFistTimeForWritingTotalReport){
                    accrualTransactionsTotalReportWriterService.writeTableHeader(totalReportLine);
                    isFistTimeForWritingTotalReport = false;
                }
                
                accrualTransactionsTotalReportWriterService.writeTableRow(totalReportLine);

                // set accrued income to zero and copy current value in prior accrued income
                for (HoldingTaxLot taxLotForUpdate : taxLotsForUpdate) {
                    taxLotForUpdate.setPriorAccrual(taxLotForUpdate.getCurrentAccrual());
                    taxLotForUpdate.setCurrentAccrual(BigDecimal.ZERO);
                }

                // save changes
                businessObjectService.save(taxLotsForUpdate);
            }
            catch (WorkflowException ex) {
                if (isFistTimeForWritingExceptionReport){
                    accrualTransactionsExceptionReportWriterService.writeTableHeader(exceptionReportLine);
                    isFistTimeForWritingExceptionReport = false;
                }
                
                accrualTransactionsExceptionReportWriterService.writeTableRow(exceptionReportLine);
                accrualTransactionsExceptionReportWriterService.writeFormattedMessageLine("Reason:  %s",
                            "WorkflowException while routing a CashIncreaseDocument for Accrual Transactions batch process: "+ex.toString());
                accrualTransactionsExceptionReportWriterService.writeNewLines(1);
                throw new RuntimeException("WorkflowException while routing a CashIncreaseDocument for Accrual Transactions batch process.", ex);
            }
        }
        else { 
            try {
                //try to save the document
                documentService.saveDocument(cashIncreaseDocument, CashIncreaseDocument.class);
                exceptionReportLine.setSecurityId(cashIncreaseDocument.getTargetTransactionSecurity().getSecurityID());
                exceptionReportLine.setIncomeAmount(cashIncreaseDocument.getTargetIncomeTotal());
                accrualTransactionsExceptionReportWriterService.writeTableRow(exceptionReportLine);
                List<String> errorMessages = extractGlobalVariableErrors();
                for (String errorMessage : errorMessages) {
                    accrualTransactionsExceptionReportWriterService.writeFormattedMessageLine("Reason:  %s",errorMessage);
                    accrualTransactionsExceptionReportWriterService.writeNewLines(1);
                }
            }catch (WorkflowException ex) {
                // have to write a table header before write the table row.
                if (isFistTimeForWritingExceptionReport){
                    accrualTransactionsExceptionReportWriterService.writeTableHeader(exceptionReportLine);
                    isFistTimeForWritingExceptionReport = false;
                }
                accrualTransactionsExceptionReportWriterService.writeTableRow(exceptionReportLine);
                // Write reason as a formatted message in a second line
                accrualTransactionsExceptionReportWriterService.writeFormattedMessageLine("Reason:  %s",
                            "WorkflowException while saving a CashIncreaseDocument for Accrual Transactions batch process: "+ex.toString());
                accrualTransactionsExceptionReportWriterService.writeNewLines(1);
                throw new RuntimeException("WorkflowException while saving a CashIncreaseDocument for Accrual Transactions batch process.", ex);

            }
                
        }
    }    


    /**
     * Extracts errors for error report writing.
     * 
     * @return a list of error messages
     */
    protected List<String> extractGlobalVariableErrors() {
        List<String> result = new ArrayList<String>();

        MessageMap errorMap = GlobalVariables.getMessageMap();

        Set<String> errorKeys = errorMap.keySet();
        List<ErrorMessage> errorMessages = null;
        Object[] messageParams;
        String errorKeyString;
        String errorString;

        for (String errorProperty : errorKeys) {
            errorMessages = (List<ErrorMessage>) errorMap.get(errorProperty);
            for (ErrorMessage errorMessage : errorMessages) {
                errorKeyString = configService.getPropertyString(errorMessage.getErrorKey());
                messageParams = errorMessage.getMessageParameters();

                // MessageFormat.format only seems to replace one
                // per pass, so I just keep beating on it until all are gone.
                if (StringUtils.isBlank(errorKeyString)) {
                    errorString = errorMessage.getErrorKey();
                }
                else {
                    errorString = errorKeyString;
                }
                System.out.println(errorString);
                while (errorString.matches("^.*\\{\\d\\}.*$")) {
                    errorString = MessageFormat.format(errorString, messageParams);
                }
                result.add(errorString);
            }
        }

        // clear the stuff out of globalvars, as we need to reformat it and put it back
        GlobalVariables.getMessageMap().clear();
        return result;
    }

    /**
     * Gets the CashIncreaseDocument type.
     * 
     * @return the CashIncreaseDocument type
     */
    private String getCashIncreaseDocumentType() {
        return "ECI";
    }
 
    /**
     * Locates all Security records for which the next income pay date is equal to the current date.
     * 
     * @return
     */
    private List<Security> getAllSecuritiesWithNextPayDateEqualCurrentDate() {
        List<Security> result = new ArrayList<Security>();

        result = securityDao.getAllSecuritiesWithNextPayDateEqualCurrentDate();

        return result;
    }

    private void initializeTotalAndExceptionReportLines (String theDocumentId, String theSecurityId){
        // create a new totalReportLine for each new CashIncreaseDocument
        this.totalReportLine = new TransactionDocumentTotalReportLine(getCashIncreaseDocumentType(),theDocumentId, theSecurityId);

        // create an exceptionReportLine instance that can be reused for reporting multiple errors for a CashIncreaseDocument
        this.exceptionReportLine = new TransactionDocumentExceptionReportLine(getCashIncreaseDocumentType(), theDocumentId, theSecurityId);                                           

    }
    /**
     * Sets the businessObjectService.
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the kemService.
     * 
     * @param kemService
     */
    public void setKemService(KEMService kemService) {
        this.kemService = kemService;
    }

    /**
     * Sets the holdingTaxLotService.
     * 
     * @param holdingTaxLotService
     */
    public void setHoldingTaxLotService(HoldingTaxLotService holdingTaxLotService) {
        this.holdingTaxLotService = holdingTaxLotService;
    }

    /**
     * Sets the securityDao.
     * 
     * @param securityDao
     */
    public void setSecurityDao(SecurityDao securityDao) {
        this.securityDao = securityDao;
    }

    /**
     * Sets the documenyService.
     * 
     * @param documentService
     */
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Sets the configService.
     * 
     * @param configService
     */
    public void setConfigService(KualiConfigurationService configService) {
        this.configService = configService;
    }

    /**
     * Sets the kualiRuleService.
     * 
     * @param kualiRuleService
     */
    public void setKualiRuleService(KualiRuleService kualiRuleService) {
        this.kualiRuleService = kualiRuleService;
    }

    /**
     * Sets the parameterService.
     * 
     * @param parameterService
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Gets the accrualTransactionsExceptionReportWriterService.
     * 
     * @return accrualTransactionsExceptionReportWriterService
     */
    public ReportWriterService getAccrualTransactionsExceptionReportWriterService() {
        return accrualTransactionsExceptionReportWriterService;
    }

    /**
     * Sets the accrualTransactionsExceptionReportWriterService.
     * 
     * @param accrualTransactionsExceptionReportWriterService
     */
    public void setAccrualTransactionsExceptionReportWriterService(ReportWriterService accrualTransactionsExceptionReportWriterService) {
        this.accrualTransactionsExceptionReportWriterService = accrualTransactionsExceptionReportWriterService;
    }
    
    /**
     * Gets the accrualTransactionsTotalReportWriterService.
     * 
     * @return accrualTransactionsTotalReportWriterService
     */
    public ReportWriterService getAccrualTransactionsTotalReportWriterService() {
        return accrualTransactionsTotalReportWriterService;
    }

    /**
     * Sets the accrualTransactionsTotalReportWriterService.
     * 
     * @param accrualTransactionsTotalReportWriterService
     */
    public void setAccrualTransactionsTotalReportWriterService(ReportWriterService accrualTransactionsTotalReportWriterService) {
        this.accrualTransactionsTotalReportWriterService = accrualTransactionsTotalReportWriterService;
    }
}
