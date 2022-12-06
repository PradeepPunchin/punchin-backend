package com.punchin.service;

import com.punchin.entity.*;
import com.punchin.enums.DocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimDraftDataRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
import com.punchin.security.AmazonClient;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ModelMapper;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BankerServiceImpl implements BankerService{

    @Autowired
    private ClaimsDataRepository claimsDataRepository;

    @Autowired
    private ClaimDraftDataRepository claimDraftDataRepository;

    @Autowired
    private DocumentUrlsRepository documentUrlsRepository;

    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Map<String, Object> saveUploadExcelData(MultipartFile[] files) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        try{
            log.info("BankerServiceImpl :: saveUploadExcelData files{}", files);
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            for(MultipartFile file : files) {
                Map<String, Object> data = convertExcelToListOfClaimsData(file.getInputStream(), bankerId);
                List<ClaimDraftData> claimsData = (List<ClaimDraftData>) Arrays.asList(data.get("claimsData")).get(0);
                if(Objects.nonNull(claimsData)) {
                    claimsData = claimDraftDataRepository.saveAll(claimsData);
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
    public Page getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        try{
            log.info("BankerServiceImpl :: getClaimsList dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Page page1 = Page.empty();
            if(claimDataFilter.ALL.equals(claimDataFilter)){
                page1 = claimsDataRepository.findAll(pageable);
            } else if(claimDataFilter.DRAFT.equals(claimDataFilter)){
                page1 = claimDraftDataRepository.findAll(pageable);
            } else if(claimDataFilter.SUBMITTED.equals(claimDataFilter)){
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.CLAIM_SUBMITTED, false, pageable);
            } else if(claimDataFilter.WIP.equals(claimDataFilter)){
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.IN_PROGRESS, true, pageable);
            } else if(claimDataFilter.SETTLED.equals(claimDataFilter)){
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.SETTLED, true, pageable);
            }
            return page1;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimsList e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Long> getDashboardData() {
        Map<String, Long> map = new HashMap<>();
        try{
            log.info("BankerController :: getDashboardData");
            map.put(ClaimStatus.ALL.name(), claimsDataRepository.count());
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatus(ClaimStatus.IN_PROGRESS));
            map.put(ClaimStatus.SETTLED.name(), claimsDataRepository.countByClaimStatus(ClaimStatus.SETTLED));
            return map;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.ALL.name(), 0L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.SETTLED.name(), 0L);
            return map;
        }
    }

    @Override
    public String submitClaims() {
        try{
            log.info("BankerController :: submitClaims");
            List<ClaimDraftData> claimDraftDatas = claimDraftDataRepository.findAll();
            List<ClaimsData> claimsDataList = new ArrayList<>();
            for (ClaimDraftData claimDraftData : claimDraftDatas){
                ClaimsData claimsData = modelMapper.map(claimDraftData, ClaimsData.class);
                claimsData.setClaimStatus(ClaimStatus.CLAIM_SUBMITTED);
                claimsData.setSubmittedBy(GenericUtils.getLoggedInUser().getUserId());
                claimsData.setSubmittedAt(System.currentTimeMillis());
                claimsDataList.add(claimsData);
            }
            if(!claimsDataList.isEmpty()) {
                claimsDataRepository.saveAll(claimsDataList);
                claimDraftDataRepository.deleteAll();
                return ResponseMessgae.success;
            }
            return ResponseMessgae.invalidClaimData;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: submitClaims e{}", e);
            return ResponseMessgae.backText;
        }
    }

    @Override
    public String discardClaims() {
        try{
            log.info("BankerController :: discardClaims");
            claimDraftDataRepository.deleteAll();
            return ResponseMessgae.success;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: discardClaims e{}", e);
            return ResponseMessgae.backText;
        }
    }

    @Override
    public ClaimsData getClaimData(Long claimId) {
        try{
            log.info("BankerController :: getClaimData");
            Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(claimId);
            return optionalClaimsData.isPresent() ? optionalClaimsData.get() : null;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimData e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> uploadDocument(ClaimsData claimsData, MultipartFile[] multipartFiles, DocType docType) {
        Map<String, Object> map = new HashMap<>();
        try{
            log.info("BankerServiceImpl :: uploadDocument claimsData {}, multipartFiles {}, docType {}", claimsData, multipartFiles, docType);
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setDocType(docType.getValue());
            claimDocuments.setBankerId(GenericUtils.getLoggedInUser().getUserId());
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for(MultipartFile multipartFile : multipartFiles){
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(multipartFile));
                if(Objects.isNull(urls.getDocUrl())){
                    map.put("message", ResponseMessgae.fileNotuploaded);
                    return map;
                }
                documentUrls.add(urls);
            }
            documentUrlsRepository.saveAll(documentUrls);
            claimDocuments.setDocumentUrls(documentUrls);
            claimDocuments.setUploadTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments);
            claimDocuments.setClaimsData(null);
            map.put("message", ResponseMessgae.success);
            map.put("claimDocuments", claimDocuments);
            return map;
        }catch (Exception e){
            log.error("EXCEPTION WHILE BankerServiceImpl :: uploadDocument e{}", e);
            map.put("message", e.getMessage());
            map.put("claimDocuments", null);
            return map;
        }
    }


    public Map<String, Object> convertExcelToListOfClaimsData(InputStream is, String bankerId) {
        Map<String, Object> map = new HashMap<>();
        List<ClaimDraftData> list = new ArrayList<>();
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

                ClaimDraftData p = new ClaimDraftData();

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
                                p.setClaimInwardDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
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
                            p.setBorrowerState(cell.getStringCellValue());
                            break;
                        case 7:
                            cell.setCellType(CellType.STRING);
                            p.setLoanAccountNumber(cell.getStringCellValue());
                            break;
                        case 8:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerAddress(cell.getStringCellValue());
                            break;
                        case 9:
                            cell.setCellType(CellType.STRING);
                            p.setLoanType(cell.getStringCellValue());
                            break;
                        case 10:
                            if(Objects.nonNull(cell.getNumericCellValue())) {
                                p.setLoanAmount(cell.getNumericCellValue());
                            }
                            break;
                        case 11:
                            cell.setCellType(CellType.STRING);
                            p.setBranchCode(cell.getStringCellValue());
                            break;
                        case 12:
                            cell.setCellType(CellType.STRING);
                            p.setBranchName(cell.getStringCellValue());
                            break;
                        case 13:
                            cell.setCellType(CellType.STRING);
                            p.setBranchAddress(cell.getStringCellValue());
                            break;
                        case 14:
                            cell.setCellType(CellType.STRING);
                            if(Objects.nonNull(cell.getStringCellValue())) {
                                p.setBranchPinCode(cell.getStringCellValue());
                            }
                            break;
                        case 15:
                            p.setBranchState(cell.getStringCellValue());
                            break;
                        case 16:
                            cell.setCellType(CellType.STRING);
                            p.setLoanAccountManagerName(cell.getStringCellValue());
                            break;
                        case 17:
                            cell.setCellType(CellType.STRING);
                            p.setAccountManagerContactNumber(cell.getStringCellValue());
                            break;
                        case 18:
                            cell.setCellType(CellType.STRING);
                            p.setInsurerName(cell.getStringCellValue());
                            break;
                        case 19:
                            cell.setCellType(CellType.STRING);
                            p.setPolicyNumber(cell.getStringCellValue());
                            break;
                        case 20:
                            cell.setCellType(CellType.STRING);
                            p.setMasterPolNumber(cell.getStringCellValue());
                            break;
                        case 21:
                            if(Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                p.setPolicyStartDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
                            }
                            break;
                        case 22:
                            if(Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicyCoverageDuration((int) cell.getNumericCellValue());
                            }
                            break;
                        case 23:
                            if(Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicySumAssured((double) cell.getNumericCellValue());
                            }
                            break;
                        case 24:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeName(cell.getStringCellValue());
                            break;
                        case 25:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeRelationShip(cell.getStringCellValue());
                            break;
                        case 26:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeContactNumber(cell.getStringCellValue());
                            break;
                        case 27:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeEmailId(cell.getStringCellValue());
                            break;
                        case 28:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeAddress(cell.getStringCellValue());
                            break;

                        case 29:
                            //p.setClaimStatus(ClaimStatus.CLAIM_SUBMITTED);
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
