<%--
   - The Kuali Financial System, a comprehensive financial management system for higher education.
   - 
   - Copyright 2005-2014 The Kuali Foundation
   - 
   - This program is free software: you can redistribute it and/or modify
   - it under the terms of the GNU Affero General Public License as
   - published by the Free Software Foundation, either version 3 of the
   - License, or (at your option) any later version.
   - 
   - This program is distributed in the hope that it will be useful,
   - but WITHOUT ANY WARRANTY; without even the implied warranty of
   - MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   - GNU Affero General Public License for more details.
   - 
   - You should have received a copy of the GNU Affero General Public License
   - along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="checkDetailMode" required="true" %>
<%@ attribute name="editingMode" required="true" type="java.util.Map" %>
<%@ attribute name="totalAmount" required="false" %>
<%@ attribute name="totalConfirmedAmount" required="false" %>
<%@ attribute name="displayHidden" required="true" %>
<%@ attribute name="confirmMode" required="false" %>

<c:set var="checkBaseAttributes" value="${DataDictionary.CheckBase.attributes}" />

<c:set var="readOnly" value="${!KualiForm.documentActions[Constants.KUALI_ACTION_CAN_EDIT]}" />
<c:set var="columnCount" value="5" />
<c:if test="${!readOnly}">
    <c:set var="columnCount" value="${columnCount + 1}" />
</c:if>

<c:if test="${checkDetailMode}">
    <kul:tab tabTitle="Check Detail" defaultOpen="true" tabErrorKey="${KFSConstants.EDIT_CASH_RECEIPT_CHECK_DETAIL_ERRORS}" >
        <div class="tab-container" align=center>
        
			<h3>Check Detail</h3>
            <table cellpadding=0 class="datatable" summary="check detail information">
                
                <tr>
                    <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.checkNumber}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.checkDate}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.description}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.amount}" />
                                
                    <c:if test="${!readOnly}">
                        <kul:htmlAttributeHeaderCell literalLabel="Action" />
                    </c:if>
                </tr>
    			<c:if test="${!readOnly}">
                <fp:checkLine readOnly="${readOnly}" rowHeading="add" propertyName="newCheck" actionImage="${ConfigProperties.kr.externalizable.images.url}tinybutton-add1.gif" actionAlt="add" actionMethod="addCheck" cssClass="infoline" displayHidden="${displayHidden}" />
                </c:if> 
                <logic:iterate id="check" name="KualiForm" property="document.checks" indexId="ctr">
                    <fp:checkLine readOnly="${readOnly}" rowHeading="${ctr + 1}" propertyName="document.check[${ctr}]" baselinePropertyName="baselineCheck[${ctr}]" actionImage="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif" actionAlt="delete" actionMethod="deleteCheck.line${ctr}" cssClass="datacell" displayHidden="${displayHidden}" />
                </logic:iterate>
    
                <c:if test="${!empty totalAmount}">
                    <tr>
                        <td class="total-line" colspan="4">&nbsp;</td>
                        <td class="total-line"><strong>Total: <c:out value="${totalAmount}" /></strong></td>
                        <c:if test="${!readOnly}">
                            <td class="total-line">&nbsp;</td>
                        </c:if>
                    </tr>
                </c:if>
            </table>
        </div>	
    
    <c:if test="${confirmMode}">
        <div class="tab-container" align="center">
    	    <html:image align="left" property="methodToCall.copyAllChecks" src="${ConfigProperties.externalizable.images.url}tinybutton-copyall.gif" title="Copy all original checks to confirmed checks" alt="Copy all checks" styleClass="tinybutton"/>
    	</div>
    	<div class="tab-container" align=center>
    		<h3>Cash Manager Confirmed Check Detail</h3>
    		<table cellpadding=0 class="datatable" summary="confirmed check detail information">
    			<tr>
                    <kul:htmlAttributeHeaderCell literalLabel="&nbsp;" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.checkNumber}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.checkDate}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.description}" />
                    <kul:htmlAttributeHeaderCell attributeEntry="${checkBaseAttributes.amount}" />
                    <kul:htmlAttributeHeaderCell literalLabel="Action" />
                </tr>
                <fp:checkLine readOnly="false" rowHeading="add" propertyName="newConfirmedCheck" actionImage="${ConfigProperties.kr.externalizable.images.url}tinybutton-add1.gif" actionAlt="add" actionMethod="addConfirmedCheck" cssClass="infoline" displayHidden="${displayHidden}" />
                <logic:iterate id="confirmedCheck" name="KualiForm" property="document.confirmedChecks" indexId="ct">
                    <fp:checkLine readOnly="false" rowHeading="${ct + 1}" propertyName="document.confirmedCheck[${ct}]" actionImage="${ConfigProperties.kr.externalizable.images.url}tinybutton-delete1.gif" actionAlt="delete" actionMethod="deleteConfirmedCheck.line${ct}" cssClass="datacell" displayHidden="${displayHidden}" />
                </logic:iterate>	
                	
                <c:if test="${!empty totalConfirmedAmount}">
                    <tr>
                        <td class="total-line" colspan="4">&nbsp;</td>
                        <td class="total-line"><strong>Total: <c:out value="${totalConfirmedAmount}" /></strong></td>
                        <td class="total-line">&nbsp;</td>
                    </tr>
                </c:if>
    		</table>
    	</div>
    </c:if>
    </kul:tab>
</c:if>

<c:if test="${!checkDetailMode}">
    <kul:hiddenTab forceOpen="true">
        <%-- maintain state of hidden checkLines --%>
        <logic:iterate id="check" name="KualiForm" property="document.checks" indexId="ctr">
            <fp:hiddenCheckLine propertyName="document.check[${ctr}]" baselinePropertyName="baselineCheck[${ctr}]"  displayHidden="${displayHidden}" />
        </logic:iterate>
    </kul:hiddenTab>
</c:if>
