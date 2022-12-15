package com.punchin.service;

import com.punchin.entity.ClaimsData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MISExport {
    /*        private final XSSFWorkbook workbook;
        private XSSFSheet sheet; */
    private Workbook workbook;
    private Sheet sheet;
    private ByteArrayOutputStream out;
    private final List<ClaimsData> claimsData;
    String filename = "/home/prince/D/Claim_Data_Format.xlsx";
    FileOutputStream fileOut;

    public MISExport(List<ClaimsData> claimsData) throws FileNotFoundException {
        this.claimsData = claimsData;
        /*        workbook = new XSSFWorkbook();*/
        Workbook workbook = new XSSFWorkbook();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileOutputStream fileOut = new FileOutputStream(filename);


    }

    private void writeHeaderLine() {
        Sheet sheet = workbook.createSheet("Sheet1");
        //     sheet = workbook.createSheet("sheet");
        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        createCell(row, 0, "Borrower Name", style);
        createCell(row, 1, "Borrower Address", style);
        createCell(row, 2, "Borrower City", style);
        createCell(row, 3, "Borrower Pincode", style);
        createCell(row, 4, "Borrower State", style);
        createCell(row, 5, "Borrower Contact Number", style);
        createCell(row, 6, "Borrower Email id", style);
        createCell(row, 7, "Borrower Alternate Contact Number", style);
        createCell(row, 8, "Borrower Alternate Contact Details", style);
        createCell(row, 9, "Loan Account Number", style);
        createCell(row, 10, "Loan Category/Type", style);
        createCell(row, 11, "Loan Disbursal Date", style);
        createCell(row, 12, "Loan Disbursal Amount", style);
        createCell(row, 13, "Lender Branch Code", style);
        createCell(row, 14, "Lender Branch Address", style);
        createCell(row, 15, "Lender Branch City", style);
        createCell(row, 16, "Lender Branch Pin code", style);
        createCell(row, 17, "Lender Branch State", style);
        createCell(row, 18, " Lenders Contact Name", style);
        createCell(row, 19, "Lender Contact Number", style);
        createCell(row, 20, "Insurer Name", style);
        createCell(row, 21, "Borrower Policy Number", style);
        createCell(row, 22, "Master Policy Number", style);
        createCell(row, 23, "Policy Start Date", style);
        createCell(row, 24, "Policy Tenure", style);
        createCell(row, 25, "Policy Sum Assured", style);
        createCell(row, 26, "Nominee Name", style);
        createCell(row, 27, "Nominee Relationship", style);
        createCell(row, 28, "Nominee Contact Number", style);
        createCell(row, 29, "Nominee Email id", style);
        createCell(row, 30, "Nominee Address", style);
        createCell(row, 31, "Claim Status", style);
    }

    private void createCell(Row row, int columnCount, Object value, CellStyle style) {
        sheet.autoSizeColumn(columnCount);
        Cell cell = row.createCell(columnCount);
        if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue((String) value);
        }
        cell.setCellStyle(style);
    }

    private void writeDataLines() {
        int rowCount = 1;
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = (XSSFFont) workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        for (ClaimsData claimData : claimsData) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            String disbursalDate = claimData.getLoanDisbursalDate().toString();
            String policyStartDate = claimData.getPolicyStartDate().toString();
            String nomineeAddress = claimData.getNomineeAddress();
            createCell(row, columnCount++, claimData.getBorrowerName(), style);
            createCell(row, columnCount++, claimData.getBorrowerAddress(), style);
            createCell(row, columnCount++, claimData.getBorrowerCity(), style);
            createCell(row, columnCount++, claimData.getBorrowerPinCode(), style);
            createCell(row, columnCount++, claimData.getBorrowerState(), style);
            createCell(row, columnCount++, claimData.getBorrowerContactNumber(), style);
            createCell(row, columnCount++, claimData.getBorrowerEmailId(), style);
            createCell(row, columnCount++, claimData.getBorrowerAlternateContactNumber(), style);
            createCell(row, columnCount++, claimData.getBorrowerAlternateContactDetails(), style);
            createCell(row, columnCount++, claimData.getLoanAccountNumber().toString(), style);
            createCell(row, columnCount++, claimData.getLoanType(), style);
            createCell(row, columnCount++, disbursalDate, style);
            createCell(row, columnCount++, "0", style);
            createCell(row, columnCount++, claimData.getBranchCode(), style);
            createCell(row, columnCount++, claimData.getBranchAddress(), style);
            createCell(row, columnCount++, claimData.getBranchCity(), style);
            createCell(row, columnCount++, claimData.getBranchPinCode(), style);
            createCell(row, columnCount++, claimData.getBranchState(), style);
            createCell(row, columnCount++, claimData.getLoanAccountManagerName(), style);
            createCell(row, columnCount++, claimData.getAccountManagerContactNumber(), style);
            createCell(row, columnCount++, claimData.getInsurerName(), style);
            createCell(row, columnCount++, claimData.getPolicyNumber(), style);
            createCell(row, columnCount++, claimData.getMasterPolNumber(), style);
            createCell(row, columnCount++, policyStartDate, style);
            createCell(row, columnCount++, claimData.getPolicyCoverageDuration(), style);
            createCell(row, columnCount++, claimData.getPolicySumAssured().toString(), style);
            createCell(row, columnCount++, claimData.getNomineeName(), style);
            createCell(row, columnCount++, claimData.getNomineeRelationShip(), style);
            createCell(row, columnCount++, claimData.getNomineeContactNumber(), style);
            createCell(row, columnCount++, claimData.getNomineeEmailId(), style);
            createCell(row, columnCount++, nomineeAddress, style);
            createCell(row, columnCount++, claimData.getClaimStatus().toString(), style);
        }
    }

    public ByteArrayInputStream export(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=claims-Data" + currentDateTime + ".xlsx";
        httpServletResponse.setHeader(headerKey, headerValue);
        writeHeaderLine();
        writeDataLines();
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        //  workbook.write(outputStream);
        workbook.write(out);
        workbook.write(fileOut);
        out.writeTo(fileOut);
        workbook.close();
        outputStream.close();
        return new ByteArrayInputStream(out.toByteArray());
    }

}
