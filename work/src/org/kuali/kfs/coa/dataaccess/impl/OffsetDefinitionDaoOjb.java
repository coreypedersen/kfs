/*
 * Copyright 2005-2006 The Kuali Foundation.
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
package org.kuali.module.chart.dao.ojb;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryFactory;
import org.kuali.module.chart.bo.OffsetDefinition;
import org.kuali.module.chart.dao.OffsetDefinitionDao;
import org.springframework.orm.ojb.support.PersistenceBrokerDaoSupport;


/**
 * This class is the OJB implementation of the OffsetDefinitionDao interface.
 * 
 * 
 */
public class OffsetDefinitionDaoOjb extends PersistenceBrokerDaoSupport implements OffsetDefinitionDao {
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(OffsetDefinitionDaoOjb.class);

    /*
     * (non-Javadoc)
     * 
     * @see org.kuali.dao.OffsetDefinitionDao#getByPrimaryId(java.lang.Long, java.lang.String, java.lang.String, java.lang.String)
     */
    public OffsetDefinition getByPrimaryId(Integer universityFiscalYear, String chartOfAccountsCode, String financialDocumentTypeCode, String financialBalanceTypeCode) {
        Criteria criteria = new Criteria();
        criteria.addEqualTo("universityFiscalYear", universityFiscalYear);
        criteria.addEqualTo("chartOfAccountsCode", chartOfAccountsCode);
        criteria.addEqualTo("financialDocumentTypeCode", financialDocumentTypeCode);
        criteria.addEqualTo("financialBalanceTypeCode", financialBalanceTypeCode);

        return (OffsetDefinition) getPersistenceBrokerTemplate().getObjectByQuery(QueryFactory.newQuery(OffsetDefinition.class, criteria));
    }
}
