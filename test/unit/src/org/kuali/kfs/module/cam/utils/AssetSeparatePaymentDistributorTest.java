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
package org.kuali.kfs.module.cam.utils;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.module.cam.businessobject.Asset;
import org.kuali.kfs.module.cam.businessobject.AssetGlobal;
import org.kuali.kfs.module.cam.businessobject.AssetGlobalDetail;
import org.kuali.kfs.module.cam.businessobject.AssetPayment;
import org.kuali.kfs.module.cam.util.AssetSeparatePaymentDistributor;
import org.kuali.rice.kns.util.KualiDecimal;

import junit.framework.TestCase;

public class AssetSeparatePaymentDistributorTest extends TestCase {
    public void testDistributeByAsset() throws Exception {
        Asset sourceAsset = new Asset();
        sourceAsset.getAssetPayments().add(createPayment(13));
        sourceAsset.getAssetPayments().add(createPayment(13));
        sourceAsset.getAssetPayments().add(createPayment(13));
        // sourceAsset.getAssetPayments().add(createPayment(13));


        AssetGlobal assetGlobal = new AssetGlobal();
        assetGlobal.setTotalCostAmount(new KualiDecimal(39));
        assetGlobal.setSeparateSourceTotalAmount(new KualiDecimal(39));
        assetGlobal.setSeparateSourceRemainingAmount(new KualiDecimal(13));
        assetGlobal.getAssetGlobalDetails().add(createDetail(1L, new KualiDecimal(22)));
        assetGlobal.getAssetGlobalDetails().add(createDetail(2L, new KualiDecimal(17)));

        List<Asset> newAssets = new ArrayList<Asset>();
        newAssets.add(createAsset(1L));
        newAssets.add(createAsset(2L));

        AssetSeparatePaymentDistributor distributor = new AssetSeparatePaymentDistributor(sourceAsset, sourceAsset.getAssetPayments(), 5, assetGlobal, newAssets);
        distributor.distribute();

        // make sure payments are added to new asset
        Asset asset = newAssets.get(0);
        assertEquals(3, asset.getAssetPayments().size());
        assertEquals(new KualiDecimal(7.33), asset.getAssetPayments().get(0).getAccountChargeAmount());
        assertEquals(new KualiDecimal(7.33), asset.getAssetPayments().get(1).getAccountChargeAmount());
        assertEquals(new KualiDecimal(7.34), asset.getAssetPayments().get(2).getAccountChargeAmount());

        assertEquals(new KualiDecimal(7.33), asset.getAssetPayments().get(0).getPrimaryDepreciationBaseAmount());
        assertEquals(new KualiDecimal(7.33), asset.getAssetPayments().get(1).getPrimaryDepreciationBaseAmount());
        assertEquals(new KualiDecimal(7.34), asset.getAssetPayments().get(2).getPrimaryDepreciationBaseAmount());

        asset = newAssets.get(1);
        assertEquals(3, asset.getAssetPayments().size());
        assertEquals(new KualiDecimal(5.67), asset.getAssetPayments().get(0).getAccountChargeAmount());
        assertEquals(new KualiDecimal(5.67), asset.getAssetPayments().get(1).getAccountChargeAmount());
        assertEquals(new KualiDecimal(5.66), asset.getAssetPayments().get(2).getAccountChargeAmount());

        assertEquals(new KualiDecimal(5.67), asset.getAssetPayments().get(0).getPrimaryDepreciationBaseAmount());
        assertEquals(new KualiDecimal(5.67), asset.getAssetPayments().get(1).getPrimaryDepreciationBaseAmount());
        assertEquals(new KualiDecimal(5.66), asset.getAssetPayments().get(2).getPrimaryDepreciationBaseAmount());

        // make sure offset payments are added to source asset
        assertEquals(6, sourceAsset.getAssetPayments().size());
        assertEquals(new KualiDecimal(-13), sourceAsset.getAssetPayments().get(3).getAccountChargeAmount());
        assertEquals(new KualiDecimal(-13), sourceAsset.getAssetPayments().get(4).getAccountChargeAmount());
        assertEquals(new KualiDecimal(-13), sourceAsset.getAssetPayments().get(5).getAccountChargeAmount());

        assertEquals(new KualiDecimal(-13), sourceAsset.getAssetPayments().get(3).getPrimaryDepreciationBaseAmount());
        assertEquals(new KualiDecimal(-13), sourceAsset.getAssetPayments().get(4).getPrimaryDepreciationBaseAmount());
        assertEquals(new KualiDecimal(-13), sourceAsset.getAssetPayments().get(5).getPrimaryDepreciationBaseAmount());

        // sum and see if dollar amount sums up correctly

    }

    private Asset createAsset(long assetNumber) {
        Asset asset = new Asset();
        asset.setCapitalAssetNumber(assetNumber);
        return asset;
    }

    private AssetGlobalDetail createDetail(long assetNumber, KualiDecimal separateAmount) {
        AssetGlobalDetail detail = new AssetGlobalDetail();
        detail.setCapitalAssetNumber(assetNumber);
        detail.setSeparateSourceAmount(separateAmount);
        return detail;
    }

    private AssetPayment createPayment(double amount) {
        AssetPayment payment = new AssetPayment();
        payment.setAccountNumber("DUMMY");
        payment.setAccountChargeAmount(new KualiDecimal(amount));
        payment.setPrimaryDepreciationBaseAmount(new KualiDecimal(amount));
        return payment;
    }
}
