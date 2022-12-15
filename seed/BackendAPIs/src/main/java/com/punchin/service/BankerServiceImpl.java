package com.punchin.service;

import com.punchin.dto.BankerClaimDocumentationDTO;
import com.punchin.dto.ClaimDocumentsDTO;
import com.punchin.dto.DocumentUrlDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.*;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.enums.RoleEnum;
import com.punchin.repository.*;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ModelMapper;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BankerServiceImpl implements BankerService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommonUtilService commonService;

    @Autowired
    private ClaimsDataRepository claimsDataRepository;

    @Autowired
    private ClaimDraftDataRepository claimDraftDataRepository;

    @Autowired
    private DocumentUrlsRepository documentUrlsRepository;

    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;

    @Autowired
    private ClaimAllocatedRepository claimAllocatedRepository;

    @Autowired
    private AmazonClient amazonClient;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Map<String, Object> saveUploadExcelData(MultipartFile[] files) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        try {
            log.info("BankerServiceImpl :: saveUploadExcelData files{}", files);
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            for (MultipartFile file : files) {
                Map<String, Object> data = convertExcelToListOfClaimsData(file.getInputStream(), bankerId);
                List<ClaimDraftData> claimsData = (List<ClaimDraftData>) Arrays.asList(data.get("claimsData")).get(0);
                if (Objects.nonNull(claimsData)) {
                    claimsData = claimDraftDataRepository.saveAll(claimsData);
                    map.put("data", claimsData);
                    map.put("status", true);
                    map.put("message", MessageCode.success);
                    return map;
                }
                map.put("message", data.get("message"));
            }
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: saveUploadExcelData e{}", e);
            return map;
        }
    }

    @Override
    public PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        try {
            log.info("BankerServiceImpl :: getClaimsList dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Page page1 = Page.empty();
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findAllByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.DRAFT.equals(claimDataFilter)) {
                page1 = claimDraftDataRepository.findAllByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByIsForwardToVerifierAndPunchinBankerId(true, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                List<ClaimStatus> claimsStatus = new ArrayList<>();
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.IN_PROGRESS);
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifierAndPunchinBankerId(ClaimStatus.SETTLED,true,  GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifierAndPunchinBankerId(ClaimStatus.CLAIM_SUBMITTED, false,  GenericUtils.getLoggedInUser().getUserId(), pageable);
            }else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndPunchinBankerId(ClaimStatus.UNDER_VERIFICATION,GenericUtils.getLoggedInUser().getUserId(), pageable);
            }
            return commonService.convertPageToDTO(page1.getContent(), page1);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimsList e {}", e);
            return null;
        }
    }

    @Override
    public Map<String, Long> getDashboardData() {
        Map<String, Long> map = new HashMap<>();
        try {
            log.info("BankerController :: getDashboardData");
            map.put(ClaimStatus.ALL.name(), claimsDataRepository.countByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId()));
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.IN_PROGRESS);
            claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
            claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
            claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
            claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.SETTLED);
            map.put(ClaimStatus.SETTLED.name(), claimsDataRepository.countByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), claimsDataRepository.countByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId()));
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.ALL.name(), 0L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.SETTLED.name(), 0L);
            return map;
        }
    }

    @Override
    public String submitClaims() {
        try {
            log.info("BankerController :: submitClaims");
            List<ClaimDraftData> claimDraftDatas = claimDraftDataRepository.findAllByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId());
            List<ClaimsData> claimsDataList = new ArrayList<>();
            for (ClaimDraftData claimDraftData : claimDraftDatas) {
                ClaimsData claimsData = modelMapper.map(claimDraftData, ClaimsData.class);
                claimsData.setPunchinClaimId("P" + RandomStringUtils.randomNumeric(10));
                claimsData.setClaimInwardDate(new Date());
                claimsData.setClaimStatus(ClaimStatus.CLAIM_SUBMITTED);
                claimsData.setSubmittedBy(GenericUtils.getLoggedInUser().getUserId());
                claimsData.setSubmittedAt(System.currentTimeMillis());
                claimsDataList.add(claimsData);
            }
            claimsDataRepository.saveAll(claimsDataList);
            claimDraftDataRepository.deleteByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId());
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: submitClaims e{}", e);
            return MessageCode.backText;
        }
    }

    @Override
    public String discardClaims() {
        try {
            log.info("BankerController :: discardClaims");
            claimDraftDataRepository.deleteByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId());
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: discardClaims e{}", e);
            return MessageCode.backText;
        }
    }

    @Override
    public BankerClaimDocumentationDTO getClaimDataForBankerAction(Long claimId) {
        try {
            log.info("BankerController :: getClaimData claimId {}", claimId);
            ClaimsData claimsData = claimsDataRepository.findByIdAndPunchinBankerId(claimId, GenericUtils.getLoggedInUser().getUserId());
            if(Objects.nonNull(claimsData)){
                BankerClaimDocumentationDTO dto = new BankerClaimDocumentationDTO();
                dto.setId(claimsData.getId());
                dto.setPunchinClaimId(claimsData.getPunchinClaimId());
                dto.setBorrowerName(claimsData.getBorrowerName());
                dto.setBorrowerAddress(claimsData.getBorrowerAddress());
                dto.setLoanType(claimsData.getLoanType());
                dto.setLoanAccountNumber(claimsData.getLoanAccountNumber());
                dto.setInsurerName(claimsData.getInsurerName());
                dto.setMasterPolicyNumbet(claimsData.getMasterPolNumber());
                dto.setBorrowerPolicyNumber(claimsData.getPolicyNumber());
                dto.setPolicySumAssured(claimsData.getPolicySumAssured());
                dto.setLoanAmount(claimsData.getLoanAmount());
                dto.setLoanAmountPaidByBorrower(0.0D);
                dto.setOutstandingLoanAmount(0.0D);
                dto.setBalanceClaimAmount(0.0D);
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideBy(claimsData.getId(), "banker");
                List<ClaimDocumentsDTO> claimDocumentsDTOS = new ArrayList<>();
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    ClaimDocumentsDTO claimDocumentsDTO = new ClaimDocumentsDTO();
                    claimDocumentsDTO.setId(claimDocuments.getId());
                    claimDocumentsDTO.setAgentDocType(claimDocuments.getAgentDocType());
                    claimDocumentsDTO.setDocType(claimDocuments.getDocType());
                    claimDocumentsDTO.setIsVerified(claimDocuments.getIsVerified());
                    claimDocumentsDTO.setIsApproved(claimDocuments.getIsApproved());
                    List<DocumentUrls> documentUrlsList = documentUrlsRepository.findDocumentUrlsByClaimDocumentId(claimDocuments.getId());
                    List<DocumentUrlDTO> documentUrlDTOS = new ArrayList<>();
                    for (DocumentUrls documentUrls : documentUrlsList) {
                        DocumentUrlDTO documentUrlListDTO = new DocumentUrlDTO();
                        documentUrlListDTO.setDocUrl(documentUrls.getDocUrl());
                        documentUrlListDTO.setDocFormat(FilenameUtils.getExtension(documentUrls.getDocUrl()));
                        documentUrlDTOS.add(documentUrlListDTO);
                    }
                    claimDocumentsDTO.setDocumentUrlDTOS(documentUrlDTOS);
                    claimDocumentsDTOS.add(claimDocumentsDTO);
                }
                dto.setClaimDocumentsDTOS(claimDocumentsDTOS);
                return dto;
            }
            return null;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimData ", e);
            return null;
        }
    }

    @Override
    public ClaimsData getClaimData(Long claimId) {
        try {
            log.info("BankerController :: getClaimData claimId {}", claimId);
            return claimsDataRepository.findByIdAndPunchinBankerId(claimId, GenericUtils.getLoggedInUser().getUserId());
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimData ", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> uploadDocument(ClaimsData claimsData, MultipartFile[] multipartFiles, BankerDocType docType) {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("BankerServiceImpl :: uploadDocument claimsData {}, multipartFiles {}, docType {}", claimsData, multipartFiles, docType);
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setIsActive(false);
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setDocType(docType.getValue());
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("banker");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(claimsData.getPunchinClaimId(), multipartFile));
                if(Objects.isNull(urls.getDocUrl())){
                    map.put("message", MessageCode.fileNotUploaded);
                    return map;
                }
                documentUrls.add(urls);
            }
            documentUrlsRepository.saveAll(documentUrls);
            claimDocuments.setDocumentUrls(documentUrls);
            claimDocuments.setUploadTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments);
            //claimDocuments.setClaimsData(null);
            //claimsData.setIsForwardToVerifier(true);
            claimsDataRepository.save(claimsData);
            map.put("message", MessageCode.success);
            map.put("claimDocuments", claimDocuments);
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: uploadDocument ", e);
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
            XSSFSheet sheet = workbook.getSheetAt(0);
            if (Objects.isNull(sheet)) {
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
                if (exit) {
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
                            if (Objects.isNull(cell) || cell.equals("") || cell.getStringCellValue() == "") {
                                exit = true;
                                break;
                            }
                            p.setPunchinBankerId(bankerId);
                            break;
                        case 1:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerName(cell.getStringCellValue());
                            break;
                        case 2:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerAddress(cell.getStringCellValue());
                            break;
                        case 3:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerCity(cell.getStringCellValue());
                            break;
                        case 4:
                            cell.setCellType(CellType.STRING);
                            if (Objects.nonNull(cell.getStringCellValue())) {
                                p.setBorrowerPinCode(cell.getStringCellValue());
                            }
                            break;
                        case 5:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerState(cell.getStringCellValue());
                            break;
                        case 6:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerContactNumber(cell.getStringCellValue());
                            break;
                        case 7:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerEmailId(cell.getStringCellValue());
                            break;
                        case 8:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerAlternateContactNumber(cell.getStringCellValue());
                            break;
                        case 9:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerAlternateContactDetails(cell.getStringCellValue());
                            break;
                        case 10:
                            cell.setCellType(CellType.STRING);
                            p.setLoanAccountNumber(cell.getStringCellValue());
                            break;
                        case 11:
                            cell.setCellType(CellType.STRING);
                            p.setLoanType(cell.getStringCellValue());
                            break;
                        case 12:
                            if (Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                p.setLoanDisbursalDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
                            }
                            break;
                        case 13:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setLoanAmount((double) cell.getNumericCellValue());
                            }
                            break;
                        case 14:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setLoanOutstandingAmount((double) cell.getNumericCellValue());
                            }
                            break;
                        case 15:
                            cell.setCellType(CellType.STRING);
                            p.setBranchCode(cell.getStringCellValue());
                            break;
                        case 16:
                            cell.setCellType(CellType.STRING);
                            p.setBranchAddress(cell.getStringCellValue());
                            break;
                        case 17:
                            cell.setCellType(CellType.STRING);
                            p.setBranchCity(cell.getStringCellValue());
                            break;
                        case 18:
                            cell.setCellType(CellType.STRING);
                            p.setBranchPinCode(cell.getStringCellValue());
                            break;
                        case 19:
                            cell.setCellType(CellType.STRING);
                            p.setBranchState(cell.getStringCellValue());
                            break;
                        case 20:
                            cell.setCellType(CellType.STRING);
                            p.setLoanAccountManagerName(cell.getStringCellValue());
                            break;
                        case 21:
                            cell.setCellType(CellType.STRING);
                            p.setAccountManagerContactNumber(cell.getStringCellValue());
                            break;
                        case 22:
                            cell.setCellType(CellType.STRING);
                            p.setInsurerName(cell.getStringCellValue());
                            break;
                        case 23:
                            cell.setCellType(CellType.STRING);
                            p.setPolicyNumber(cell.getStringCellValue());
                            break;
                        case 24:
                            cell.setCellType(CellType.STRING);
                            p.setMasterPolNumber(cell.getStringCellValue());
                            break;
                        case 25:
                            if (Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                p.setPolicyStartDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
                            }
                            break;
                        case 26:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicyCoverageDuration((int) cell.getNumericCellValue());
                            }
                            break;
                        case 27:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicySumAssured((double) cell.getNumericCellValue());
                            }
                            break;
                        case 28:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeName(cell.getStringCellValue());
                            break;
                        case 29:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeRelationShip(cell.getStringCellValue());
                            break;
                        case 30:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeContactNumber(cell.getStringCellValue());
                            break;
                        case 31:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeEmailId(cell.getStringCellValue());
                            break;
                        case 32:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeAddress(cell.getStringCellValue());
                            break;
                        default:
                            break;
                    }
                    cid++;
                }
                if (!exit) {
                    list.add(p);
                }
            }
            map.put("claimsData", list);
            if (list.isEmpty()) {
                map.put("message", "data.not.found");
            }
            return map;
        } catch (IllegalStateException e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: convertExcelToListOfClaimsData ", e);
            map.put("message", "invalid.column.type");
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: convertExcelToListOfClaimsData ", e);
            map.put("message", MessageCode.invalidFormat);
            return map;
        }
    }


    @Override
    public ByteArrayInputStream downloadMISFile() {
        try {
            String filename = "/home/tarun/Documents/Projects/Punchin/punchin-backend/seed/BackendAPIs/logs/Claim_Data_Format.xlsx";
            log.info("BankerController :: getAllClaimsData dataFilter{}");
            List<ClaimsData> claimsDataList =  claimsDataRepository.findAll();
            final String[] HEADERs = { "S.No", "Borrower Name", "Borrower Address", "Borrower City", "Borrower Pincode", "Borrower State", "Borrower Contact Number", "Borrower Email id",
                    "Borrower Alternate Contact Number", "Borrower Alternate Contact Details", "Loan Account Number", "Loan Category/Type", "Loan Disbursal Date", "Loan Disbursal Amount",
            "Lender Branch Code", "Lender Branch Address", "Lender Branch City", "Lender Branch Pin code", "Lender Branch State", "Lenders Contact Name", "Lender Contact Number",
            "Insurer Name", "Borrower Policy Number", "Master Policy Number", "Policy Start Date", "Policy Tenure", "Policy Sum Assured", "Nominee Name", "Nominee Relationship",
            "Nominee Contact Number", "Nominee Email id", "Nominee Address"};
            try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream();) {
                Sheet sheet = workbook.createSheet("Sheet1");
                FileOutputStream fileOut = new FileOutputStream(filename);
                // Header
                Row headerRow = sheet.createRow(0);

                /*CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                headerStyle.setWrapText(true);
                XSSFFont font = ((XSSFWorkbook) workbook).createFont();
                font.setFontName("Arial");
                font.setFontHeightInPoints((short) 13);
                font.setBold(true);
                headerStyle.setFont(font);*/

                for (int col = 0; col < HEADERs.length; col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(HEADERs[col]);
                    /*cell.setCellStyle(headerStyle);*/
                }
                CellStyle style = workbook.createCellStyle();
                CreationHelper createHelper = workbook.getCreationHelper();
                SimpleDateFormat dateOnly = new SimpleDateFormat("MM/dd/yyyy");
                int rowIdx = 1;
                for (ClaimsData claimsData : claimsDataList) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(claimsData.getId());
                    row.createCell(1).setCellValue(claimsData.getBorrowerName());
                    row.createCell(2).setCellValue(claimsData.getBorrowerAddress());
                    row.createCell(3).setCellValue(claimsData.getBorrowerCity());
                    row.createCell(4).setCellValue(claimsData.getBorrowerPinCode());
                    row.createCell(5).setCellValue(claimsData.getBorrowerState());
                    row.createCell(6).setCellValue(claimsData.getBorrowerContactNumber());
                    row.createCell(7).setCellValue(claimsData.getBorrowerEmailId());
                    row.createCell(8).setCellValue(claimsData.getBorrowerAlternateContactNumber());
                    row.createCell(9).setCellValue(claimsData.getBorrowerAlternateContactDetails());
                    row.createCell(10).setCellValue(claimsData.getLoanAccountNumber());
                    row.createCell(11).setCellValue(claimsData.getLoanType());
                    style.setDataFormat(createHelper.createDataFormat().getFormat("MM/dd/yyyy"));
                    row.createCell(12).setCellStyle(style);
                    row.createCell(12).setCellValue(dateOnly.format(claimsData.getLoanDisbursalDate()));
                    row.createCell(13).setCellValue(claimsData.getLoanAmount());
                    row.createCell(14).setCellValue(claimsData.getLoanOutstandingAmount());
                    row.createCell(15).setCellValue(claimsData.getBranchCode());
                    row.createCell(16).setCellValue(claimsData.getBranchAddress());
                    row.createCell(17).setCellValue(claimsData.getBranchCity());
                    row.createCell(18).setCellValue(claimsData.getBranchPinCode());
                    row.createCell(19).setCellValue(claimsData.getBranchState());
                    row.createCell(20).setCellValue(claimsData.getLoanAccountManagerName());
                    row.createCell(21).setCellValue(claimsData.getAccountManagerContactNumber());
                    row.createCell(22).setCellValue(claimsData.getInsurerName());
                    row.createCell(23).setCellValue(claimsData.getPolicyNumber());
                    row.createCell(24).setCellValue(claimsData.getMasterPolNumber());
                    row.createCell(25).setCellStyle(style);
                    row.createCell(25).setCellValue(dateOnly.format(claimsData.getPolicyStartDate()));
                    row.createCell(26).setCellValue(claimsData.getPolicyCoverageDuration());
                    row.createCell(27).setCellValue(claimsData.getPolicySumAssured());
                    row.createCell(28).setCellValue(claimsData.getNomineeName());
                    row.createCell(29).setCellValue(claimsData.getNomineeRelationShip());
                    row.createCell(30).setCellValue(claimsData.getNomineeContactNumber());
                    row.createCell(31).setCellValue(claimsData.getNomineeEmailId());
                    row.createCell(32).setCellValue(claimsData.getNomineeAddress());

                }

                workbook.write(out);
                workbook.write(fileOut);
                out.writeTo(fileOut);
                return new ByteArrayInputStream(out.toByteArray());
            } catch (IOException e) {
                throw new RuntimeException("fail to import data to Excel file: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getAllClaimsData ", e);
            return null;
        }
    }

    @Override
    public String forwardToVerifier(ClaimsData claimsData) {
        try {
            log.info("BankerController :: forwardToVerifier");
            claimsData.setClaimStatus(ClaimStatus.AGENT_ALLOCATED);
            claimsData.setIsForwardToVerifier(true);
            claimsData.setAgentToVerifierTime(System.currentTimeMillis());
            claimsDataRepository.save(claimsData);
            User user = userRepository.findByRoleAndStateIgnoreCase(RoleEnum.AGENT, claimsData.getBorrowerState());
            if(Objects.nonNull(user)){
                ClaimAllocated claimAllocated = new ClaimAllocated();
                claimAllocated.setUser(user);
                claimAllocated.setClaimsData(claimsData);
                claimAllocatedRepository.save(claimAllocated);
            }
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: forwardToVerifier e{}", e);
            return MessageCode.backText;
        }
    }

    @Override
    public boolean isBanker() {
        return userRepository.existsByIdAndRole(GenericUtils.getLoggedInUser().getId(), RoleEnum.BANKER);
    }

    @Override
    public ClaimsData isClaimByBanker(Long claimId) {
        return claimsDataRepository.findByIdAndPunchinBankerId(claimId,GenericUtils.getLoggedInUser().getUserId());
    }

    @Override
    public ClaimDocuments getClaimDocuments(Long docId) {
        try {
            log.info("BankerController :: getClaimDocuments docId {}", docId);
            Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(docId);
            return optionalClaimDocuments.isPresent() ? optionalClaimDocuments.get() : null;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimDocuments ", e);
            return null;
        }
    }

    @Override
    public String deleteBankDocument(ClaimDocuments claimDocuments) {
        try {
            log.info("BankerController :: deleteBankDocument claimDocuments {}", claimDocuments);
            claimDocumentsRepository.delete(claimDocuments);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: deleteBankDocument ", e);
            return MessageCode.backText;
        }
    }

    @Override
    public String saveASDraftDocument(ClaimsData claimsData) {
        try {
            log.info("BankerController :: saveASDraftDocument claimsData {}", claimsData);
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataId(claimsData.getId());
            claimDocumentsList.forEach(claimDocuments -> {claimDocuments.setIsActive(true);});
            claimDocumentsRepository.saveAll(claimDocumentsList);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: saveASDraftDocument ", e);
            return MessageCode.backText;
        }
    }
}
