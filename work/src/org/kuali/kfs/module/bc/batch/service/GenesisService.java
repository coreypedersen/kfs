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
package org.kuali.kfs.module.bc.batch.service;

/*
 * this service intializes/updates the budget construction data used by the budget module to build a new budget for the coming
 * fiscal year
 */
public interface GenesisService {
    /*
     * these routines indicate which actions are allowed in genesis
     */
    public boolean BatchPositionSynchAllowed(Integer BaseYear);
    
    public boolean CSFUpdatesAllowed(Integer BaseYear);

    public boolean GLUpdatesAllowed(Integer BaseYear);
    
    public boolean IsBudgetConstructionInUpdateMode(Integer BaseYear);

    // this step clears out the database for genesis
    public void clearDBForGenesis(Integer BaseYear);
    
    /*
     * this step updates budget construction with new data from the sources after genesis has run
     */
   public void bCUpdateStep(Integer baseYear); 
    
    /*
     * this step fetches the base fiscal year based on today's date
     */
    public Integer genesisFiscalYearFromToday();

    /*
     *  this step runs genesis
     */
    public void genesisStep(Integer baseYear);
}
