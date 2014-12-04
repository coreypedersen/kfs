########################################
# The Kuali Financial System, a comprehensive financial management system for higher education.
# 
# Copyright 2005-2014 The Kuali Foundation
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as
# published by the Free Software Foundation, either version 3 of the
# License, or (at your option) any later version.
# 
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
# 
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
########################################
# DO NOT add comments before the blank line below, or they will disappear.

groovy CountryCodeUpdateGenerator.groovy CG_SUBCNR_T CGSUBCNR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_AP_CMP_PARM_T PUR_DEPT_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_BILL_ADDR_T BILL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_BLK_RCVNG_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_BLK_RCVNG_T DLVY_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FP_DV_NONEM_TRVL_T DV_TRVL_FRM_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FP_DV_NONEM_TRVL_T DV_TRVL_TO_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FP_DV_NRA_TAX_T POSTAL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FP_DV_PAYEE_DTL_T DV_PAYEE_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FP_DV_PAYEE_DTL_T DV_RMT_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FP_DV_WIRE_TRNFR_T DV_BNK_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CA_ORG_T ORG_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CA_PRIOR_YR_ORG_T ORG_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FS_TAX_COUNTY_T POSTAL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FS_TAX_POSTAL_CD_T POSTAL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy FS_TAX_STATE_T POSTAL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_PO_CPTL_AST_LOC_T CPTL_AST_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_PO_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_PO_T DLVY_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PDP_CUST_PRFL_T CUST_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AR_ORG_OPTION_T ORG_POSTAL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_VNDR_ADDR_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_VNDR_CNTCT_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy SH_BUILDING_T BLDG_ADDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CM_CPTLAST_DTL_T AST_OFFCMP_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CM_EQPLNRTRN_DOC_T AST_BORWR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_CRDT_MEMO_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_ELCTRNC_INV_RJT_DOC_T INV_SHP_TO_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_ELCTRNC_INV_RJT_DOC_T INV_BILL_TO_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_ELCTRNC_INV_RJT_DOC_T INV_RMT_TO_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_ELCTRNC_INV_RJT_ITM_T INV_REF_ITM_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_PMT_RQST_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AP_PMT_RQST_T TAX_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_REQS_CPTL_AST_LOC_T CPTL_AST_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_REQS_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_REQS_T DLVY_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_REQS_T BILL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_REQS_T PUR_RCVNG_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AR_CUST_ADDR_T CUST_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AR_INV_DOC_T BILL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy AR_INV_DOC_T SHIP_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CM_AST_LOC_T AST_LOC_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CM_AST_RETIRE_DOC_T AST_RETIR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy CM_AST_TRNFR_DOC_T AST_OFFCMP_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_PO_T BILL_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_PO_T PUR_RCVNG_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_PO_VNDR_QT_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_RCVNG_ADDR_T PUR_RCVNG_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_RCVNG_LN_T VNDR_CNTRY_CD country_code_updates
groovy CountryCodeUpdateGenerator.groovy PUR_RCVNG_LN_T DLVY_CNTRY_CD country_code_updates

cat country_code_updates/convert*.sql > country_code_updates/convertAll.sql
cat country_code_updates/validate*.sql > country_code_updates/validateAll.sql
