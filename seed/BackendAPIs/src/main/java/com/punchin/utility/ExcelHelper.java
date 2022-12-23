package com.punchin.utility;

import com.punchin.entity.ClaimDraftData;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ExcelHelper {
    private ExcelHelper() {
    }

    static final String SHEET = "sheet1";

    public static List<ClaimDraftData> excelToClaimsDraftData(InputStream is) {
        try {
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();
            List<ClaimDraftData> claimDraftDatas = new ArrayList<>();
            boolean exit = false;
            int rowNumber = 0;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                Iterator<Cell> currentCellsInRow = currentRow.iterator();
                ClaimDraftData claimDraftData = new ClaimDraftData();
                while (currentCellsInRow.hasNext()) {
                    Cell currentCell = currentCellsInRow.next();
                    switch (currentCell.getColumnIndex()) {
                        case 0:
                            if (Objects.isNull(currentCell) || currentCell.equals("") || currentCell.getNumericCellValue() == 0) {
                                exit = true;
                                break;
                            }
                            claimDraftData.setPunchinBankerId(bankerId);
                            break;
                        case 1:
                            claimDraftData.setBorrowerName(currentCell.getStringCellValue());
                            break;
                        case 2:
                            claimDraftData.setBorrowerAddress(currentCell.getStringCellValue());
                            break;
                        case 3:
                            claimDraftData.setBorrowerCity(currentCell.getStringCellValue());
                            break;
                        case 4:
                            if (Objects.nonNull(currentCell.getStringCellValue()))
                                claimDraftData.setBorrowerPinCode(currentCell.getStringCellValue());
                            break;
                        case 5:
                            claimDraftData.setBorrowerState(currentCell.getStringCellValue());
                            break;
                        case 6:
                            claimDraftData.setBorrowerContactNumber(currentCell.getStringCellValue());
                            break;
                        case 7:
                            claimDraftData.setBorrowerEmailId(currentCell.getStringCellValue());
                            break;
                        case 8:
                            if (currentCell.getColumnIndex() != 8)
                                claimDraftData.setBorrowerAlternateContactNumber(" ");
                            else claimDraftData.setBorrowerAlternateContactNumber(currentCell.getStringCellValue());
                            break;
                        case 9:
                            claimDraftData.setBorrowerAlternateContactDetails(currentCell.getStringCellValue());
                            break;
                        case 10:
                            long numericCellValue = (long) currentCell.getNumericCellValue();
                            claimDraftData.setLoanAccountNumber(String.valueOf(numericCellValue));
                            break;
                        case 11:
                            claimDraftData.setLoanType(currentCell.getStringCellValue());
                            break;
                        case 12:
                            if (Objects.nonNull(currentCell.getStringCellValue())) {
                                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(currentCell.getStringCellValue());
                                claimDraftData.setLoanDisbursalDate(date1);
                            }
                            break;
                        case 13:
                            if (Objects.nonNull(currentCell.getNumericCellValue()))
                                claimDraftData.setLoanAmount(currentCell.getNumericCellValue());
                            break;
                        case 14:
                            if (Objects.nonNull(currentCell.getNumericCellValue()))
                                claimDraftData.setLoanOutstandingAmount(currentCell.getNumericCellValue());
                            break;
                        case 15:
                            claimDraftData.setBranchCode(currentCell.getStringCellValue());
                            break;
                        case 16:
                            claimDraftData.setBranchAddress(currentCell.getStringCellValue());
                            break;
                        case 17:
                            claimDraftData.setBranchCity(currentCell.getStringCellValue());
                            break;
                        case 18:
                            claimDraftData.setBranchPinCode(currentCell.getStringCellValue());
                            break;
                        case 19:
                            claimDraftData.setBranchState(currentCell.getStringCellValue());
                            break;
                        case 20:
                            claimDraftData.setLoanAccountManagerName(currentCell.getStringCellValue());
                            break;
                        case 21:
                            claimDraftData.setAccountManagerContactNumber(currentCell.getStringCellValue());
                            break;
                        case 22:
                            claimDraftData.setInsurerName(currentCell.getStringCellValue());
                            break;
                        case 23:
                            claimDraftData.setPolicyNumber(currentCell.getStringCellValue());
                            break;
                        case 24:
                            claimDraftData.setMasterPolNumber(currentCell.getStringCellValue());
                            break;
                        case 25:
                            if (Objects.nonNull(currentCell.getStringCellValue())) {
                                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(currentCell.getStringCellValue());
                                claimDraftData.setPolicyStartDate(date1);
                            }
                            break;
                        case 26:
                            claimDraftData.setPolicyCoverageDuration((int) currentCell.getNumericCellValue());
                            break;
                        case 27:
                            claimDraftData.setPolicySumAssured(currentCell.getNumericCellValue());
                            break;
                        case 28:
                            claimDraftData.setNomineeName(currentCell.getStringCellValue());
                            break;
                        case 29:
                            claimDraftData.setNomineeRelationShip(currentCell.getStringCellValue());
                            break;
                        case 30:
                            claimDraftData.setNomineeContactNumber(currentCell.getStringCellValue());
                            break;
                        case 31:
                            claimDraftData.setNomineeEmailId(currentCell.getStringCellValue());
                            break;
                        case 32:
                            claimDraftData.setNomineeAddress(currentCell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                }
                claimDraftDatas.add(claimDraftData);
             }
            workbook.close();
            return claimDraftDatas;
        } catch (Exception e) {
            log.error("fail to parse Excel file: " + e.getMessage());
        }
        return Collections.emptyList();
    }
}
