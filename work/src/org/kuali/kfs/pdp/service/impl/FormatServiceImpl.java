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
package org.kuali.kfs.pdp.service.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.kuali.kfs.pdp.GeneralUtilities;
import org.kuali.kfs.pdp.PdpConstants;
import org.kuali.kfs.pdp.businessobject.AchAccountNumber;
import org.kuali.kfs.pdp.businessobject.AchInformation;
import org.kuali.kfs.pdp.businessobject.CustomerBank;
import org.kuali.kfs.pdp.businessobject.CustomerProfile;
import org.kuali.kfs.pdp.businessobject.DisbursementNumberRange;
import org.kuali.kfs.pdp.businessobject.DisbursementType;
import org.kuali.kfs.pdp.businessobject.FormatProcess;
import org.kuali.kfs.pdp.businessobject.FormatResult;
import org.kuali.kfs.pdp.businessobject.FormatSelection;
import org.kuali.kfs.pdp.businessobject.PaymentDetail;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.pdp.businessobject.PaymentProcess;
import org.kuali.kfs.pdp.businessobject.PaymentStatus;
import org.kuali.kfs.pdp.businessobject.PostFormatProcessSummary;
import org.kuali.kfs.pdp.businessobject.PreFormatProcessSummary;
import org.kuali.kfs.pdp.businessobject.ProcessSummary;
import org.kuali.kfs.pdp.dataaccess.CustomerProfileDao;
import org.kuali.kfs.pdp.dataaccess.DisbursementNumberRangeDao;
import org.kuali.kfs.pdp.dataaccess.FormatPaymentDao;
import org.kuali.kfs.pdp.dataaccess.FormatProcessDao;
import org.kuali.kfs.pdp.dataaccess.PaymentDetailDao;
import org.kuali.kfs.pdp.dataaccess.PaymentGroupDao;
import org.kuali.kfs.pdp.dataaccess.ProcessDao;
import org.kuali.kfs.pdp.dataaccess.ProcessSummaryDao;
import org.kuali.kfs.pdp.exception.ConfigurationError;
import org.kuali.kfs.pdp.service.AchService;
import org.kuali.kfs.pdp.service.FormatService;
import org.kuali.kfs.pdp.service.PendingTransactionService;
import org.kuali.kfs.pdp.service.ReferenceService;
import org.kuali.kfs.pdp.service.impl.exception.DisbursementRangeExhaustedException;
import org.kuali.kfs.pdp.service.impl.exception.MissingDisbursementRangeException;
import org.kuali.kfs.pdp.service.impl.exception.NoBankForCustomerException;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.service.KualiCodeService;
import org.kuali.kfs.sys.businessobject.Bank;
import org.kuali.kfs.sys.service.ParameterService;
import org.kuali.kfs.sys.service.impl.ParameterConstants;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.bo.user.UniversalUser;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class FormatServiceImpl implements FormatService {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(FormatServiceImpl.class);

    private CustomerProfileDao customerProfileDao;
    private DisbursementNumberRangeDao disbursementNumberRangeDao;
    private FormatProcessDao formatProcessDao;
    private PaymentDetailDao paymentDetailDao;
    private PaymentGroupDao paymentGroupDao;
    private ProcessSummaryDao processSummaryDao;
    private ProcessDao processDao;
    private AchService achService;
    private ReferenceService referenceService;
    private PendingTransactionService glPendingTransactionService;
    private ParameterService parameterService;
    private FormatPaymentDao formatPaymentDao;
    private SchedulerService schedulerService;
    private BusinessObjectService businessObjectService;
    private KualiCodeService kualiCodeService;

    public FormatServiceImpl() {
        super();
    }

    /**
     * @see org.kuali.kfs.pdp.service.FormatService#formatSelectionAction(org.kuali.module.pdp.bo.PdpUser, boolean)
     */
    public FormatSelection formatSelectionAction(UniversalUser user, boolean clearFormat) {
        LOG.debug("formatSelectionAction() started");

        FormatSelection fs = new FormatSelection();

        fs.setCampus(user.getCampusCode());

        Date startDate = getFormatProcessStartDate(fs.getCampus());

        // If they want to clear the flag, do it
        if ((startDate != null) && clearFormat) {
            startDate = null;
            endFormatProcess(fs.getCampus());
        }
        fs.setStartDate(startDate);

        if (startDate == null) {
            fs.setCustomerList(getAllCustomerProfiles());
            fs.setRangeList(getAllDisbursementNumberRanges());
        }
        return fs;
    }

    public Date getFormatProcessStartDate(String campus) {
        LOG.debug("getFormatProcessStartDate() started");

        FormatProcess fp = formatProcessDao.getByCampus(campus);
        if (fp != null) {
            LOG.debug("getFormatProcessStartDate() found");
            return new Date(fp.getBeginFormat().getTime());
        }
        else {
            LOG.debug("getFormatProcessStartDate() not found");
            return null;
        }
    }

    private int getMaxNoteLines() {
        return GeneralUtilities.getParameterInteger(parameterService, ParameterConstants.PRE_DISBURSEMENT_ALL.class, PdpConstants.ApplicationParameterKeys.MAX_NOTE_LINES);
    }

    private int getFormatSummaryListSize() {
        return GeneralUtilities.getParameterInteger(parameterService, ParameterConstants.PRE_DISBURSEMENT_LOOKUP.class, PdpConstants.ApplicationParameterKeys.FORMAT_SUMMARY_ROWS, 40);
    }

    public List performFormat(Integer procId) {
        LOG.debug("performFormat() started");

        PaymentProcess proc = processDao.get(procId);
        if (proc == null) {
            LOG.error("performFormat() Invalid proc ID " + procId);
            throw new ConfigurationError("Invalid proc ID");
        }
        Map disbursementTypes = referenceService.getallMap("DisbursementType");
        Map paymentStatusCodes = referenceService.getallMap("PaymentStatus");

        DisbursementType checkDisbursementType = (DisbursementType) disbursementTypes.get("CHCK");
        DisbursementType achDisbursementType = (DisbursementType) disbursementTypes.get("ACH");

        int maxNoteLines = getMaxNoteLines();

        // Step one, get ACH or Check, Bank info, ACH info, sorting
        Iterator groups = paymentGroupDao.getByProcess(proc);

        PostFormatProcessSummary fps = new PostFormatProcessSummary();
        CustomerProfile customer = null;
        Bank checkBank = null;
        Bank achBank = null;

        while (groups.hasNext()) {
            PaymentGroup pg = (PaymentGroup) groups.next();
            LOG.debug("performFormat() Step 1 Payment Group ID " + pg.getId());

            // Set the sort field to be saved in the database
            pg.setSortValue(pg.getFormatSortField());

            if ((customer == null) || (!customer.equals(pg.getBatch().getCustomerProfile()))) {
                customer = pg.getBatch().getCustomerProfile();
                CustomerBank cb = customer.getCustomerBankByDisbursementType("CHCK");
                if (cb != null) {
                    checkBank = cb.getBank();
                }

                cb = customer.getCustomerBankByDisbursementType("ACH");
                if (cb != null) {
                    achBank = cb.getBank();
                }
            }

            pg.setDisbursementDate(proc.getProcessTimestamp());
            pg.setPhysCampusProcessCd(proc.getCampus());
            pg.setProcess(proc);

            // Do we do ACH for this person?
            AchInformation ai = null;
            boolean check = true;
            boolean noNegativeDetails = true;

            // If any one of the payment details in the group are negative, we always force a check
            for (Iterator it = pg.getPaymentDetails().iterator(); it.hasNext();) {
                PaymentDetail pd = (PaymentDetail) it.next();
                if (pd.getNetPaymentAmount().doubleValue() < 0) {
                    LOG.debug("performFormat() Payment Group " + pg + " has payment detail net payment amount " + pd.getNetPaymentAmount());
                    LOG.debug("performFormat() Forcing a Check for Group");
                    noNegativeDetails = false;
                    break;
                }
            }

            // Attachments, Process Immediate & Special Handling are always checks
            // If there isn't a PSD Transaction code for the customer, don't even look to see if any payment is ACH
            // If the payment ID is X, it's always a check
            // If any one of the payment details in the group are negative, we always force a check
            if ((!"X".equals(pg.getPayeeIdTypeCd())) && (!"".equals(pg.getPayeeIdTypeCd())) && (pg.getPayeeIdTypeCd() != null) && (!"".equals(pg.getPayeeId())) && (pg.getPayeeId() != null) && (!pg.getPymtAttachment().booleanValue()) && (!pg.getProcessImmediate().booleanValue()) && (!pg.getPymtSpecialHandling().booleanValue()) && (customer.getPsdTransactionCode() != null) && (noNegativeDetails)) {
                // Check ACH service
                LOG.debug("performFormat() Checking ACH");
                ai = achService.getAchInformation(pg.getPayeeIdTypeCd(), pg.getPayeeId(), customer.getPsdTransactionCode());
                check = (ai == null);
            }

            if (check) {
                PaymentStatus ps = (PaymentStatus) paymentStatusCodes.get("EXTR");
                LOG.debug("performFormat() Check: " + ps);
                pg.setDisbursementType(checkDisbursementType);
                pg.setPaymentStatus(ps);
                if (checkBank == null) {
                    LOG.error("performFormat() A bank is needed for CHCK for customer: " + customer);
                    throw new NoBankForCustomerException("A bank is needed for CHCK for customer: " + customer, customer.getChartCode() + "-" + customer.getOrgCode() + "-" + customer.getSubUnitCode());
                }
                pg.setBank(checkBank);
            }
            else {
                PaymentStatus ps = (PaymentStatus) paymentStatusCodes.get("PACH");
                LOG.debug("performFormat() ACH: " + ps);
                pg.setDisbursementType(achDisbursementType);
                pg.setPaymentStatus(ps);
                if (achBank == null) {
                    LOG.error("performFormat() A bank is needed for ACH for customer: " + customer);
                    throw new NoBankForCustomerException("A bank is needed for ACH for customer: " + customer, customer.getChartCode() + "-" + customer.getOrgCode() + "-" + customer.getSubUnitCode());
                }
                pg.setBank(achBank);

                pg.setAchBankRoutingNbr(ai.getAchBankRoutingNbr());
                pg.setAdviceEmailAddress(ai.getAdviceEmailAddress());
                pg.setAchAccountType(ai.getAchAccountType());

                AchAccountNumber aan = new AchAccountNumber();
                aan.setAchBankAccountNbr(ai.getAchBankAccountNbr());
                aan.setId(pg.getId());
                pg.setAchAccountNumber(aan);
            }

            paymentGroupDao.save(pg);

            // Add to summary information
            fps.add(pg);
        }

        // step 2 figure out if we combine checks into one
        LOG.debug("performFormat() Combining");

        PaymentInfo lastPaymentInfo = new PaymentInfo();
        groups = paymentGroupDao.getByProcess(proc);

        while (groups.hasNext()) {
            PaymentGroup pg = (PaymentGroup) groups.next();

            // Only look at checks
            if (checkDisbursementType.equals(pg.getDisbursementType())) {
                // Attachments, Special Handling and Immediates don't ever get combined
                // Also, don't combine if the XML file says not to do so
                if (pg.getPymtAttachment().booleanValue() || pg.getProcessImmediate().booleanValue() || pg.getPymtSpecialHandling().booleanValue() || (!pg.getCombineGroups().booleanValue())) {
                    // This one doesn't combine with the next one
                    LOG.debug("performFormat() This payment can't combine " + pg.getPymtAttachment() + " " + pg.getProcessImmediate() + " " + pg.getPymtSpecialHandling() + " " + pg.getCombineGroups());
                    lastPaymentInfo = null;
                }
                else {
                    PaymentInfo pi = new PaymentInfo();
                    pi.customer = pg.getBatch().getCustomerProfile();
                    pi.line1Address = pg.getLine1Address();
                    pi.payeeName = pg.getPayeeName();
                    pi.noteLines = pg.getNoteLines();
                    pi.payeeId = pg.getPayeeId();
                    pi.payeeIdType = pg.getPayeeIdTypeCd();
                    LOG.debug("performFormat() This payment might combine " + pi);

                    boolean combine = false;
                    if (lastPaymentInfo != null) {
                        if (lastPaymentInfo.equals(pi)) {
                            if (((lastPaymentInfo.noteLines + pi.noteLines) <= maxNoteLines)) {
                                LOG.debug("performFormat() Combining");
                                pg.setDisbursementNbr(new Integer(-1)); // Mark it for later
                                lastPaymentInfo.noteLines += pi.noteLines;
                                combine = true;
                            }
                        }
                    }

                    if (!combine) {
                        LOG.debug("performFormat() Not combining");
                        lastPaymentInfo = pi;
                    }
                }
            }
        }

        // step 3 now assign disbursement numbers
        LOG.debug("performFormat() Assigning disbursement numbers");
        pass2(proc.getCampus(), disbursementTypes, paymentStatusCodes, proc, fps);

        // step 4 save the summarizing info
        LOG.debug("performFormat() Save summarizing information");
        fps.save(processSummaryDao);

        // step 5 tell the extract batch job to start
        LOG.debug("performFormat() Start extract batch job");
        triggerExtract(procId);

        // step 6 end the format process for this campus
        LOG.debug("performFormat() End the format process for this campus");
        endFormatProcess(proc.getCampus());

        // step 7 return all the process summaries
        List processSummaryResults = processSummaryDao.getByPaymentProcess(proc);
        return convertProcessSummary2FormatResult(processSummaryResults);
    }

    private void triggerExtract(Integer procId) {
        LOG.debug("triggerExtract() started");

        saveProcessId(procId);
        String emailAddress = parameterService.getParameterValue(ParameterConstants.PRE_DISBURSEMENT_ALL.class, PdpConstants.ApplicationParameterKeys.NO_PAYMENT_FILE_EMAIL);
        schedulerService.runJob("pdpExtractChecksJob", emailAddress);
    }

    private void saveProcessId(Integer id) {
        Map fields = new HashMap();
        fields.put("parameterNamespaceCode", "KFS-PDP");
        fields.put("parameterDetailTypeCode", "All");
        fields.put("parameterName", PdpConstants.ApplicationParameterKeys.EXTRACT_PROCESS_ID);
        Parameter processParam = (Parameter) businessObjectService.findByPrimaryKey(Parameter.class, fields);
        if (processParam == null) {
            processParam = new Parameter();
            processParam.setParameterNamespaceCode("KFS-PDP");
            processParam.setParameterDetailTypeCode("All");
            processParam.setParameterTypeCode("CONFG");
            processParam.setParameterName(PdpConstants.ApplicationParameterKeys.EXTRACT_PROCESS_ID);
            processParam.setParameterConstraintCode("A");
            processParam.setParameterWorkgroupName("FP_OPERATIONS");
        }
        processParam.setParameterValue(id.toString());
        businessObjectService.save(processParam);
    }

    private List convertProcessSummary2FormatResult(List processSummaryResults) {
        List results = new ArrayList();
        for (Iterator iter = processSummaryResults.iterator(); iter.hasNext();) {
            ProcessSummary element = (ProcessSummary) iter.next();
            FormatResult fr = new FormatResult(element.getProcess().getId(), element.getCustomer());
            fr.setSortGroupOverride(element.getSortGroupId());
            fr.setAmount(element.getProcessTotalAmount());
            fr.setPayments(element.getProcessTotalCount().intValue());
            fr.setBeginDisbursementNbr(element.getBeginDisbursementNbr().intValue());
            fr.setEndDisbursementNbr(element.getEndDisbursementNbr().intValue());
            fr.setDisbursementType(element.getDisbursementType());
            results.add(fr);
        }
        Collections.sort(results);
        return results;
    }

    // If the start format process was run and the user doesn't want to continue,
    // this needs to be run to set all payments back to open
    public void clearUnfinishedFormat(Integer procId) {
        LOG.debug("clearUnfinishedFormat() started");

        PaymentProcess proc = processDao.get(procId);
        LOG.debug("clearUnfinishedFormat() Process: " + proc);

        formatPaymentDao.unmarkPaymentsForFormat(proc);

        endFormatProcess(proc.getCampus());
    }

    // If the start format process was run and errored out,
    // this needs to be run to allow formats to continue to function
    public void resetFormatPayments(Integer procId) {
        LOG.debug("resetFormatPayments() started");
        clearUnfinishedFormat(procId);
    }

    // Mark the process log so a format only happens once per campus. Mark all the
    // payments that will be formatted and return a summary
    public List startFormatProcess(UniversalUser user, String campus, List customers, Date paydate, boolean immediate, String paymentTypes) {
        LOG.debug("startFormatProcess() started");

        for (Iterator iter = customers.iterator(); iter.hasNext();) {
            CustomerProfile element = (CustomerProfile) iter.next();
            LOG.debug("startFormatProcess() Customer: " + element);
        }

        PaymentStatus formatStatus = (PaymentStatus) kualiCodeService.getByCode(PaymentStatus.class, PdpConstants.PaymentStatusCodes.FORMAT);

        Date now = new Date();
        formatProcessDao.add(campus, now);

        // Create the process
        PaymentProcess p = processDao.createProcess(campus, user);

        // Mark all of them ready for format
        formatPaymentDao.markPaymentsForFormat(p, customers, paydate, immediate, paymentTypes);

        // summarize them
        PreFormatProcessSummary fps = new PreFormatProcessSummary();
        Iterator i = paymentGroupDao.getByProcess(p);

        int count = 0;
        while (i.hasNext()) {
            PaymentGroup pg = (PaymentGroup) i.next();

            count++;
            fps.add(pg);
        }

        if (count == 0) {
            LOG.debug("startFormatProcess() No payments to process.  Format process ending");
            endFormatProcess(campus);
        }

        return convertProcessSummary2FormatResult(fps.getProcessSummaryList());
    }

    public void endFormatProcess(String campus) {
        LOG.debug("endFormatProcess() starting");

        formatProcessDao.removeByCampus(campus);
    }

    public List getAllCustomerProfiles() {
        LOG.debug("getAllCustomerProfiles() started");

        return customerProfileDao.getAll();
    }

    public List getAllDisbursementNumberRanges() {
        LOG.debug("getAllDisbursementNumberRanges() started");

        return disbursementNumberRangeDao.getAll();
    }

    // This is the second pass. It determines the
    // disbursement number and creates GL entries
    private void pass2(String campus, Map disbursementTypes, Map paymentStatusCodes, PaymentProcess p, PostFormatProcessSummary fps) throws DisbursementRangeExhaustedException, MissingDisbursementRangeException {
        LOG.debug("pass2() starting");

        // This is the date used for last update
        Date now = new Date();
        Timestamp nowTs = new Timestamp(now.getTime());

        List disbursementRanges = paymentDetailDao.getDisbursementNumberRanges(campus);

        int checkNumber = 0;

        Iterator payGroupIterator = paymentGroupDao.getByProcess(p);
        while (payGroupIterator.hasNext()) {
            PaymentGroup pg = (PaymentGroup) payGroupIterator.next();
            LOG.debug("performFormat() Payment Group ID " + pg.getId());

            DisbursementNumberRange range = getRange(disbursementRanges, pg.getBank(), now);

            if (range == null) {
                String err = "No disbursement range for bank code=" + pg.getBank().getBankCode() + ", campus Id=" + campus;
                LOG.error("pass2() " + err);
                throw new MissingDisbursementRangeException(err);
            }

            if ("CHCK".equals(pg.getDisbursementType().getCode())) {
                if ((pg.getDisbursementNbr() != null) && (pg.getDisbursementNbr().intValue() == -1)) {
                    pg.setDisbursementNbr(new Integer(checkNumber));
                }
                else {
                    int number = 1 + range.getLastAssignedDisbNbr().intValue();
                    checkNumber = number; // Save for next payment
                    if (number > range.getEndDisbursementNbr().intValue()) {
                        String err = "No more disbursement numbers for bank code=" + pg.getBank().getBankCode() + ", campus Id=" + campus;
                        LOG.error("pass2() " + err);
                        throw new MissingDisbursementRangeException(err);
                    }
                    pg.setDisbursementNbr(new Integer(number));

                    range.setLastAssignedDisbNbr(new Integer(number));

                    // Update the summary information
                    fps.setDisbursementNumber(pg, new Integer(number));
                }
            }
            else if ("ACH".equals(pg.getDisbursementType().getCode())) {
                int number = 1 + range.getLastAssignedDisbNbr().intValue();
                if (number > range.getEndDisbursementNbr().intValue()) {
                    String err = "No more disbursement numbers for bank code=" + pg.getBank().getBankCode() + ", campus Id=" + campus;
                    LOG.error("pass2() " + err);
                    throw new MissingDisbursementRangeException(err);
                }
                pg.setDisbursementNbr(new Integer(number));

                range.setLastAssignedDisbNbr(new Integer(number));

                // Update the summary information
                fps.setDisbursementNumber(pg, new Integer(number));
            }
            else {
                // if it isn't check or ach, we're in trouble
                LOG.error("pass2() Payment group " + pg.getId() + " must be CHCK or ACH.  It is: " + pg.getDisbursementType());
                throw new IllegalArgumentException("Payment group " + pg.getId() + " must be Check or ACH");
            }
            paymentGroupDao.save(pg);

            // Generate a GL entry for CHCK & ACH
            for (Iterator iter = pg.getPaymentDetails().iterator(); iter.hasNext();) {
                PaymentDetail element = (PaymentDetail) iter.next();
                glPendingTransactionService.createProcessPaymentTransaction(element, pg.getBatch().getCustomerProfile().getRelieveLiabilities());
            }
        }

        // Update all the ranges
        LOG.debug("pass2() Save ranges");
        int rc = 0;
        for (Iterator iter = disbursementRanges.iterator(); iter.hasNext();) {
            DisbursementNumberRange element = (DisbursementNumberRange) iter.next();
                rc++;
                paymentDetailDao.saveDisbursementNumberRange(element);
            }
        LOG.debug("pass2() " + rc + " ranges saved");
    }

    private DisbursementNumberRange getRange(List ranges, Bank bank, Date today) {
        LOG.debug("getRange() Looking for bank = " + bank.getBankCode() + " for " + today);
        for (Iterator iter = ranges.iterator(); iter.hasNext();) {
            DisbursementNumberRange element = (DisbursementNumberRange) iter.next();
            Date eff = element.getDisbNbrEffectiveDt();
            Date exp = element.getDisbNbrExpirationDt();

            if (element.getBank().getBankCode().equals(bank.getBankCode()) && (today.getTime() >= eff.getTime()) && (today.getTime() <= exp.getTime())) {
                LOG.debug("getRange() Found match");
                return element;
            }
        }
        return null;
    }

    public List getFormatSummary(Integer procId) {
        LOG.debug("getFormatSummary() starting");
        List processSummaryResults = processSummaryDao.getByProcessId(procId);
        return convertProcessSummary2FormatResult(processSummaryResults);
    }

    public List getMostCurrentProcesses() {
        LOG.debug("getMostCurrent() starting");
        return processDao.getMostCurrentProcesses(new Integer(getFormatSummaryListSize()));
    }

    private class PaymentInfo {
        public CustomerProfile customer;
        public String payeeName;
        public String line1Address;
        public int noteLines;
        public String payeeId;
        public String payeeIdType;

        public int hashCode() {
            return new HashCodeBuilder(3, 5).append(customer).append(payeeName).append(line1Address).toHashCode();
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof PaymentInfo)) {
                return false;
            }
            PaymentInfo o = (PaymentInfo) obj;
            return new EqualsBuilder().append(customer, o.customer).append(payeeName, o.payeeName).append(line1Address, o.line1Address).append(payeeId, o.payeeId).append(payeeIdType, o.payeeIdType).isEquals();
        }

        public String toString() {
            return new ToStringBuilder(this).append("customer", this.customer).append("payeeName", this.payeeName).append("line1Address", this.line1Address).append("noteLines", this.noteLines).append("payeeId", this.payeeId).append("payeeIdType", this.payeeIdType).toString();
        }
    }

    // ***************************************************
    // IoC stuff below here
    // ***************************************************

    // Inject
    public void setFormatPaymentDao(FormatPaymentDao fpd) {
        formatPaymentDao = fpd;
    }

    // Inject
    public void setGlPendingTransactionService(PendingTransactionService gs) {
        glPendingTransactionService = gs;
    }

    // Inject
    public void setReferenceService(ReferenceService rs) {
        referenceService = rs;
    }

    // Inject
    public void setAchService(AchService as) {
        achService = as;
    }

    // Inject
    public void setProcessDao(ProcessDao pd) {
        processDao = pd;
    }

    // Inject
    public void setCustomerProfileDao(CustomerProfileDao cpd) {
        customerProfileDao = cpd;
    }

    // Inject
    public void setDisbursementNumberRangeDao(DisbursementNumberRangeDao dnrd) {
        disbursementNumberRangeDao = dnrd;
    }

    // Inject
    public void setFormatProcessDao(FormatProcessDao fpd) {
        formatProcessDao = fpd;
    }

    // Inject
    public void setPaymentGroupDao(PaymentGroupDao pgd) {
        paymentGroupDao = pgd;
    }

    // Inject
    public void setPaymentDetailDao(PaymentDetailDao pdd) {
        paymentDetailDao = pdd;
    }

    // Inject
    public void setProcessSummaryDao(ProcessSummaryDao psd) {
        processSummaryDao = psd;
    }

    // Inject
    public void setSchedulerService(SchedulerService ss) {
        schedulerService = ss;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBusinessObjectService(BusinessObjectService bos) {
        this.businessObjectService = bos;
    }
    
    public KualiCodeService getKualiCodeService() {
        return kualiCodeService;
    }

    public void setKualiCodeService(KualiCodeService kualiCodeService) {
        this.kualiCodeService = kualiCodeService;
    }
    
}
