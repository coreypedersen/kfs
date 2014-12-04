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
package org.kuali.kfs.module.bc.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.bc.BCParameterKeyConstants;
import org.kuali.kfs.module.bc.batch.GenesisBatchStep;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionIntendedIncumbent;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPayRateHolding;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPosition;
import org.kuali.kfs.module.bc.document.BudgetConstructionDocument;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;

/**
 * A convenient utility that can delegate the calling client to retrieve system parameters of budget construction module.
 */
public class BudgetParameterFinder {
    private static ParameterService parameterService = SpringContext.getBean(ParameterService.class);

    /**
     * get the biweekly pay type codes setup in system parameters
     * 
     * @return the biweekly pay type codes setup in system parameters
     */
    public static Collection<String> getBiweeklyPayTypeCodes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.BIWEEKLY_PAY_TYPE_CODES);
    }

    /**
     * get the annual working hours setup in system paremters for extract process
     * 
     * @return the annual working hours setup in system paremters
     */
    public static Integer getAnnualWorkingHours() {
        String annualWorkingHours = parameterService.getParameterValueAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.ANNUAL_WORKING_HOURS);

        return Integer.valueOf(StringUtils.trim(annualWorkingHours));
    }

    /**
     * get the weekly working hours setup in system paremters for extract process
     * 
     * @return the weekly working hours setup in system paremters
     */
    public static Integer getWeeklyWorkingHours() {
        String weeklyWorkingHours = parameterService.getParameterValueAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.WEEKLY_WORKING_HOURS);

        return Integer.valueOf(StringUtils.trim(weeklyWorkingHours));
    }

    /**
     * get the weekly working hours setup in system paremters for extract process
     * 
     * @return the weekly working hours setup in system paremters
     */
    public static BigDecimal getWeeklyWorkingHoursAsDecimal() {
        Integer weeklyWorkingHours = getWeeklyWorkingHours();

        return BigDecimal.valueOf(weeklyWorkingHours);
    }

    /**
     * get the sub fund group codes not allowed 2plg generation setup in system parameters
     * 
     * @return the sub fund group codes not allowed 2plg generation setup in system parameters
     */
    public static Collection<String> getNotGenerate2PlgSubFundGroupCodes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.GENERATE_2PLG_SUB_FUND_GROUPS);
    }

    /**
     * get the biweekly pay object codes setup in system parameters
     * 
     * @return the biweekly pay object codes setup in system parameters
     */
    public static Collection<String> getBiweeklyPayObjectCodes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionPayRateHolding.class, BCParameterKeyConstants.BIWEEKLY_PAY_OBJECT_CODES);
    }

    /**
     * get the revenue object types allowed in budget setup in system parameters
     * 
     * @return the revenue object types allowed in budget setup in system parameters
     */
    public static Collection<String> getRevenueObjectTypes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.REVENUE_OBJECT_TYPES);
    }

    /**
     * get the expenditure object types allowed in budget setup in system parameters
     * 
     * @return the expenditure object types allowed in budget setup in system parameters
     */
    public static Collection<String> getExpenditureObjectTypes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.EXPENDITURE_OBJECT_TYPES);
    }

    /**
     * get the budget aggregation codes setup in system parameters
     * 
     * @return the budget aggregation codes setup in system parameters
     */
    public static Collection<String> getBudgetAggregationCodes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.BUDGET_AGGREGATION_CODES);
    }

    /**
     * get the fringe benefit designator codes setup in system parameters
     * 
     * @return the fringe benefit designator codes setup in system parameters
     */
    public static Collection<String> getFringeBenefitDesignatorCodes() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.FRINGE_BENEFIT_DESIGNATOR_CODES);
    }

    /**
     * get the salary setting fund groups setup in system parameters
     * 
     * @return the salary setting fund groups setup in system parameters
     */
    public static Collection<String> getSalarySettingFundGroups() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.SALARY_SETTING_FUND_GROUPS);
    }

    /**
     * get the salary setting sub fund groups setup in system parameters
     * 
     * @return the salary setting sub fund groups setup in system parameters
     */
    public static Collection<String> getSalarySettingSubFundGroups() {
        return parameterService.getParameterValuesAsString(BudgetConstructionDocument.class, BCParameterKeyConstants.SALARY_SETTING_SUB_FUND_GROUPS);
    }

    /**
     * indicates whether the data for the budget construction intended incumbent table is populated from an external system or is
     * maintained within the KFS.
     */
    public static boolean getPayrollIncumbentFeedIndictor() {
        return parameterService.getParameterValueAsBoolean(BudgetConstructionIntendedIncumbent.class, BCParameterKeyConstants.EXTERNAL_INCUMBENT_FEED_IND);
    }

    /**
     * Indicates whether the data for the budget construction position table is populated from an external system or is maintained
     * within the KFS.
     */
    public static boolean getPayrollPositionFeedIndicator() {
        return parameterService.getParameterValueAsBoolean(BudgetConstructionPosition.class, BCParameterKeyConstants.EXTERNAL_POSITION_FEED_IND);
    }
    
    /**
     * returns the base fiscal year to use to initialize budget construction
     */
    
    public static Integer getBaseFiscalYear()
    {
        String yearValue = parameterService.getParameterValueAsString(GenesisBatchStep.class, BCParameterKeyConstants.SOURCE_FISCAL_YEAR);
        return (Integer.valueOf(StringUtils.trim(yearValue)));
    }
}
