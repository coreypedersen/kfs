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

<%@ tag description="display all detail lines of current document in a table" %>

<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<%@ attribute name="detailLines" required="true" type="java.util.List"
    description="the detail lines being displayed" %>             
<%@ attribute name="attributes" required="true" type="java.util.Map"
	description="The DataDictionary entry containing attributes for the line fields."%>   
<%@ attribute name="detailFieldNamesWithHiddenFormWhenReadonly" required="false"
    description="The names of the fields that will have hidden forms when the fields are readonly. The attribute can hold multiple filed names, which are separated by commas."%>	             

<%@ attribute name="hiddenFieldNames" required="false"
    description="The names of the fields that can be rendered as hidden form field" %> 
<%@ attribute name="detailFieldNames" required="true"
    description="The names of the fields that will be displayed" %>
<%@ attribute name="editableFieldNames" required="false"
    description="The names of the fields that can be editable" %>  
    
<%@ attribute name="inquirableUrl" required="false" type="java.util.List"
    description="The list of URLs for the inquirable fields" %>  
<%@ attribute name="fieldInfo" required="false" type="java.util.List"
    description="The descriptive information of the fields in the detail lines" %>
<%@ attribute name="relationshipMetadata" required="false" type="java.util.Map"
	description="This is a Map that holds a property name list of the primary key of the referenced class for each eligible field. The value of the attribute is used to build quick finder for the eligible fields."%>				                 
              
<%@ attribute name="onchangeForEditableFieldNames" required="false"
    description="The funation names that retrives the information of the given editable fields" %> 
<%@ attribute name="onchangeableInfoFieldNames" required="false"
    description="The names of the fields that hold the descriptive information of editable fields" %> 
              
<%@ attribute name="hasActions" required="false"
    description="Determine if a user can take any action on the given lines" %>
<%@ attribute name="actions" required="false"
	description="If a user can take an action on the detail lines, the  given actions can be rendered with the detail lines."%>
<%@ attribute name="actionImageFileNames" required="false"
	description="The graphic representation of the actions"%>	              

<table width="100%" border="0" cellpadding="0" cellspacing="0" class="datatable">
	<tr>
		<ec:detailLineHeader attributes="${attributes}"
			detailFieldNames="${detailFieldNames}"
			hasActions="${hasActions}"/>
	</tr> 

	<!-- populate the table with the given deatil lines -->
	<c:forEach var="detailLine" items="${detailLines}" varStatus="status">
		<tr>
			<kul:htmlAttributeHeaderCell literalLabel="${status.index + 1}"/>
			
			<ec:detailLine detailLine="${detailLine}" 
				detailLineFormName="document.effortCertificationDetailLines[${status.index}]"
				attributes="${attributes}"
				detailFieldNames="${detailFieldNames}"
				detailFieldNamesWithHiddenFormWhenReadonly="${detailFieldNamesWithHiddenFormWhenReadonly}"
				editableFieldNames="${editableFieldNames}"
				hiddenFieldNames="${hiddenFieldNames}"
				onchangeForEditableFieldNames="${onchangeForEditableFieldNames}"
				onchangeableInfoFieldNames="${onchangeableInfoFieldNames}"
				inquirableUrl="${inquirableUrl[status.index]}"
				fieldInfo="${fieldInfo[status.index]}"
				relationshipMetadata ="${relationshipMetadata}" 
				index="${status.index}" 
				hasActions="${hasActions}" 
				actions="${actions}" 
				actionImageFileNames="${actionImageFileNames}"/>			
		</tr>		
	</c:forEach>
	
	<tr>
		<jsp:doBody/>
	</tr>	
</table>       
