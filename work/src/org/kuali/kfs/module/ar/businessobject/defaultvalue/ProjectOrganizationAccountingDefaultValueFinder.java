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
package org.kuali.kfs.module.ar.businessobject.defaultvalue;

import org.kuali.rice.krad.valuefinder.ValueFinder;

public class ProjectOrganizationAccountingDefaultValueFinder extends ObjectOrganizationAccountingDefaultValueFinder implements ValueFinder {

    /**
     * Returns default project code from organization accounting default if BO is not null.
     * 
     * @see org.kuali.rice.krad.valuefinder.ValueFinder#getValue()
     */
    public String getValue() {
        return (organizationAccountingDefault != null)? organizationAccountingDefault.getDefaultInvoiceProjectCode() : "";
    }

}
