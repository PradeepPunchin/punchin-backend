package com.punchin.service;

import com.punchin.entity.ClaimsData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class MISExport {

    private final XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private final List<ClaimsData> claimsData;

    public MISExport(List<ClaimsData> claimsData) {
        this.claimsData = claimsData;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine() {
        sheet = workbook.createSheet("ProPlaylist");
        Row row = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(16);
        style.setFont(font);
        createCell(row, 0, "PunchIn ClaimId", style);
        createCell(row, 1, "Insurer ClaimId", style);
        createCell(row, 2, "PunchIn BankerId", style);
        createCell(row, 3, "claim InwardDate", style);
        createCell(row, 4, "Borrower Name", style);
        createCell(row, 5, "Borrower Contact Number", style);
        createCell(row, 6, "Loan Account Number", style);
        createCell(row, 7, "Borrower Address", style);
        createCell(row, 8, "Loan Type", style);
        createCell(row, 9, "Loan Amount", style);
        createCell(row, 10, "Branch Code", style);
        createCell(row, 11, "Branch Name", style);
        createCell(row, 12, "Branch Address", style);
        createCell(row, 13, "Branch PinCode", style);
        createCell(row, 14, "Branch State", style);
        createCell(row, 15, "Loan Account Manager Name", style);
        createCell(row, 16, "Account Manager Contact Number", style);
        createCell(row, 17, "Insurer Name", style);
        createCell(row, 18, "Master PolNumber", style);
        createCell(row, 19, "Policy Number", style);
        createCell(row, 20, "Policy StartDate", style);
        createCell(row, 21, "Policy Coverage Duration", style);
        createCell(row, 22, "Policy SumAssured", style);
        createCell(row, 23, "Nominee Name", style);
        createCell(row, 24, "Nominee RelationShip", style);
        createCell(row, 25, "Nominee Contact Number", style);
        createCell(row, 26, "Nominee EmailId", style);
        createCell(row, 27, "Nominee Address", style);
        createCell(row, 28, "Claim Status", style);
      //  createCell(row, 29, "Claim Documents", style);
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
        XSSFFont font = workbook.createFont();
        font.setFontHeight(14);
        style.setFont(font);
        for (ClaimsData claimData : claimsData) {
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;
            String claimInwardDate = claimData.getClaimInwardDate().toString();
            String policyStartDate = claimData.getPolicyStartDate().toString();
            String nomineeAddress = claimData.getNomineeAddress();
            createCell(row, columnCount++, claimData.getPunchinClaimId(), style);
            createCell(row, columnCount++, claimData.getInsurerClaimId(), style);
            createCell(row, columnCount++, claimData.getPunchinBankerId(), style);
            createCell(row, columnCount++, claimInwardDate, style);
            createCell(row, columnCount++, claimData.getBorrowerName(), style);
            createCell(row, columnCount++, claimData.getBorrowerContactNumber(), style);
            createCell(row, columnCount++, claimData.getLoanAccountNumber(), style);
            createCell(row, columnCount++, claimData.getBorrowerAddress(), style);
            createCell(row, columnCount++, claimData.getLoanType(), style);
            createCell(row, columnCount++, claimData.getLoanAmount().toString(), style);
            createCell(row, columnCount++, claimData.getBranchCode(), style);
            createCell(row, columnCount++, claimData.getBranchName(), style);
            createCell(row, columnCount++, claimData.getBranchAddress(), style);
            createCell(row, columnCount++, claimData.getBranchPinCode(), style);
            createCell(row, columnCount++, claimData.getBranchState(), style);
            createCell(row, columnCount++, claimData.getLoanAccountManagerName(), style);
            createCell(row, columnCount++, claimData.getAccountManagerContactNumber(), style);
            createCell(row, columnCount++, claimData.getInsurerName(), style);
            createCell(row, columnCount++, claimData.getMasterPolNumber(), style);
            createCell(row, columnCount++, claimData.getPolicyNumber(), style);
            createCell(row, columnCount++, policyStartDate, style);
            createCell(row, columnCount++, claimData.getPolicyCoverageDuration(), style);
            createCell(row, columnCount++, claimData.getPolicySumAssured().toString(), style);
            createCell(row, columnCount++, claimData.getNomineeName(), style);
            createCell(row, columnCount++, claimData.getNomineeRelationShip(), style);
            createCell(row, columnCount++, claimData.getNomineeContactNumber(), style);
            createCell(row, columnCount++, claimData.getNomineeEmailId(), style);
            createCell(row, columnCount++, nomineeAddress, style);
            createCell(row, columnCount++, claimData.getClaimStatus().toString(), style);
        //    createCell(row, columnCount++, claimData.getClaimDocuments().toString(), style);
        }
    }

    public void export(HttpServletResponse httpServletResponse) throws IOException {
        httpServletResponse.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Claims-Data.xlsx";
        httpServletResponse.setHeader(headerKey, headerValue);
        writeHeaderLine();
        writeDataLines();
        ServletOutputStream outputStream = httpServletResponse.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }

}
