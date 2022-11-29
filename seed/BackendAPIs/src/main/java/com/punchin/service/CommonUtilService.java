package com.punchin.service;

import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimData;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class CommonUtilService {

    public List<ClaimData> convertExcelToListOfProduct(InputStream is) {
        List<ClaimData> list = new ArrayList<>();

        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);

            XSSFSheet sheet = workbook.getSheet("Sheet1");

            int rowNumber = 0;

            for (Row row : sheet) {
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }

                Iterator<Cell> cells = row.iterator();

                int cid = 0;

                ClaimData p = new ClaimData();

                while (cells.hasNext()) {
                    Cell cell = cells.next();

                    switch (cid) {
                        case 0:
                            p.setSerialNumber((int) cell.getNumericCellValue());
                            break;
                        case 1:
                            p.setPunchinclaimId((int) cell.getNumericCellValue());
                            break;
                        case 2:
                            try {
                                p.setInsurerClaimId(cell.getStringCellValue());
                            } catch (IllegalStateException e) {
                                p.setInsurerClaimId(Integer.toString((int) cell.getNumericCellValue()));
                            }
                            break;
                        case 3:
                            p.setClaimInwardDate(cell.getLocalDateTimeCellValue());
                            break;
                        case 4:
                            p.setBorrowerName(cell.getStringCellValue());
                            break;
                        case 5:
                            p.setBorrowerContactNumber((int) cell.getNumericCellValue());
                            break;
                        case 6:
                            p.setLoanAccountNumber((long) cell.getNumericCellValue());
                            break;
                        case 7:
                            p.setBorrowerAddress(cell.getStringCellValue());
                            break;
                        case 8:
                            p.setLoanType(cell.getStringCellValue());
                            break;
                        case 9:
                            p.setLoanAmount((long) cell.getNumericCellValue());
                            break;
                        case 10:
                            p.setBranchCode((int) cell.getNumericCellValue());
                            break;
                        case 11:
                            p.setBranchName(cell.getStringCellValue());
                            break;
                        case 12:
                            p.setBranchAddress(cell.getStringCellValue());
                            break;
                        case 13:
                            p.setBranchPinCode((int) cell.getNumericCellValue());
                            break;
                        case 14:
                            p.setState(cell.getStringCellValue());
                            break;
                        case 15:
                            p.setLoanAmountMgrName(cell.getStringCellValue());
                            break;
                        case 16:
                            p.setAcntMgrPhoneNumber((long) cell.getNumericCellValue());
                            break;
                        case 17:
                            p.setInsurerName(cell.getStringCellValue());
                            break;
                        case 18:
                            p.setBorrowerPolicyNumber((int) cell.getNumericCellValue());
                            break;
                        case 19:
                            p.setMasterPolNumber(cell.getStringCellValue());
                            break;
                        case 20:
                            p.setPolicyStartdate(cell.getLocalDateTimeCellValue());
                            break;
                        case 21:
                            p.setPolicyCoverageDuration((int) cell.getNumericCellValue());
                            break;
                        case 22:
                            p.setPolicySumAssured((double) cell.getNumericCellValue());
                            break;
                        case 23:
                            p.setNomineeName(cell.getStringCellValue());
                            break;
                        case 24:
                            p.setNomineeRelationShip(cell.getStringCellValue());
                            break;
                        case 25:
                            p.setNomineeContactNumber((long) cell.getNumericCellValue());
                            break;
                        case 26:
                            p.setNomineeEmailId(cell.getStringCellValue());
                            break;
                        case 27:
                            p.setNomineeAddress(cell.getStringCellValue());
                            break;

                        case 28:
                            p.setStatusEnum(cell.getStringCellValue());
                            break;

                        default:
                            break;
                    }
                    cid++;
                }
                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public PageDTO getDetailsPage(Object contentList, long size, Integer totalPages, long totalrecords) {
        PageDTO pageDTO = new PageDTO();
        pageDTO.setContent(contentList);
        pageDTO.setSize(size);
        pageDTO.setTotalPages(totalPages);
        pageDTO.setTotalRecords(totalrecords);
        return pageDTO;
    }
}
