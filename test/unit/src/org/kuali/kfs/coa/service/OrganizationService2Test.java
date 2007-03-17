/*
 * Copyright 2006-2007 The Kuali Foundation.
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
package org.kuali.module.chart.service;

import static org.kuali.kfs.util.SpringServiceLocator.getOrganizationService;

import java.util.Collections;
import java.util.List;

import org.kuali.test.KualiTestBase;
import org.kuali.test.WithTestSpringContext;

@WithTestSpringContext
public class OrganizationService2Test extends KualiTestBase {

    private static final String GOOD_CHART = "BL";
    private static final String GOOD_ORG = "PSY";
    private static final String BAD_CHART = "ZZ";
    private static final String BAD_ORG = "ZZZ";
    private static final String GOOD_ORG2 = "BUS";

    public void testGetActiveAccountsByOrg_good() {

        List accounts;

        accounts = getOrganizationService().getActiveAccountsByOrg(GOOD_CHART, GOOD_ORG);

        assertFalse("List of Accounts should not contain no elements.", accounts.size() == 0);
        assertFalse("List of Accounts should not be empty.", accounts.isEmpty());

    }

    public void testGetActiveAccountsByOrg_bad() {

        List accounts;

        accounts = getOrganizationService().getActiveAccountsByOrg(BAD_CHART, BAD_ORG);

        assertEquals(Collections.EMPTY_LIST, accounts);
        assertTrue("List of Accounts should contain no elements.", accounts.size() == 0);
        assertTrue("List of Accounts should be empty.", accounts.isEmpty());

    }

    public void testGetActiveChildOrgs_good() {

        List orgs;

        orgs = getOrganizationService().getActiveChildOrgs(GOOD_CHART, GOOD_ORG2);

        assertFalse("List of Orgs should not contain no elements.", orgs.size() == 0);
        assertFalse("List of Orgs should not be empty.", orgs.isEmpty());

    }

    public void testGetActiveChildOrgs_bad() {

        List orgs;

        orgs = getOrganizationService().getActiveChildOrgs(BAD_CHART, BAD_ORG);

        assertEquals(Collections.EMPTY_LIST, orgs);
        assertTrue("List of Orgs should contain no elements.", orgs.size() == 0);
        assertTrue("List of Orgs should be empty.", orgs.isEmpty());

    }

}
