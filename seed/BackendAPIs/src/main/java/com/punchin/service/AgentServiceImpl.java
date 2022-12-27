package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.*;
import com.punchin.repository.ClaimAllocatedRepository;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
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
                statusList.add(ClaimStatus.AGENT_ALLOCATED);
                statusList.add(ClaimStatus.CLAIM_INTIMATED);
                statusList.add(ClaimStatus.CLAIM_SUBMITTED);
                page1 = claimsDataRepository.findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                statusList.add(ClaimStatus.IN_PROGRESS);
                page1 = claimsDataRepository.findByClaimStatusInAndAgentIdOrderByCreatedAtDesc(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                statusList.add(ClaimStatus.VERIFIER_DISCREPENCY);
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
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatusInAndAgentId(statusList, GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.ACTION_PENDING);
            statusList.add(ClaimStatus.CLAIM_SUBMITTED);
            statusList.add(ClaimStatus.CLAIM_INTIMATED);
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
            claimsData.setCauseOfDeath(documentDTO.getCauseOfDeath());
            claimsData.setIsMinor(documentDTO.isMinor());
            Map<String, MultipartFile> isMinorDoc = documentDTO.getIsMinorDoc();
            List<String> keys = new ArrayList<>(isMinorDoc.keySet());
            for(String key : keys){
                if(key.contains(":")){
                    String keyArray[] = key.split(":");
                    claimDocuments.add(uploadDocumentOnS3(AgentDocType.valueOf(keyArray[0].trim()), keyArray[1].trim(), claimsData, new MultipartFile[]{isMinorDoc.get(key)}));
                }else{
                    claimDocuments.add(uploadDocumentOnS3(AgentDocType.valueOf(key), key, claimsData, new MultipartFile[]{isMinorDoc.get(key)}));
                }
            }
            claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
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
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.getClaimDocumentWithDiscrepancyStatus(id);
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
    public Map<String, Object> discrepancyDocumentUpload(Long claimId, MultipartFile[] multipartFiles, String docType) {
        log.info("AgentServiceImpl :: discrepancyDocumentUpload claimsData {}, multipartFiles {}, docType {}", claimId, multipartFiles, docType);
        Map<String, Object> map = new HashMap<>();
        try {
            String oldDocType = docType;
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndAgentDocType(claimId, AgentDocType.valueOf(docType));
            if (!claimDocumentsList.isEmpty()) {
                for (ClaimDocuments claimDocuments : claimDocumentsList) {
                    claimDocuments.setIsActive(false);
                    oldDocType = claimDocuments.getDocType();
                }
                claimDocumentsList.forEach(claimDocuments -> {
                });
                claimDocumentsRepository.saveAll(claimDocumentsList);
            }
            ClaimsData claimsData = claimsDataRepository.findById(claimId).get();
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setAgentDocType(AgentDocType.valueOf(docType));
            claimDocuments.setDocType(oldDocType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
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
    public boolean checkDocumentIsInDiscrepancy(Long claimId, String docType) {
        try {
            log.info("AgentServiceImpl :: checkDocumentIsInDiscrepancy");
            ClaimDocuments claimDocuments = claimDocumentsRepository.findFirstByClaimsDataIdAndAgentDocTypeAndUploadSideByAndIsVerifiedAndIsApprovedOrderByIdDesc(claimId, AgentDocType.valueOf(docType), "agent", true, false);
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
            return claimDocumentsRepository.existsByClaimsDataIdAndUploadSideByAndIsVerified(claimId, "agent", false);
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
            //claimsData.setAgentToVerifier(true);
            //claimsData.setAgentToVerifierTime(System.currentTimeMillis());
            claimsDataRepository.save(claimsData);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: forwardToVerifier e{}", e);
            return e.getMessage();
        }
    }

    public ClaimDocuments uploadDocumentOnS3(AgentDocType agentDocType, String docType, ClaimsData claimsData, MultipartFile[] multipartFiles) {
        try {
            log.info("AgentServiceImpl :: uploadFiles claimsData {}, multipartFiles {}, docType {}", claimsData, multipartFiles, docType);
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setAgentDocType(agentDocType);
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setDocType(docType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(claimsData.getPunchinClaimId(), multipartFile, "agent"));
                documentUrls.add(urls);
            }
            documentUrlsRepository.saveAll(documentUrls);
            claimDocuments.setDocumentUrls(documentUrls);
            claimDocuments.setUploadTime(System.currentTimeMillis());
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
            agentSearchDTO1s.add(agentSearchDTO1);
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
            urls.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
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

    public String deleteClaimDocument(Long documentId) {
        Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(documentId);
        if (!optionalClaimDocuments.isPresent()) {
            return MessageCode.NO_RECORD_FOUND;
        }
        claimDocumentsRepository.delete(optionalClaimDocuments.get());
        return MessageCode.DOCUMENT_DELETED;
    }

    public List<UploadResponseUrl> uploadAgentNewDocument(Long id, CauseOfDeathEnum causeOfDeath, AgentDocType deathCertificate, MultipartFile[] deathCertificateMultipart, String nomineeStatus, AgentDocType signedClaim, MultipartFile[] signedClaimMultipart, AgentDocType relation_shipProof, MultipartFile[] relation_shipProofMultipart, AgentDocType gUARDIAN_ID_PROOF, MultipartFile[] gUARDIAN_ID_PROOFMultipart, AgentDocType gUARDIAN_ADD_PROOF, MultipartFile[] gUARDIAN_ADD_PROOFMultipart, AgentDocType borowerProof, MultipartFile[] borowerProofMultipart) {
        Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(id);
        if (!optionalClaimsData.isPresent()) {
            return Collections.emptyList();
        }
        List<UploadResponseUrl> urlResponseList = new ArrayList<>();
        ClaimsData claimsData = optionalClaimsData.get();
        claimsData.setCauseOfDeath(causeOfDeath);
        ClaimDocuments claimDocuments = new ClaimDocuments();
        claimDocuments.setAgentDocType(deathCertificate);
        claimDocuments.setDocType(deathCertificate.name());
        claimDocuments.setClaimsData(claimsData);
        claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments.setUploadSideBy("agent");
        List<DocumentUrls> documentUrls = new ArrayList<>();
        DocumentUrls urls = new DocumentUrls();
        List<String> urlList = new ArrayList<>();
        for (MultipartFile multipartFile : deathCertificateMultipart) {
            urls.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
            String url = urls.getDocUrl();
            urlList.add(url);
            documentUrls.add(urls);
        }
        UploadResponseUrl uploadResponseUrl = new UploadResponseUrl();
        uploadResponseUrl.setUrls(urlList);
        uploadResponseUrl.setDocType(deathCertificate.name());
        urlResponseList.add(uploadResponseUrl);
        documentUrlsRepository.saveAll(documentUrls);
        claimDocuments.setDocumentUrls(documentUrls);
        claimDocuments.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments);

        ClaimDocuments claimDocuments0 = new ClaimDocuments();
        claimDocuments0.setAgentDocType(borowerProof);
        claimDocuments0.setDocType(borowerProof.name());
        claimDocuments0.setClaimsData(claimsData);
        claimDocuments0.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments0.setUploadSideBy("agent");
        List<DocumentUrls> documentUrls0 = new ArrayList<>();
        DocumentUrls urls0 = new DocumentUrls();
        List<String> urlList0 = new ArrayList<>();
        for (MultipartFile multipartFile : borowerProofMultipart) {
            urls0.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
            String url = urls0.getDocUrl();
            urlList0.add(url);
            documentUrls0.add(urls0);
        }
        UploadResponseUrl uploadResponseUrl0 = new UploadResponseUrl();
        uploadResponseUrl0.setUrls(urlList0);
        uploadResponseUrl0.setDocType(borowerProof.name());
        urlResponseList.add(uploadResponseUrl0);
        documentUrlsRepository.saveAll(documentUrls0);
        claimDocuments0.setDocumentUrls(documentUrls0);
        claimDocuments0.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments0);

        ClaimDocuments claimDocuments1 = new ClaimDocuments();
        claimDocuments1.setAgentDocType(signedClaim);
        claimDocuments1.setDocType(signedClaim.name());
        claimDocuments1.setClaimsData(claimsData);
        claimDocuments1.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments1.setUploadSideBy("agent");
        List<DocumentUrls> documentUrls1 = new ArrayList<>();
        DocumentUrls urls1 = new DocumentUrls();
        List<String> urlList1 = new ArrayList<>();
        for (MultipartFile multipartFile : signedClaimMultipart) {
            urls1.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
            String url = urls1.getDocUrl();
            documentUrls1.add(urls1);
            urlList1.add(url);

        }
        UploadResponseUrl uploadResponseUrl1 = new UploadResponseUrl();
        uploadResponseUrl1.setUrls(urlList1);
        uploadResponseUrl1.setDocType(signedClaim.name());
        //documentUrls1.add(urls1);
        urlResponseList.add(uploadResponseUrl1);
        documentUrlsRepository.saveAll(documentUrls1);
        claimDocuments1.setDocumentUrls(documentUrls1);
        claimDocuments1.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments1);

        if (nomineeStatus.equalsIgnoreCase("Minor")) {
            ClaimDocuments claimDocuments2 = new ClaimDocuments();
            claimDocuments2.setAgentDocType(relation_shipProof);
            claimDocuments2.setDocType(relation_shipProof.name());
            claimDocuments2.setClaimsData(claimsData);
            claimDocuments2.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments2.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls2 = new ArrayList<>();
            List<String> urlList2 = new ArrayList<>();
            DocumentUrls urls2 = new DocumentUrls();
            for (MultipartFile multipartFile : relation_shipProofMultipart) {
                urls2.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
                String url = urls2.getDocUrl();
                urlList2.add(url);
                documentUrls2.add(urls2);
            }
            UploadResponseUrl uploadResponseUrl2 = new UploadResponseUrl();
            uploadResponseUrl2.setUrls(urlList2);
            uploadResponseUrl2.setDocType(relation_shipProof.name());
            urlResponseList.add(uploadResponseUrl2);
            documentUrlsRepository.saveAll(documentUrls2);
            claimDocuments2.setDocumentUrls(documentUrls2);
            claimDocuments2.setUploadTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments2);

            ClaimDocuments claimDocuments3 = new ClaimDocuments();
            claimDocuments3.setAgentDocType(gUARDIAN_ID_PROOF);
            claimDocuments3.setDocType(gUARDIAN_ID_PROOF.name());
            claimDocuments3.setClaimsData(claimsData);
            claimDocuments3.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments3.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls3 = new ArrayList<>();
            List<String> urlList3 = new ArrayList<>();
            DocumentUrls urls3 = new DocumentUrls();
            for (MultipartFile multipartFile : gUARDIAN_ID_PROOFMultipart) {
                urls3.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
                String url = urls3.getDocUrl();
                urlList3.add(url);
                documentUrls3.add(urls3);
            }
            UploadResponseUrl uploadResponseUrl3 = new UploadResponseUrl();
            uploadResponseUrl3.setUrls(urlList3);
            uploadResponseUrl3.setDocType(gUARDIAN_ID_PROOF.name());
            //documentUrls3.add(urls3);
            urlResponseList.add(uploadResponseUrl3);
            documentUrlsRepository.saveAll(documentUrls3);
            claimDocuments3.setDocumentUrls(documentUrls3);
            claimDocuments3.setUploadTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments3);

            ClaimDocuments claimDocuments4 = new ClaimDocuments();
            claimDocuments4.setAgentDocType(gUARDIAN_ADD_PROOF);
            claimDocuments4.setDocType(gUARDIAN_ADD_PROOF.name());
            claimDocuments4.setClaimsData(claimsData);
            claimDocuments4.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments4.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls4 = new ArrayList<>();
            List<String> urlList4 = new ArrayList<>();
            DocumentUrls urls4 = new DocumentUrls();
            for (MultipartFile multipartFile : gUARDIAN_ADD_PROOFMultipart) {
                urls4.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
                documentUrls4.add(urls4);
                String url = urls4.getDocUrl();
                urlList4.add(url);
            }
            UploadResponseUrl uploadResponseUrl4 = new UploadResponseUrl();
            uploadResponseUrl4.setUrls(urlList4);
            uploadResponseUrl4.setDocType(gUARDIAN_ID_PROOF.name());
            urlResponseList.add(uploadResponseUrl4);
            documentUrlsRepository.saveAll(documentUrls4);
            claimDocuments4.setDocumentUrls(documentUrls4);
            claimDocuments4.setUploadTime(System.currentTimeMillis());
            claimDocumentsRepository.save(claimDocuments4);
        }
        claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
        return urlResponseList;
    }

    public List<UploadResponseUrl> uploadAgentNewDocument2(Long id, KycOrAddressDocType nomineeProof, MultipartFile[] nomineeMultiparts, AgentDocType bankerProof, MultipartFile[] bankerPROOFMultipart, AgentDocType additionalDocs, MultipartFile[] additionalMultipart) {
        Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(id);
        if (!optionalClaimsData.isPresent()) {
            return Collections.emptyList();
        }
        List<UploadResponseUrl> urlResponseList = new ArrayList<>();
        ClaimsData claimsData = optionalClaimsData.get();
        ClaimDocuments claimDocuments = new ClaimDocuments();
        claimDocuments.setDocType(nomineeProof.toString());
        claimDocuments.setAgentDocType(AgentDocType.NOMINEE_ID_PROOF);
        claimDocuments.setDocType(AgentDocType.NOMINEE_ID_PROOF.name());
        claimDocuments.setClaimsData(claimsData);
        claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments.setUploadSideBy("agent");
        List<DocumentUrls> documentUrls = new ArrayList<>();
        List<String> urlList = new ArrayList<>();
        DocumentUrls urls = new DocumentUrls();
        for (MultipartFile multipartFile : nomineeMultiparts) {
            urls.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
            String url = urls.getDocUrl();
            documentUrls.add(urls);
            urlList.add(url);
        }
        UploadResponseUrl uploadResponseUrl = new UploadResponseUrl();
        uploadResponseUrl.setDocType(nomineeProof.toString());
        uploadResponseUrl.setUrls(urlList);
        urlResponseList.add(uploadResponseUrl);
        documentUrlsRepository.saveAll(documentUrls);
        claimDocuments.setDocumentUrls(documentUrls);
        claimDocuments.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments);

        ClaimDocuments claimDocuments1 = new ClaimDocuments();
        claimDocuments1.setDocType(bankerProof.name());
        claimDocuments1.setAgentDocType(bankerProof);
        claimDocuments1.setClaimsData(claimsData);
        claimDocuments1.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments1.setUploadSideBy("agent");
        List<String> urlList1 = new ArrayList<>();
        List<DocumentUrls> documentUrls1 = new ArrayList<>();
        DocumentUrls urls1 = new DocumentUrls();
        for (MultipartFile multipartFile : bankerPROOFMultipart) {
            urls1.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
            documentUrls1.add(urls1);
            String url = urls.getDocUrl();
            urlList1.add(url);
        }
        UploadResponseUrl uploadResponseUrl1 = new UploadResponseUrl();
        uploadResponseUrl1.setDocType(bankerProof.toString());
        uploadResponseUrl1.setUrls(urlList1);
        urlResponseList.add(uploadResponseUrl1);
        documentUrlsRepository.saveAll(documentUrls1);
        claimDocuments1.setDocumentUrls(documentUrls1);
        claimDocuments1.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments1);

        ClaimDocuments claimDocuments2 = new ClaimDocuments();
        claimDocuments2.setAgentDocType(additionalDocs);
        claimDocuments2.setClaimsData(claimsData);
        claimDocuments2.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
        claimDocuments2.setUploadSideBy("agent");
        List<String> urlList2 = new ArrayList<>();
        List<DocumentUrls> documentUrls2 = new ArrayList<>();
        DocumentUrls urls2 = new DocumentUrls();
        for (MultipartFile multipartFile : additionalMultipart) {
            urls2.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile, "agent"));
            documentUrls2.add(urls2);
            String url = urls2.getDocUrl();
            urlList2.add(url);
        }
        UploadResponseUrl uploadResponseUrl2 = new UploadResponseUrl();
        uploadResponseUrl2.setDocType(additionalDocs.toString());
        uploadResponseUrl2.setUrls(urlList2);
        urlResponseList.add(uploadResponseUrl2);
        documentUrlsRepository.saveAll(documentUrls2);
        claimDocuments2.setDocumentUrls(documentUrls2);
        claimDocuments2.setUploadTime(System.currentTimeMillis());
        claimDocumentsRepository.save(claimDocuments2);
        claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
        return urlResponseList;
    }



    private Map<String, Object> convertInDocumentStatusDTO(ClaimsData page) {
        Map<String, Object> map = new HashMap<>();
        try {
            log.info("Verifier Controller :: convertInDocumentStatusDTO page {}, limit {}", page);
            List<ClaimsData> claimsData = new ArrayList<>();
            claimsData.add(page);
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

