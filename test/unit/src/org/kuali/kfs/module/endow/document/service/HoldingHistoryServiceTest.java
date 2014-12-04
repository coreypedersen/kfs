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
package org.kuali.kfs.module.endow.document.service;

import static org.kuali.kfs.sys.fixture.UserNameFixture.khuntley;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;

import org.kuali.kfs.module.endow.businessobject.HoldingHistory;
import org.kuali.kfs.module.endow.businessobject.KEMID;
import org.kuali.kfs.module.endow.businessobject.MonthEndDate;
import org.kuali.kfs.module.endow.businessobject.Security;
import org.kuali.kfs.module.endow.fixture.HoldingHistoryFixture;
import org.kuali.kfs.module.endow.fixture.KemIdFixture;
import org.kuali.kfs.module.endow.fixture.MonthEndDateFixture;
import org.kuali.kfs.module.endow.fixture.SecurityFixture;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.type.KualiInteger;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * Contains methods that test the HoldingHistoryService.
 */
@ConfigureContext(session = khuntley)
public class HoldingHistoryServiceTest extends KualiTestBase {

    private HoldingHistoryService holdingHistoryService;
    private BusinessObjectService businessObjectService;
    
    /**
     * 
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        holdingHistoryService = SpringContext.getBean(HoldingHistoryService.class);
        businessObjectService = SpringContext.getBean(BusinessObjectService.class);
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Retrieves the records from history holding table for the given security id and monthend id
     * First use case is to set monthendid = 0 and making sure no records will be retrieved.
     */
    public void testGetHoldingHistoryBySecuritIdAndMonthEndId_NoRecords() {
        String securityId = "123456789";
        KualiInteger monthEndId = new KualiInteger(0);
        
        // there should not be any records that match monthEndId = 0
        Collection<HoldingHistory> holdingHistory = new ArrayList();  
        holdingHistory = holdingHistoryService.getHoldingHistoryBySecuritIdAndMonthEndId(securityId, monthEndId);
        assertTrue("There will not be any records since MonthEndID = 0 does not correspond to any date", holdingHistory.isEmpty());
    }
    
    /**
     * Retrieves the records from history holding table for the given security id and monthend id
     * First use case is to set monthendId = 0 and making sure no records will be retrieved.
     */
    public void testGetHoldingHistoryBySecuritIdAndMonthEndId_RecordsFound() {
        MonthEndDate monthEndDate = MonthEndDateFixture.MONTH_END_DATE_TEST_RECORD.createMonthEndDate();
        
        Collection<HoldingHistory> holdingHistory = new ArrayList();   
        
        Security security = SecurityFixture.ENDOWMENT_SECURITY_RECORD.createSecurityRecord("TESTSECID", "910", BigDecimal.ONE, "M01", Date.valueOf("2010-01-01"), BigDecimal.valueOf(20L), true, BigDecimal.valueOf(100.20));
        KEMID kemid = KemIdFixture.ALLOW_TRAN_KEMID_RECORD.createKemidRecord();
        
        //insert into end_hldg_hist_t a dummy record so it can be queries.
        HoldingHistory hh = HoldingHistoryFixture.HOLDING_HISTORY_RECORD2.createHoldingHistoryRecord();
        
        String securityId = hh.getSecurityId();
        KualiInteger monthEndId = monthEndDate.getMonthEndDateId();
        
        holdingHistory = holdingHistoryService.getHoldingHistoryBySecuritIdAndMonthEndId(securityId, monthEndId);
        assertTrue("There should have been at least one record since we just added it!", !holdingHistory.isEmpty());
    }
}
