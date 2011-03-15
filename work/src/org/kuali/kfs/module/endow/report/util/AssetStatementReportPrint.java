/*
 * Copyright 2011 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.endow.report.util;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import org.kuali.kfs.module.endow.EndowConstants;
import org.kuali.kfs.module.endow.EndowConstants.IncomePrincipalIndicator;
import org.kuali.kfs.module.endow.report.util.AssetStatementReportDataHolder.ReportGroupData;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

public class AssetStatementReportPrint extends EndowmentReportPrintBase {
    
    final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(AssetStatementReportPrint.class);
    
    /**
     * Generates all reports into a PDF file
     * 
     * @param reportHeaderDataHolderForEndowment
     * @param reportHeaderDataHolderForNonEndowed
     * @param endowmentAssetStatementReportDataHolders
     * @param nonEndowedAssetStatementReportDataHolders
     * @param endowmentOption
     * @param reportOption
     * @param listKemidsOnHeader
     * @return
     */
    public ByteArrayOutputStream printAssetStatementReport(
            EndowmentReportHeaderDataHolder reportHeaderDataHolderForEndowment, 
            EndowmentReportHeaderDataHolder reportHeaderDataHolderForNonEndowed,
            List<AssetStatementReportDataHolder> endowmentAssetStatementReportDataHolders, 
            List<AssetStatementReportDataHolder> nonEndowedAssetStatementReportDataHolders, 
            String endowmentOption,
            String reportOption, 
            String listKemidsOnHeader) {
        
        // prepare iText document
        Document document = new Document();
        document.addTitle("Endowment Asset Statement");
        
        ByteArrayOutputStream pdfStream = new ByteArrayOutputStream();

        try {            
            PdfWriter.getInstance(document, pdfStream);            
            document.open();

            // generates a PDF based on the options and data
            if ("Y".equalsIgnoreCase(endowmentOption) && "D".equalsIgnoreCase(reportOption)) {
                if (endowmentAssetStatementReportDataHolders != null && endowmentAssetStatementReportDataHolders.size() > 0) {
                    // endowment detail
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);                    
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentDetail(endowmentAssetStatementReportDataHolders, document);
                }
            }
            else if ("Y".equalsIgnoreCase(endowmentOption) && "T".equalsIgnoreCase(reportOption)) {
                if (endowmentAssetStatementReportDataHolders != null && endowmentAssetStatementReportDataHolders.size() > 0) {
                    // endowment total
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);   
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentTotal(endowmentAssetStatementReportDataHolders, document);
                }
            }
            else if ("Y".equalsIgnoreCase(endowmentOption) && "B".equalsIgnoreCase(reportOption)) {
                if (endowmentAssetStatementReportDataHolders != null && endowmentAssetStatementReportDataHolders.size() > 0) {
                    // endowment detail
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);       
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    document.newPage();
                    printAssetStatementReportBodyForEndowmentDetail(endowmentAssetStatementReportDataHolders, document);
                    
                    // endowment total
                    document.setPageSize(LETTER_PORTRAIT);
                    document.newPage();
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);                
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentTotal(endowmentAssetStatementReportDataHolders, document);
                }
            }
            else if ("N".equalsIgnoreCase(endowmentOption) && "D".equalsIgnoreCase(reportOption)) {
                if (nonEndowedAssetStatementReportDataHolders != null && nonEndowedAssetStatementReportDataHolders.size() > 0) {
                    // non-endowed detail
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader); 
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedDetail(nonEndowedAssetStatementReportDataHolders, document);
                }
            }
            else if ("N".equalsIgnoreCase(endowmentOption) && "T".equalsIgnoreCase(reportOption)) {
                if (nonEndowedAssetStatementReportDataHolders != null && nonEndowedAssetStatementReportDataHolders.size() > 0) {
                    // non-endowed total
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedTotal(nonEndowedAssetStatementReportDataHolders, document);
                }
            }
            else if ("N".equalsIgnoreCase(endowmentOption) && "B".equalsIgnoreCase(reportOption)) {
                if (nonEndowedAssetStatementReportDataHolders != null && nonEndowedAssetStatementReportDataHolders.size() > 0) {
                    // non-endowed detail
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    document.newPage();
                    printAssetStatementReportBodyForNonEndowedDetail(nonEndowedAssetStatementReportDataHolders, document);
                    
                    // non-endowed total
                    document.setPageSize(LETTER_PORTRAIT);
                    document.newPage();
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader);                
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedTotal(nonEndowedAssetStatementReportDataHolders, document);
                }
            }
            else if ("B".equalsIgnoreCase(endowmentOption) && "D".equalsIgnoreCase(reportOption)) {
                if (endowmentAssetStatementReportDataHolders != null && endowmentAssetStatementReportDataHolders.size() > 0) {
                    // endowment detail
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);  
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentDetail(endowmentAssetStatementReportDataHolders, document);
                }
                if (nonEndowedAssetStatementReportDataHolders != null && nonEndowedAssetStatementReportDataHolders.size() > 0) {
                    // non-endowed detail
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader); 
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedDetail(nonEndowedAssetStatementReportDataHolders, document);
                }
            }
            else if ("B".equalsIgnoreCase(endowmentOption) && "T".equalsIgnoreCase(reportOption)) {
                if (endowmentAssetStatementReportDataHolders != null && endowmentAssetStatementReportDataHolders.size() > 0) {
                    // endowment total
                    document.setPageSize(LETTER_PORTRAIT);
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);  
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentTotal(endowmentAssetStatementReportDataHolders, document);
                }
                if (nonEndowedAssetStatementReportDataHolders != null && nonEndowedAssetStatementReportDataHolders.size() > 0) {
                    // non-endowed total
                    document.setPageSize(LETTER_PORTRAIT);
                    document.newPage();
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedTotal(nonEndowedAssetStatementReportDataHolders, document);
                }
            }
            else if ("B".equalsIgnoreCase(endowmentOption) && "B".equalsIgnoreCase(reportOption)) {
                if (endowmentAssetStatementReportDataHolders != null && endowmentAssetStatementReportDataHolders.size() > 0) {
                    // endowment detail
                    document.setPageSize(LETTER_PORTRAIT);                    
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentDetail(endowmentAssetStatementReportDataHolders, document);
                                       
                    document.setPageSize(LETTER_PORTRAIT);
                    document.newPage();
                    reportHeaderDataHolderForEndowment.setEndowmentOption(EndowConstants.EndowmentReport.ENDOWMENT);
                    reportHeaderDataHolderForEndowment.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);                    
                    printReportHeaderPage(reportHeaderDataHolderForEndowment, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForEndowmentTotal(endowmentAssetStatementReportDataHolders, document);
                }
                if (nonEndowedAssetStatementReportDataHolders != null && nonEndowedAssetStatementReportDataHolders.size() > 0) {
                    // non-endowed detail
                    document.setPageSize(LETTER_PORTRAIT);
                    document.newPage();
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.DETAIL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader);
                    
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedDetail(nonEndowedAssetStatementReportDataHolders, document);
                    
                    // non-endowed total
                    document.setPageSize(LETTER_PORTRAIT);
                    document.newPage();
                    reportHeaderDataHolderForNonEndowed.setEndowmentOption(EndowConstants.EndowmentReport.NON_ENDOWED);
                    reportHeaderDataHolderForNonEndowed.setReportOption(EndowConstants.EndowmentReport.TOTAL_REPORT);
                    printReportHeaderPage(reportHeaderDataHolderForNonEndowed, document, listKemidsOnHeader);
                                        
                    //document.resetPageCount();
                    document.setPageSize(LETTER_LANDSCAPE);
                    printAssetStatementReportBodyForNonEndowedTotal(nonEndowedAssetStatementReportDataHolders, document);
                }

            } else {
                LOG.error("Asset Statement Report Header Error");
            }

            document.close();

        } catch (Exception e) {
            LOG.error("PDF Error: " + e.getMessage());
            return null;
        }
        
        return pdfStream;
    }

    /**
     * Prints report body for endowment detail
     * 
     * @param endowmentAssetStatementReportDataHolders
     * @param document
     * @return
     */
    public boolean printAssetStatementReportBodyForEndowmentTotal(List<AssetStatementReportDataHolder> endowmentAssetStatementReportDataHolders, Document document) {
        
        //document.setPageCount(0);
        //document.setPageSize(LETTER_LANDSCAPE);
        
        BigDecimal totalHistoryIncomeCash = BigDecimal.ZERO;
        BigDecimal totalHistoryPrincipalCash = BigDecimal.ZERO;
        TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForIncomeTotal = null;
        TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForPrincipalTotal = null;

        // get the cash totals
        for (AssetStatementReportDataHolder data : endowmentAssetStatementReportDataHolders) {
            totalHistoryIncomeCash = totalHistoryIncomeCash.add(data.getHistoryIncomeCash());
            totalHistoryPrincipalCash = totalHistoryPrincipalCash.add(data.getHistoryPrincipalCash());
        }
        
        // for income
        reportGroupsForIncomeTotal = createReportGroupsForTotal(endowmentAssetStatementReportDataHolders, IncomePrincipalIndicator.INCOME);
        
        // for principal
        reportGroupsForPrincipalTotal = createReportGroupsForTotal(endowmentAssetStatementReportDataHolders, IncomePrincipalIndicator.PRINCIPAL);
                
        // for each kemid
        try {                               
            Font cellFont = regularFont;
        
            // for the common info
            AssetStatementReportDataHolder reportData = endowmentAssetStatementReportDataHolders.get(0);
            
            document.newPage();
            
            // header
            StringBuffer title = new StringBuffer();
            title.append(reportData.getInstitution()).append("\n");
            title.append("STATEMENT OF ASSETS FOR PERIOD ENDING").append("\n");
            title.append(reportData.getMonthEndDate()).append("\n\n");
            Paragraph header = new Paragraph(title.toString());
            header.setAlignment(Element.ALIGN_CENTER);                
            document.add(header);

            // report table
            float[] colsWidth = {15f, 17f, 17f, 17f, 17f, 17f};
            PdfPTable table = new PdfPTable(colsWidth);
            table.setWidthPercentage(FULL_TABLE_WIDTH);                
            table.getDefaultCell().setPadding(5);
            
            // column titles
            table.addCell("");
            table.addCell(createCell("UNITS HELD", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("MARKET VALUE", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("FY REMAINDER ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("NEXT FY ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                          
            // 1. Expendable funds 
            
            PdfPCell cellExpendableFunds = new PdfPCell(new Paragraph("EXPENDABLE FUNDS", titleFont));
            cellExpendableFunds.setColspan(6);
            table.addCell(cellExpendableFunds);
            
            PdfPCell cellCashEquivalnets = new PdfPCell(new Paragraph("CASH AND EQUIVALENTS", cellFont));
            cellCashEquivalnets.setColspan(6);
            table.addCell(cellCashEquivalnets);

            // report groups for income
            printReportGroupForIncomeEndowmentTotal(reportGroupsForIncomeTotal, totalHistoryIncomeCash, document, table, cellFont);
            
            // 2. Endowed funds 
            
            PdfPCell cellEndowedFunds = new PdfPCell(new Paragraph("ENDOWMED FUNDS", titleFont));
            cellEndowedFunds.setColspan(6);
            table.addCell(cellEndowedFunds);
 
            table.addCell(cellCashEquivalnets);
                
            // report groups for principal
            printReportGroupForPrincipalEndowmentTotal(reportGroupsForPrincipalTotal, totalHistoryPrincipalCash, document, table, cellFont);
            
            // 3. total (endowment + non-endowed)
            PdfPCell blank = new PdfPCell(new Paragraph("", cellFont));
            blank.setColspan(6);
            blank.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(blank);
            
            BigDecimal totalKemidMarketValue = BigDecimal.ZERO;
            BigDecimal totalKemidEstimatedAnnualIncome = BigDecimal.ZERO;
            BigDecimal totalKemidFYRemainderEstimatedAnnualIncome = BigDecimal.ZERO;
            BigDecimal totalKemidNextFYEstimayedAnnualIncome = BigDecimal.ZERO;
            for (AssetStatementReportDataHolder data : endowmentAssetStatementReportDataHolders) {
                totalKemidMarketValue = totalKemidMarketValue.add(data.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME).add(data.getTotalSumOfMarketValue(IncomePrincipalIndicator.PRINCIPAL)));
                totalKemidEstimatedAnnualIncome = totalKemidEstimatedAnnualIncome.add(data.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.INCOME).add(data.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL)));
                totalKemidFYRemainderEstimatedAnnualIncome = totalKemidFYRemainderEstimatedAnnualIncome.add(data.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.INCOME).add(data.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.PRINCIPAL)));
                totalKemidNextFYEstimayedAnnualIncome = totalKemidNextFYEstimayedAnnualIncome.add(data.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.INCOME).add(data.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL)));
            }
            
            table.addCell(new Paragraph("TOTAL KEMID VALUE", titleFont));
            table.addCell("");
            table.addCell(getAmountCell(totalKemidMarketValue.add(totalHistoryIncomeCash).add(totalHistoryPrincipalCash), titleFont));
            table.addCell(getAmountCell(totalKemidEstimatedAnnualIncome, titleFont));
            table.addCell(getAmountCell(totalKemidFYRemainderEstimatedAnnualIncome, titleFont));
            table.addCell(getAmountCell(totalKemidNextFYEstimayedAnnualIncome, titleFont));
            
            document.add(table);
            
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
        
        return true;

    }
 
    /**
     * Generates the Asset Statement report for Non-Endowed total 
     * 
     * @param transactionStatementReports
     * @param document
     * @return
     */
    public boolean printAssetStatementReportBodyForNonEndowedTotal(List<AssetStatementReportDataHolder> nonEndowedAssetStatementReportDataHolders, Document document) {
        
        //document.setPageCount(0);
        //document.setPageSize(LETTER_LANDSCAPE);
        
        BigDecimal totalHistoryIncomeCash = BigDecimal.ZERO;
        BigDecimal totalHistoryPrincipalCash = BigDecimal.ZERO;
        TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForTotal = null;

        // get the cash totals
        for (AssetStatementReportDataHolder data : nonEndowedAssetStatementReportDataHolders) {
            totalHistoryIncomeCash = totalHistoryIncomeCash.add(data.getHistoryIncomeCash());
            totalHistoryPrincipalCash = totalHistoryPrincipalCash.add(data.getHistoryPrincipalCash());
        }
        
        reportGroupsForTotal = createReportGroupsForTotal(nonEndowedAssetStatementReportDataHolders, IncomePrincipalIndicator.INCOME);

        // for each kemid
        try {                               
            Font cellFont = regularFont;
        
            // for the common info
            AssetStatementReportDataHolder reportData = nonEndowedAssetStatementReportDataHolders.get(0);
            
            document.newPage();
            
            // header
            StringBuffer title = new StringBuffer();
            title.append(reportData.getInstitution()).append("\n");
            title.append("STATEMENT OF ASSETS FOR PERIOD ENDING").append("\n");
            title.append(reportData.getMonthEndDate()).append("\n\n");
            Paragraph header = new Paragraph(title.toString());
            header.setAlignment(Element.ALIGN_CENTER);                
            document.add(header);

            // report table
            float[] colsWidth = {15f, 17f, 17f, 17f, 17f, 17f};
            PdfPTable table = new PdfPTable(colsWidth);
            table.setWidthPercentage(FULL_TABLE_WIDTH);                
            table.getDefaultCell().setPadding(5);
            
            // column titles
            table.addCell("");
            table.addCell(createCell("UNITS HELD", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("MARKET VALUE", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("FY REMAINDER ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
            table.addCell(createCell("NEXT FY ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
            
            PdfPCell cellCashEquivalnets = new PdfPCell(new Paragraph("CASH AND EQUIVALENTS", cellFont));
            cellCashEquivalnets.setColspan(6);
            table.addCell(cellCashEquivalnets);

            // report groups
            printReportGroupForNonEndowedTotal(reportGroupsForTotal, totalHistoryIncomeCash, totalHistoryPrincipalCash, document, table, cellFont);
                                 
            // total
            PdfPCell blank = new PdfPCell(new Paragraph("", cellFont));
            blank.setColspan(6);
            blank.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(blank);
            
            BigDecimal totalKemidMarketValue = BigDecimal.ZERO;
            BigDecimal totalKemidEstimatedAnnualIncome = BigDecimal.ZERO;
            BigDecimal totalKemidFYRemainderEstimatedAnnualIncome = BigDecimal.ZERO;
            BigDecimal totalKemidNextFYEstimayedAnnualIncome = BigDecimal.ZERO;
            for (AssetStatementReportDataHolder data : nonEndowedAssetStatementReportDataHolders) {
                totalKemidMarketValue = totalKemidMarketValue.add(data.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME));
                totalKemidEstimatedAnnualIncome = totalKemidEstimatedAnnualIncome.add(data.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.INCOME));
                totalKemidFYRemainderEstimatedAnnualIncome = totalKemidFYRemainderEstimatedAnnualIncome.add(totalKemidFYRemainderEstimatedAnnualIncome.add(data.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.INCOME)));
                totalKemidNextFYEstimayedAnnualIncome = totalKemidNextFYEstimayedAnnualIncome.add(data.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.INCOME));
            }
            
            table.addCell(new Paragraph("TOTAL KEMID VALUE", titleFont));
            table.addCell("");
            table.addCell(getAmountCell(totalKemidMarketValue.add(totalHistoryIncomeCash).add(totalHistoryPrincipalCash), titleFont));
            table.addCell(getAmountCell(totalKemidEstimatedAnnualIncome, titleFont));
            table.addCell(getAmountCell(totalKemidFYRemainderEstimatedAnnualIncome, titleFont));
            table.addCell(getAmountCell(totalKemidNextFYEstimayedAnnualIncome, titleFont));
            
            document.add(table);
            
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Generates the Asset Statement report for Endowment detail  
     * 
     * @param transactionStatementReports
     * @param document
     * @return
     */
    public boolean printAssetStatementReportBodyForEndowmentDetail(List<AssetStatementReportDataHolder> endowmentAssetStatementReportDataHolders, Document document) {

        // for each kemid
        try {                               
            Font cellFont = regularFont;
            for (AssetStatementReportDataHolder reportData : endowmentAssetStatementReportDataHolders) {
                                
                document.newPage();
                
                // header
                StringBuffer title = new StringBuffer();
                title.append(reportData.getInstitution()).append("\n");
                title.append("STATEMENT OF ASSETS FOR PERIOD ENDING").append("\n");
                title.append(reportData.getMonthEndDate()).append("\n");
                title.append(reportData.getKemid()).append("     ").append(reportData.getKemidLongTitle()).append("\n\n");
                Paragraph header = new Paragraph(title.toString());
                header.setAlignment(Element.ALIGN_CENTER);                
                document.add(header);

                // report table
                float[] colsWidth = {15f, 17f, 17f, 17f, 17f, 17f};
                PdfPTable table = new PdfPTable(colsWidth);
                table.setWidthPercentage(FULL_TABLE_WIDTH);                
                table.getDefaultCell().setPadding(5);
                
                // column titles
                table.addCell("");
                table.addCell(createCell("UNITS HELD", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("MARKET VALUE", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("FY REMAINDER ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("NEXT FY ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                              
                // 1. Expendable funds 
                
                PdfPCell cellExpendableFunds = new PdfPCell(new Paragraph("EXPENDABLE FUNDS", titleFont));
                cellExpendableFunds.setColspan(6);
                //cellExpendableFunds.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cellExpendableFunds);
                
                PdfPCell cellCashEquivalnets = new PdfPCell(new Paragraph("CASH AND EQUIVALENTS", cellFont));
                cellCashEquivalnets.setColspan(6);
                table.addCell(cellCashEquivalnets);

                // report groups 
                printReportGroupForIncomeEndowmentDetail(reportData, document, table, cellFont);
                
                // 2. Endowed funds 
                
                PdfPCell cellEndowedFunds = new PdfPCell(new Paragraph("ENDOWMED FUNDS", titleFont));
                cellEndowedFunds.setColspan(6);
                //cellEndowedFunds.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(cellEndowedFunds);
 
                table.addCell(cellCashEquivalnets);
                
                printReportGroupForPrincipalEndowmentDetail(reportData, document, table, cellFont);
                
                // 3. total (endowment + non-endowed)
                PdfPCell blank = new PdfPCell(new Paragraph("", cellFont));
                blank.setColspan(6);
                blank.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(blank);
                
                BigDecimal totalKemidMarketValue = reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME).add(reportData.getHistoryIncomeCash())
                        .add(reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.PRINCIPAL).add(reportData.getHistoryPrincipalCash()));
                BigDecimal totalKemidEstimatedAnnualIncome = reportData.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.INCOME).add(reportData.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL));
                BigDecimal totalKemidFYRemainderEstimatedAnnualIncome = reportData.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.INCOME).add(reportData.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.PRINCIPAL));
                BigDecimal totalKemidNextFYEstimayedAnnualIncome = reportData.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.INCOME).add(reportData.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL));
                
                table.addCell(new Paragraph("TOTAL KEMID VALUE", titleFont));
                table.addCell("");
                table.addCell(getAmountCell(totalKemidMarketValue, titleFont));
                table.addCell(getAmountCell(totalKemidEstimatedAnnualIncome, titleFont));
                table.addCell(getAmountCell(totalKemidFYRemainderEstimatedAnnualIncome, titleFont));
                table.addCell(getAmountCell(totalKemidNextFYEstimayedAnnualIncome, titleFont));
                
                document.add(table);
                
                // footer
                printFooter(reportData.getFooter(), document);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
        
        return true;
    }
 
    /**
     * Generates the Asset Statement report for Non-Endowed    
     * 
     * @param transactionStatementReports
     * @param document
     * @return
     */
    public boolean printAssetStatementReportBodyForNonEndowedDetail(List<AssetStatementReportDataHolder> nonEndowedAssetStatementReportDataHolders, Document document) {
        
        //document.setPageCount(0);
        //document.setPageSize(LETTER_LANDSCAPE);
                
        // for each kemid
        try {                               
            Font cellFont = regularFont;
            for (AssetStatementReportDataHolder reportData : nonEndowedAssetStatementReportDataHolders) {
                
                document.newPage();
                
                // header
                StringBuffer title = new StringBuffer();
                title.append(reportData.getInstitution()).append("\n");
                title.append("STATEMENT OF ASSETS FOR PERIOD ENDING").append("\n");
                title.append(reportData.getMonthEndDate()).append("\n");
                title.append(reportData.getKemid()).append("     ").append(reportData.getKemidLongTitle()).append("\n\n");
                Paragraph header = new Paragraph(title.toString());
                header.setAlignment(Element.ALIGN_CENTER);                
                document.add(header);

                // report table
                float[] colsWidth = {15f, 17f, 17f, 17f, 17f, 17f};
                PdfPTable table = new PdfPTable(colsWidth);
                table.setWidthPercentage(FULL_TABLE_WIDTH);                
                table.getDefaultCell().setPadding(5);
                
                // column titles
                table.addCell("");
                table.addCell(createCell("UNITS HELD", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("MARKET VALUE", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("FY REMAINDER ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                table.addCell(createCell("NEXT FY ESTIMATED\nANNUAL INCOME", titleFont, Element.ALIGN_RIGHT, true));
                              
                PdfPCell cellCashEquivalnets = new PdfPCell(new Paragraph("CASH AND EQUIVALENTS", cellFont));
                cellCashEquivalnets.setColspan(6);
                table.addCell(cellCashEquivalnets);

                // report groups 
                printReportGroupForNonEndowedDetail(reportData, document, table, cellFont);
                
                // total
                PdfPCell blank = new PdfPCell(new Paragraph("", cellFont));
                blank.setColspan(6);
                blank.setBackgroundColor(Color.LIGHT_GRAY);
                table.addCell(blank);
                
                BigDecimal totalKemidMarketValue = reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME).add(reportData.getHistoryIncomeCash()).add(reportData.getHistoryPrincipalCash());
                BigDecimal totalKemidEstimatedAnnualIncome = reportData.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.INCOME).add(reportData.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL));
                BigDecimal totalKemidFYRemainderEstimatedAnnualIncome = reportData.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.INCOME).add(reportData.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.PRINCIPAL));
                BigDecimal totalKemidNextFYEstimayedAnnualIncome = reportData.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.INCOME).add(reportData.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL));
                
                table.addCell(new Paragraph("TOTAL KEMID VALUE", titleFont));
                table.addCell("");
                table.addCell(getAmountCell(totalKemidMarketValue, titleFont));
                table.addCell(getAmountCell(totalKemidEstimatedAnnualIncome, titleFont));
                table.addCell(getAmountCell(totalKemidFYRemainderEstimatedAnnualIncome, titleFont));
                table.addCell(getAmountCell(totalKemidNextFYEstimayedAnnualIncome, titleFont));
                
                document.add(table);
                
                // footer
                printFooter(reportData.getFooter(), document);
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return false;
        }
        
        return true;
    }
    
    /**
     * Generates report group for income endowment detail
     * 
     * @param reportData
     * @param docuement
     * @param table
     * @param cellFont
     * @throws Exception
     */
    protected void printReportGroupForIncomeEndowmentDetail(AssetStatementReportDataHolder reportData, Document docuement, PdfPTable table, Font cellFont) throws Exception {
                
        table.addCell(new Paragraph("Income Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getHistoryIncomeCash(), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        TreeMap<Integer, TreeMap<String, ReportGroupData>> reportGroups = reportData.getReportGroupsForIncome();
        
        if (reportGroups == null || reportGroups.isEmpty()) {
            table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
            table.addCell("");
            table.addCell(getAmountCell(reportData.getHistoryIncomeCash(), cellFont));
            table.addCell("");
            table.addCell("");
            table.addCell("");
            return;
        }
        
        // Cash and equivalents first
        TreeMap<String, ReportGroupData> cashEquivalentsData = reportData.getReportGroupsForIncome().get(1);
        if (cashEquivalentsData != null && !cashEquivalentsData.isEmpty()) {
            Iterator<String> secirutyIdSet = cashEquivalentsData.keySet().iterator();        
            while (secirutyIdSet.hasNext()) {
                // get securityId
                String securityId = secirutyIdSet.next();
                ReportGroupData data = cashEquivalentsData.get(securityId);
                table.addCell(new Paragraph(data.getSecurityDesc(), cellFont));
                table.addCell(getAmountCell(data.getSumOfUnits(), cellFont, FORMAT164));
                table.addCell(getAmountCell(data.getSumOfMarketValue(), cellFont));
                table.addCell(getAmountCell(data.getSumOfEstimatedIncome(), cellFont));
                table.addCell(getAmountCell(data.getSumOfRemainderOfFYEstimated(), cellFont));
                table.addCell(getAmountCell(data.getSumOfNextFYEstimatedIncome(), cellFont));
            }
        }    
        table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getTotalMarketValueForCashEquivalents(IncomePrincipalIndicator.INCOME).add(reportData.getHistoryIncomeCash()), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");            
        
        // Other report group
        Iterator<Integer> reportGroupOrderSet = reportGroups.keySet().iterator();
        while (reportGroupOrderSet.hasNext()) {                    
            
            Integer reportGroupOrder = reportGroupOrderSet.next();
            if (reportGroupOrder.intValue() > 1) {                  
                TreeMap<String, ReportGroupData> reportGroupData = reportData.getReportGroupsForIncome().get(reportGroupOrder);
                if (reportGroupData == null || reportGroupData.isEmpty()) {
                    continue;
                }
                
                Iterator<String> secirutyIdSet = reportGroupData.keySet().iterator();
                while (secirutyIdSet.hasNext()) {
                    String securityId = secirutyIdSet.next();
                    ReportGroupData data = reportGroupData.get(securityId);
                    PdfPCell groupDescCell = new PdfPCell(new Paragraph(data.getReportGroupDesc() + " " + data.getReportGroupOrder(), cellFont));
                    groupDescCell.setColspan(6);
                    table.addCell(groupDescCell);  
                    table.addCell(new Paragraph(data.getSecurityDesc(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfUnits(), cellFont, FORMAT164));
                    table.addCell(getAmountCell(data.getSumOfMarketValue(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfEstimatedIncome(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfRemainderOfFYEstimated(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfNextFYEstimatedIncome(), cellFont));
                    
                    // totals
                    table.addCell(new Paragraph("TOTAL <" + data.getReportGroupDesc() + " " + data.getReportGroupOrder() + ">", cellFont));
                    table.addCell("");
                    table.addCell(getAmountCell(reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME, reportGroupOrder), cellFont));
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }

        // total expendable funds
        table.addCell(new Paragraph("TOTAL EXPENDABLE FUNDS", titleFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME).add(reportData.getHistoryIncomeCash()), cellFont));
        table.addCell(getAmountCell(reportData.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.INCOME), cellFont));
        table.addCell(getAmountCell(reportData.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.INCOME), cellFont));
        table.addCell(getAmountCell(reportData.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.INCOME), cellFont));                  
    }

    /**
     * Generates report group for principal endowment detail
     * 
     * @param reportData
     * @param docuement
     * @param table
     * @param cellFont
     */
    protected void printReportGroupForPrincipalEndowmentDetail(AssetStatementReportDataHolder reportData, Document docuement, PdfPTable table, Font cellFont) {
       
        table.addCell(new Paragraph("Principal Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getHistoryPrincipalCash(), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
                                                    
        TreeMap<Integer, TreeMap<String, ReportGroupData>> reportGroups = reportData.getReportGroupsForPrincipal();
        if (reportGroups == null || reportGroups.isEmpty()) {
            table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
            table.addCell("");
            table.addCell(getAmountCell(reportData.getHistoryPrincipalCash(), cellFont));
            table.addCell("");
            table.addCell("");
            table.addCell("");
            return;
        }
        
        // Cash and equivalents first
        TreeMap<String, ReportGroupData> cashEquivalentsData = reportData.getReportGroupsForPrincipal().get(1);
        if (cashEquivalentsData != null && !cashEquivalentsData.isEmpty()) {
            Iterator<String> secirutyIdSet = cashEquivalentsData.keySet().iterator();
            while (secirutyIdSet.hasNext()) {
                // get securityId
                String securityId = secirutyIdSet.next();
                ReportGroupData data = cashEquivalentsData.get(securityId);
                table.addCell(new Paragraph(data.getSecurityDesc(), cellFont));
                table.addCell(getAmountCell(data.getSumOfUnits(), cellFont, FORMAT164));
                table.addCell(getAmountCell(data.getSumOfMarketValue(), cellFont));
                table.addCell(getAmountCell(data.getSumOfEstimatedIncome(), cellFont));
                table.addCell(getAmountCell(data.getSumOfRemainderOfFYEstimated(), cellFont));
                table.addCell(getAmountCell(data.getSumOfNextFYEstimatedIncome(), cellFont));
            }
        }
        table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getTotalMarketValueForCashEquivalents(IncomePrincipalIndicator.PRINCIPAL).add(reportData.getHistoryPrincipalCash()), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        // other report groups
        Iterator<Integer> reportGroupOrderSet = reportGroups.keySet().iterator();
        while (reportGroupOrderSet.hasNext()) {                    
            
            Integer reportGroupOrder = reportGroupOrderSet.next();
            if (reportGroupOrder.intValue() > 1) {   
                TreeMap<String, ReportGroupData> reportGroupData = reportData.getReportGroupsForPrincipal().get(reportGroupOrder);
                if (reportGroupData == null || reportGroupData.isEmpty()) {
                    continue;
                }
    
                Iterator<String> secirutyIdSet = reportGroupData.keySet().iterator();
                while (secirutyIdSet.hasNext()) {
                    String securityId = secirutyIdSet.next();
                    ReportGroupData data = reportGroupData.get(securityId);
                    PdfPCell groupDescCell = new PdfPCell(new Paragraph(data.getReportGroupDesc() + " " + data.getReportGroupOrder(), cellFont));
                    groupDescCell.setColspan(6);
                    table.addCell(groupDescCell);                    
                    table.addCell(new Paragraph(data.getSecurityDesc(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfUnits(), cellFont, FORMAT164));
                    table.addCell(getAmountCell(data.getSumOfMarketValue(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfEstimatedIncome(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfRemainderOfFYEstimated(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfNextFYEstimatedIncome(), cellFont));
                    
                    // totals
                    table.addCell(new Paragraph("TOTAL <" + data.getReportGroupDesc() + " " + data.getReportGroupOrder() + ">", cellFont));
                    table.addCell("");
                    table.addCell(getAmountCell(reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.PRINCIPAL, reportGroupOrder), cellFont));
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }

        // total expendable funds
        table.addCell(new Paragraph("TOTAL ENDOWED FUNDS", titleFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.PRINCIPAL).add(reportData.getHistoryPrincipalCash()), cellFont));
        table.addCell(getAmountCell(reportData.getTotalSumOfEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL), cellFont));
        table.addCell(getAmountCell(reportData.getTotalSumOfRemainderOfFYEstimated(IncomePrincipalIndicator.PRINCIPAL), cellFont));
        table.addCell(getAmountCell(reportData.getTotalSumOfNextFYEstimatedIncome(IncomePrincipalIndicator.PRINCIPAL), cellFont));                  
    }
  
    /**
     * Generates report group non-endowed detail
     * 
     * @param reportData
     * @param docuement
     * @param table
     * @param cellFont
     * @throws Exception
     */
    protected void printReportGroupForNonEndowedDetail(AssetStatementReportDataHolder reportData, Document docuement, PdfPTable table, Font cellFont) throws Exception {
        
        table.addCell(new Paragraph("Income Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getHistoryIncomeCash(), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        table.addCell(new Paragraph("Principal Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(reportData.getHistoryPrincipalCash(), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        TreeMap<Integer, TreeMap<String, ReportGroupData>> reportGroups = reportData.getReportGroupsForIncome();
        if (reportGroups == null || reportGroups.isEmpty()) {
            table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
            table.addCell("");
            table.addCell(getAmountCell(reportData.getHistoryIncomeCash().add(reportData.getHistoryPrincipalCash()), cellFont));
            table.addCell("");
            table.addCell("");
            table.addCell("");
            return;
        }
        
        // Cash and equivalents 
        TreeMap<String, ReportGroupData> cashEquivalentsData = reportData.getReportGroupsForIncome().get(1);
        if (cashEquivalentsData != null && !cashEquivalentsData.isEmpty()) {
            Iterator<String> secirutyIdSet = cashEquivalentsData.keySet().iterator();              
            while (secirutyIdSet.hasNext()) {
                // get securityId
                String securityId = secirutyIdSet.next();
                ReportGroupData data = cashEquivalentsData.get(securityId);
                table.addCell(new Paragraph(data.getSecurityDesc(), cellFont));
                table.addCell(getAmountCell(data.getSumOfUnits(), cellFont, FORMAT164));
                table.addCell(getAmountCell(data.getSumOfMarketValue(), cellFont));
                table.addCell(getAmountCell(data.getSumOfEstimatedIncome(), cellFont));
                table.addCell(getAmountCell(data.getSumOfRemainderOfFYEstimated(), cellFont));
                table.addCell(getAmountCell(data.getSumOfNextFYEstimatedIncome(), cellFont));
            }
        }   
        BigDecimal totalCashEquivalents = reportData.getTotalMarketValueForCashEquivalents(IncomePrincipalIndicator.INCOME).add(reportData.getTotalMarketValueForCashEquivalents(IncomePrincipalIndicator.PRINCIPAL)).add(reportData.getHistoryIncomeCash()).add(reportData.getHistoryPrincipalCash());
        table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(totalCashEquivalents, cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");     
        
        // other report groups
        Iterator<Integer> reportGroupOrderSet = reportGroups.keySet().iterator();
        while (reportGroupOrderSet.hasNext()) {                    
            
            Integer reportGroupOrder = reportGroupOrderSet.next();
            TreeMap<String, ReportGroupData> reportGroupData = reportData.getReportGroupsForIncome().get(reportGroupOrder);
            if (reportGroupData == null || reportGroupData.isEmpty()) {
                continue;
            }
            
            Iterator<String> secirutyIdSet = reportGroupData.keySet().iterator();
            while (secirutyIdSet.hasNext()) {
                String securityId = secirutyIdSet.next();
                if (reportGroupOrder.intValue() > 1) {
                    ReportGroupData data = reportGroupData.get(securityId);
                    PdfPCell groupDescCell = new PdfPCell(new Paragraph(data.getReportGroupDesc() + " " + data.getReportGroupOrder(), cellFont));
                    groupDescCell.setColspan(6);
                    table.addCell(groupDescCell);  
                    table.addCell(new Paragraph(data.getSecurityDesc(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfUnits(), cellFont, FORMAT164));
                    table.addCell(getAmountCell(data.getSumOfMarketValue(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfEstimatedIncome(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfRemainderOfFYEstimated(), cellFont));
                    table.addCell(getAmountCell(data.getSumOfNextFYEstimatedIncome(), cellFont));
                    
                    // totals
                    table.addCell(new Paragraph("TOTAL <" + data.getReportGroupDesc() + " " + data.getReportGroupOrder() + ">", cellFont));
                    table.addCell("");
                    table.addCell(getAmountCell(reportData.getTotalSumOfMarketValue(IncomePrincipalIndicator.INCOME, reportGroupOrder), cellFont));
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }             
    }
    
    /**
     * Generates report group income non-endowed total
     * 
     * @param reportGroupsForIncomeTotal
     * @param totalHistoryIncomeCash
     * @param docuement
     * @param table
     * @param cellFont
     * @throws Exception
     */
    protected void printReportGroupForIncomeEndowmentTotal(TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForIncomeTotal, BigDecimal totalHistoryIncomeCash, Document docuement, PdfPTable table, Font cellFont) throws Exception {
        
        table.addCell(new Paragraph("Income Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(totalHistoryIncomeCash, cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        if (reportGroupsForIncomeTotal == null || reportGroupsForIncomeTotal.isEmpty()) {
            table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
            table.addCell("");
            table.addCell(getAmountCell(totalHistoryIncomeCash, cellFont));
            table.addCell("");
            table.addCell("");
            table.addCell("");
            return;
        }

        // Cash and equivalents         
        BigDecimal grandTotalMarketValue1 = BigDecimal.ZERO;
        BigDecimal grandTotalEstimatedAnnualIncome1 = BigDecimal.ZERO;
        BigDecimal grandTotalFyRemainderEAI1 = BigDecimal.ZERO;
        BigDecimal grandTotalNextFyEAI1 = BigDecimal.ZERO;
        
        TreeMap<String, List<ReportGroupData>> cashEquivalentsData = reportGroupsForIncomeTotal.get(1);
        if (cashEquivalentsData != null && !cashEquivalentsData.isEmpty()) {
            Iterator<String> secirutyIdSet = cashEquivalentsData.keySet().iterator();                 
            while (secirutyIdSet.hasNext()) {
                // get securityId
                String securityId = secirutyIdSet.next();
                List<ReportGroupData> dataList = cashEquivalentsData.get(securityId);
                BigDecimal totalUnits = BigDecimal.ZERO;
                BigDecimal totalMarketValue = BigDecimal.ZERO;
                BigDecimal totalEstimatedAnnualIncome = BigDecimal.ZERO;
                BigDecimal totalFyRemainderEAI = BigDecimal.ZERO;
                BigDecimal totalNextFyEAI = BigDecimal.ZERO;
 
                for (ReportGroupData data : dataList) {
                    totalUnits = totalUnits.add(data.getSumOfUnits());
                    totalMarketValue = totalMarketValue.add(data.getSumOfMarketValue());
                    totalEstimatedAnnualIncome = totalEstimatedAnnualIncome.add(data.getSumOfEstimatedIncome());
                    totalFyRemainderEAI = totalFyRemainderEAI.add(data.getSumOfRemainderOfFYEstimated());
                    totalNextFyEAI = totalNextFyEAI.add(data.getSumOfNextFYEstimatedIncome());
                }
                
                table.addCell(new Paragraph(dataList.get(0).getSecurityDesc(), cellFont));
                table.addCell(getAmountCell(totalUnits, cellFont, FORMAT164));
                table.addCell(getAmountCell(totalMarketValue, cellFont));
                table.addCell(getAmountCell(totalEstimatedAnnualIncome, cellFont));
                table.addCell(getAmountCell(totalFyRemainderEAI, cellFont));
                table.addCell(getAmountCell(totalNextFyEAI, cellFont));
                
                grandTotalMarketValue1 = grandTotalMarketValue1.add(totalMarketValue);
                grandTotalEstimatedAnnualIncome1 = grandTotalEstimatedAnnualIncome1.add(totalEstimatedAnnualIncome);
                grandTotalFyRemainderEAI1 = grandTotalFyRemainderEAI1.add(totalFyRemainderEAI);
                grandTotalNextFyEAI1 = grandTotalNextFyEAI1.add(totalNextFyEAI);
            }
        }    
        table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(grandTotalMarketValue1.add(totalHistoryIncomeCash), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");            
        
        // Other report groups
        BigDecimal grandTotalMarketValueN = BigDecimal.ZERO;
        BigDecimal grandTotalEstimatedAnnualIncomeN = BigDecimal.ZERO;
        BigDecimal grandTotalFyRemainderEAIN = BigDecimal.ZERO;
        BigDecimal grandTotalNextFyEAIN = BigDecimal.ZERO;
        
        Iterator<Integer> reportGroupOrderSet = reportGroupsForIncomeTotal.keySet().iterator();
        while (reportGroupOrderSet.hasNext()) {                    
            
            Integer reportGroupOrder = reportGroupOrderSet.next();
            if (reportGroupOrder.intValue() > 1) {                  
                TreeMap<String, List<ReportGroupData>> reportGroupDataBySecurity = reportGroupsForIncomeTotal.get(reportGroupOrder);
                if (reportGroupDataBySecurity != null && !reportGroupDataBySecurity.isEmpty()) {
                
                    String reportGroupDesc = "";
                    Iterator<String> securityIdSet = reportGroupDataBySecurity.keySet().iterator();                    
                    while (securityIdSet.hasNext()) {
                        String securityId = securityIdSet.next();
                        List<ReportGroupData> dataList = reportGroupDataBySecurity.get(securityId);
                        BigDecimal totalUnits = BigDecimal.ZERO;
                        BigDecimal totalMarketValue = BigDecimal.ZERO;
                        BigDecimal totalEstimatedAnnualIncome = BigDecimal.ZERO;
                        BigDecimal totalFyRemainderEAI = BigDecimal.ZERO;
                        BigDecimal totalNextFyEAI = BigDecimal.ZERO;

                        for (ReportGroupData data : dataList) {
                            totalUnits = totalUnits.add(data.getSumOfUnits());
                            totalMarketValue = totalMarketValue.add(data.getSumOfMarketValue());
                            totalEstimatedAnnualIncome = totalEstimatedAnnualIncome.add(data.getSumOfEstimatedIncome());
                            totalFyRemainderEAI = totalFyRemainderEAI.add(data.getSumOfRemainderOfFYEstimated());
                            totalNextFyEAI = totalNextFyEAI.add(data.getSumOfNextFYEstimatedIncome());
                        }
                        
                        PdfPCell groupDescCell = new PdfPCell(new Paragraph(dataList.get(0).getReportGroupDesc() + " " + dataList.get(0).getReportGroupOrder(), cellFont));
                        groupDescCell.setColspan(6);
                        table.addCell(groupDescCell);
                        
                        table.addCell(new Paragraph(dataList.get(0).getSecurityDesc(), cellFont));
                        table.addCell(getAmountCell(totalUnits, cellFont, FORMAT164));
                        table.addCell(getAmountCell(totalMarketValue, cellFont));
                        table.addCell(getAmountCell(totalEstimatedAnnualIncome, cellFont));
                        table.addCell(getAmountCell(totalFyRemainderEAI, cellFont));
                        table.addCell(getAmountCell(totalNextFyEAI, cellFont));
                        
                        reportGroupDesc = dataList.get(0).getReportGroupDesc();
                        grandTotalMarketValueN = grandTotalMarketValueN.add(totalMarketValue);
                        grandTotalEstimatedAnnualIncomeN = grandTotalEstimatedAnnualIncomeN.add(totalEstimatedAnnualIncome);
                        grandTotalFyRemainderEAIN = grandTotalFyRemainderEAIN.add(totalFyRemainderEAI);
                        grandTotalNextFyEAIN = grandTotalNextFyEAIN.add(totalNextFyEAI);
                    }
                    // totals
                    table.addCell(new Paragraph("TOTAL <" + reportGroupDesc + " " + reportGroupOrder + ">", cellFont));
                    table.addCell("");
                    table.addCell(getAmountCell(grandTotalMarketValueN, cellFont));
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }

        // total expendable funds
        table.addCell(new Paragraph("TOTAL EXPENDABLE FUNDS", titleFont));
        table.addCell("");
        table.addCell(getAmountCell(grandTotalMarketValue1.add(grandTotalMarketValueN).add(totalHistoryIncomeCash), cellFont));
        table.addCell(getAmountCell(grandTotalEstimatedAnnualIncome1.add(grandTotalEstimatedAnnualIncomeN), cellFont));
        table.addCell(getAmountCell(grandTotalFyRemainderEAI1.add(grandTotalFyRemainderEAIN), cellFont));
        table.addCell(getAmountCell(grandTotalNextFyEAI1.add(grandTotalNextFyEAIN), cellFont));                  
    }

    /**
     * Generates report group principal non-endowed total
     * 
     * @param reportGroupsForPrincipalTotal
     * @param totalHistoryPrincipalCash
     * @param docuement
     * @param table
     * @param cellFont
     */
    protected void printReportGroupForPrincipalEndowmentTotal(TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForPrincipalTotal, BigDecimal totalHistoryPrincipalCash, Document docuement, PdfPTable table, Font cellFont) {
       
        table.addCell(new Paragraph("Principal Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(totalHistoryPrincipalCash, cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        if (reportGroupsForPrincipalTotal == null || reportGroupsForPrincipalTotal.isEmpty()) {
            table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
            table.addCell("");
            table.addCell(getAmountCell(totalHistoryPrincipalCash, cellFont));
            table.addCell("");
            table.addCell("");
            table.addCell("");
            return;
        }

        // Cash and equivalents         
        BigDecimal grandTotalMarketValue1 = BigDecimal.ZERO;
        BigDecimal grandTotalEstimatedAnnualIncome1 = BigDecimal.ZERO;
        BigDecimal grandTotalFyRemainderEAI1 = BigDecimal.ZERO;
        BigDecimal grandTotalNextFyEAI1 = BigDecimal.ZERO;
        
        TreeMap<String, List<ReportGroupData>> cashEquivalentsData = reportGroupsForPrincipalTotal.get(1);
        if (cashEquivalentsData != null && !cashEquivalentsData.isEmpty()) {
            Iterator<String> secirutyIdSet = cashEquivalentsData.keySet().iterator();                 
            while (secirutyIdSet.hasNext()) {
                // get securityId
                String securityId = secirutyIdSet.next();
                List<ReportGroupData> dataList = cashEquivalentsData.get(securityId);
                BigDecimal totalUnits = BigDecimal.ZERO;
                BigDecimal totalMarketValue = BigDecimal.ZERO;
                BigDecimal totalEstimatedAnnualIncome = BigDecimal.ZERO;
                BigDecimal totalFyRemainderEAI = BigDecimal.ZERO;
                BigDecimal totalNextFyEAI = BigDecimal.ZERO;
                //getTotals(dataList, totalUnits, totalMarketValue, totalEstimatedAnnualIncome, totalFyRemainderEAI, totalNextFyEAI);
                for (ReportGroupData data : dataList) {
                    totalUnits = totalUnits.add(data.getSumOfUnits());
                    totalMarketValue = totalMarketValue.add(data.getSumOfMarketValue());
                    totalEstimatedAnnualIncome = totalEstimatedAnnualIncome.add(data.getSumOfEstimatedIncome());
                    totalFyRemainderEAI = totalFyRemainderEAI.add(data.getSumOfRemainderOfFYEstimated());
                    totalNextFyEAI = totalNextFyEAI.add(data.getSumOfNextFYEstimatedIncome());
                }
                
                table.addCell(new Paragraph(dataList.get(0).getSecurityDesc(), cellFont));
                table.addCell(getAmountCell(totalUnits, cellFont, FORMAT164));
                table.addCell(getAmountCell(totalMarketValue, cellFont));
                table.addCell(getAmountCell(totalEstimatedAnnualIncome, cellFont));
                table.addCell(getAmountCell(totalFyRemainderEAI, cellFont));
                table.addCell(getAmountCell(totalNextFyEAI, cellFont));
                
                grandTotalMarketValue1 = grandTotalMarketValue1.add(totalMarketValue);
                grandTotalEstimatedAnnualIncome1 = grandTotalEstimatedAnnualIncome1.add(totalEstimatedAnnualIncome);
                grandTotalFyRemainderEAI1 = grandTotalFyRemainderEAI1.add(totalFyRemainderEAI);
                grandTotalNextFyEAI1 = grandTotalNextFyEAI1.add(totalNextFyEAI);
            }
        }    
        table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(grandTotalMarketValue1.add(totalHistoryPrincipalCash), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");            
        
        // Other report groups
        BigDecimal grandTotalMarketValueN = BigDecimal.ZERO;
        BigDecimal grandTotalEstimatedAnnualIncomeN = BigDecimal.ZERO;
        BigDecimal grandTotalFyRemainderEAIN = BigDecimal.ZERO;
        BigDecimal grandTotalNextFyEAIN = BigDecimal.ZERO;
        
        Iterator<Integer> reportGroupOrderSet = reportGroupsForPrincipalTotal.keySet().iterator();
        while (reportGroupOrderSet.hasNext()) {                    
            
            Integer reportGroupOrder = reportGroupOrderSet.next();
            if (reportGroupOrder.intValue() > 1) {                  
                TreeMap<String, List<ReportGroupData>> reportGroupDataBySecurity = reportGroupsForPrincipalTotal.get(reportGroupOrder);
                if (reportGroupDataBySecurity != null && !reportGroupDataBySecurity.isEmpty()) {
                
                    String reportGroupDesc = "";
                    Iterator<String> securityIdSet = reportGroupDataBySecurity.keySet().iterator();                    
                    while (securityIdSet.hasNext()) {
                        String securityId = securityIdSet.next();
                        List<ReportGroupData> dataList = reportGroupDataBySecurity.get(securityId);
                        BigDecimal totalUnits = BigDecimal.ZERO;
                        BigDecimal totalMarketValue = BigDecimal.ZERO;
                        BigDecimal totalEstimatedAnnualIncome = BigDecimal.ZERO;
                        BigDecimal totalFyRemainderEAI = BigDecimal.ZERO;
                        BigDecimal totalNextFyEAI = BigDecimal.ZERO;
                        //getTotals(dataList, totalUnits, totalMarketValue, totalEstimatedAnnualIncome, totalFyRemainderEAI, totalNextFyEAI);
                        for (ReportGroupData data : dataList) {
                            totalUnits = totalUnits.add(data.getSumOfUnits());
                            totalMarketValue = totalMarketValue.add(data.getSumOfMarketValue());
                            totalEstimatedAnnualIncome = totalEstimatedAnnualIncome.add(data.getSumOfEstimatedIncome());
                            totalFyRemainderEAI = totalFyRemainderEAI.add(data.getSumOfRemainderOfFYEstimated());
                            totalNextFyEAI = totalNextFyEAI.add(data.getSumOfNextFYEstimatedIncome());
                        }
                        PdfPCell groupDescCell = new PdfPCell(new Paragraph(dataList.get(0).getReportGroupDesc() + " " + dataList.get(0).getReportGroupOrder(), cellFont));
                        groupDescCell.setColspan(6);
                        table.addCell(groupDescCell);
                        
                        table.addCell(new Paragraph(dataList.get(0).getSecurityDesc(), cellFont));
                        table.addCell(getAmountCell(totalUnits, cellFont, FORMAT164));
                        table.addCell(getAmountCell(totalMarketValue, cellFont));
                        table.addCell(getAmountCell(totalEstimatedAnnualIncome, cellFont));
                        table.addCell(getAmountCell(totalFyRemainderEAI, cellFont));
                        table.addCell(getAmountCell(totalNextFyEAI, cellFont));
                        
                        reportGroupDesc = dataList.get(0).getReportGroupDesc();
                        grandTotalMarketValueN = grandTotalMarketValueN.add(totalMarketValue);
                        grandTotalEstimatedAnnualIncomeN = grandTotalEstimatedAnnualIncomeN.add(totalEstimatedAnnualIncome);
                        grandTotalFyRemainderEAIN = grandTotalFyRemainderEAIN.add(totalFyRemainderEAI);
                        grandTotalNextFyEAIN = grandTotalNextFyEAIN.add(totalNextFyEAI);
                    }
                    // totals
                    table.addCell(new Paragraph("TOTAL <" + reportGroupDesc + " " + reportGroupOrder + ">", cellFont));
                    table.addCell("");
                    table.addCell(getAmountCell(grandTotalMarketValueN, cellFont));
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }
        
        // total expendable funds
        table.addCell(new Paragraph("TOTAL ENDOWED FUNDS", titleFont));
        table.addCell("");
        table.addCell(getAmountCell(grandTotalMarketValue1.add(grandTotalMarketValueN).add(totalHistoryPrincipalCash), cellFont));
        table.addCell(getAmountCell(grandTotalEstimatedAnnualIncome1.add(grandTotalEstimatedAnnualIncomeN), cellFont));
        table.addCell(getAmountCell(grandTotalFyRemainderEAI1.add(grandTotalFyRemainderEAIN), cellFont));
        table.addCell(getAmountCell(grandTotalNextFyEAI1.add(grandTotalNextFyEAIN), cellFont));   
                   
    }
  
    /**
     * Generates report group non-endowed total
     * 
     * @param reportGroupsForTotal
     * @param totalHistoryIncomeCash
     * @param totalHistoryPrincipalCash
     * @param docuement
     * @param table
     * @param cellFont
     * @throws Exception
     */
    protected void printReportGroupForNonEndowedTotal(TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForTotal, BigDecimal totalHistoryIncomeCash, BigDecimal totalHistoryPrincipalCash, Document docuement, PdfPTable table, Font cellFont) throws Exception {
        
        table.addCell(new Paragraph("Income Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(totalHistoryIncomeCash, cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        table.addCell(new Paragraph("Principal Cash", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(totalHistoryPrincipalCash, cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");
        
        if (reportGroupsForTotal == null || reportGroupsForTotal.isEmpty()) {
            table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
            table.addCell("");
            table.addCell(getAmountCell(totalHistoryIncomeCash, cellFont));
            table.addCell("");
            table.addCell("");
            table.addCell("");
            return;
        }
        
        // Cash and equivalents         
        BigDecimal grandTotalMarketValue1 = BigDecimal.ZERO;
        BigDecimal grandTotalEstimatedAnnualIncome1 = BigDecimal.ZERO;
        BigDecimal grandTotalFyRemainderEAI1 = BigDecimal.ZERO;
        BigDecimal grandTotalNextFyEAI1 = BigDecimal.ZERO;
        
        TreeMap<String, List<ReportGroupData>> cashEquivalentsData = reportGroupsForTotal.get(1);
        if (cashEquivalentsData != null && !cashEquivalentsData.isEmpty()) {
            Iterator<String> secirutyIdSet = cashEquivalentsData.keySet().iterator();                 
            while (secirutyIdSet.hasNext()) {
                // get securityId
                String securityId = secirutyIdSet.next();
                List<ReportGroupData> dataList = cashEquivalentsData.get(securityId);
                BigDecimal totalUnits = BigDecimal.ZERO;
                BigDecimal totalMarketValue = BigDecimal.ZERO;
                BigDecimal totalEstimatedAnnualIncome = BigDecimal.ZERO;
                BigDecimal totalFyRemainderEAI = BigDecimal.ZERO;
                BigDecimal totalNextFyEAI = BigDecimal.ZERO;
                //getTotals(dataList, totalUnits, totalMarketValue, totalEstimatedAnnualIncome, totalFyRemainderEAI, totalNextFyEAI);
                for (ReportGroupData data : dataList) {
                    totalUnits = totalUnits.add(data.getSumOfUnits());
                    totalMarketValue = totalMarketValue.add(data.getSumOfMarketValue());
                    totalEstimatedAnnualIncome = totalEstimatedAnnualIncome.add(data.getSumOfEstimatedIncome());
                    totalFyRemainderEAI = totalFyRemainderEAI.add(data.getSumOfRemainderOfFYEstimated());
                    totalNextFyEAI = totalNextFyEAI.add(data.getSumOfNextFYEstimatedIncome());
                }
                table.addCell(new Paragraph(dataList.get(0).getSecurityDesc(), cellFont));
                table.addCell(getAmountCell(totalUnits, cellFont, FORMAT164));
                table.addCell(getAmountCell(totalMarketValue, cellFont));
                table.addCell(getAmountCell(totalEstimatedAnnualIncome, cellFont));
                table.addCell(getAmountCell(totalFyRemainderEAI, cellFont));
                table.addCell(getAmountCell(totalNextFyEAI, cellFont));
                
                grandTotalMarketValue1 = grandTotalMarketValue1.add(totalMarketValue);
                grandTotalEstimatedAnnualIncome1 = grandTotalEstimatedAnnualIncome1.add(totalEstimatedAnnualIncome);
                grandTotalFyRemainderEAI1 = grandTotalFyRemainderEAI1.add(totalFyRemainderEAI);
                grandTotalNextFyEAI1 = grandTotalNextFyEAI1.add(totalNextFyEAI);
            }
        }    
        table.addCell(new Paragraph("TOTAL CASH AND\nEQUIVALENTS", cellFont));
        table.addCell("");
        table.addCell(getAmountCell(grandTotalMarketValue1.add(totalHistoryIncomeCash).add(totalHistoryPrincipalCash), cellFont));
        table.addCell("");
        table.addCell("");
        table.addCell("");            
        
        // Other report groups
        BigDecimal grandTotalMarketValueN = BigDecimal.ZERO;
        BigDecimal grandTotalEstimatedAnnualIncomeN = BigDecimal.ZERO;
        BigDecimal grandTotalFyRemainderEAIN = BigDecimal.ZERO;
        BigDecimal grandTotalNextFyEAIN = BigDecimal.ZERO;
        
        Iterator<Integer> reportGroupOrderSet = reportGroupsForTotal.keySet().iterator();
        while (reportGroupOrderSet.hasNext()) {                    
            
            Integer reportGroupOrder = reportGroupOrderSet.next();
            if (reportGroupOrder.intValue() > 1) {                  
                TreeMap<String, List<ReportGroupData>> reportGroupDataBySecurity = reportGroupsForTotal.get(reportGroupOrder);
                if (reportGroupDataBySecurity != null && !reportGroupDataBySecurity.isEmpty()) {
                
                    String reportGroupDesc = "";
                    Iterator<String> securityIdSet = reportGroupDataBySecurity.keySet().iterator();                    
                    while (securityIdSet.hasNext()) {
                        String securityId = securityIdSet.next();
                        List<ReportGroupData> dataList = reportGroupDataBySecurity.get(securityId);
                        BigDecimal totalUnits = BigDecimal.ZERO;
                        BigDecimal totalMarketValue = BigDecimal.ZERO;
                        BigDecimal totalEstimatedAnnualIncome = BigDecimal.ZERO;
                        BigDecimal totalFyRemainderEAI = BigDecimal.ZERO;
                        BigDecimal totalNextFyEAI = BigDecimal.ZERO;
                        //getTotals(dataList, totalUnits, totalMarketValue, totalEstimatedAnnualIncome, totalFyRemainderEAI, totalNextFyEAI);
                        for (ReportGroupData data : dataList) {
                            totalUnits = totalUnits.add(data.getSumOfUnits());
                            totalMarketValue = totalMarketValue.add(data.getSumOfMarketValue());
                            totalEstimatedAnnualIncome = totalEstimatedAnnualIncome.add(data.getSumOfEstimatedIncome());
                            totalFyRemainderEAI = totalFyRemainderEAI.add(data.getSumOfRemainderOfFYEstimated());
                            totalNextFyEAI = totalNextFyEAI.add(data.getSumOfNextFYEstimatedIncome());
                        }
                        
                        PdfPCell groupDescCell = new PdfPCell(new Paragraph(dataList.get(0).getReportGroupDesc() + " " + dataList.get(0).getReportGroupOrder(), cellFont));
                        groupDescCell.setColspan(6);
                        table.addCell(groupDescCell);
                        
                        table.addCell(new Paragraph(dataList.get(0).getSecurityDesc(), cellFont));
                        table.addCell(getAmountCell(totalUnits, cellFont, FORMAT164));
                        table.addCell(getAmountCell(totalMarketValue, cellFont));
                        table.addCell(getAmountCell(totalEstimatedAnnualIncome, cellFont));
                        table.addCell(getAmountCell(totalFyRemainderEAI, cellFont));
                        table.addCell(getAmountCell(totalNextFyEAI, cellFont));
                        
                        reportGroupDesc = dataList.get(0).getReportGroupDesc();
                        grandTotalMarketValueN = grandTotalMarketValueN.add(totalMarketValue);
                        grandTotalEstimatedAnnualIncomeN = grandTotalEstimatedAnnualIncomeN.add(totalEstimatedAnnualIncome);
                        grandTotalFyRemainderEAIN = grandTotalFyRemainderEAIN.add(totalFyRemainderEAI);
                        grandTotalNextFyEAIN = grandTotalNextFyEAIN.add(totalNextFyEAI);
                    }
                    // totals
                    table.addCell(new Paragraph("TOTAL <" + reportGroupDesc + " " + reportGroupOrder + ">", cellFont));
                    table.addCell("");
                    table.addCell(getAmountCell(grandTotalMarketValueN, cellFont));
                    table.addCell("");
                    table.addCell("");
                    table.addCell("");
                }
            }
        }
    }
    
    /**
     * Constructs the data structure for grouping by security 
     * 
     * @param endowmentAssetStatementReportDataHolders
     * @param ipInd
     * @return
     */
    protected TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> createReportGroupsForTotal(List<AssetStatementReportDataHolder> endowmentAssetStatementReportDataHolders, String ipInd) {
        
        TreeMap<Integer, TreeMap<String, List<ReportGroupData>>> reportGroupsForTotal = new TreeMap<Integer, TreeMap<String, List<ReportGroupData>>>();
        for (AssetStatementReportDataHolder reportDataHolder : endowmentAssetStatementReportDataHolders) {

            TreeMap<Integer, TreeMap<String, ReportGroupData>> reportGroupsData = IncomePrincipalIndicator.INCOME.equalsIgnoreCase(ipInd) ? reportDataHolder.getReportGroupsForIncome() : reportDataHolder.getReportGroupsForPrincipal();
            
            if (reportGroupsData != null) {
                
                Set<Integer> reportGroupOrders = reportGroupsData.keySet();
                // per report group order
                for (Integer reportGroupOrder : reportGroupOrders) {
                    // ReportGroupData with securityId
                    TreeMap<String,ReportGroupData> reportGroupOrderData = reportGroupsData.get(reportGroupOrder);
                    Set<String> securityIds = reportGroupOrderData.keySet();
                    // per security Id
                    for (String securityId : securityIds) {
                        ReportGroupData reportGroupDataBySecurityId = reportGroupOrderData.get(securityId);
                        // add the ReportGroupData to reportGroupsForAllIncomes
                        if (reportGroupsForTotal.containsKey(reportGroupOrder)) {
                            // the report group order exists
                            if (reportGroupsForTotal.get(reportGroupOrder).containsKey(securityId)) {
                                // the security id exists
                                reportGroupsForTotal.get(reportGroupOrder).get(securityId).add(reportGroupDataBySecurityId);
                            } else {
                                List<ReportGroupData> reportGroupDataList = new ArrayList<ReportGroupData>();
                                reportGroupDataList.add(reportGroupDataBySecurityId);
                                reportGroupsForTotal.get(reportGroupOrder).put(securityId, reportGroupDataList);
                            }
                        } else {
                            TreeMap<String, List<ReportGroupData>> newReportGroupOrderData = new TreeMap<String,List<ReportGroupData>>();
                            List<ReportGroupData> reportGroupDataList = new ArrayList<ReportGroupData>();
                            reportGroupDataList.add(reportGroupDataBySecurityId);
                            newReportGroupOrderData.put(securityId, reportGroupDataList);
                            reportGroupsForTotal.put(reportGroupOrder, newReportGroupOrderData);
                        }                    
                    }
                }
            }
        }
        
        return reportGroupsForTotal;
    }

}
