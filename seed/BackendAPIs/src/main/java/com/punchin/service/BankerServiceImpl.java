package com.punchin.service;

import com.punchin.dto.BankerClaimDocumentationDTO;
import com.punchin.dto.ClaimDocumentsDTO;
import com.punchin.dto.DocumentUrlDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.*;
import com.punchin.enums.*;
import com.punchin.repository.*;
import com.punchin.utility.CSVHelper;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ModelMapper;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@Transactional
public class BankerServiceImpl implements BankerService {
    @Value("${data.downloads.folder.url}")
    String downloadFolderPath;
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
    private AmazonS3FileManagers amazonS3FileManagers;
    @Autowired
    private AmazonClient amazonClient;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CommonUtilService commonUtilService;

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
    public PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit, String searchedKeyword, SearchCaseEnum searchCaseEnum) {
        try {
            log.info("BankerServiceImpl :: getClaimsList dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Long bankerId = GenericUtils.getLoggedInUser().getId();
            Page page1 = Page.empty();
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                if (Objects.nonNull(searchCaseEnum) && Objects.nonNull(searchedKeyword)) {
                    if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                        page1 = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId1(searchedKeyword, bankerId, pageable);
                    } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                        page1 = claimsDataRepository.findAllBankerClaimSearchedDataByClaimDataId2(searchedKeyword, bankerId, pageable);
                    } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                        page1 = claimsDataRepository.findAllBankerClaimSearchedDataByClaimDataId3(searchedKeyword, bankerId, pageable);
                    }
                } else
                    page1 = claimsDataRepository.findAllByPunchinBankerIdOrderByCreatedAtDesc(GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.DRAFT.equals(claimDataFilter)) {
                page1 = claimDraftDataRepository.findAllByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.IN_PROGRESS);
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.SETTLED);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
                page1 = claimsDataRepository.findByClaimStatusInOrClaimBankerStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus,claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
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
            claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
            claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
            claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.SETTLED);
            claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
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
                claimsData.setClaimStatus(ClaimStatus.CLAIM_INTIMATED);
                claimsData.setBankerId(GenericUtils.getLoggedInUser().getId());
                claimsData.setUploadDate(new Date());
                User agent = userRepository.findByAgentAndState(RoleEnum.AGENT.name(), claimsData.getBorrowerState().toLowerCase());
                if (Objects.nonNull(agent)) {
                    claimsData.setAgentId(agent.getId());
                }
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
            if (Objects.nonNull(claimsData)) {
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
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActive(claimsData.getId(), "banker", true);
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
                //For delete unsaved document
                List<ClaimDocuments> claimDocuments = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActive(claimsData.getId(), "banker", false);
                claimDocumentsRepository.deleteAll(claimDocuments);
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
            claimDocuments.setAgentDocType(AgentDocType.valueOf(docType.getValue()));
            claimDocuments.setDocType(docType.getValue());
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("banker");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(claimsData.getPunchinClaimId(), multipartFile, "banker"));
                if (Objects.isNull(urls.getDocUrl())) {
                    map.put("message", MessageCode.fileNotUploaded);
                    return map;
                }
                documentUrls.add(urls);
            }
            documentUrlsRepository.saveAll(documentUrls);
            claimDocuments.setDocumentUrls(documentUrls);
            claimDocuments.setUploadTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments);
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
                            //  cell.setCellType(CellType.STRING);
                            p.setLoanAccountNumber(cell.getStringCellValue());
                            break;
                        case 11:
                            cell.setCellType(CellType.STRING);
                            p.setLoanType(cell.getStringCellValue());
                            break;
                        case 12:
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                if (Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                    p.setLoanDisbursalDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
                                }
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                p.setLoanDisbursalDate(new Date(cell.getStringCellValue()));
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
                            if (Objects.nonNull(cell.getStringCellValue())) {
                                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(cell.getStringCellValue());
                                p.setPolicyStartDate(date1);
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
    public ByteArrayInputStream downloadMISFile(ClaimStatus claimStatus) {
        try {
            String filename = "/home/prince/D/Claim_Data_Format.xlsx";
            log.info("BankerController :: getAllClaimsData dataFilter{}");
            List<ClaimsData> claimsDataList = claimsDataRepository.findByClaimStatus(claimStatus);
            final String[] HEADERs = {"S.No", "Borrower Name", "Borrower Address", "Borrower City", "Borrower Pincode", "Borrower State", "Borrower Contact Number", "Borrower EmailId",
                    "Borrower Alternate Contact Number", "Borrower Alternate Contact Details", "Loan Account Number", "Loan Category/Type", "Loan Disbursal Date", "Loan Disbursal Amount",
                    "Lender Branch Code", "Lender Branch Address", "Lender Branch City", "Lender Branch Pin code", "Lender Branch State", "Lenders Contact Name", "Lender Contact Number",
                    "Insurer Name", "Borrower Policy Number", "Master Policy Number", "Policy StartDate", "Policy Tenure", "Policy SumAssured", "Nominee Name", "Nominee Relationship",
                    "Nominee Contact Number", "Nominee EmailId", "Nominee Address"};
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
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideBy(claimsData.getId(), "banker");
            if (claimDocumentsList.isEmpty()) {
                return MessageCode.UPLOAD_BANKER_DOCUMENT;
            }
            for (ClaimDocuments claimDocuments : claimDocumentsList) {
                claimDocuments.setIsActive(true);
            }
            claimsData.setClaimBankerStatus(ClaimStatus.CLAIM_SUBMITTED);
            claimsData.setSubmittedAt(System.currentTimeMillis());
            claimsData.setSubmittedBy(GenericUtils.getLoggedInUser().getId());
            claimsDataRepository.save(claimsData);
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
        return claimsDataRepository.findByIdAndPunchinBankerId(claimId, GenericUtils.getLoggedInUser().getUserId());
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
            claimDocumentsList.forEach(claimDocuments -> {
                claimDocuments.setIsActive(true);
            });
            claimDocumentsRepository.saveAll(claimDocumentsList);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: saveASDraftDocument ", e);
            return MessageCode.backText;
        }
    }

    @Override
    public String downloadMISReport(ClaimDataFilter claimDataFilter) {
        try {
            log.info("BankerController :: downloadMISReport claimDataFilter {}", claimDataFilter);
            List<ClaimsData> claimsDataList = new ArrayList<>();
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                claimsDataList = claimsDataRepository.findAllByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId());
            } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId());
            } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId());
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.IN_PROGRESS);
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId());
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId());
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.SETTLED);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
                claimsDataList = claimsDataRepository.findByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId());
            }
            return amazonClient.uploadFile(generateMisExcelReport(claimsDataList));
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: downloadMISReport ", e);
            return null;
        }
    }

    private File generateMisExcelReport(List<ClaimsData> claimsDataList) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
            log.info("System path : path {}" + System.getProperty("user.dir"));
            log.info("downloadFolderPath : path {}" + downloadFolderPath);
            //String filename = "/home/tarun/Documents/Projects/Punchin/punchin-backend/seed/BackendAPIs/downloads/Claim_MIS_" + format.format(new Date()) + ".xlsx";
            String filename = "/Claim_MIS_" + format.format(new Date()) + ".xlsx";
            //downloadFolderPath = System.getProperty("user.dir");
            File file = new File(downloadFolderPath);
            file.mkdirs();
            //File file = new File(filename);
            log.info("file location path {}", file.getAbsolutePath());
            final String[] HEADERs = {"S.No", "PunchIn Ref Id", "Case Inward date ", "Borrower Name", "Borrower Address", "Borrower City", "Borrower Pin Code", "Borrower State", "Borrower Contact Number", "Borrower Email id",
                    "Alternate Mobile No.", "Alternate Contact Details", "Loan Account Number", "Loan Category/Type", "Loan Disbursal Date", "Loan Disbursal Amount", "Loan O/S Amount",
                    "Lender Branch Code", "Lender Branch Address", "Lender Branch City", "Lender Branch Pin code", "Lender Branch State", "Lenders Local Contact Name", "Lenders Local Contact Mobile No.",
                    "Insurer Name", "Borrower Policy Number", "Master Policy Number", "Policy Start Date", "Policy Tenure", "Policy Sum Assured", "Nominee Name", "Nominee Relationship",
                    "Nominee Contact Number", "Nominee Email id", "Nominee Address", "Claim Action", "Claim Status", "Claim Status Date", "Documents Pending"};
            try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fileOut = new FileOutputStream(file.getAbsolutePath() + filename, true)) {
                Sheet sheet = workbook.createSheet("Sheet1");
                // Header
                Row headerRow = sheet.createRow(0);
                HSSFWorkbook hwb = new HSSFWorkbook();
                HSSFPalette palette = hwb.getCustomPalette();
                HSSFColor headerBackgroundColor = palette.findSimilarColor(222, 234, 246);

                CellStyle headerStyle = workbook.createCellStyle();
                headerStyle.setFillForegroundColor(headerBackgroundColor.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                XSSFFont font = ((XSSFWorkbook) workbook).createFont();
                font.setFontName("Calibri");
                font.setFontHeightInPoints((short) 12);
                font.setBold(true);
                headerStyle.setFont(font);

                for (int col = 0; col < HEADERs.length; col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(HEADERs[col]);
                    cell.setCellStyle(headerStyle);
                }
                int rowIdx = 1;
                for (ClaimsData claimsData : claimsDataList) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(rowIdx);
                    row.createCell(1).setCellValue(claimsData.getPunchinBankerId());
                    row.createCell(2).setCellValue(format.format(claimsData.getClaimInwardDate()).toString());
                    row.createCell(3).setCellValue(claimsData.getBorrowerName());
                    row.createCell(4).setCellValue(claimsData.getBorrowerAddress());
                    row.createCell(5).setCellValue(claimsData.getBorrowerCity());
                    row.createCell(6).setCellValue(claimsData.getBorrowerPinCode());
                    row.createCell(7).setCellValue(claimsData.getBorrowerState());
                    row.createCell(8).setCellValue(claimsData.getBorrowerContactNumber());
                    row.createCell(9).setCellValue(claimsData.getBorrowerEmailId());
                    row.createCell(10).setCellValue(claimsData.getBorrowerAlternateContactNumber());
                    row.createCell(11).setCellValue(claimsData.getBorrowerAlternateContactDetails());
                    row.createCell(12).setCellValue(claimsData.getLoanAccountNumber());
                    row.createCell(13).setCellValue(claimsData.getLoanType());
                    row.createCell(14).setCellValue(format.format(claimsData.getLoanDisbursalDate()));
                    row.createCell(15).setCellValue(claimsData.getLoanAmount());
                    row.createCell(16).setCellValue(claimsData.getLoanOutstandingAmount());
                    row.createCell(17).setCellValue(claimsData.getBranchCode());
                    row.createCell(18).setCellValue(claimsData.getBranchAddress());
                    row.createCell(19).setCellValue(claimsData.getBranchCity());
                    row.createCell(20).setCellValue(claimsData.getBranchPinCode());
                    row.createCell(21).setCellValue(claimsData.getBranchState());
                    row.createCell(22).setCellValue(claimsData.getLoanAccountManagerName());
                    row.createCell(23).setCellValue(claimsData.getAccountManagerContactNumber());
                    row.createCell(24).setCellValue(claimsData.getInsurerName());
                    row.createCell(25).setCellValue(claimsData.getPolicyNumber());
                    row.createCell(26).setCellValue(claimsData.getMasterPolNumber());
                    row.createCell(27).setCellValue(format.format(claimsData.getPolicyStartDate()));
                    row.createCell(28).setCellValue(claimsData.getPolicyCoverageDuration());
                    row.createCell(29).setCellValue(claimsData.getPolicySumAssured());
                    row.createCell(30).setCellValue(claimsData.getNomineeName());
                    row.createCell(32).setCellValue(claimsData.getNomineeRelationShip());
                    row.createCell(33).setCellValue(claimsData.getNomineeContactNumber());
                    row.createCell(34).setCellValue(claimsData.getNomineeEmailId());
                    row.createCell(35).setCellValue(claimsData.getNomineeAddress());
                    row.createCell(36).setCellValue("OPEN");
                    row.createCell(37).setCellValue(claimsData.getClaimStatus().name());
                    row.createCell(38).setCellValue(format.format(new Date()));
                    row.createCell(39).setCellValue("");
                }
                /*for (int i = 0; i < 40; i++) {
                    sheet.autoSizeColumn(i);
                }*/
                workbook.write(fileOut);
                log.info("file exist file {}" + new File(downloadFolderPath + filename).exists());
                return new File(downloadFolderPath + filename);
            } catch (IOException e) {
                throw new RuntimeException("fail to export data to Excel file: " + e.getMessage());
            }
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: generateMisExcelReport ", e);
            return null;
        }
    }

    public boolean save(MultipartFile file) {
        try {
            List<ClaimsData> claimsDataList = CSVHelper.csvToClaimsData(file.getInputStream());
            if (!claimsDataList.isEmpty()) {
                claimsDataRepository.saveAll(claimsDataList);
                return true;
            }
        } catch (IOException e) {
            log.error("fail to store csv data: " + e.getMessage());
        }
        return false;
    }

    @Override
    public ResponseEntity<Object> saveUploadCSVData(MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        try {
            String message = "";
            if (CSVHelper.hasCSVFormat(file)) {
                try {
                    boolean save = save(file);
                    if (save) {
                        message = "Uploaded the file successfully: " + file.getOriginalFilename();
                        return ResponseHandler.response(message, MessageCode.success, true, HttpStatus.CREATED);
                    }
                    return ResponseHandler.response(message, MessageCode.success, true, HttpStatus.BAD_REQUEST);

                } catch (Exception e) {
                    message = "Could not upload the file: " + file.getOriginalFilename() + "!";
                    return ResponseHandler.response(message, MessageCode.success, true, HttpStatus.EXPECTATION_FAILED);
                }
            }
            message = "Please upload a csv file!";
            return ResponseHandler.response(message, MessageCode.backText, true, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: saveUploadCSVData ", e);
        }
        return ResponseHandler.response("", MessageCode.backText, true, HttpStatus.BAD_REQUEST);

    }

    @Override
    public List<Map<String, Object>> getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter) {
        log.info("Get Searched data request received for searchCaseEnum :{} , searchedKeyword :{} , pageNo :{} , limit :{} ", searchCaseEnum, searchedKeyword, pageNo, limit);
        String queryCondition = "";
        if (Objects.isNull(searchedKeyword))
            searchedKeyword = "";
        List<String> statusList = new ArrayList<>();
        if (claimDataFilter.ALLOCATED.equals(claimDataFilter) || Objects.isNull(claimDataFilter)) {
            ClaimStatus claimStatuses[] = ClaimStatus.values();
            for (ClaimStatus claimStatus : claimStatuses)
                statusList.add(claimStatus.toString());
        } else if (claimDataFilter.ACTION_PENDING.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.ACTION_PENDING.toString());
            statusList.add(ClaimStatus.AGENT_ALLOCATED.toString());
            statusList.add(ClaimStatus.CLAIM_INTIMATED.toString());
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
        } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.IN_PROGRESS.toString());
        } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.toString());
        } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.UNDER_VERIFICATION.toString());
        }
        if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
            queryCondition = getPunchInClaimId(searchedKeyword);
        } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
            queryCondition = getLoanAccountNo(searchedKeyword);
        } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
            queryCondition = getBorrowerName(searchedKeyword);
        }
        return getClaimDataFilter(searchedKeyword, statusList, pageNo, limit, queryCondition);
    }

    private String getPunchInClaimId(String search) {
        if (!search.equals(""))
            return "and cd.punchin_claim_id Ilike '%" + search + "%') ";
        return "";
    }

    private String getLoanAccountNo(String search) {
        if (!search.equals(""))
            return "and cd.loan_account_number Ilike '%" + search + "%') ";
        return "";
    }

    private String getBorrowerName(String search) {
        if (!search.equals(""))
            return "and (cd.borrower_name Ilike '%" + search + "%')";
        return "";
    }

    private List<Map<String, Object>> getClaimDataFilter(String search, List<String> claimStatus, Integer pageNo, Integer pageSize, String queryCondition) {
        String query = "select distinct cd.punchin_claim_id as punchinClaimId,cd.insurer_claim_id as insurerClaimId, " +
                " cd.punchin_banker_id as punchinBankerId,cd.claim_inward_date as claimInwardDate,cd.borrower_name as borrowerName, " +
                " cd.borrower_contact_number as borrowerContactNumber,cd.borrower_city as borrowerCity,cd.borrower_state as borrowerState, " +
                " cd.borrower_pin_code as borrowerPinCode, cd.borrower_email_id as borrowerEmailId,cd.borrower_alternate_contact_number as borrowerAlternateContactNumber, " +
                " cd.borrower_alternate_contact_details as borrowerAlternateContactDetails,cd.borrower_dob as borrowerDob, cd.loan_account_number as loanAccountNumber, " +
                " cd.borrower_address as borrowerAddress,cd.loan_type as loanType ,cd.loan_disbursal_date as loanDisbursalDate, " +
                " cd.loan_outstanding_amount as loanOutstandingAmount,cd.loan_amount as loanAmount, " +
                " cd.loan_amount_paid_by_borrower as loanAmountPaidByBorrower,cd.loan_amount_balance as loanAmountBalance, " +
                " cd.branch_code as branchCode,cd.branch_name as branchName,cd.branch_address as branchAddress,cd.branch_pin_code as branchPinCode, " +
                " cd.branch_city as branchCity,cd.branch_state as branchState,cd.loan_account_manager_name as loanAccountManagerName, " +
                " cd.account_manager_contact_number as accountManagerContactNumber,cd.insurer_name as insurerName,cd.master_pol_number as masterPolNumber, " +
                " cd.policy_number as policyNumber,cd.policy_start_date as policyStartDate,cd.policy_coverage_duration as policyCoverageDuration, " +
                " cd.policy_sum_assured as policySumAssured,cd.nominee_name as nomineeName,cd.nominee_relation_ship as nomineeRelationShip, " +
                " cd.nominee_contact_number as nomineeContactNumber,cd.nominee_email_id as nomineeEmailId,cd.nominee_address as nomineeAddress, cd.claim_status as claimStatus " +
                " from claims_data cd where cd.claim_status in (:claimStatus) " + queryCondition;
        Query q = entityManager.createNativeQuery(query);
        Query q1 = entityManager.createNativeQuery(query);
        q.setParameter("claimStatus", claimStatus);
        q1.setParameter("claimStatus", claimStatus);
        q.setMaxResults(pageSize);
        q.setFirstResult(pageNo * pageSize);
        List<Object[]> list = q.getResultList();
        List<Object[]> list1 = q1.getResultList();
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (Object[] row : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("claimId;", row[0]);
            map.put("claimDate;", row[3]);
            map.put("allocationDate;", row[3]);
            map.put("claimStatus", row[40]);
            map.put("punchinClaimId", row[0]);
            map.put("insurerClaimId", row[1]);
            map.put("punchinBankerId", row[2]);
            map.put("claimInwardDate", row[3]);
            map.put("borrowerName", row[4]);
            map.put("borrowerContactNumber", row[5]);
            map.put("borrowerCity", row[6]);
            map.put("borrowerState", row[7]);
            map.put("borrowerPinCode", row[8]);
            map.put("borrowerEmailId", row[9]);
            map.put("borrowerAlternateContactNumber", row[10]);
            map.put("borrowerAlternateContactDetails", row[11]);
            map.put("borrowerDob", row[12]);
            map.put("loanAccountNumber", row[13]);
            map.put("borrowerAddress", row[14]);
            map.put("loanType", row[15]);
            map.put("loanDisbursalDate", row[16]);
            map.put("loanOutstandingAmount", row[17]);
            map.put("loanAmount", row[18]);
            map.put("loanAmountPaidByBorrower", row[19]);
            map.put("loanAmountBalance", row[20]);
            map.put("branchCode", row[21]);
            map.put("branchName", row[22]);
            map.put("branchAddress", row[23]);
            map.put("branchPinCode", row[24]);
            map.put("branchCity", row[25]);
            map.put("branchState", row[26]);
            map.put("loanAccountManagerName", row[27]);
            map.put("accountManagerContactNumber", row[28]);
            map.put("insurerName", row[29]);
            map.put("masterPolNumber", row[30]);
            map.put("policyNumber", row[31]);
            map.put("policyStartDate", row[32]);
            map.put("policyCoverageDuration", row[33]);
            map.put("policySumAssured", row[34]);
            map.put("nomineeName", row[35]);
            map.put("nomineeRelationShip", row[36]);
            map.put("nomineeContactNumber", row[37]);
            map.put("nomineeEmailId", row[38]);
            map.put("nomineeAddress", row[39]);
            map.put("claimStatus", row[40]);
            map.put("count", list1.stream().count());
            mapList.add(map);
        }
        return mapList;
    }

    @Override
    public PageDTO getBankerClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize) {
        log.info("Get Searched data request received for caseType :{} , searchedKeyword :{}  ", searchCaseEnum, searchedKeyword);
        Long bankerId = GenericUtils.getLoggedInUser().getId();
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<ClaimsData> claimSearchedData = null;
        List<String> statusList = new ArrayList<>();
        if (claimDataFilter.ALL.equals(claimDataFilter)) {
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId1(searchedKeyword, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findAllBankerClaimSearchedDataByClaimDataId2(searchedKeyword, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findAllBankerClaimSearchedDataByClaimDataId3(searchedKeyword, bankerId, pageable);
            }
        } else if (claimDataFilter.DRAFT.equals(claimDataFilter)) {
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.CLAIM_INTIMATED.toString());
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.IN_PROGRESS.toString());
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
            statusList.add(ClaimStatus.CLAIM_INTIMATED.toString());
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.toString());
            statusList.add(ClaimStatus.AGENT_ALLOCATED.toString());
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.UNDER_VERIFICATION.toString());
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.SETTLED.toString());
            statusList.add(ClaimStatus.SUBMITTED_TO_INSURER.toString());
            if (searchCaseEnum.getValue().equalsIgnoreCase("Claim Id")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Loan Account Number")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.getValue().equalsIgnoreCase("Name")) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        }
        if (claimSearchedData == null || claimSearchedData.isEmpty()) {
            log.info("No claims data found");
            return null;
        }
        log.info("searched claim data fetched successfully");
        log.info("searched claim data fetched successfully");
        return commonService.convertPageToDTO(claimSearchedData);
    }

    @Override
    public boolean checkDocumentAlreadyExist(Long id, BankerDocType docType) {
        try {
            log.info("BankerServiceImpl :: checkDocumentAlreadyExist claimId - {}, docType - {}", id, docType);
            return claimDocumentsRepository.existsByClaimsDataIdAndUploadSideByAndAgentDocType(id, "banker", docType.getValue());
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: checkDocumentAlreadyExist e - {}" + e);
            return false;
        }
    }
}
