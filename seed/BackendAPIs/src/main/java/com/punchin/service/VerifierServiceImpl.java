package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.*;
import com.punchin.enums.AgentDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.enums.SearchCaseEnum;
import com.punchin.repository.*;
import com.punchin.utility.BASE64DecodedMultipartFile;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ZipUtils;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class VerifierServiceImpl implements VerifierService {
    @Autowired
    private ClaimsDataRepository claimsDataRepository;
    @Autowired
    private ModelMapperService modelMapperService;
    @Autowired
    private CommonUtilService commonService;
    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;
    @Autowired
    private ClaimAllocatedRepository claimAllocatedRepository;
    @Autowired
    private AmazonClient amazonClient;
    @Autowired
    private AmazonS3FileManagers amazonS3FileManagers;
    @Autowired
    private DocumentUrlsRepository documentUrlsRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public PageDTO getAllClaimsData(ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize, SearchCaseEnum searchCaseEnum, String searchedKeyword) {
        Page<ClaimsData> page1 = Page.empty();
        try {
            log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimDataFilter, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            User verifier = GenericUtils.getLoggedInUser();
            String verifierState = verifier.getState();
            if (claimDataFilter.ALL.equals(claimDataFilter)) {
                if (Objects.nonNull(searchCaseEnum) && Objects.nonNull(searchedKeyword)) {
                    if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                        page1 = claimsDataRepository.findAllVerifierClaimSearchedDataByClaimDataId(searchedKeyword, verifierState, pageable);
                    } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                        page1 = claimsDataRepository.findAllVerifierClaimSearchedDataByLoanAccountNumber(searchedKeyword, verifierState, pageable);
                    } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                        page1 = claimsDataRepository.findAllVerifierClaimDataBySearchName(searchedKeyword, verifierState, pageable);
                    }
                } else
                    page1 = claimsDataRepository.findByBorrowerStateOrderByCreatedAtDesc(GenericUtils.getLoggedInUser().getState(), pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.IN_PROGRESS);
                claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
                claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
                claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
                claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
                page1 = claimsDataRepository.findByClaimStatusInAndBorrowerStateIgnoreCaseOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getState(), pageable);
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
                page1 = claimsDataRepository.findByClaimStatusInAndBorrowerStateIgnoreCaseOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getState(), pageable);
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.SETTLED);
                claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
                page1 = claimsDataRepository.findByClaimStatusInAndBorrowerStateIgnoreCaseOrderByCreatedAtDesc(claimsStatus, GenericUtils.getLoggedInUser().getState(), pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                claimsStatus.removeAll(claimsStatus);
                claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
                claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
                page1 = claimsDataRepository.findByClaimStatusInOrClaimBankerStatusInAndPunchinBankerIdOrderByCreatedAtDesc(claimsStatus, claimsStatus, GenericUtils.getLoggedInUser().getState(), pageable);
            }
            return convertInDocumentStatusDTO(page1);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getAllClaimsData", e);
            return commonService.convertPageToDTO(page1.getContent(), page1);
        }
    }

    public Map<String, Long> getDashboardData() {
        Map<String, Long> map = new HashMap<>();
        try {
            log.info("VerifierServiceImpl :: getDashboardData");
            List<ClaimStatus> claimsStatus = new ArrayList<>();
            map.put(ClaimStatus.ALL.name(), claimsDataRepository.countByBorrowerState(GenericUtils.getLoggedInUser().getState()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.IN_PROGRESS);
            claimsStatus.add(ClaimStatus.CLAIM_SUBMITTED);
            claimsStatus.add(ClaimStatus.CLAIM_INTIMATED);
            claimsStatus.add(ClaimStatus.VERIFIER_DISCREPENCY);
            claimsStatus.add(ClaimStatus.AGENT_ALLOCATED);
            claimsStatus.add(ClaimStatus.NEW_REQUIREMENT);
            claimsStatus.add(ClaimStatus.BANKER_DISCREPANCY);
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatusInAndBorrowerStateIgnoreCase(claimsStatus, GenericUtils.getLoggedInUser().getState()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.UNDER_VERIFICATION);
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), claimsDataRepository.countByClaimStatusInAndBorrowerStateIgnoreCase(claimsStatus, GenericUtils.getLoggedInUser().getState()));
            claimsStatus.removeAll(claimsStatus);
            claimsStatus.add(ClaimStatus.SETTLED);
            claimsStatus.add(ClaimStatus.SUBMITTED_TO_INSURER);
            map.put(ClaimStatus.SUBMITTED_TO_INSURER.name(), claimsDataRepository.countByClaimStatusInAndBorrowerStateIgnoreCase(claimsStatus, GenericUtils.getLoggedInUser().getState()));
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.ALL.name(), 0L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), 0L);
            map.put(ClaimStatus.SUBMITTED_TO_INSURER.name(), 0L);
            return map;
        }
    }

    @Override
    public ClaimsData getClaimData(Long claimId) {
        try {
            log.info("VerifierServiceImpl :: getClaimData");
            return claimsDataRepository.findByIdAndBorrowerState(claimId, GenericUtils.getLoggedInUser().getState());
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getClaimData ", e);
            return null;
        }
    }

    @Override
    public boolean allocateClaimToAgent(ClaimsData claimsData, User user) {
        try {
            log.info("VerifierServiceImpl :: allocateClaimToAgent");
            ClaimAllocated claimAllocated = new ClaimAllocated();
            claimAllocated.setClaimsData(claimsData);
            claimAllocated.setUser(user);
            claimAllocatedRepository.save(claimAllocated);
            claimsData.setClaimStatus(ClaimStatus.AGENT_ALLOCATED);
            claimsDataRepository.save(claimsData);
            return true;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: allocateClaimToAgent ", e);
            return false;
        }
    }

    public ClaimDetailForVerificationDTO getClaimDocuments(ClaimsData claimsData) {
        log.info("VerifierController :: getDocumentDetails claimId {}", claimsData.getId());
        try {
            ClaimDetailForVerificationDTO claimDetailForVerificationDTO = new ClaimDetailForVerificationDTO();
            claimDetailForVerificationDTO.setBorrowerName(claimsData.getBorrowerName());
            claimDetailForVerificationDTO.setBorrowerAddress(claimsData.getBorrowerAddress());
            claimDetailForVerificationDTO.setLoanAccountNumber(claimsData.getLoanAccountNumber());
            claimDetailForVerificationDTO.setInsurerName(claimsData.getInsurerName());
            claimDetailForVerificationDTO.setNomineeName(claimsData.getNomineeName());
            claimDetailForVerificationDTO.setNomineeAddress(claimsData.getNomineeAddress());
            claimDetailForVerificationDTO.setNomineeRelationShip(claimsData.getNomineeRelationShip());

            //Agent
            List<ClaimDocuments> agentDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(claimsData.getId(), "agent", true);
            List<ClaimDocumentsDTO> agentDocumentsListDTOs = new ArrayList<>();
            for (ClaimDocuments claimDocuments : agentDocumentsList) {
                ClaimDocumentsDTO claimDocumentsDTO = new ClaimDocumentsDTO();
                claimDocumentsDTO.setId(claimDocuments.getId());
                claimDocumentsDTO.setDocType(claimDocuments.getDocType());
                claimDocumentsDTO.setAgentDocType(claimDocuments.getAgentDocType());
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
                agentDocumentsListDTOs.add(claimDocumentsDTO);
            }
            claimDetailForVerificationDTO.setAgentClaimDocumentsDTOs(agentDocumentsListDTOs);

            //Banker
            List<ClaimDocuments> bankerDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(claimsData.getId(), "banker", true);
            List<ClaimDocumentsDTO> bankerDocumentsListDTOs = new ArrayList<>();
            for (ClaimDocuments claimDocuments : bankerDocumentsList) {
                ClaimDocumentsDTO claimDocumentsDTO = new ClaimDocumentsDTO();
                claimDocumentsDTO.setId(claimDocuments.getId());
                claimDocumentsDTO.setDocType(claimDocuments.getDocType());
                claimDocumentsDTO.setAgentDocType(claimDocuments.getAgentDocType());
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
                bankerDocumentsListDTOs.add(claimDocumentsDTO);
            }
            claimDetailForVerificationDTO.setBankerClaimDocumentsDTOs(bankerDocumentsListDTOs);
            return claimDetailForVerificationDTO;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: getDocumentDetails e {}", e);
            return null;
        }
    }

    public String acceptAndRejectDocument(ClaimsData claimsData, ClaimDocuments claimDocuments, DocumentApproveRejectPayloadDTO approveRejectPayloadDTO) {
        log.info("VerifierController :: acceptAndRejectDocuments claimsData {}, claimDocuments {}, approveRejectPayloadDTO {}", claimsData, claimDocuments, approveRejectPayloadDTO);
        try {
            claimDocuments.setIsVerified(true);
            claimDocuments.setIsApproved(approveRejectPayloadDTO.isApproved());
            claimDocuments.setReason(approveRejectPayloadDTO.getReason());
            claimDocuments.setRejectRemark(approveRejectPayloadDTO.getRemark());
            claimDocuments.setVerifierId(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setVerifyTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments);
            if (claimDocuments.getUploadSideBy().equalsIgnoreCase("agent")) {
                if (!approveRejectPayloadDTO.isApproved()) {
                    claimsData.setClaimStatus(ClaimStatus.VERIFIER_DISCREPENCY);
                } else {
                    if (!claimDocumentsRepository.existsByClaimsDataIdAndUploadSideByAndIsActiveAndIsApproved(claimsData.getId(), "agent", true, false)) {
                        claimsData.setClaimStatus(ClaimStatus.SUBMITTED_TO_INSURER);
                    }
                }
            } else if (claimDocuments.getUploadSideBy().equalsIgnoreCase("banker")) {
                if (!approveRejectPayloadDTO.isApproved()) {
                    claimsData.setClaimBankerStatus(ClaimStatus.BANKER_DISCREPANCY);
                }
            }
            claimsDataRepository.save(claimsData);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: acceptAndRejectDocuments ", e);
            return e.getMessage();
        }
    }

    @Override
    public ClaimDocuments getClaimDocumentById(Long docId) {
        try {
            log.info("VerifierServiceImpl :: getClaimDocumentById docId {}", docId);
            Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(docId);
            return optionalClaimDocuments.isPresent() ? optionalClaimDocuments.get() : null;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getClaimDocumentById ", e);
            return null;
        }
    }

    @Override
    public PageDTO getClaimDataWithDocumentStatus(Integer page, Integer limit) {
        try {
            log.info("BankerController :: getClaimDataWithDocumentStatus page {}, limit {}", page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Page page1 = claimsDataRepository.findByClaimStatusAndBorrowerStateIgnoreCase(ClaimStatus.UNDER_VERIFICATION, GenericUtils.getLoggedInUser().getState(), pageable);
            return convertInDocumentStatusDTO(page1);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getClaimDataWithDocumentStatus", e);
            return null;
        }
    }

    @Override
    public String downloadAllDocuments(Long claimId) {
        try {
            String filePath = System.getProperty("user.dir") + "BackendAPIs/downloads/";
            log.info("VerifierServiceImpl :: downloadAllDocuments docId {}, Path {}", claimId, filePath);
            String punchinClaimId = claimsDataRepository.findPunchinClaimIdById(claimId);
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(claimId, "agent", true);
            for (ClaimDocuments claimDocuments : claimDocumentsList) {
                List<DocumentUrls> documentUrlsList = documentUrlsRepository.findDocumentUrlsByClaimDocumentId(claimDocuments.getId());
                for (DocumentUrls documentUrls : documentUrlsList) {
                    downloadDocumentInDirectory(documentUrls.getDocUrl(), claimId, filePath);
                }
            }
            ZipUtils appZip = new ZipUtils();
            appZip.generateFileList(new File(filePath + claimId), filePath + claimId);
            appZip.zipIt(filePath + punchinClaimId + ".zip", filePath + claimId);
            new File(filePath + claimId).deleteOnExit();
            File file = new File(filePath + punchinClaimId + ".zip");
            String fileName = file.getName();
            String version = amazonS3FileManagers.uploadFileToAmazonS3("verifier/", new File(filePath + punchinClaimId + ".zip"), fileName);
            amazonS3FileManagers.cleanUp(file);
            return version;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: downloadAllDocuments ", e);
            return null;
        }
    }

    private void downloadDocumentInDirectory(String docUrl, Long claimId, String filePath) {
        try {
            log.info("ready to download claim documents docUrl {}", docUrl);
            URL url = new URL(docUrl);
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            File file1 = new File(filePath + claimId);
            file1.mkdirs();
            log.info("Directory created");
            FileOutputStream fos = new FileOutputStream(file1.getAbsolutePath() + "/" + FilenameUtils.getName(docUrl), true);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            log.info("File downloaded");
            fos.close();
            rbc.close();
        } catch (Exception e) {
            log.error("ERROR WHILE DOWNLOADING FILE FROM URL e {}", e);
        }
    }


    private PageDTO convertInDocumentStatusDTO(Page page) {
        try {
            log.info("Verifier Controller :: convertInDocumentStatusDTO page {}, limit {}", page);
            List<ClaimsData> claimsData = page.getContent();
            List<VerifierClaimDataResponseDTO> dtos = new ArrayList<>();
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
                dto.setBorrowerContactNumber(claimData.getBorrowerContactNumber());
                dto.setClaimStatus(claimData.getClaimStatus());
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByOrderById(claimData.getId(), "agent");
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.SIGNED_FORM)) {
                        dto.setSingnedClaimDocument("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setSingnedClaimDocument("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setSingnedClaimDocument("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.DEATH_CERTIFICATE)) {
                        dto.setDeathCertificate("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setDeathCertificate("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setDeathCertificate("REJECTED");
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
            return commonService.convertPageToDTO(dtos, page);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: convertInDocumentStatusDTO", e);
            return null;
        }
    }

    @Override
    public String downloadClaimDataWithDocumentStatus(Integer page, Integer limit) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            log.info("VerifierController :: downloadClaimDataVerifier page {}, limit {}", page, limit);
            String filename = "/home/prince/D/Claim_Data_Format_Verifier.xlsx";
            final String[] HEADERs = {"Id", "Claim Date", "Borrower Name", "Nominee Name", "Nominee Contact Number", "Nominee Address",
                    "Singned Claim Document", "Death Certificate", "Borrower IdProof", "Borrower Address Proof", " Nominee IdProof",
                    "Nominee Address Proof", "Bank Account Proof", "Fir Postmortem Report", "Additional Doc"};
            Pageable pageable = PageRequest.of(page, limit);
            Page page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.UNDER_VERIFICATION, true, pageable);
            List<ClaimsData> claimsData = page1.getContent();
            List<VerifierClaimDataResponseDTO> dtos = new ArrayList<>();
            for (ClaimsData claimData : claimsData) {
                VerifierClaimDataResponseDTO dto = new VerifierClaimDataResponseDTO();
                dto.setId(claimData.getId());
                dto.setClaimDate(claimData.getClaimInwardDate());
                dto.setBorrowerName(claimData.getBorrowerName());
                dto.setNomineeAddress(claimData.getNomineeAddress());
                dto.setNomineeName(claimData.getNomineeName());
                dto.setBorrowerContactNumber(claimData.getBorrowerContactNumber());
                dto.setNomineeContactNumber(claimData.getNomineeContactNumber());
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByOrderById(claimData.getId(), "agent");
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.SIGNED_FORM)) {
                        dto.setSingnedClaimDocument("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setSingnedClaimDocument("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setSingnedClaimDocument("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.DEATH_CERTIFICATE)) {
                        dto.setDeathCertificate("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setDeathCertificate("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setDeathCertificate("REJECTED");
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
            Sheet sheet = workbook.createSheet("Sheet1");
            FileOutputStream fileOut = new FileOutputStream(filename);
            SimpleDateFormat dateOnly = new SimpleDateFormat("MM/dd/yyyy");
            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < HEADERs.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(HEADERs[col]);
            }
            int rowIdx = 1;
            for (VerifierClaimDataResponseDTO verifierClaimDataResponseDTO : dtos) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(verifierClaimDataResponseDTO.getId());
                row.createCell(1).setCellValue(dateOnly.format(verifierClaimDataResponseDTO.getClaimDate()));
                row.createCell(2).setCellValue(verifierClaimDataResponseDTO.getBorrowerName());
                row.createCell(3).setCellValue(verifierClaimDataResponseDTO.getNomineeName());
                row.createCell(4).setCellValue(verifierClaimDataResponseDTO.getNomineeContactNumber());
                row.createCell(5).setCellValue(verifierClaimDataResponseDTO.getNomineeAddress());
                row.createCell(6).setCellValue(verifierClaimDataResponseDTO.getSingnedClaimDocument());
                row.createCell(7).setCellValue(verifierClaimDataResponseDTO.getDeathCertificate());
                //row.createCell(8).setCellValue(verifierClaimDataResponseDTO.getBorrowerIdProof());
                //row.createCell(9).setCellValue(verifierClaimDataResponseDTO.getBorrowerAddressProof());
                //row.createCell(10).setCellValue(verifierClaimDataResponseDTO.getNomineeIdProof());
                //row.createCell(11).setCellValue(verifierClaimDataResponseDTO.getNomineeAddressProof());
                row.createCell(12).setCellValue(verifierClaimDataResponseDTO.getBankAccountProof());
                //row.createCell(13).setCellValue(verifierClaimDataResponseDTO.getFirPostmortemReport());
                row.createCell(14).setCellValue(verifierClaimDataResponseDTO.getAdditionalDoc());
            }
            workbook.write(out);
            workbook.write(fileOut);
            out.writeTo(fileOut);
            BASE64DecodedMultipartFile base64DecodedMultipartFile = null;//new BASE64DecodedMultipartFile(new ByteArrayInputStream(out.toByteArray()).readAllBytes(), "Claims-VerifierData" + ".xlsx");
            return amazonS3FileManagers.uploadFile("Claims-VerifierData", base64DecodedMultipartFile, "verifier/");
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getAllClaimsData ", e);
            return null;
        }
    }

    @Override
    public PageDTO getVerifierClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, ClaimDataFilter claimDataFilter, Integer pageNo, Integer limit) {
        Page<ClaimsData> claimSearchedData = Page.empty();
        List<String> statusList = new ArrayList<>();
        Pageable pageable = PageRequest.of(pageNo, limit);
        User verifier = GenericUtils.getLoggedInUser();
        String verifierState = verifier.getState();
        if (claimDataFilter.ALL.equals(claimDataFilter)) {
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findAllVerifierClaimSearchedDataByClaimDataId(searchedKeyword, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findAllVerifierClaimSearchedDataByLoanAccountNumber(searchedKeyword, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findAllVerifierClaimDataBySearchName(searchedKeyword, verifierState, pageable);
            }
        } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.IN_PROGRESS.toString());
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.toString());
            statusList.add(ClaimStatus.AGENT_ALLOCATED.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByClaimDataId(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataBySearchName(searchedKeyword, statusList, verifierState, pageable);
            }
        } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.UNDER_VERIFICATION.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByClaimDataId(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataBySearchName(searchedKeyword, statusList, verifierState, pageable);
            }
        } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.SETTLED.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByClaimDataId(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataBySearchName(searchedKeyword, statusList, verifierState, pageable);
            }
        } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByClaimDataId(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataByLoanAccountNumber(searchedKeyword, statusList, verifierState, pageable);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findVerifierClaimSearchedDataBySearchName(searchedKeyword, statusList, verifierState, pageable);
            }
        }
        if (claimSearchedData == null || claimSearchedData.isEmpty()) {
            log.info("No claims data found");
            return null;
        }
        return convertInDocumentStatusDTO(claimSearchedData);
    }

    public List<AgentListResponseDTO> getAllAgentsForVerifier(User verifier) {
        List<User> allAgents = userRepository.findAllAgentsForVerifier(verifier.getState());
        if (allAgents.isEmpty()) {
            log.info(MessageCode.NO_RECORD_FOUND);
            return Collections.emptyList();
        }
        List<AgentListResponseDTO> agentListResponseDTOList = new ArrayList<>();
        for (User agent : allAgents) {
            AgentListResponseDTO agentListResponseDTO = new AgentListResponseDTO();
            agentListResponseDTO.setId(agent.getId());
            agentListResponseDTO.setFirstName(agent.getFirstName() + "-" + agent.getCity() + "-" + agent.getState());
            agentListResponseDTOList.add(agentListResponseDTO);
        }
        log.info(MessageCode.ALL_AGENTS_LIST_FETCHED_SUCCESS);
        return agentListResponseDTOList;
    }

    public String claimDataAgentAllocation(Long agentId, Long claimDataId) {
        String state = GenericUtils.getLoggedInUser().getState();
        Boolean agentExists = userRepository.findAgentState(agentId, state);
        if (!agentExists) {
            return MessageCode.invalidAgentId;
        }
        Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(claimDataId);
        if (!optionalClaimsData.isPresent()) {
            return MessageCode.invalidClaimId;
        }
        ClaimsData claimsData = optionalClaimsData.get();
        claimsData.setAgentId(agentId);
        claimsDataRepository.save(claimsData);
        return MessageCode.AGENT_ALLOCATED_SAVED_SUCCESS;
    }

}

