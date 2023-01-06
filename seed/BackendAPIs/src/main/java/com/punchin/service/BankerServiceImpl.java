package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.*;
import com.punchin.enums.*;
import com.punchin.repository.*;
import com.punchin.utility.*;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.ZoneId;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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
    private AmazonS3FileManagers amazonS3FileManagers;
    @Autowired
    private AmazonClient amazonClient;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CommonUtilService commonUtilService;
    @Autowired
    private ClaimHistoryRepository claimHistoryRepository;
    @Autowired
    private InvalidClaimsDataRepository invalidClaimsDataRepository;
    @Autowired
    private BankerVerifierRemarkRepository bankerVerifierRemarkRepository;

    @Override
    public Map<String, Object> saveUploadExcelData(MultipartFile[] files) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        try {
            log.info("BankerServiceImpl :: saveUploadExcelData files{}", files);
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            for (MultipartFile file : files) {
                Map<String, Object> data = convertExcelToListOfClaimsData(file.getInputStream(), GenericUtils.getLoggedInUser().getUserId());
                List<ClaimDraftData> claimsData = (List<ClaimDraftData>) Arrays.asList(data.get("claimsData")).get(0);
                List<ClaimDraftData> claimsDataList = new ArrayList<>();
                List<InvalidClaimsData> invalidClaimsDataList = new ArrayList<>();
                for (ClaimDraftData claimDraftData : claimsData) {
                    if (StringUtils.isNotBlank(claimDraftData.getBorrowerName()) && StringUtils.isNotBlank(claimDraftData.getBorrowerAddress()) && StringUtils.isNotBlank(claimDraftData.getBorrowerCity()) &&
                            StringUtils.isNotBlank(claimDraftData.getBorrowerPinCode()) && StringUtils.isNotBlank(claimDraftData.getBorrowerState()) && StringUtils.isNotBlank(claimDraftData.getBorrowerContactNumber()) &&
                            StringUtils.isNotBlank(claimDraftData.getLoanAccountNumber()) && claimDraftData.getLoanDisbursalDate() != null && claimDraftData.getLoanAmount() != null &&
                            StringUtils.isNotBlank(claimDraftData.getInsurerName()) && claimDraftData.getPolicySumAssured() != null && StringUtils.isNotBlank(claimDraftData.getNomineeName()) && StringUtils.isNotBlank(claimDraftData.getNomineeRelationShip())) {
                        List<Long> claimId = claimsDataRepository.findExistingLoanNumber(claimDraftData.getLoanAccountNumber());
                        if (claimId.isEmpty()) {
                            claimsDataList.add(claimDraftData);
                        } else {
                            claimDraftData.setValidClaimData(false);
                            claimsDataList.add(claimDraftData);
                            log.info("Loan number already exists :: {}", claimId);
                            InvalidClaimsData invalidClaimsData = ObjectMapperUtils.map(claimDraftData, InvalidClaimsData.class);
                            invalidClaimsData.setValidClaimData(false);
                            invalidClaimsData.setInvalidClaimDataReason("Loan number already exists");
                            invalidClaimsDataList.add(invalidClaimsData);

                        }
                    } else {
                        claimDraftData.setValidClaimData(false);
                        claimsDataList.add(claimDraftData);
                        log.info("Mandatory fields are missing :: {}", claimDraftData.getId());
                        InvalidClaimsData invalidClaimsData = ObjectMapperUtils.map(claimDraftData, InvalidClaimsData.class);
                        invalidClaimsData.setValidClaimData(false);
                        invalidClaimsData.setInvalidClaimDataReason("Mandatory fields are missing");
                        invalidClaimsDataList.add(invalidClaimsData);
                    }
                }
                if (!claimsData.isEmpty()) {
                    if (!invalidClaimsDataList.isEmpty()) {
                        log.info("Invalid claimsData Saved successfully");
                        invalidClaimsDataRepository.saveAll(invalidClaimsDataList);
                    }
                    claimsDataList = claimDraftDataRepository.saveAll(claimsDataList);
                    map.put("data", claimsDataList);
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
            Page<ClaimsData> page1 = Page.empty();
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                PageDTO pageDTO = new PageDTO();
                if (Objects.nonNull(searchCaseEnum) && Objects.nonNull(searchedKeyword)) {
                    if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                        page1 = claimsDataRepository.findAllBankerClaimSearchedDataByClaimDataId(searchedKeyword, bankerId, pageable);
                    } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                        page1 = claimsDataRepository.findAllBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, bankerId, pageable);
                    } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                        page1 = claimsDataRepository.findAllBankerClaimSearchedDataBySearchName(searchedKeyword, bankerId, pageable);
                    }
                    if (page1.isEmpty()) {
                        page1 = claimsDataRepository.findAllByPunchinBankerIdOrderByCreatedAtDesc(GenericUtils.getLoggedInUser().getUserId(), pageable);
                        List<BankerClaimListResponseDTO> bankerClaimListResponseDTOS = mappedAgentDetails(page1);
                        pageDTO = commonService.convertPageToDTO(bankerClaimListResponseDTOS, page1);
                        pageDTO.setMessage(MessageCode.CLAIM_NOT_FOUND);
                        return pageDTO;
                    }
                } else {
                    page1 = claimsDataRepository.findAllByPunchinBankerIdOrderByCreatedAtDesc(GenericUtils.getLoggedInUser().getUserId(), pageable);
                }
            } else if (claimDataFilter.DRAFT.equals(claimDataFilter)) {
                Page page2 = claimDraftDataRepository.findAllByPunchinBankerId(GenericUtils.getLoggedInUser().getUserId(), pageable);
                return commonService.convertPageToDTO(page2.getContent(), page2);
            } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                page1 = claimsDataRepository.findBySubmittedClaims(GenericUtils.getLoggedInUser().getUserId(), pageable);
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
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_LENDER);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
                page1 = claimsDataRepository.findByClaimStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
                claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
                page1 = claimsDataRepository.findByClaimStatusInOrClaimBankerStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, claimsStatus, GenericUtils.getLoggedInUser().getUserId(), pageable);
            } else if (claimDataFilter.BANKER_DRAFT.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusByDraftSavedByBanker(GenericUtils.getLoggedInUser().getUserId(), pageable);
            }
            List<BankerClaimListResponseDTO> bankerClaimListResponseDTOS = mappedAgentDetails(page1);
            return commonService.convertPageToDTO(bankerClaimListResponseDTOS, page1);
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
            claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
            claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatusInAndPunchinBankerId(claimsStatus, GenericUtils.getLoggedInUser().getUserId()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.SETTLED);
            claimsStatus.add(ClaimStatus.SUBMITTED_TO_LENDER);
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
                claimsData.setLenderName(GenericUtils.getLoggedInUser().getFirstName());
                claimsData.setClaimStatus(ClaimStatus.CLAIM_INTIMATED);
                claimsData.setBankerId(GenericUtils.getLoggedInUser().getId());
                claimsData.setUploadDate(new Date());
                claimsDataList.add(claimsData);
            }
            claimsDataList = claimsDataRepository.saveAll(claimsDataList);
            List<ClaimHistory> claimHistories = new ArrayList<>();
            for (ClaimsData claimsData : claimsDataList) {
                claimHistories.add(new ClaimHistory(claimsData.getId(), ClaimStatus.CLAIM_INTIMATED, "Claim Intimation"));
            }
            claimHistoryRepository.saveAll(claimHistories);
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
                dto.setOutstandingLoanAmount(claimsData.getLoanOutstandingAmount());
                dto.setBalanceClaimAmount(0d);
                dto.setLoanAmountPaidByBorrower(0d);
                if (Objects.nonNull(claimsData.getPolicySumAssured()) && Objects.nonNull(claimsData.getLoanOutstandingAmount())) {
                    double outStandingLoan = claimsData.getPolicySumAssured() - claimsData.getLoanOutstandingAmount();
                    dto.setLoanAmountPaidByBorrower(claimsData.getLoanAmount() - claimsData.getLoanOutstandingAmount());
                    if (outStandingLoan > 0) {
                        dto.setBalanceClaimAmount(outStandingLoan);
                    }
                }

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
                urls.setDocUrl(amazonS3FileManagers.uploadFile(claimsData.getPunchinClaimId(), multipartFile, "banker/"));
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
            DataFormatter formatter = new DataFormatter();
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
                            String borrowPinCode = formatter.formatCellValue(cell);
                            if (borrowPinCode.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                borrowPinCode = String.valueOf(cell.getStringCellValue());
                                p.setBorrowerPinCode(borrowPinCode);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setBorrowerPinCode(borrowPinCode);
                            }
                            break;
                        case 5:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerState(cell.getStringCellValue());
                            break;
                        case 6:
                            String borrowContactNumber = formatter.formatCellValue(cell);
                            if (borrowContactNumber.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                borrowContactNumber = String.valueOf(cell.getStringCellValue());
                                p.setBorrowerContactNumber(borrowContactNumber);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setBorrowerContactNumber(borrowContactNumber);
                            }
                            break;
                        case 7:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerEmailId(cell.getStringCellValue());
                            break;
                        case 8:
                            String borrowerAlternateNo = formatter.formatCellValue(cell);
                            if (borrowerAlternateNo.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                borrowerAlternateNo = String.valueOf(cell.getStringCellValue());
                                p.setBorrowerAlternateContactNumber(borrowerAlternateNo);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setBorrowerAlternateContactNumber(borrowerAlternateNo);
                            }
                            break;
                        case 9:
                            cell.setCellType(CellType.STRING);
                            p.setBorrowerAlternateContactDetails(cell.getStringCellValue());
                            break;
                        case 10:
                            String loanAccountNo = formatter.formatCellValue(cell);
                            if (loanAccountNo.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                loanAccountNo = String.valueOf(cell.getStringCellValue());
                                p.setLoanAccountNumber(loanAccountNo);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setLoanAccountNumber(loanAccountNo);
                            }
                            break;
                        case 11:
                            cell.setCellType(CellType.STRING);
                            p.setLoanType(cell.getStringCellValue());
                            break;
                        case 12:
                            cell.setCellType(CellType.STRING);
                            p.setCategory(cell.getStringCellValue().toLowerCase());
                            break;
                        case 13:
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                if (Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                    p.setLoanDisbursalDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
                                }
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                p.setLoanDisbursalDate(new Date(cell.getStringCellValue()));
                            }
                            break;
                        case 14:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setLoanAmount((double) cell.getNumericCellValue());
                            }
                            break;
                        case 15:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setLoanOutstandingAmount((double) cell.getNumericCellValue());
                            }
                            break;
                        case 16:
                            cell.setCellType(CellType.STRING);
                            p.setBranchCode(cell.getStringCellValue());
                            break;
                        case 17:
                            cell.setCellType(CellType.STRING);
                            p.setBranchAddress(cell.getStringCellValue());
                            break;
                        case 18:
                            cell.setCellType(CellType.STRING);
                            p.setBranchCity(cell.getStringCellValue());
                            break;
                        case 19:
                            cell.setCellType(CellType.STRING);
                            p.setBranchPinCode(cell.getStringCellValue());
                            break;
                        case 20:
                            cell.setCellType(CellType.STRING);
                            p.setBranchState(cell.getStringCellValue());
                            break;
                        case 21:
                            String loanAccMangName = formatter.formatCellValue(cell);
                            if (loanAccMangName.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                loanAccMangName = String.valueOf(cell.getStringCellValue());
                                p.setLoanAccountManagerName(loanAccMangName);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setLoanAccountManagerName(loanAccMangName);
                            }
                            break;
                        case 22:
                            String accMngContactNo = formatter.formatCellValue(cell);
                            if (accMngContactNo.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                accMngContactNo = String.valueOf(cell.getStringCellValue());
                                p.setAccountManagerContactNumber(accMngContactNo);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setAccountManagerContactNumber(accMngContactNo);
                            }
                            break;
                        case 23:
                            cell.setCellType(CellType.STRING);
                            p.setInsurerName(cell.getStringCellValue());
                            break;
                        case 24:
                            String policyNo = formatter.formatCellValue(cell);
                            if (policyNo.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                policyNo = String.valueOf(cell.getStringCellValue());
                                p.setPolicyNumber(policyNo);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setPolicyNumber(policyNo);
                            }
                            break;
                        case 25:
                            String masterPolicyNo = formatter.formatCellValue(cell);
                            if (masterPolicyNo.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                masterPolicyNo = String.valueOf(cell.getStringCellValue());
                                p.setMasterPolNumber(masterPolicyNo);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setMasterPolNumber(masterPolicyNo);
                            }
                            break;
                        case 26:
                            if (cell.getCellType().equals(CellType.NUMERIC)) {
                                if (Objects.nonNull(cell.getLocalDateTimeCellValue())) {
                                    p.setPolicyStartDate(Date.from(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()).toInstant()));
                                }
                            } else if (cell.getCellType().equals(CellType.STRING)) {
                                p.setPolicyStartDate(new Date(cell.getStringCellValue()));
                            }
                            /*if (Objects.nonNull(cell.getStringCellValue())) {
                                Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(cell.getStringCellValue());
                                p.setPolicyStartDate(date1);
                            }*/
                            break;
                        case 27:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicyCoverageDuration((int) cell.getNumericCellValue());
                            }
                            break;
                        case 28:
                            cell.setCellType(CellType.NUMERIC);
                            if (Objects.nonNull(cell.getNumericCellValue())) {
                                p.setPolicySumAssured((double) cell.getNumericCellValue());
                            }
                            break;
                        case 29:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeName(cell.getStringCellValue());
                            break;
                        case 30:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeRelationShip(cell.getStringCellValue());
                            break;
                        case 31:
                            String nomineeContactNumber = formatter.formatCellValue(cell);
                            if (nomineeContactNumber.contains("+")) {
                                cell.setCellType(CellType.STRING);
                                nomineeContactNumber = String.valueOf(cell.getStringCellValue());
                                p.setNomineeContactNumber(nomineeContactNumber);
                            } else {
                                cell.setCellType(CellType.STRING);
                                p.setNomineeContactNumber(nomineeContactNumber);
                            }
                            break;
                        case 32:
                            cell.setCellType(CellType.STRING);
                            p.setNomineeEmailId(cell.getStringCellValue());
                            break;
                        case 33:
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
            if (claimsData.getClaimStatus().equals(ClaimStatus.CLAIM_INTIMATED)) {
                claimsData.setClaimBankerStatus(ClaimStatus.CLAIM_SUBMITTED);
            }
            claimHistoryRepository.save(new ClaimHistory(claimsData.getId(), ClaimStatus.CLAIM_SUBMITTED, "Claim Submitted"));
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

    public List<ClaimDraftData> save(MultipartFile file) {
        try {
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            List<ClaimDraftData> claimsDataList = CSVHelper.csvToClaimsData(file.getInputStream(), bankerId);
            List<ClaimDraftData> claimsDraftDataList = new ArrayList<>();
            List<InvalidClaimsData> invalidClaimsDataList = new ArrayList<>();
            for (ClaimDraftData claimDraftData : claimsDataList) {
                if (StringUtils.isNotBlank(claimDraftData.getBorrowerName()) && StringUtils.isNotBlank(claimDraftData.getBorrowerAddress()) && StringUtils.isNotBlank(claimDraftData.getBorrowerCity()) &&
                        StringUtils.isNotBlank(claimDraftData.getBorrowerPinCode()) && StringUtils.isNotBlank(claimDraftData.getBorrowerState()) && StringUtils.isNotBlank(claimDraftData.getBorrowerContactNumber()) &&
                        StringUtils.isNotBlank(claimDraftData.getLoanAccountNumber()) && claimDraftData.getLoanDisbursalDate() != null && claimDraftData.getLoanAmount() != null &&
                        StringUtils.isNotBlank(claimDraftData.getInsurerName()) && claimDraftData.getPolicySumAssured() != null && StringUtils.isNotBlank(claimDraftData.getNomineeName()) && StringUtils.isNotBlank(claimDraftData.getNomineeRelationShip())) {
                    List<Long> claimId = claimsDataRepository.findExistingLoanNumber(claimDraftData.getLoanAccountNumber());
                    if (claimId.isEmpty()) {
                        claimsDraftDataList.add(claimDraftData);
                    } else {
                        claimDraftData.setValidClaimData(false);
                        claimsDraftDataList.add(claimDraftData);
                        log.info("Loan number already exists :: {}", claimId);
                        InvalidClaimsData invalidClaimsData = ObjectMapperUtils.map(claimDraftData, InvalidClaimsData.class);
                        invalidClaimsData.setValidClaimData(false);
                        invalidClaimsData.setInvalidClaimDataReason("Loan number already exists");
                        invalidClaimsDataList.add(invalidClaimsData);
                    }
                } else {
                    claimDraftData.setValidClaimData(false);
                    claimsDraftDataList.add(claimDraftData);
                    log.info("Mandatory fields are missing :: {}", claimDraftData);
                    InvalidClaimsData invalidClaimsData = ObjectMapperUtils.map(claimDraftData, InvalidClaimsData.class);
                    invalidClaimsData.setValidClaimData(false);
                    invalidClaimsData.setInvalidClaimDataReason("Mandatory fields are missing");
                    invalidClaimsDataList.add(invalidClaimsData);
                }
            }
            if (!claimsDataList.isEmpty()) {
                if (!invalidClaimsDataList.isEmpty()) {
                    log.info("Invalid claimsData Saved successfully");
                    invalidClaimsDataRepository.saveAll(invalidClaimsDataList);
                }
                return claimDraftDataRepository.saveAll(claimsDraftDataList);
            }
        } catch (IOException e) {
            log.error("fail to store csv data: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    @Override
    public ResponseEntity<Object> saveUploadCSVData(MultipartFile file) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", false);
        try {
            String message = "";
            if (CSVHelper.hasCSVFormat(file)) {
                try {
                    List<ClaimDraftData> csvFile = save(file);
                    if (!csvFile.isEmpty()) {
                        message = "Uploaded the file successfully: " + file.getOriginalFilename();
                        return ResponseHandler.response(csvFile, MessageCode.success, true, HttpStatus.CREATED);
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
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findAllBankerClaimSearchedDataByClaimDataId(searchedKeyword, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findAllBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findAllBankerClaimSearchedDataBySearchName(searchedKeyword, bankerId, pageable);
            }
        } else if (claimDataFilter.DRAFT.equals(claimDataFilter)) {
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.BANKER_ACTION_PENDING.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.CLAIM_INTIMATED.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.SUBMITTED.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.IN_PROGRESS.toString());
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
            statusList.add(ClaimStatus.CLAIM_INTIMATED.toString());
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.toString());
            statusList.add(ClaimStatus.AGENT_ALLOCATED.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.UNDER_VERIFICATION.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.SETTLED.toString());
            statusList.add(ClaimStatus.SUBMITTED_TO_LENDER.toString());
            statusList.add(ClaimStatus.SUBMITTED_TO_INSURER.name());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByClaimDataId(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, bankerId, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findBankerClaimSearchedDataBySearchName(searchedKeyword, statusList, bankerId, pageable);
            }
        }
        if (claimSearchedData == null || claimSearchedData.isEmpty()) {
            log.info("No claims data found");
            return null;
        }
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

    @Override
    public Map<String, Object> getClaimBankerDocuments(Long id) {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("AgentServiceImpl :: getClaimDocuments claimId {}", id);
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.getClaimDocumentWithDiscrepancyStatusAndBanker(id);
            List<ClaimDocumentsDTO> claimDocumentsDTOS = new ArrayList<>();
            List<String> rejectedDocList = new ArrayList<>();
            for (ClaimDocuments claimDocuments : claimDocumentsList) {
                ClaimDocumentsDTO claimDocumentsDTO = new ClaimDocumentsDTO();
                claimDocumentsDTO.setId(claimDocuments.getId());
                claimDocumentsDTO.setAgentDocType(claimDocuments.getAgentDocType());
                claimDocumentsDTO.setDocType(claimDocuments.getDocType());
                claimDocumentsDTO.setIsVerified(claimDocuments.getIsVerified());
                claimDocumentsDTO.setIsApproved(claimDocuments.getIsApproved());
                claimDocumentsDTO.setReason(claimDocuments.getReason());
                if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                    rejectedDocList.add(claimDocuments.getAgentDocType().name());
                }
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
            rejectedDocList.add(AgentDocType.OTHER.name());
            map.put("claimDocuments", claimDocumentsDTOS);
            map.put("rejectedDocList", rejectedDocList);
            map.put("message", MessageCode.success);
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimDocuments e {}", e);
            map.put("claimDocuments", null);
            map.put("rejectedDocList", null);
            map.put("message", e.getMessage());
            return map;
        }
    }

    @Override
    public boolean checkDocumentIsInDiscrepancy(Long id, String docType) {
        try {
            log.info("BankerServiceImpl :: checkDocumentIsInDiscrepancy");
            ClaimDocuments claimDocuments = claimDocumentsRepository.findFirstByClaimsDataIdAndAgentDocTypeAndUploadSideByAndIsVerifiedAndIsApprovedOrderByIdDesc(id, AgentDocType.valueOf(docType), "banker", true, false);
            return Objects.nonNull(claimDocuments) ? true : false;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: checkDocumentIsInDiscrepancy e{}", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> discrepancyDocumentUpload(Long claimId, MultipartFile[] multipartFiles, String docType) {
        log.info("BankerServiceImpl :: discrepancyDocumentUpload claimsData {}, multipartFiles {}, docType {}", claimId, multipartFiles, docType);
        Map<String, Object> map = new HashMap<>();
        try {
            String oldDocType = docType;
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndAgentDocType(claimId, AgentDocType.valueOf(docType));
            if (!claimDocumentsList.isEmpty()) {
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    claimDocuments.setIsActive(false);
                    oldDocType = claimDocuments.getDocType();
                }
                claimDocumentsRepository.saveAll(claimDocumentsList);
            }
            ClaimsData claimsData = claimsDataRepository.findById(claimId).get();
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setAgentDocType(AgentDocType.valueOf(docType));
            claimDocuments.setDocType(oldDocType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("banker");
            claimDocuments.setIsActive(true);
            claimDocuments.setIsDeleted(false);
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonS3FileManagers.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "banker/"));
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
            //inactive old rejected doc

            //claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
            //claimsDataRepository.save(claimsData);
            ClaimDocumentsDTO claimDocumentsDTO = new ClaimDocumentsDTO();
            claimDocumentsDTO.setId(claimDocuments.getId());
            claimDocumentsDTO.setAgentDocType(claimDocuments.getAgentDocType());
            claimDocumentsDTO.setDocType(claimDocuments.getDocType());
            claimDocumentsDTO.setIsVerified(claimDocuments.getIsVerified());
            claimDocumentsDTO.setIsApproved(claimDocuments.getIsApproved());
            List<DocumentUrlDTO> documentUrlDTOS = new ArrayList<>();
            for (DocumentUrls documentUrl : documentUrls) {
                DocumentUrlDTO documentUrlListDTO = new DocumentUrlDTO();
                documentUrlListDTO.setDocUrl(documentUrl.getDocUrl());
                documentUrlListDTO.setDocFormat(FilenameUtils.getExtension(documentUrl.getDocUrl()));
                documentUrlDTOS.add(documentUrlListDTO);
            }
            claimDocumentsDTO.setDocumentUrlDTOS(documentUrlDTOS);
            map.put("message", MessageCode.success);
            map.put("claimDocuments", claimDocumentsDTO);
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: discrepancyDocumentUpload e {} ", e);
            map.put("message", e.getMessage());
            map.put("claimDocuments", null);
            return map;
        }
    }

    @Override
    public boolean requestForAdditionalDocument(ClaimsData claimsData, List<AgentDocType> docTypes, String remark) {
        try {
            log.info("BankerServiceImpl :: requestForAdditionalDocument claimsData - {}, docTypes - {}, remark - {}", claimsData, docTypes, remark);
            List<ClaimDocuments> claimDocumentsList = new ArrayList<>();
            claimsData.setClaimStatus(ClaimStatus.NEW_REQUIREMENT);
            claimsDataRepository.save(claimsData);
            for (AgentDocType docType : docTypes) {
                ClaimDocuments documents = new ClaimDocuments();
                documents.setIsActive(false);
                documents.setIsDeleted(false);
                documents.setReason(remark);
                documents.setIsVerified(true);
                documents.setIsApproved(false);
                documents.setDocumentUrls(null);
                documents.setAgentDocType(docType);
                documents.setUploadSideBy("New Requirement");
                documents.setClaimsData(claimsData);
                claimDocumentsList.add(documents);
            }
            claimHistoryRepository.save(new ClaimHistory(claimsData.getId(), ClaimStatus.NEW_REQUIREMENT, "New Requirement"));
            claimDocumentsRepository.saveAll(claimDocumentsList);
            return true;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: requestForAdditionalDocument e{}", e);
            return false;
        }
    }

    @Override
    public String downloadAllDocuments(Long claimId) {
        try {
            String filePath = System.getProperty("user.dir") + "/BackendAPIs/downloads/";
            log.info("BankerServiceImpl :: downloadAllDocuments docId {}, Path {}", claimId, filePath);
            String punchinClaimId = claimsDataRepository.findPunchinClaimIdById(claimId);
            byte[] buffer = new byte[1024];
            File zipfile = new File(filePath + punchinClaimId + ".zip");
            FileOutputStream fileOutputStream = new FileOutputStream(zipfile);
            ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(claimId, "banker", true);
            for (ClaimDocuments claimDocuments : claimDocumentsList) {
                List<DocumentUrls> documentUrlsList = claimDocuments.getDocumentUrls();
                for (DocumentUrls documentUrls : documentUrlsList) {
                    InputStream inputStream = amazonS3FileManagers.getStreamFromS3(documentUrls.getDocUrl());
                    if (Objects.nonNull(inputStream)) {
                        ZipEntry zipEntry = new ZipEntry(FilenameUtils.getName(documentUrls.getDocUrl()));
                        zipOutputStream.putNextEntry(zipEntry);
                        writeStreamToZip(buffer, zipOutputStream, inputStream);
                        inputStream.close();
                    }
                }
            }
            zipOutputStream.closeEntry();
            zipOutputStream.close();
            String version = amazonS3FileManagers.uploadFileToAmazonS3("agent/", zipfile, punchinClaimId + ".zip");
//            ZipUtils appZip = new ZipUtils();
//            appZip.generateFileList(new File(filePath + claimId), filePath + claimId);
//            appZip.zipIt(filePath + punchinClaimId + ".zip", filePath + claimId);
//            new File(filePath + claimId).deleteOnExit();
//            File file = new File(filePath + punchinClaimId + ".zip");
//            String fileName = file.getName();
//            String version = amazonS3FileManagers.uploadFileToAmazonS3("banker/", new File(filePath + punchinClaimId + ".zip"), fileName);
            amazonS3FileManagers.cleanUp(zipfile);
            return version;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: downloadAllDocuments ", e);
            return null;
        }
    }

    private void writeStreamToZip(byte[] buffer, ZipOutputStream zipOutputStream,
                                  InputStream inputStream) {
        try {
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {

        }
    }

    @Override
    public Map<String, Object> getClaimHistory(Long id) {
        try {
            Map<String, Object> map = new HashMap<>();
            log.info("BankerServiceImpl :: getClaimHistory claimId - {}", id);
            List<ClaimHistoryDTO> claimHistoryDTOS = new ArrayList<>();
            ClaimsData claimsData = claimsDataRepository.findById(id).get();
            if (Objects.nonNull(claimsData)) {
                List<ClaimHistory> claimHistories = claimHistoryRepository.findByClaimIdOrderById(claimsData.getId());
                ClaimHistoryDTO oldClaimHistory = new ClaimHistoryDTO();
                for (ClaimHistory claimHistory : claimHistories) {
                    ClaimHistoryDTO claimHistoryDTO = modelMapper.map(claimHistory, ClaimHistoryDTO.class);
                    if (Objects.nonNull(oldClaimHistory) && !claimHistoryDTO.getClaimStatus().equals(oldClaimHistory.getClaimStatus())) {
                        claimHistoryDTOS.add(claimHistoryDTO);
                    }
                    oldClaimHistory = claimHistoryDTO;
                }
            }
            map.put("claimHistoryDTOS", claimHistoryDTOS);
            map.put("claimStatus", claimsData.getClaimStatus());
            map.put("startedAt", claimsData.getCreatedAt());
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimHistory e - {}", e);
            return Collections.EMPTY_MAP;
        }
    }

    @Override
    public Map<String, Object> getRemarkHistory(Long id) {
        try {
            Map<String, Object> map = new HashMap<>();
            log.info("BankerServiceImpl :: getRemarkHistory claimId - {}", id);
            List<ClaimsRemarksDTO> claimHistoryDTOS = new ArrayList<>();
            Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(id);
            Long lastRemarkTime = 0L;
            String claimStatus = null;
            if(optionalClaimsData.isPresent()) {
                ClaimsData claimsData = optionalClaimsData.get();
                claimStatus = claimsData.getClaimStatus().name();
                List<BankerVerifierRemark> bankerVerifierRemarks = bankerVerifierRemarkRepository.findByClaimIdOrderById(claimsData.getId());
                    for (BankerVerifierRemark bankerVerifierRemark : bankerVerifierRemarks) {
                        lastRemarkTime = bankerVerifierRemark.getCreatedAt();
                        claimHistoryDTOS.add(modelMapper.map(bankerVerifierRemark, ClaimsRemarksDTO.class));
                    }
            }
            map.put("claimRemarkDTOS", claimHistoryDTOS);
            map.put("claimStatus", claimStatus);
            map.put("lastRemarkTime", lastRemarkTime);
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getRemarkHistory e - {}", e);
            return Collections.EMPTY_MAP;
        }
    }

    private void downloadDocumentInDirectory(String docUrl, Long claimId, String filePath) {
        File file1 = new File(filePath + claimId);
        file1.mkdirs();
        log.info("Directory created");
        try (FileOutputStream fos = new FileOutputStream(file1.getAbsolutePath() + "/" + FilenameUtils.getName(docUrl), true);) {
            log.info("ready to download claim documents docUrl {}", docUrl);
            ByteArrayOutputStream byteArrayOutputStream = amazonS3FileManagers.downloadFile("agent/" + FilenameUtils.getName(docUrl));
            byteArrayOutputStream.writeTo(fos);
            log.info("File downloaded");
            byteArrayOutputStream.close();
        } catch (Exception e) {
            log.error("ERROR WHILE DOWNLOADING FILE FROM URL e {}", e);
        }
    }

    private Map<String, Object> convertInDocumentStatusDTO(ClaimsData page) {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("Verifier Controller :: convertInDocumentStatusDTO page {}, limit {}", page);
            List<ClaimsData> claimsData = new ArrayList<>();
            claimsData.add(page);
            List<VerifierClaimDataResponseDTO> dtos = new ArrayList<>();
            List<String> rejectedDocList = new ArrayList<>();
            for (ClaimsData claimData : claimsData) {
                VerifierClaimDataResponseDTO dto = new VerifierClaimDataResponseDTO();
                dto.setId(claimData.getId());
                dto.setPunchinClaimId(claimData.getPunchinClaimId());
                dto.setClaimDate(claimData.getClaimInwardDate());
                dto.setBorrowerName(claimData.getBorrowerName());
                dto.setBorrowerAddress(claimData.getBorrowerAddress());
                dto.setNomineeAddress(claimData.getNomineeAddress());
                dto.setNomineeName(claimData.getNomineeName());
                dto.setNomineeContactNumber(claimData.getNomineeContactNumber());
                dto.setClaimStatus(claimData.getClaimStatus());
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByOrderById(claimData.getId(), "banker");
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.SIGNED_FORM)) {
                        dto.setSingnedClaimDocument("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setSingnedClaimDocument("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setSingnedClaimDocument("REJECTED");
                            rejectedDocList.add(claimDocuments.getAgentDocType().name());
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.DEATH_CERTIFICATE)) {
                        dto.setDeathCertificate("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setDeathCertificate("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setDeathCertificate("REJECTED");
                            rejectedDocList.add(claimDocuments.getAgentDocType().name());
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.BORROWER_KYC_PROOF)) {
                        dto.setBorrowerKycProof("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setBorrowerKycProof("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setBorrowerKycProof("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.NOMINEE_KYC_PROOF)) {
                        dto.setNomineeKycProof("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setNomineeKycProof("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setNomineeKycProof("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.BANK_ACCOUNT_PROOF)) {
                        dto.setBankAccountProof("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setBankAccountProof("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setBankAccountProof("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.ADDITIONAL)) {
                        dto.setAdditionalDoc("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setAdditionalDoc("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setAdditionalDoc("REJECTED");
                        }
                    }
                }
                dtos.add(dto);
            }
            rejectedDocList.add(AgentDocType.OTHER.name());
            map.put("claimDocuments", dtos);
            map.put("rejectedDocList", rejectedDocList);
            map.put("message", MessageCode.success);
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: getClaimBankerDocuments e {}", e);
            map.put("claimDocuments", null);
            map.put("rejectedDocList", null);
            map.put("message", e.getMessage());
            return map;
        }
    }

    private List<BankerClaimListResponseDTO> mappedAgentDetails(Page<ClaimsData> page1) {
        List<ClaimsData> claimsDataList = page1.getContent();
        List<BankerClaimListResponseDTO> bankerClaimListResponseDTOS = new ArrayList<>();
        for (ClaimsData claimData : claimsDataList) {
            BankerClaimListResponseDTO bankerClaimListResponseDTO = ObjectMapperUtils.map(claimData, BankerClaimListResponseDTO.class);
            if (claimData.getAgentId() > 0) {
                Optional<User> optionalUser = userRepository.findById(claimData.getAgentId());
                if (optionalUser.isPresent()) {
                    User agent = optionalUser.get();
                    bankerClaimListResponseDTO.setAgentName((agent.getFirstName() + "-" + agent.getCity() + "-" + agent.getState()));
                }
            }
            bankerClaimListResponseDTOS.add(bankerClaimListResponseDTO);
        }
        return bankerClaimListResponseDTOS;
    }

    @Override
    public ClaimsRemarksDTO addClaimRemark(ClaimsData claimsData, ClaimRemarkRequestDTO requestDTO) {
        try {
            log.info("BankerServiceImpl :: addClaimRemark claimsData {}, requestDTO {}", claimsData, requestDTO);
            ClaimsRemarksDTO claimsRemarksDTO = new ClaimsRemarksDTO();
                BankerVerifierRemark bankerVerifierRemark = new BankerVerifierRemark();
                bankerVerifierRemark.setClaimId(claimsData.getId());
                bankerVerifierRemark.setRole(RoleEnum.VERIFIER.name());
                bankerVerifierRemark.setRemarkDoneBy(GenericUtils.getLoggedInUser().getId());
                bankerVerifierRemark.setRemark(requestDTO.getRemark());
                claimsRemarksDTO = modelMapper.map(bankerVerifierRemarkRepository.save(bankerVerifierRemark), ClaimsRemarksDTO.class);
            return claimsRemarksDTO;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerServiceImpl :: addClaimRemark", e);
            return null;
        }
    }
}
