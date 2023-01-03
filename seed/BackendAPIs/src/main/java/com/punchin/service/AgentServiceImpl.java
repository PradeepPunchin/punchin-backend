package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.*;
import com.punchin.enums.*;
import com.punchin.repository.*;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ObjectMapperUtils;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {
    @Autowired
    private ModelMapperService mapperService;
    @Autowired
    private CommonUtilService commonService;
    @Autowired
    private ClaimsDataRepository claimsDataRepository;
    @Autowired
    private ClaimAllocatedRepository claimAllocatedRepository;
    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;
    @Autowired
    private DocumentUrlsRepository documentUrlsRepository;
    @Autowired
    private AmazonS3FileManagers amazonS3FileManagers;
    @Autowired
    private AmazonClient amazonClient;
    @Autowired
    private CommonUtilService commonUtilService;
    @Autowired
    private ClaimsRemarksRepository remarksRepository;
    @Autowired
    private ClaimHistoryRepository claimHistoryRepository;

    @Override
    public PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        try {
            log.info("AgentServiceImpl :: getClaimsList dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Page<ClaimsData> page1 = Page.empty();
            List<ClaimStatus> statusList = new ArrayList<>();
            if (claimDataFilter.ALLOCATED.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByAgentIdOrderByCreatedAtDesc(GenericUtils.getLoggedInUser().getId(), pageable);    //findAllByAgentAllocated(GenericUtils.getLoggedInUser().getId(), pageable);
            } else if (claimDataFilter.ACTION_PENDING.equals(claimDataFilter)) {
                statusList.add(ClaimStatus.ACTION_PENDING);
                statusList.add(ClaimStatus.CLAIM_INTIMATED);
                statusList.add(ClaimStatus.CLAIM_SUBMITTED);
                statusList.add(ClaimStatus.AGENT_ALLOCATED);
                page1 = claimsDataRepository.findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                statusList.add(ClaimStatus.IN_PROGRESS);
                page1 = claimsDataRepository.findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                statusList.add(ClaimStatus.VERIFIER_DISCREPENCY);
                statusList.add(ClaimStatus.NEW_REQUIREMENT);
                page1 = claimsDataRepository.findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                statusList.add(ClaimStatus.UNDER_VERIFICATION);
                page1 = claimsDataRepository.findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            }
            if (!page1.isEmpty()) {
                List<AgentClaimListDTO> agentClaimListDTOS = new ArrayList<>();
                List<ClaimsData> claimsDataList = page1.getContent();
                for (ClaimsData claimsData : claimsDataList) {
                    AgentClaimListDTO agentClaimListDTO = new AgentClaimListDTO();
                    agentClaimListDTO.setId(claimsData.getId());
                    agentClaimListDTO.setClaimDate(claimsData.getClaimInwardDate());
                    agentClaimListDTO.setAllocationDate(claimsData.getClaimInwardDate());
                    agentClaimListDTO.setClaimId(claimsData.getPunchinClaimId());
                    agentClaimListDTO.setBorrowerName(claimsData.getBorrowerName());
                    agentClaimListDTO.setBorrowerAddress(claimsData.getBorrowerAddress());
                    agentClaimListDTO.setNomineeName(claimsData.getNomineeName());
                    agentClaimListDTO.setNomineeContactNumber(claimsData.getNomineeContactNumber());
                    agentClaimListDTO.setClaimStatus(claimsData.getClaimStatus());
                    agentClaimListDTOS.add(agentClaimListDTO);
                }
                return commonService.convertPageToDTO(agentClaimListDTOS, page1);
            }
            return commonService.convertPageToDTO(page1);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimsList e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("AgentServiceImpl :: getDashboardData");
            List<ClaimStatus> statusList = new ArrayList<>();
            map.put(ClaimStatus.AGENT_ALLOCATED.name(), claimsDataRepository.countByAgentId(GenericUtils.getLoggedInUser().getId()));//claimAllocatedRepository.countByClaimStatusByAgent(GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.IN_PROGRESS);
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY);
            statusList.add(ClaimStatus.NEW_REQUIREMENT);
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatusInAndAgentId(statusList, GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.ACTION_PENDING);
            statusList.add(ClaimStatus.CLAIM_INTIMATED);
            statusList.add(ClaimStatus.CLAIM_SUBMITTED);
            statusList.add(ClaimStatus.AGENT_ALLOCATED);
            map.put(ClaimStatus.ACTION_PENDING.name(), claimsDataRepository.countByClaimStatusInAndAgentId(statusList, GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.UNDER_VERIFICATION);
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), claimsDataRepository.countByClaimStatusInAndAgentId(statusList, GenericUtils.getLoggedInUser().getId()));

            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.AGENT_ALLOCATED.name(), 0L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.ACTION_PENDING.name(), 0L);
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), 0L);
            return map;
        }
    }

    @Override
    public boolean checkAccess(Long claimId) {
        try {
            log.info("AgentServiceImpl :: checkAccess");
            return claimsDataRepository.existsByIdAndAgentId(claimId, GenericUtils.getLoggedInUser().getId());
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: checkAccess e{}", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getClaimData(Long claimId) {
        try {
            log.info("AgentServiceImpl :: getClaimData");
            Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(claimId);
            return optionalClaimsData.isPresent() ? convertInDocumentStatusDTO(optionalClaimsData.get()) : null;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimData e{}", e);
            return null;
        }
    }

    @Override
    public ClaimsData getClaimsData(Long claimId) {
        try {
            log.info("AgentServiceImpl :: getClaimsData");
            Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(claimId);
            return optionalClaimsData.isPresent() ? optionalClaimsData.get() : null;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimsData e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> uploadDocument(AgentUploadDocumentDTO documentDTO) {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("AgentServiceImpl :: uploadDocument documentDTO {]", documentDTO);
            List<ClaimDocuments> claimDocuments = new ArrayList<>();
            ClaimsData claimsData = documentDTO.getClaimsData();
            if(Objects.nonNull(documentDTO.getCauseOfDeath())) {
                claimsData.setCauseOfDeath(documentDTO.getCauseOfDeath());
                claimsData.setIsMinor(documentDTO.isMinor());
            }
            Map<String, MultipartFile> isMinorDoc = documentDTO.getIsMinorDoc();
            List<String> keys = new ArrayList<>(isMinorDoc.keySet());
            for(String key : keys){
                if(key.contains(":")){
                    String keyArray[] = key.split(":");
                    GenericUtils.hasMatchingSubstring2(keyArray[0].trim(), keys);
                    claimDocuments.add(uploadDocumentOnS3(AgentDocType.valueOf(keyArray[0].trim()), keyArray[1].trim(), claimsData, new MultipartFile[]{isMinorDoc.get(key)}));
                }else{
                    claimDocuments.add(uploadDocumentOnS3(AgentDocType.valueOf(key), key, claimsData, new MultipartFile[]{isMinorDoc.get(key)}));
                }
            }
            claimsData.setClaimStatus(ClaimStatus.IN_PROGRESS);
            claimHistoryRepository.save(new ClaimHistory(claimsData.getId(), ClaimStatus.IN_PROGRESS, "In Progress"));
            if(Objects.nonNull(documentDTO.getAgentRemark())) {
                ClaimsRemarks claimsRemarks = new ClaimsRemarks();
                claimsRemarks.setRemark(documentDTO.getAgentRemark());
                remarksRepository.save(claimsRemarks);
                claimsData.setAgentRemark(documentDTO.getAgentRemark());
            }
            ClaimDataDTO claimDataDTO = mapperService.map(claimsDataRepository.save(claimsData), ClaimDataDTO.class);
            claimDataDTO.setClaimDocuments(claimDocuments);
            map.put("claimsData", claimDataDTO);
            map.put("status", true);
            map.put("message", MessageCode.success);
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: uploadDocument e{}", e);
            map.put("claimsData", null);
            map.put("status", false);
            map.put("message", e.getMessage());
            return map;
        }
    }

    @Override
    public Map<String, Object> getClaimDocuments(Long id) {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("AgentServiceImpl :: getClaimDocuments claimId {}", id);
            List<ClaimDocumentsDTO> claimDocumentsDTOS = new ArrayList<>();
            List<String> rejectedDocList = new ArrayList<>();
            List<String> uploadedDocTypes = claimDocumentsRepository.findDistinctByClaimsDataIdAndUploadSideByAndIsActiveOrderByAgentDocType(id, "agent", true);
            for(String docTypes : uploadedDocTypes) {
                List<ClaimDocuments> agentDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActiveAndAgentDocTypeOrderByAgentDocTypeLimit(id, "agent", true, docTypes);
                for (ClaimDocuments claimDocuments : agentDocumentsList) {
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
                    List<DocumentUrls> documentUrlsList = documentUrlsRepository.findDocumentUrlsByClaimDocument(id, "agent", true, docTypes);
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
            }

            //Add new document request claims
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.getAdditionalDocumentRequestClaims(id);
            for (ClaimDocuments claimDocuments : claimDocumentsList) {
                ClaimDocumentsDTO claimDocumentsDTO = new ClaimDocumentsDTO();
                claimDocumentsDTO.setId(claimDocuments.getId());
                claimDocumentsDTO.setAgentDocType(claimDocuments.getAgentDocType());
                claimDocumentsDTO.setDocType(claimDocuments.getDocType());
                claimDocumentsDTO.setIsVerified(claimDocuments.getIsVerified());
                claimDocumentsDTO.setIsApproved(claimDocuments.getIsApproved());
                claimDocumentsDTO.setReason(claimDocuments.getReason());
                rejectedDocList.add(claimDocuments.getAgentDocType().name());
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
    public Map<String, Object> discrepancyDocumentUpload(Long claimId, MultipartFile[] multipartFiles, AgentDocType docType, boolean isDiscrepancy) {
        log.info("AgentServiceImpl :: discrepancyDocumentUpload claimsData {}, multipartFiles {}, docType {}", claimId, multipartFiles, docType);
        Map<String, Object> map = new HashMap<>();
        try {
            String oldDocType = docType.name();
            List<ClaimDocuments> claimDocumentsList;
            if(isDiscrepancy){
                claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndAgentDocType(claimId, docType);
            }else{
                claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndAgentDocTypeAndUploadSideByOrderByIdDesc(claimId, docType, "New Requirement");
            }
            if (!claimDocumentsList.isEmpty()) {
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    if(isDiscrepancy) {
                        claimDocuments.setIsActive(false);
                    } else {
                        claimDocuments.setUploadSideBy("New Requirement Done");
                    }
                    oldDocType = claimDocuments.getDocType();
                }
                claimDocumentsRepository.saveAll(claimDocumentsList);
            }
            ClaimsData claimsData = claimsDataRepository.findById(claimId).get();
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setAgentDocType(docType);
            claimDocuments.setDocType(oldDocType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            if(isDiscrepancy) {
                claimDocuments.setUploadSideBy("agent");
            } else {
                claimDocuments.setUploadSideBy("agent New Requirement");
            }
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonS3FileManagers.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent/"));
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
            log.error("EXCEPTION WHILE AgentServiceImpl :: discrepancyDocumentUpload e {} ", e);
            map.put("message", e.getMessage());
            map.put("claimDocuments", null);
            return map;
        }
    }

    @Override
    public boolean checkDocumentIsInDiscrepancy(Long claimId, AgentDocType docType, boolean isDiscrepancy) {
        try {
            log.info("AgentServiceImpl :: checkDocumentIsInDiscrepancy");
            ClaimDocuments claimDocuments;
            if(isDiscrepancy){
                claimDocuments = claimDocumentsRepository.findFirstByClaimsDataIdAndAgentDocTypeAndUploadSideByAndIsVerifiedAndIsApprovedOrderByIdDesc(claimId, docType, "agent", true, false);
            }else {
                claimDocuments = claimDocumentsRepository.findFirstByClaimsDataIdAndAgentDocTypeAndUploadSideByOrderByIdDesc(claimId, docType, "New Requirement");
            }
            return Objects.nonNull(claimDocuments) ? true : false;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: checkDocumentIsInDiscrepancy e{}", e);
            return false;
        }
    }

    @Override
    public boolean checkDocumentUploaded(Long claimId) {
        try {
            log.info("AgentServiceImpl :: checkDocumentUploaded");
            return claimDocumentsRepository.existsByClaimsDataIdAndUploadSideByAndIsVerifiedAndIsApprovedAndIsActive(claimId, "agent", true, false, true);
            //return claimDocumentsRepository.existsByClaimsDataIdAndUploadSideByAndIsVerified(claimId, "agent", false);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: checkDocumentUploaded e{}", e);
            return false;
        }
    }

    @Override
    public String forwardToVerifier(Long claimId) {
        try {
            log.info("AgentServiceImpl :: forwardToVerifier claimId {}", claimId);
            ClaimsData claimsData = claimsDataRepository.findById(claimId).get();
            claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
            claimHistoryRepository.save(new ClaimHistory(claimsData.getId(), ClaimStatus.UNDER_VERIFICATION, "Under Verification"));
            claimsDataRepository.save(claimsData);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: forwardToVerifier e{}", e);
            return e.getMessage();
        }
    }

    public ClaimDocuments uploadDocumentOnS3(AgentDocType agentDocType, String docType, ClaimsData claimsData, MultipartFile[] multipartFiles) {
        try {
            log.info("AgentServiceImpl :: uploadDocumentOnS3 claimsData {}, multipartFiles {}, docType {}", claimsData, multipartFiles, docType);
            log.info("AgentServiceImpl :: uploadDocumentOnS3 uploading agentDocType - {}, ClaimId - {}, multipartFiles - {}", agentDocType, claimsData.getId(), multipartFiles.length);
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setAgentDocType(agentDocType);
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setDocType(docType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonS3FileManagers.uploadFile(claimsData.getPunchinClaimId(), multipartFile, "agent/"));
                documentUrls.add(urls);
            }
            documentUrlsRepository.saveAll(documentUrls);
            claimDocuments.setDocumentUrls(documentUrls);
            claimDocuments.setUploadTime(System.currentTimeMillis());
            log.info("AgentServiceImpl :: uploadDocumentOnS3 uploaded agentDocType - {}, ClaimId - {}, multipartFiles - {}", agentDocType, claimsData.getId(), multipartFiles.length);
            return claimDocumentsRepository.save(claimDocuments);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: uploadFiles ", e);
            return null;
        }
    }

    public PageDTO getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter) {
        log.info("Get Searched data request received for caseType :{} , searchedKeyword :{} , pageNo :{} , limit :{} ", searchCaseEnum, searchedKeyword, pageNo, limit);
        Pageable pageable = PageRequest.of(pageNo, limit);
        Long agentId = GenericUtils.getLoggedInUser().getId();
        Page<ClaimsData> claimSearchedData = null;
        List<String> statusList = new ArrayList<>();
        if (claimDataFilter.ALLOCATED.equals(claimDataFilter)) {
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId1(searchedKeyword, pageable, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId2(searchedKeyword, pageable, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId3(searchedKeyword, pageable, agentId);
            }
        } else if (claimDataFilter.ACTION_PENDING.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.ACTION_PENDING.toString());
            statusList.add(ClaimStatus.AGENT_ALLOCATED.toString());
            statusList.add(ClaimStatus.CLAIM_INTIMATED.toString());
            statusList.add(ClaimStatus.CLAIM_SUBMITTED.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByLoanAccountNumber(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataBySearchName(searchedKeyword, pageable, statusList, agentId);
            }
        } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.IN_PROGRESS.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByLoanAccountNumber(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataBySearchName(searchedKeyword, pageable, statusList, agentId);
            }
        } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByLoanAccountNumber(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataBySearchName(searchedKeyword, pageable, statusList, agentId);
            }
        } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
            statusList.add(ClaimStatus.UNDER_VERIFICATION.toString());
            if (searchCaseEnum.equals(SearchCaseEnum.CLAIM_DATA_ID)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByClaimDataId(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.LOAN_ACCOUNT_NUMBER)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataByLoanAccountNumber(searchedKeyword, pageable, statusList, agentId);
            } else if (searchCaseEnum.equals(SearchCaseEnum.NAME)) {
                claimSearchedData = claimsDataRepository.findClaimSearchedDataBySearchName(searchedKeyword, pageable, statusList, agentId);
            }
        }
        if (claimSearchedData == null || claimSearchedData.isEmpty()) {
            log.info("No claims data found");
            return null;
        }
        List<AgentSearchDTO> agentSearchDTO1s = ObjectMapperUtils.mapAll(claimSearchedData.toList(), AgentSearchDTO.class);
        for (AgentSearchDTO agentSearchDTO1 : agentSearchDTO1s) {
            agentSearchDTO1.setClaimId(agentSearchDTO1.getPunchinClaimId());
            agentSearchDTO1.setAllocationDate(agentSearchDTO1.getClaimInwardDate());
            agentSearchDTO1.setClaimDate(agentSearchDTO1.getClaimInwardDate());
            agentSearchDTO1.setClaimStatus(agentSearchDTO1.getClaimStatus());
        }
        log.info("searched claim data fetched successfully");
        return commonService.convertPageToDTO(agentSearchDTO1s, claimSearchedData);
    }

    public List<DocumentUrls> uploadAgentDocument(Long id, MultipartFile[] multipartFiles, AgentDocType docType) {
        Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(id);
        if (!optionalClaimsData.isPresent()) {
            return Collections.emptyList();
        }
        ClaimsData claimsData = optionalClaimsData.get();
        ClaimDocuments claimDocuments = new ClaimDocuments();
        claimDocuments.setClaimsData(claimsData);
        claimDocuments.setAgentDocType(AgentDocType.valueOf(docType.getValue()));
        claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments.setUploadSideBy("agent");
        List<DocumentUrls> documentUrls = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            DocumentUrls urls = new DocumentUrls();
            urls.setDocUrl(amazonS3FileManagers.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent/"));
            if (Objects.isNull(urls.getDocUrl())) {
                log.info("file not uploaded");
                return Collections.emptyList();
            }
            documentUrls.add(urls);
        }
        List<DocumentUrls> documentUrlsList = documentUrlsRepository.saveAll(documentUrls);
        claimDocuments.setDocumentUrls(documentUrls);
        claimDocuments.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments);
        return documentUrlsList;
    }

    @Override
    public List<ClaimHistoryDTO> getClaimHistory(String id) {
        try {
            log.info("AgentServiceImpl :: getClaimHistory claimId - {}", id);
            List<ClaimHistoryDTO> claimHistoryDTOS = new ArrayList<>();
            Long claimId = claimsDataRepository.findIdByPunchinId(id);
            if(Objects.nonNull(claimId)) {
                List<ClaimHistory> claimHistories = claimHistoryRepository.findByClaimIdOrderById(claimId);
                ClaimHistoryDTO oldClaimHistory = new ClaimHistoryDTO();
                for (ClaimHistory claimHistory : claimHistories) {
                    ClaimHistoryDTO claimHistoryDTO = mapperService.map(claimHistory, ClaimHistoryDTO.class);
                    if (Objects.nonNull(oldClaimHistory) && !claimHistoryDTO.getClaimStatus().equals(oldClaimHistory.getClaimStatus())) {
                        claimHistoryDTOS.add(claimHistoryDTO);
                    }
                    oldClaimHistory = claimHistoryDTO;
                }
            }
            return claimHistoryDTOS;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimHistory e - {}", e);
            return Collections.EMPTY_LIST;
        }
    }

    public String deleteClaimDocument(Long documentId) {
        Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(documentId);
        if (!optionalClaimDocuments.isPresent()) {
            return MessageCode.NO_RECORD_FOUND;
        }
        claimDocumentsRepository.delete(optionalClaimDocuments.get());
        return MessageCode.DOCUMENT_DELETED;
    }

    private Map<String, Object> convertInDocumentStatusDTO(ClaimsData page) {
        Map<String, Object> map = new HashMap<>();
        try {
            String category = "";
            log.info("Verifier Controller :: convertInDocumentStatusDTO page {}, limit {}", page);
            List<ClaimsData> claimsData = new ArrayList<>();
            claimsData.add(page);
            List<VerifierClaimDataResponseDTO> dtos = new ArrayList<>();
            for (ClaimsData claimData : claimsData) {
                category = claimData.getCategory();
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
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByAndIsActiveOrderById(claimData.getId(), "agent", true);
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
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.RELATIONSHIP_PROOF)) {
                        dto.setRelationshipDoc("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setRelationshipDoc("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setRelationshipDoc("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.GUARDIAN_ID_PROOF)) {
                        dto.setGuardianIdProof("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setGuardianIdProof("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setGuardianIdProof("REJECTED");
                        }
                    }
                    if (claimDocuments.getAgentDocType().equals(AgentDocType.GUARDIAN_ADD_PROOF)) {
                        dto.setGuardianAddressProof("UPLOADED");
                        if (claimDocuments.getIsVerified() && claimDocuments.getIsApproved()) {
                            dto.setGuardianAddressProof("APPROVED");
                        } else if (claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()) {
                            dto.setGuardianAddressProof("REJECTED");
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
                }
                dtos.add(dto);
            }
            map.put("claimDocuments", dtos);
            map.put("claimCategory", category);
            map.put("claimData", claimsData.get(0));
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
}

