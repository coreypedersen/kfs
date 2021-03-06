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
package org.kuali.kfs.module.endow.document.validation.impl;

import java.sql.Date;

import org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase;
import org.kuali.kfs.module.endow.businessobject.PooledFundControl;
import org.kuali.kfs.module.endow.businessobject.PooledFundValue;
import org.kuali.kfs.module.endow.document.service.PooledFundControlService;
import org.kuali.kfs.module.endow.document.service.PooledFundValueService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.document.MaintenanceDocument;
import org.kuali.rice.krad.util.ObjectUtils;

public class PooledFundValuePreRule extends MaintenancePreRulesBase {

    private PooledFundValue newPooledFundValue;
    private PooledFundControl pooledFundControl;

    /**
     * Set value for newCashSweepModel.
     * 
     * @param document the CashSweepModel Maintenance document
     */
    private void setupConvenienceObjects(MaintenanceDocument document) {

        // setup newCashSweepModel convenience objects, make sure all possible sub-objects are populated
        newPooledFundValue = (PooledFundValue) document.getNewMaintainableObject().getBusinessObject();
        newPooledFundValue.refreshNonUpdateableReferences();
        pooledFundControl = newPooledFundValue.getPooledFundControl();
        if (!ObjectUtils.isNotNull(pooledFundControl)) {
            PooledFundControlService pooledFundControlService = SpringContext.getBean(PooledFundControlService.class);
            pooledFundControl = pooledFundControlService.getByPrimaryKey(newPooledFundValue.getPooledSecurityID());
        }
    }

    /**
     * @see org.kuali.kfs.coa.document.validation.impl.MaintenancePreRulesBase#doCustomPreRules(org.kuali.rice.kns.document.MaintenanceDocument)
     */
    @Override
    protected boolean doCustomPreRules(MaintenanceDocument maintenanceDocument) {

        setupConvenienceObjects(maintenanceDocument);
        if (ObjectUtils.isNotNull(pooledFundControl)) {
            // Set the date of last sweep model change -- the date is the time when the maintenance doc is submitted, not
            // the time when that maintenance doc gets approved.
            PooledFundValueService pooledFundValueService = SpringContext.getBean(PooledFundValueService.class);
            Date valuationDate = newPooledFundValue.getValuationDate();
            String pooledSecurityID = newPooledFundValue.getPooledSecurityID();

            if (valuationDate != null && pooledSecurityID != null) {

                Date valueEffectiveDate = pooledFundValueService.calculateValueEffectiveDate(valuationDate, pooledSecurityID);
                newPooledFundValue.setValueEffectiveDate(valueEffectiveDate);
                return true;
            }
        }

        return true;
    }

}
