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
package org.kuali.kra.external.Cfda.service;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import org.kuali.kfs.integration.cg.dto.HashMapElement;
import org.kuali.kfs.module.external.kc.KcConstants;
import org.kuali.kfs.module.external.kc.businessobject.CfdaDTO;

/**
 * This class was generated by Apache CXF 2.2.10
 * Wed Mar 02 08:02:23 HST 2011
 * Generated source version: 2.2.10
 *
 */

@WebService(targetNamespace = KcConstants.KC_NAMESPACE_URI, name = KcConstants.Cfda.SOAP_SERVICE_NAME)
public interface CfdaNumberService {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getCfdaNumber", targetNamespace = KcConstants.KC_NAMESPACE_URI, className = "kc.GetCfdaNumber")
    @WebMethod
    @ResponseWrapper(localName = "getCfdaNumberResponse", targetNamespace = KcConstants.KC_NAMESPACE_URI, className = "kc.GetCfdaNumberResponse")
    public java.util.List<CfdaDTO> getCfdaNumber(
        @WebParam(name = "financialAccountNumber", targetNamespace = "")
        java.lang.String financialAccountNumber,
        @WebParam(name = "chartOfAccounts", targetNamespace = "")
        java.lang.String chartOfAccounts
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "lookupCfda", targetNamespace = KcConstants.KC_NAMESPACE_URI, className = "kc.LookupCfda")
    @WebMethod
    @ResponseWrapper(localName = "lookupCfdaResponse", targetNamespace = KcConstants.KC_NAMESPACE_URI, className = "kc.LookupCfdaResponse")
    public java.util.List<CfdaDTO> lookupCfda(
        @WebParam(name = "searchCriteria", targetNamespace = "")
        java.util.List<HashMapElement> searchCriteria
    );
}
