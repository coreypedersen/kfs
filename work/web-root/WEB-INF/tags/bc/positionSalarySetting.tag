<%--
 Copyright 2006-2007 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%@ taglib prefix="c" uri="/tlds/c.tld" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="kul" %>
<%@ taglib uri="/tlds/fmt.tld" prefix="fmt" %>
<%@ taglib uri="/tlds/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags/bc" prefix="bc"%>

<c:if test="${!accountingLineScriptsLoaded}">
	<script type='text/javascript' src="dwr/interface/ChartService.js"></script>
	<script type='text/javascript' src="dwr/interface/AccountService.js"></script>
	<script type='text/javascript' src="dwr/interface/SubAccountService.js"></script>
	<script type='text/javascript' src="dwr/interface/ObjectCodeService.js"></script>
	<script type='text/javascript' src="dwr/interface/ObjectTypeService.js"></script>
	<script type='text/javascript' src="dwr/interface/SubObjectCodeService.js"></script>
	<script type='text/javascript' src="dwr/interface/ProjectCodeService.js"></script>
	<script type='text/javascript' src="dwr/interface/OriginationCodeService.js"></script>
	<script type='text/javascript' src="dwr/interface/DocumentTypeService.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/kfs/objectInfo.js"></script>
	<c:set var="accountingLineScriptsLoaded" value="true" scope="page" />
</c:if>

<c:set var="bcafAttributes"
	value="${DataDictionary['PendingBudgetConstructionAppointmentFunding'].attributes}" />
<c:set var="positionAttributes"
	value="${DataDictionary['BudgetConstructionPosition'].attributes}" />
<c:set var="intincAttributes"
	value="${DataDictionary['BudgetConstructionIntendedIncumbent'].attributes}" />
<c:set var="bcsfAttributes"
	value="${DataDictionary['BudgetConstructionCalculatedSalaryFoundationTracker'].attributes}" />

<c:set var="readOnly" value="${KualiForm.editingMode['systemViewOnly'] || !KualiForm.editingMode['fullEntry']}" />

<kul:tabTop tabTitle="Salary Setting by Position" defaultOpen="true" tabErrorKey="${Constants.BUDGET_CONSTRUCTION_POSITION_SALARY_SETTING_TAB_ERRORS}">
<div class="tab-container" align=center>
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable" summary="">
        <bc:subheadingWithDetailToggleRow
          columnCount="15"
          subheading="Position" />
        <tr>
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.positionNumber"
                literalLabel="Fy/Pos#:"
                horizontal="true" >
              <html:hidden property="returnAnchor" />
              <html:hidden property="returnFormKey" />
              <html:hidden property="chartOfAccountsCode" />
              <html:hidden property="accountNumber" />
              <html:hidden property="subAccountNumber" />
              <html:hidden property="financialObjectCode" />
              <html:hidden property="financialSubObjectCode" />
              <html:hidden property="returnFormKey" />
            </kul:htmlAttributeHeaderCell>

            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.universityFiscalYear"
                field="universityFiscalYear"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                accountingLine="budgetConstructionPosition"
                field="positionNumber"
                attributes="${posnAttributes}" inquiry="true"
                boClassSimpleName="BudgetConstructionPosition"
                boPackageName="org.kuali.module.budget.bo"
                readOnly="true"
                displayHidden="false"
                lookupOrInquiryKeys="universityFiscalYear"
                accountingLineValuesMap="${KualiForm.budgetConstructionPosition.valuesMap}" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.positionDescription"
                field="positionDescription"
                attributes="${positionAttributes}"
                colSpan="8"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.iuDefaultObjectCode"
                literalLabel="Dflt.Obj:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.iuDefaultObjectCode"
                field="iuDefaultObjectCode"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.positionDepartmentIdentifier"
                literalLabel="Dflt.Obj:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.positionDepartmentIdentifier"
                field="positionDepartmentIdentifier"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
        </tr>
        <tr>
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.jobCode"
                literalLabel="SetId/JCode:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.setidJobCode"
                field="setidJobCode"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.jobCode"
                field="jobCode"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.positionSalaryPlanDefault"
                literalLabel="SalPln:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.positionSalaryPlanDefault"
                field="positionSalaryPlanDefault"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.positionGradeDefault"
                literalLabel="Grd:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.positionGradeDefault"
                field="positionGradeDefault"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.iuNormalWorkMonths"
                literalLabel="WM:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.iuNormalWorkMonths"
                field="iuNormalWorkMonths"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.iuPayMonths"
                literalLabel="PM:"
                horizontal="true" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.iuPayMonths"
                field="iuPayMonths"
                attributes="${positionAttributes}"
                readOnly="true"
                displayHidden="false" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.positionStandardHoursDefault"
                literalLabel="SHr:"
                horizontal="true" />
            <fmt:formatNumber value="${KualiForm.budgetConstructionPosition.positionStandardHoursDefault}" var="formattedNumber" type="number" groupingUsed="true" minFractionDigits="2" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.positionStandardHoursDefault"
                field="positionStandardHoursDefault"
                attributes="${positionAttributes}"
                readOnly="true"
                formattedNumberValue="${formattedNumber}"
                displayHidden="false" dataFieldCssClass="amount" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.positionFullTimeEquivalency"
                literalLabel="FTE:"
                horizontal="true" />
            <fmt:formatNumber value="${KualiForm.budgetConstructionPosition.positionFullTimeEquivalency}" var="formattedNumber" type="number" groupingUsed="true" minFractionDigits="2" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                cellProperty="budgetConstructionPosition.positionFullTimeEquivalency"
                field="positionFullTimeEquivalency"
                attributes="${positionAttributes}"
                readOnly="true"
                formattedNumberValue="${formattedNumber}"
                displayHidden="false" />
        </tr>
    </table>
        
    <table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable">
        <tr>
            <td colspan="11" class="subhead">
            <span class="subhead-left">Funding</span>
            </td>
        </tr>
        <c:forEach items="${KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding}" var="item" varStatus="status">
        <tr>
            <kul:htmlAttributeHeaderCell literalLabel="Del" scope="row">
            </kul:htmlAttributeHeaderCell>
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.chartOfAccountCode"
                literalLabel="Cht" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.accountNumber"
                literalLabel="Acct" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.subAccountNumber"
                literalLabel="SAcct" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.financialObjectCode"
                literalLabel="Obj" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.financialSubObjectCode"
                literalLabel="SObj" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.emplid"
                literalLabel="Emplid" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.budgetConstructionIntendedIncumbent.personName"
                literalLabel="Name" />
            <kul:htmlAttributeHeaderCell
                labelFor="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding.budgetConstructionIntendedIncumbent.iuClassificationLevel"
                literalLabel="Lvl" />
<%-- TODO add administrative post table ref to BCAF --%>
            <th>
                AdmPst
            </th>
            <th>
                Actions
            </th>
              
        </tr>
        <tr>
            <td valign="top">
                <%-- these hidden fields are inside a table cell to keep the HTML valid --%>
                <a name="salaryexistingLineLineAnchor${status.index}"></a>
                ${item.appointmentFundingDeleteIndicator}
<%-- TODO add the others --%>
                <html:hidden property="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].universityFiscalYear" />
                <html:hidden property="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].positionNumber" />
                <html:hidden property="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].versionNumber" />
           </td>
           <bc:pbglLineDataCell dataCellCssClass="datacell"
               accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
               field="chartOfAccountsCode"
               detailField="chartOfAccounts.finChartOfAccountDescription"
               attributes="${bcafAttributes}" inquiry="true"
               boClassSimpleName="Chart"
               readOnly="true"
               displayHidden="false"
               accountingLineValuesMap="${item.valuesMap}" />
           <bc:pbglLineDataCell dataCellCssClass="datacell"
               accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
               field="accountNumber" detailFunction="loadAccountInfo"
               detailField="account.accountName"
               attributes="${bcafLineAttributes}" inquiry="true"
               boClassSimpleName="Account"
               readOnly="true"
               displayHidden="false"
               lookupOrInquiryKeys="chartOfAccountsCode"
               accountingLineValuesMap="${item.valuesMap}" />
           
           <c:set var="doAccountLookupOrInquiry" value="false"/>
           <c:if test="${item.subAccountNumber ne Constants.DASHES_SUB_ACCOUNT_NUMBER}">
               <c:set var="doAccountLookupOrInquiry" value="true"/>
           </c:if>
           <bc:pbglLineDataCell dataCellCssClass="datacell"
               accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
               field="subAccountNumber" detailFunction="loadSubAccountInfo"
               detailField="subAccount.subAccountName"
               attributes="${bcafLineAttributes}" inquiry="${doAccountLookupOrInquiry}"
               boClassSimpleName="SubAccount"
               readOnly="true"
               displayHidden="false"
               lookupOrInquiryKeys="chartOfAccountsCode,accountNumber"
               accountingLineValuesMap="${item.valuesMap}" />

           <bc:pbglLineDataCell dataCellCssClass="datacell"
               accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
               field="financialObjectCode" detailFunction="loadObjectInfo"
               detailFunctionExtraParam="KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].universityFiscalYear', "
               detailField="financialObject.financialObjectCodeShortName"
               attributes="${bcafAttributes}" lookup="true" inquiry="true"
               boClassSimpleName="ObjectCode"
               readOnly="true"
               displayHidden="false"
               lookupOrInquiryKeys="chartOfAccountsCode"
               accountingLineValuesMap="${item.valuesMap}"
               inquiryExtraKeyValues="universityFiscalYear=${item.universityFiscalYear}" />

           <c:set var="doLookupOrInquiry" value="false"/>
           <c:if test="${item.financialSubObjectCode ne Constants.DASHES_SUB_OBJECT_CODE}">
               <c:set var="doLookupOrInquiry" value="true"/>
           </c:if>
           <bc:pbglLineDataCell dataCellCssClass="datacell"
               accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
               field="financialSubObjectCode" detailFunction="loadSubObjectInfo"
               detailFunctionExtraParam="'KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].universityFiscalYear', "
               detailField="financialSubObject.financialSubObjectCdshortNm"
               attributes="${bcafAttributes}" inquiry="${doLookupOrInquiry}"
               boClassSimpleName="SubObjCd"
               readOnly="true"
               displayHidden="false"
               lookupOrInquiryKeys="chartOfAccountsCode,financialObjectCode,accountNumber"
               accountingLineValuesMap="${item.valuesMap}"
               inquiryExtraKeyValues="universityFiscalYear=${item.universityFiscalYear}" />

           <c:set var="doLookupOrInquiry" value="false"/>
           <c:if test="${item.emplid ne Constants.BudgetConstructionConstants.VACANT_EMPLID}">
               <c:set var="doLookupOrInquiry" value="true"/>
           </c:if>
           <bc:pbglLineDataCell dataCellCssClass="datacell"
               accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
               field="emplid"
               attributes="${bcafAttributes}" inquiry="${doLookupOrInquiry}"
               boClassSimpleName="BudgetConstructionIntendedIncumbent"
               boPackageName="org.kuali.module.budget.bo"
               readOnly="true"
               displayHidden="false"
               accountingLineValuesMap="${item.valuesMap}" />

            <c:choose>
            <c:when test="${item.emplid ne Constants.BudgetConstructionConstants.VACANT_EMPLID}">
                <bc:pbglLineDataCell dataCellCssClass="datacell"
                    cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].budgetConstructionIntendedIncumbent.personName"
                    field="personName"
                    attributes="${intincAttributes}"
                    readOnly="true"
                    displayHidden="false" />
                <bc:pbglLineDataCell dataCellCssClass="datacell"
                    cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].budgetConstructionIntendedIncumbent.iuClassificationLevel"
                    field="iuClassificationLevel"
                    attributes="${intincAttributes}"
                    readOnly="true"
                    displayHidden="false" />
<%--
                <td>${item.budgetConstructionIntendedIncumbent.iuClassificationLevel}</td>
--%>
<%-- TODO add adminstrative post ref to BCAF and here --%>
                <td>&nbsp;</td>
            </c:when>
            <c:otherwise>
                <bc:pbglLineDataCell dataCellCssClass="datacell"
                    cellProperty="budgetConstructionPosition.positionFullTimeEquivalency"
                    field="positionFullTimeEquivalency"
                    attributes="${positionAttributes}"
                    readOnly="true"
                    formattedNumberValue="${Constants.BudgetConstructionConstants.VACANT_EMPLID}"
                    displayHidden="false" />
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </c:otherwise>
            </c:choose>

            <td class="datacell" nowrap>
                <div align="center">
                  <c:if test="${item.emplid ne Constants.BudgetConstructionConstants.VACANT_EMPLID}">
                    <c:if test="${!readOnly}">
                        <html:image property="methodToCall.performVacateSalarySettingLine.line${status.index}.anchorsalaryexistingLineLineAnchor${status.index}" src="images/tinybutton-clear1.gif" title="Vacate Salary Setting Line ${status.index}" alt="Vacate Salary Setting Line ${status.index}" styleClass="tinybutton" />
                        <br>
                    </c:if>
                  </c:if>
                  <c:if test="${!empty item.bcnCalculatedSalaryFoundationTracker && !readOnly}">
                    <html:image property="methodToCall.performPercentAdjustmentSalarySettingLine.line${status.index}.anchorsalaryexistingLineLineAnchor${status.index}" src="images/tinybutton-percentincdec.gif" title="Percent Adjustment For Line ${status.index}" alt="Percent Adjustment For Line ${status.index}" styleClass="tinybutton" />
                  </c:if>
                  &nbsp;
                </div>
            </td>
        </tr>
                                        
        <tr>
            <th>
                &nbsp;
            </th>
            <kul:htmlAttributeHeaderCell colspan="2" align="left" literalLabel="CSF" scope="col" />
            <kul:htmlAttributeHeaderCell colspan="3" align="left" literalLabel="Request" scope="col" />
            <kul:htmlAttributeHeaderCell colspan="2" align="left" literalLabel="Leaves Req.CSF" scope="col" />
            <kul:htmlAttributeHeaderCell colspan="3" align="left" literalLabel="Tot.Int." scope="col" />
        </tr>
        <tr>
            <kul:htmlAttributeHeaderCell scope="col" />

            <c:choose>
            <c:when test="${!empty item.bcnCalculatedSalaryFoundationTracker}">
                <bc:pbglLineDataCell dataCellCssClass="datacell"
                    cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].bcnCalculatedSalaryFoundationTracker[0].csfAmount"
                    field="csfAmount"
                    fieldAlign="right"
                    attributes="${bcsfAttributes}"
                    readOnly="true"
                    displayHidden="false" />
                <fmt:formatNumber value="${KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[status.index].bcnCalculatedSalaryFoundationTracker[0].csfFullTimeEmploymentQuantity}" var="formattedNumber" type="number" groupingUsed="true" minFractionDigits="5" />
                <bc:pbglLineDataCell dataCellCssClass="datacell"
                    cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].bcnCalculatedSalaryFoundationTracker[0].csfFullTimeEmploymentQuantity"
                    field="csfTimePercent"
                    fieldAlign="right"
                    attributes="${bcsfAttributes}"
                    readOnly="true"
                    formattedNumberValue="${formattedNumber}"
                    displayHidden="false" />
            </c:when>
            <c:otherwise>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
            </c:otherwise>
            </c:choose>

            <bc:pbglLineDataCell dataCellCssClass="datacell"
                accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
                cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].appointmentRequestedAmount"
                attributes="${bcafAttributes}"
                field="appointmentRequestedAmount"
                fieldAlign="right"
                readOnly="${readOnly}"
                rowSpan="1" dataFieldCssClass="amount" />
            <fmt:formatNumber value="${KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[status.index].appointmentRequestedFteQuantity}" var="formattedNumber" type="number" groupingUsed="true" minFractionDigits="5" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
                cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].appointmentRequestedFteQuantity"
                attributes="${bcafAttributes}"
                field="appointmentRequestedFteQuantity"
                fieldAlign="right"
                readOnly="true"
                formattedNumberValue="${formattedNumber}"
                colSpan="2" rowSpan="1" dataFieldCssClass="amount" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
                cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].appointmentRequestedCsfAmount"
                attributes="${bcafAttributes}"
                field="appointmentRequestedCsfAmount"
                fieldAlign="right"
                readOnly="${readOnly}"
                rowSpan="1" dataFieldCssClass="amount" />
            <fmt:formatNumber value="${KualiForm.budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[status.index].appointmentRequestedCsfFteQuantity}" var="formattedNumber" type="number" groupingUsed="true" minFractionDigits="5" />
            <bc:pbglLineDataCell dataCellCssClass="datacell"
                accountingLine="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}]"
                cellProperty="budgetConstructionPosition.pendingBudgetConstructionAppointmentFunding[${status.index}].appointmentRequestedCsfFteQuantity"
                attributes="${bcafAttributes}"
                field="appointmentRequestedCsfFteQuantity"
                fieldAlign="right"
                readOnly="true"
                formattedNumberValue="${formattedNumber}"
                rowSpan="1" dataFieldCssClass="amount" />
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
        </tr>
        <tr>
            <td colspan="3">&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
                <td>&nbsp;</td>
        </tr>

        </c:forEach>
    </table>
</div>
</kul:tabTop>