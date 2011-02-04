<%--
 Copyright 2006-2008 The Kuali Foundation
 
 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl2.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>

<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>
	
	<c:set var="kemidAttributes" value="${DataDictionary.KEMID.attributes}" />
	<c:set var="kemidBenOrgAttributes" value="${DataDictionary.KemidBenefittingOrganization.attributes}" />
	<c:set var="typeCodeAttributes" value="${DataDictionary.TypeCode.attributes}" />
	<c:set var="purposeCodeAttributes" value="${DataDictionary.PurposeCode.attributes}" />
	<c:set var="combineGroupCodeAttributes" value="${DataDictionary.CombineGroupCode.attributes}" />
	<c:set var="campusAttributes" value="${DataDictionary.CampusImpl.attributes}" />
		
<kul:page  showDocumentInfo="false"
	headerTitle="Trial Balance Report" docTitle="Trial Balance Report" renderMultipart="true"
	transactionalDocument="false" htmlFormAction="endowReportTrialBalance" errorKey="foo">

	 <table cellpadding="0" cellspacing="0" class="datatable-80" summary="Trial Balance">
			<tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${kemidAttributes.kemid}" readOnly="true" /></div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<%-- <kul:htmlControlAttribute attributeEntry="${kemidAttributes.kemid}" property="kemid" /> --%>
					<input type="text" name="kemid" size="100" maxlength="100"/>
                    <kul:lookup boClassName="org.kuali.kfs.module.endow.businessobject.KEMID"  fieldConversions="kemid:kemid" />
                </td>				                       
            </tr>
            <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right">Benefitting Organization Campus</div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<kul:htmlControlAttribute attributeEntry="${campusAttributes.campusCode}" property="benefittingOrganziationCampus" />	
                    <kul:lookup boClassName="org.kuali.rice.kns.bo.CampusImpl"  fieldConversions="campusCode:benefittingOrganziationCampus" />
                </td>				                      
            </tr>                          
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${kemidBenOrgAttributes.benefittingChartCode}" readOnly="true" /></div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<kul:htmlControlAttribute attributeEntry="${kemidBenOrgAttributes.benefittingChartCode}" property="benefittingOrganziationChart" />	
                    <kul:lookup boClassName="org.kuali.kfs.module.endow.businessobject.KemidBenefittingOrganization"  fieldConversions="benefittingChartCode:benefittingOrganziationChart" />
                </td>				                      
            </tr>          
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${kemidBenOrgAttributes.benefittingOrgCode}" readOnly="true" /></div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<kul:htmlControlAttribute attributeEntry="${kemidBenOrgAttributes.benefittingOrgCode}" property="benefittingOrganziation" />	
                    <kul:lookup boClassName="org.kuali.kfs.module.endow.businessobject.KemidBenefittingOrganization"  fieldConversions="benefittingOrgCode:benefittingOrganziation" />
                </td>				                      
            </tr>
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${typeCodeAttributes.code}" readOnly="true" /></div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<kul:htmlControlAttribute attributeEntry="${typeCodeAttributes.code}" property="typeCode"  />	
                    <kul:lookup boClassName="org.kuali.kfs.module.endow.businessobject.TypeCode"  fieldConversions="code:typeCode"  />
                </td>				                      
            </tr>
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${purposeCodeAttributes.code}" readOnly="true" /></div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<kul:htmlControlAttribute attributeEntry="${purposeCodeAttributes.code}" property="purposeCode"  />	
                    <kul:lookup boClassName="org.kuali.kfs.module.endow.businessobject.PurposeCode"  fieldConversions="code:purposeCode"  />
                </td>				                      
            </tr>
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right"><kul:htmlAttributeLabel attributeEntry="${combineGroupCodeAttributes.code}" readOnly="true" /></div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<kul:htmlControlAttribute attributeEntry="${combineGroupCodeAttributes.code}" property="combineGroupCode"  />	
                    <kul:lookup boClassName="org.kuali.kfs.module.endow.businessobject.CombineGroupCode"  fieldConversions="code:combineGroupCode"  />
                </td>				                      
            </tr>            
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right">As of Date:</div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					11/07/2010
                </td>				                      
            </tr> 
            <tr>		
                <th align=right valign=middle class="grid" style="width: 25%;">
                    <div align="right">Endowment Option:</div>
                </th>
                <td align=left valign=middle class="grid" style="width: 25%;">
					<input type="radio" name="endowmentOption" value="Y" />Endowment&nbsp;&nbsp;
					<input type="radio" name="endowmentOption" value="N" />Non-Endowed&nbsp;&nbsp;
					<input type="radio" name="endowmentOption" value="B" checked />Both<br/>									
                </td>				                      
            </tr>             
        </table>
    
     <c:set var="extraButtons" value="${KualiForm.extraButtons}"/>  	
  	
	
     <div id="globalbuttons" class="globalbuttons">
	        	
	        	<c:if test="${!empty extraButtons}">
		        	<c:forEach items="${extraButtons}" var="extraButton">
		        		<html:image src="${extraButton.extraButtonSource}" styleClass="globalbuttons" property="${extraButton.extraButtonProperty}" title="${extraButton.extraButtonAltText}" alt="${extraButton.extraButtonAltText}"/>
		        	</c:forEach>
	        	</c:if>
	</div>
	
	<div>
	  <c:if test="${!empty KualiForm.message }">
            	 ${KualiForm.message }
            </c:if>
   </div>
	
</kul:page>
