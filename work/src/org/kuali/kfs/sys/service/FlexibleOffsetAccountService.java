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
package org.kuali.kfs.sys.service;

import org.kuali.kfs.fp.businessobject.OffsetAccount;
import org.kuali.kfs.gl.businessobject.FlexibleAccountUpdateable;

/**
 * 
 * This interface defines methods that a FlexibleOffsetAccount Service must provide.
 * 
 */
public interface FlexibleOffsetAccountService {

    /**
     * Retrieves the OffsetAccount by its composite primary key (all passed in as parameters) if the SYSTEM parameter
     * FLEXIBLE_OFFSET_ENABLED_FLAG is true.
     * 
     * @param chartOfAccountsCode The chart code of the account to be retrieved.
     * @param accountNumber The account number of the account to be retrieved.
     * @param financialOffsetObjectCode Offset object code used to retrieve the OffsetAccount.
     * @return An OffsetAccount object instance. Returns null if there is none with the given key, or if the SYSTEM parameter
     *         FLEXIBLE_OFFSET_ENABLED_FLAG is false.
     */
    public OffsetAccount getByPrimaryIdIfEnabled(String chartOfAccountsCode, String accountNumber, String financialOffsetObjectCode);

    /**
     * Retrieves whether the SYSTEM parameter FLEXIBLE_OFFSET_ENABLED_FLAG is true.
     * 
     * @return Whether the SYSTEM parameter FLEXIBLE_OFFSET_ENABLED_FLAG is true.
     */
    public boolean getEnabled();

    /**
     * This method will apply the flexible offset account if necessary. It will only change the chart, account, sub account and sub
     * object on the transaction. If the flexible offset isn't enabled or valid for this transaction, it will be unchanged.
     * 
     * It throws an InvalidFlexibleOffsetException if the flexible offset account associated with the transaction
     * is invalid, closed or expired or if the object code is invalid for the flexible offset.
     * 
     * @param transaction The OriginEntryFull object to be updated.
     * @return True if transaction was changed, false if not.
     */
    public boolean updateOffset(FlexibleAccountUpdateable transaction);
}
