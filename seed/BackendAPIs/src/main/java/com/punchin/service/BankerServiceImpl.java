package com.punchin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.constant.ResponseMessgae;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BankerServiceImpl implements BankerService{

    @Autowired
    private ClaimsDataRepository claimsDataRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> saveUploadExcelData(MultipartFile[] files) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        try{
            log.info("BankerServiceImpl :: saveUploadExcelData files{}", files);
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            for(MultipartFile file : files) {
                Map<String, Object> data = convertExcelToListOfClaimsData(file.getInputStream(), bankerId);
                List<ClaimsData> claimsData = (List<ClaimsData>) Arrays.asList(data.get("claimsData")).get(0);
                if(Objects.nonNull(claimsData)) {
                    claimsData = claimsDataRepository.saveAll(claimsData);
                    map.put("data", claimsData);
                    map.put("status", true);
                    map.put("message", ResponseMessgae.success);
                    return map;
                }
                map.put("message", data.get("message"));
            }
            return map;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: saveUploadExcelData e{}", e);
            return map;
        }
    }

    @Override
    public Page getAllClaimsData(ClaimStatus claimStatus, Integer page, Integer limit) {
        try{
            log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimStatus, page, limit);
            Pageable pageable = PageRequest.of(page, limit, Sort.by("punchin_claim_id"));
            Page page1;
            if(ClaimDataFilter.ALL.equals(claimStatus)){
                page1 = claimsDataRepository.findAll(pageable);
            } else {
                page1 = claimsDataRepository.findByClaimStatus(claimStatus.toString(), pageable);
            }
            return page1;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: getAllClaimsData e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Long> getDashboardData() {
        try{
            log.info("BankerController :: getDashboardData");
            Map<String, Long> map = new HashMap<>();
            map.put(ClaimStatus.ALL.name(), 15L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 10L);
            map.put(ClaimStatus.SETTLED.name(), 5L);
            return map;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: getDashboardData e{}", e);
            return Collections.EMPTY_MAP;
        }
    }


    public Map<String, Object> convertExcelToListOfClaimsData(InputStream is, String bankerId) {
        Map<String, Object> map = new HashMap<>();
        List<ClaimsData> list = new ArrayList<>();
        log.info("BankerServiceImpl :: saveUploadExcelData file{}, bankerId{}", is, bankerId);
        try {
            XSSFWorkbook workbook = new XSSFWorkbook(is);
            XSSFSheet sheet = workbook.getSheet("Sheet1");
            if(Objects.isNull(sheet)){
                map.put("message", "sheet.not.found");
                return map;
            }
            int rowNumber = 0;
            boolean exit = false;
            Row row1 = sheet.getRow(1);
            for (Row row : sheet) {
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                if(exit){
                    break;
                }
                Iterator<Cell> cells = row.iterator();

                int cid = 0;

                ClaimsData p = new ClaimsData();

                while (cells.hasNext()) {
                    Cell cell = cells.next();

                    switch (cid) {
                        case 0:
                            cell.setCellType(CellType.STRING);
                            if(Objects.isNull(cell) || cell.equals("") || cell.getStringCellValue() == ""){
                                exit = true;
                                break;
                            }
                            p.setPunchinBankerId(bankerId);
                            break;
                        case 1:
                            p.setPunchinClaimId("PUN" + RandomStringUtils.randomAlphanumeric(10));
                            break;
                        case 2:
                            cell.setCellType(CellType.STRING);
                            p.setInsurerClaimId(cell.getStringCellValue());
                            break;
                        case 3:
                            if(Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                p.setClaimInwardDate(cell.getLocalDateTimeCellValue().toLocalDate());
                            }
                            break;
                        case 4:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerName(cell.getStringCellValue());
                            break;
                        case 5:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerContactNumber(cell.getStringCellValue());
                            break;
                        case 6:
                            cell.setCellType(CellType.STRING);
                            p.setLoanAccountNumber(cell.getStringCellValue());
                            break;
                        case 7:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerAddress(cell.getStringCellValue());
                            break;
                        case 8:
                            cell.setCellType(CellType.STRING);
                            p.setLoanType(cell.getStringCellValue());
                            break;
                        case 9:
                            if(Objects.nonNull(cell.getNumericCellValue())) {
                                p.setLoanAmount(cell.getNumericCellValue());
                            }
                            break;
                        case 10:
                            cell.setCellType(CellType.STRING);
                            p.setBranchCode(cell.getStringCellValue());
                            break;
                        case 11:
                            cell.setCellType(CellType.STRING);
                            p.setBranchName(cell.getStringCellValue());
                            break;
                        case 12:
                            cell.setCellType(CellType.STRING);
                            p.setBranchAddress(cell.getStringCellValue());
                            break;
                        case 13:
                            cell.setCellType(CellType.STRING);
                            if(Objects.nonNull(cell.getStringCellValue())) {
                                p.setBranchPinCode(cell.getStringCellValue());
                            }
                            break;
                        case 14:
                            p.setBranchState(cell.getStringCellValue());
                            break;
                        case 15:
                            cell.setCellType(CellType.STRING);
                            p.setLoanAccountManagerName(cell.getStringCellValue());
                            break;
                        case 16:
                            cell.setCellType(CellType.STRING);
                            p.setAccountManagerContactNumber(cell.getStringCellValue());
                            break;
                        case 17:
                            cell.setCellType(CellType.STRING);
                            p.setInsurerName(cell.getStringCellValue());
                            break;
                        case 18:
                            cell.setCellType(CellType.STRING);
                            p.setPolicyNumber(cell.getStringCellValue());
                            break;
                        case 19:
                            cell.setCellType(CellType.STRING);
                            p.setMasterPolNumber(cell.getStringCellValue());
                            break;
                        case 20:
                            if(Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                p.setPolicyStartDate(cell.getLocalDateTimeCellValue().toLocalDate());
                            }
                            break;
                        case 21:
                            if(Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicyCoverageDuration((int) cell.getNumericCellValue());
                            }
                            break;
                        case 22:
                            if(Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicySumAssured((double) cell.getNumericCellValue());
                            }
                            break;
                        case 23:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeName(cell.getStringCellValue());
                            break;
                        case 24:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeRelationShip(cell.getStringCellValue());
                            break;
                        case 25:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeContactNumber(cell.getStringCellValue());
                            break;
                        case 26:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeEmailId(cell.getStringCellValue());
                            break;
                        case 27:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeAddress(cell.getStringCellValue());
                            break;

                        case 28:
                            p.setClaimStatus(ClaimStatus.CLAIM_SUBMITTED);
                            break;

                        default:
                            break;
                    }
                    cid++;
                }
                if(!exit) {
                    list.add(p);
                }
            }
            map.put("claimsData", list);
            if(list.isEmpty()) {
                map.put("message", "data.not.found");
            }
            return map;
        } catch (IllegalStateException e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: convertExcelToListOfClaimsData e{}", e);
            map.put("message", "invalid.column.type");
            return map;
        }catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: convertExcelToListOfClaimsData e{}", e);
            map.put("message", ResponseMessgae.invalidFormat);
            return map;
        }
    }
}
