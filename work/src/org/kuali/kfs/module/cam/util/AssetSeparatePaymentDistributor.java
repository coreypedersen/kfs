/*
 * Copyright 2009 The Kuali Foundation.
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
package org.kuali.kfs.module.cam.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.rice.kns.util.KualiDecimal;

/**
 * This class is a calculator which will distribute the amounts and balance them by ratio
 */
public class AssetSeparatePaymentDistributor {
    private Asset sourceAsset;
    private AssetGlobal assetGlobal;
    private List<Asset> newAssets;
    private List<AssetPayment> sourcePayments = new ArrayList<AssetPayment>();
    private List<AssetPayment> separatedPayments = new ArrayList<AssetPayment>();
    private List<AssetPayment> offsetPayments = new ArrayList<AssetPayment>();
    private List<AssetPayment> remainingPayments = new ArrayList<AssetPayment>();
    private HashMap<Long, KualiDecimal> totalByAsset = new HashMap<Long, KualiDecimal>();
    private HashMap<Integer, List<AssetPayment>> paymentSplitMap = new HashMap<Integer, List<AssetPayment>>();
    private double[] assetAllocateRatios;
    private double separateRatio;
    private double retainRatio;
    private Integer maxPaymentSeqNo;
    private static PropertyDescriptor[] assetPaymentProperties = PropertyUtils.getPropertyDescriptors(AssetPayment.class);


    public AssetSeparatePaymentDistributor(Asset sourceAsset, List<AssetPayment> sourcePayments, Integer maxPaymentSeqNo, AssetGlobal assetGlobal, List<Asset> newAssets) {
        super();
        this.sourceAsset = sourceAsset;
        this.sourcePayments = sourcePayments;
        this.maxPaymentSeqNo = maxPaymentSeqNo;
        this.assetGlobal = assetGlobal;
        this.newAssets = newAssets;
    }


    public void distribute() {
        KualiDecimal totalSourceAmount = this.assetGlobal.getTotalCostAmount();
        KualiDecimal totalSeparateAmount = this.assetGlobal.getSeparateSourceTotalAmount();
        KualiDecimal remainingAmount = totalSourceAmount.subtract(totalSeparateAmount);

        separateRatio = totalSeparateAmount.doubleValue() / totalSourceAmount.doubleValue();
        retainRatio = remainingAmount.doubleValue() / totalSourceAmount.doubleValue();
        List<AssetGlobalDetail> assetGlobalDetails = this.assetGlobal.getAssetGlobalDetails();
        int size = assetGlobalDetails.size();
        assetAllocateRatios = new double[size];
        AssetGlobalDetail assetGlobalDetail = null;
        for (int i = 0; i < size; i++) {
            assetGlobalDetail = assetGlobalDetails.get(i);
            Long capitalAssetNumber = assetGlobalDetail.getCapitalAssetNumber();
            totalByAsset.put(capitalAssetNumber, KualiDecimal.ZERO);
            assetAllocateRatios[i] = assetGlobalDetail.getSeparateSourceAmount().doubleValue() / totalSeparateAmount.doubleValue();
        }
        // Prepare the source and offset payments for split
        prepareSourcePaymentsForSplit();
        // Distribute payments by ratio
        allocatePaymentAmountsByRatio();
        // balance by each payment line
        roundPaymentAmounts();
        // balance by separate source amount
        roundAccountChargeAmount();
        // create offset payments
        createOffsetPayments();
        this.sourceAsset.getAssetPayments().addAll(this.offsetPayments);
    }

    private void prepareSourcePaymentsForSplit() {
        // Call the allocate with ratio for each payments
        for (AssetPayment assetPayment : this.sourcePayments) {
            // Separate amount
            AssetPayment separatePayment = new AssetPayment();
            ObjectValueUtils.copySimpleProperties(assetPayment, separatePayment);
            this.separatedPayments.add(separatePayment);

            // Remaining amount
            AssetPayment remainingPayment = new AssetPayment();
            ObjectValueUtils.copySimpleProperties(assetPayment, remainingPayment);
            this.remainingPayments.add(remainingPayment);

            applyRatioToPaymentAmounts(assetPayment, new AssetPayment[] { separatePayment, remainingPayment }, new double[] { separateRatio, retainRatio });
        }


    }

    private void createOffsetPayments() {
        // create offset payment by negating the amount fields
        for (AssetPayment separatePayment : this.separatedPayments) {
            AssetPayment offsetPayment = new AssetPayment();
            ObjectValueUtils.copySimpleProperties(separatePayment, offsetPayment);
            try {
                negatePaymentAmounts(offsetPayment);
            }
            catch (Exception e) {
                throw new RuntimeException();
            }
            offsetPayment.setDocumentNumber(assetGlobal.getDocumentNumber());
            offsetPayment.setFinancialDocumentTypeCode(CamsConstants.PaymentDocumentTypeCodes.ASSET_GLOBAL_SEPARATE);
            offsetPayment.setVersionNumber(null);
            offsetPayment.setObjectId(null);
            offsetPayment.setPaymentSequenceNumber(++maxPaymentSeqNo);
            this.offsetPayments.add(offsetPayment);
        }

    }

    private void allocatePaymentAmountsByRatio() {
        for (AssetPayment source : this.separatedPayments) {

            // for each source payment, create target payments by ratio
            AssetPayment[] targets = new AssetPayment[assetAllocateRatios.length];
            for (int j = 0; j < assetAllocateRatios.length; j++) {
                AssetPayment newPayment = new AssetPayment();
                ObjectValueUtils.copySimpleProperties(source, newPayment);
                Asset currentAsset = this.newAssets.get(j);
                Long capitalAssetNumber = currentAsset.getCapitalAssetNumber();
                newPayment.setCapitalAssetNumber(capitalAssetNumber);
                newPayment.setDocumentNumber(assetGlobal.getDocumentNumber());
                newPayment.setFinancialDocumentTypeCode(CamsConstants.PaymentDocumentTypeCodes.ASSET_GLOBAL_SEPARATE);
                targets[j] = newPayment;
                newPayment.setVersionNumber(null);
                newPayment.setObjectId(null);
                currentAsset.getAssetPayments().add(newPayment);
            }
            applyRatioToPaymentAmounts(source, targets, assetAllocateRatios);

            // keep track of split happened for the source
            this.paymentSplitMap.put(source.getPaymentSequenceNumber(), Arrays.asList(targets));

            // keep track of total amount by asset
            for (int j = 0; j < targets.length; j++) {
                Asset currentAsset = this.newAssets.get(j);
                Long capitalAssetNumber = currentAsset.getCapitalAssetNumber();
                this.totalByAsset.put(capitalAssetNumber, this.totalByAsset.get(capitalAssetNumber).add(targets[j].getAccountChargeAmount()));
            }
        }
    }

    /**
     * Rounds the last payment by adjusting the amounts against source amount
     */
    private void roundPaymentAmounts() {
        for (int i = 0; i < this.separatedPayments.size(); i++) {
            applyBalanceToPaymentAmounts(separatedPayments.get(i), this.paymentSplitMap.get(separatedPayments.get(i).getPaymentSequenceNumber()));
        }
    }

    /**
     * Rounds the last payment by adjusting the amount compared against separate source amount
     */
    private void roundAccountChargeAmount() {
        for (int j = 0; j < this.newAssets.size(); j++) {
            Asset currentAsset = this.newAssets.get(j);
            AssetGlobalDetail detail = this.assetGlobal.getAssetGlobalDetails().get(j);
            AssetPayment lastPayment = currentAsset.getAssetPayments().get(currentAsset.getAssetPayments().size() - 1);
            KualiDecimal totalForAsset = this.totalByAsset.get(currentAsset.getCapitalAssetNumber());
            KualiDecimal diff = detail.getSeparateSourceAmount().subtract(totalForAsset);
            lastPayment.setAccountChargeAmount(lastPayment.getAccountChargeAmount().add(diff));
            currentAsset.setTotalCostAmount(totalForAsset.add(diff));
            // adjust primary depreciation base amount, same as account charge amount
            if (lastPayment.getPrimaryDepreciationBaseAmount() != null && lastPayment.getPrimaryDepreciationBaseAmount().isNonZero()) {
                lastPayment.setPrimaryDepreciationBaseAmount(lastPayment.getAccountChargeAmount());
            }
        }
    }

    /**
     * Utility method which can take one payment and distribute its amount by ratio to the target payments
     * 
     * @param source Source Payment
     * @param targets Target Payment
     * @param ratios Ratio to be applied for each target
     */
    private void applyRatioToPaymentAmounts(AssetPayment source, AssetPayment[] targets, double[] ratios) {
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && propertyDescriptor.getPropertyType() != null && KualiDecimal.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(source);
                    if (amount != null && amount.isNonZero()) {
                        KualiDecimal[] ratioAmounts = KualiDecimalUtils.allocateByRatio(amount, ratios);
                        Method writeMethod = propertyDescriptor.getWriteMethod();
                        if (writeMethod != null) {
                            for (int i = 0; i < ratioAmounts.length; i++) {
                                writeMethod.invoke(targets[i], ratioAmounts[i]);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Utility method which can compute the difference between source amount and consumed amounts, then will adjust the last amount
     * 
     * @param source Source payments
     * @param consumedList Consumed Payments
     */
    private void applyBalanceToPaymentAmounts(AssetPayment source, List<AssetPayment> consumedList) {
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && propertyDescriptor.getPropertyType() != null && KualiDecimal.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(source);
                    if (amount != null && amount.isNonZero()) {
                        Method writeMethod = propertyDescriptor.getWriteMethod();
                        KualiDecimal consumedAmount = KualiDecimal.ZERO;
                        KualiDecimal currAmt = KualiDecimal.ZERO;
                        if (writeMethod != null) {
                            for (int i = 0; i < consumedList.size(); i++) {
                                currAmt = (KualiDecimal) readMethod.invoke(consumedList.get(i));
                                consumedAmount = consumedAmount.add(currAmt != null ? currAmt : KualiDecimal.ZERO);
                            }
                        }
                        if (!consumedAmount.equals(amount)) {
                            AssetPayment lastPayment = consumedList.get(consumedList.size() - 1);
                            writeMethod.invoke(lastPayment, currAmt.add(amount.subtract(consumedAmount)));
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Utility method which will negate the payment amounts for a given payment
     * 
     * @param assetPayment Payment to be negated
     */
    public void negatePaymentAmounts(AssetPayment assetPayment) {
        try {
            for (PropertyDescriptor propertyDescriptor : assetPaymentProperties) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null && propertyDescriptor.getPropertyType() != null && KualiDecimal.class.isAssignableFrom(propertyDescriptor.getPropertyType())) {
                    KualiDecimal amount = (KualiDecimal) readMethod.invoke(assetPayment);
                    Method writeMethod = propertyDescriptor.getWriteMethod();
                    if (writeMethod != null && amount != null) {
                        writeMethod.invoke(assetPayment, (amount.negated()));
                    }

                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the remainingPayments attribute.
     * 
     * @return Returns the remainingPayments.
     */
    public List<AssetPayment> getRemainingPayments() {
        return remainingPayments;
    }


    /**
     * Sets the remainingPayments attribute value.
     * 
     * @param remainingPayments The remainingPayments to set.
     */
    public void setRemainingPayments(List<AssetPayment> remainingPayments) {
        this.remainingPayments = remainingPayments;
    }


    /**
     * Gets the offsetPayments attribute.
     * 
     * @return Returns the offsetPayments.
     */
    public List<AssetPayment> getOffsetPayments() {
        return offsetPayments;
    }


    /**
     * Sets the offsetPayments attribute value.
     * 
     * @param offsetPayments The offsetPayments to set.
     */
    public void setOffsetPayments(List<AssetPayment> offsetPayments) {
        this.offsetPayments = offsetPayments;
    }


    /**
     * Gets the separatedPayments attribute.
     * 
     * @return Returns the separatedPayments.
     */
    public List<AssetPayment> getSeparatedPayments() {
        return separatedPayments;
    }


    /**
     * Sets the separatedPayments attribute value.
     * 
     * @param separatedPayments The separatedPayments to set.
     */
    public void setSeparatedPayments(List<AssetPayment> separatedPayments) {
        this.separatedPayments = separatedPayments;
    }


    /**
     * Gets the assetGlobal attribute.
     * 
     * @return Returns the assetGlobal.
     */
    public AssetGlobal getAssetGlobal() {
        return assetGlobal;
    }


    /**
     * Sets the assetGlobal attribute value.
     * 
     * @param assetGlobal The assetGlobal to set.
     */
    public void setAssetGlobal(AssetGlobal assetGlobal) {
        this.assetGlobal = assetGlobal;
    }


    /**
     * Gets the newAssets attribute.
     * 
     * @return Returns the newAssets.
     */
    public List<Asset> getNewAssets() {
        return newAssets;
    }


    /**
     * Sets the newAssets attribute value.
     * 
     * @param newAssets The newAssets to set.
     */
    public void setNewAssets(List<Asset> newAssets) {
        this.newAssets = newAssets;
    }


    /**
     * Gets the assetAllocateRatios attribute.
     * 
     * @return Returns the assetAllocateRatios.
     */
    public double[] getAssetAllocateRatios() {
        return assetAllocateRatios;
    }


    /**
     * Sets the assetAllocateRatios attribute value.
     * 
     * @param assetAllocateRatios The assetAllocateRatios to set.
     */
    public void setAssetAllocateRatios(double[] assetAllocateRatios) {
        this.assetAllocateRatios = assetAllocateRatios;
    }


    /**
     * Gets the separateRatio attribute.
     * 
     * @return Returns the separateRatio.
     */
    public double getSeparateRatio() {
        return separateRatio;
    }


    /**
     * Sets the separateRatio attribute value.
     * 
     * @param separateRatio The separateRatio to set.
     */
    public void setSeparateRatio(double separateRatio) {
        this.separateRatio = separateRatio;
    }


    /**
     * Gets the retainRatio attribute.
     * 
     * @return Returns the retainRatio.
     */
    public double getRetainRatio() {
        return retainRatio;
    }


    /**
     * Sets the retainRatio attribute value.
     * 
     * @param retainRatio The retainRatio to set.
     */
    public void setRetainRatio(double retainRatio) {
        this.retainRatio = retainRatio;
    }


    /**
     * Gets the sourcePayments attribute.
     * 
     * @return Returns the sourcePayments.
     */
    public List<AssetPayment> getSourcePayments() {
        return sourcePayments;
    }


    /**
     * Sets the sourcePayments attribute value.
     * 
     * @param sourcePayments The sourcePayments to set.
     */
    public void setSourcePayments(List<AssetPayment> sourcePayments) {
        this.sourcePayments = sourcePayments;
    }


}
