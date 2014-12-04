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
package org.kuali.kfs.module.endow.document.service.impl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.endow.EndowPropertyConstants;
import org.kuali.kfs.module.endow.businessobject.HoldingTaxLot;
import org.kuali.kfs.module.endow.document.service.KemidHoldingTaxLotOpenRecordsService;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * This KemidHoldingTaxLotOpenRecordsServiceImpl class provides the implementation for the method to test whether a KEMID has open
 * records in Holding Tax Lot: records with values greater or less than zero for the following fields: Holding Units, Holding Cost
 * and Current Accrual.
 */
public class KemidHoldingTaxLotOpenRecordsServiceImpl implements KemidHoldingTaxLotOpenRecordsService {

    private BusinessObjectService businessObjectService;

    /**
     * @see org.kuali.kfs.module.endow.document.service.KemidHoldingTaxLotOpenRecordsService#hasKemidHoldingTaxLotOpenRecords(java.lang.String)
     */
    public boolean hasKemidHoldingTaxLotOpenRecords(String kemid) {
        boolean hasOpenRecords = false;

        Map fieldValuesMap = new HashMap();
        fieldValuesMap.put(EndowPropertyConstants.KEMID, kemid);
        List<HoldingTaxLot> kemidHoldingTaxLotList = (List<HoldingTaxLot>) businessObjectService.findMatching(HoldingTaxLot.class, fieldValuesMap);

        if (kemidHoldingTaxLotList.size() != 0) {
            for (HoldingTaxLot holdingTaxLot : kemidHoldingTaxLotList) {
                // if the record has values greater or less than zero than return true
                if (holdingTaxLot.getUnits().compareTo(BigDecimal.ZERO) != 0 || holdingTaxLot.getCost().compareTo(BigDecimal.ZERO) == 0 || holdingTaxLot.getCurrentAccrual().compareTo(BigDecimal.ZERO) == 0) {
                    hasOpenRecords = true;
                    break;
                }
            }
        }
        return hasOpenRecords;
    }


    /**
     * Gets the businessObjectService.
     * 
     * @return businessObjectService
     */
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * Sets the businessObjectService.
     * 
     * @param businessObjectService
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }


}
