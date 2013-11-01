/*
 * Copyright 2011 The Kuali Foundation.
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
package org.kuali.kfs.module.tem.service;

import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.tem.businessobject.TEMProfile;
import org.kuali.kfs.module.tem.businessobject.TemProfileAddress;
import org.kuali.rice.kim.api.identity.Person;

public interface TemProfileService {

    /**
     *
     * This method returns a TEMProfile associated with the principalId.
     * @param principalId
     * @return
     */
    public TEMProfile findTemProfileByPrincipalId(String principalId);

    /**
     *
     * This method retrieves a KIM user's address if it is a KIM user, or the default address if they are not a KIM user.
     * @param profile
     * @param defaultAddress
     * @return
     */
    public TemProfileAddress getAddressFromProfile(TEMProfile profile, TemProfileAddress defaultAddress);

    /**
     *
     * This method creates a {@link TemProfileAddress} from the KIM Person
     * @param person
     * @param profileId
     * @param defaultAddress
     * @return
     */
    public TemProfileAddress createTemProfileAddressFromPerson(Person person, Integer profileId, TemProfileAddress defaultAddress);

    /**
     *
     * This method returns a TEMProfile matching the criteria passed in.
     * @param criteria
     * @return
     */
    public TEMProfile findTemProfile(Map<String,String> criteria);

    /**
     *
     * This method returns a TEMProfile from the profileId
     * @param profileId
     * @return
     */
    public TEMProfile findTemProfileById(Integer profileId);

    /**
     *
     * This method returns all active TEMProfiles
     * @return
     */
    public List<TEMProfile> getAllActiveTemProfile();

    /**
     * Lookup PayeeACHAccount with matching employee number in the payee Id field.
     * If PayeeACHAccount is found, update the two profile fields: AchSignUp & AchTransactionType
     *
     * @param profile
     */
    public void updateACHAccountInfo(TEMProfile profile);

    /**
     * Determines if the given profile is a record for a non-employee
     * @param profile the profile to check
     * @return true if profile is a non-employee, false if the profile is an employee or if it could not be determined
     */
    public boolean isProfileNonEmploye(TEMProfile profile);

    /**
     * Determines if the given profile has any active arrangers
     * @param profile the profile to check
     * @return true if there are active arrangers associated with the profile, false otherwise
     */
    public boolean hasActiveArrangers(TEMProfile profile);

    /**
     * This method returns a TEMProfile for the given employeeId
     *
     * @param employeeId
     * @return
     */
    public TEMProfile findTemProfileByEmployeeId(String employeeId);

    /**
     * This method returns a TEMProfile for the given customer number
     *
     * @param customerNumber
     * @return
     */
    public TEMProfile findTemProfileByCustomerNumber(String customerNumber);
}