package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.AgentDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimAllocatedRepository;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
import com.punchin.utility.GenericUtils;
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
    private AmazonClient amazonClient;

    @Override
    public PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        try {
            log.info("AgentServiceImpl :: getClaimsList dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Page<ClaimsData> page1 = Page.empty();
            List<String> statusList = new ArrayList<>();
            if(claimDataFilter.ALLOCATED.equals(claimDataFilter)){
                statusList.add(ClaimStatus.AGENT_ALLOCATED.name());
                statusList.add(ClaimStatus.IN_PROGRESS.name());
                statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.name());
                statusList.add(ClaimStatus.ACTION_PENDING.name());
                statusList.add(ClaimStatus.UNDER_VERIFICATION.name());
                page1 = claimsDataRepository.findAllByAgentAllocated(statusList, GenericUtils.getLoggedInUser().getId(), pageable);
            } else if(claimDataFilter.ACTION_PENDING.equals(claimDataFilter)){
                statusList.add(ClaimStatus.ACTION_PENDING.name());
                statusList.add(ClaimStatus.AGENT_ALLOCATED.name());
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), statusList, pageable);
            } else if(claimDataFilter.WIP.equals(claimDataFilter)){
                statusList.add(ClaimStatus.IN_PROGRESS.name());
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), statusList, pageable);
            } else if(claimDataFilter.DISCREPENCY.equals(claimDataFilter)){
                statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.name());
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), statusList, pageable);
            } else if(claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)){
                statusList.add(ClaimStatus.UNDER_VERIFICATION.name());
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), statusList, pageable);
            }
            if (!page1.isEmpty()) {
                List<AgentClaimListDTO> agentClaimListDTOS = new ArrayList<>();
                List<ClaimsData> claimsDataList = page1.getContent();
                for (ClaimsData claimsData : claimsDataList) {
                    AgentClaimListDTO agentClaimListDTO = new AgentClaimListDTO();
                    agentClaimListDTO.setId(claimsData.getId());
                    agentClaimListDTO.setClaimDate(claimsData.getClaimInwardDate());
                    agentClaimListDTO.setAllocationDate(new Date(claimAllocatedRepository.getAllocationDate(claimsData.getId(), GenericUtils.getLoggedInUser().getId())));
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
        try{
            log.info("AgentServiceImpl :: getDashboardData");
            List<String> statusList = new ArrayList<>();
            statusList.add(ClaimStatus.IN_PROGRESS.name());
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.name());
            map.put(ClaimStatus.IN_PROGRESS.name(), claimAllocatedRepository.countByClaimStatusByAgent(statusList, GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.ACTION_PENDING.name());
             statusList.add(ClaimStatus.AGENT_ALLOCATED.name());
            map.put(ClaimStatus.ACTION_PENDING.name(), claimAllocatedRepository.countByClaimStatusByAgent(statusList, GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.UNDER_VERIFICATION.name());
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), claimAllocatedRepository.countByClaimStatusByAgent(statusList, GenericUtils.getLoggedInUser().getId()));
            statusList.removeAll(statusList);
            statusList.add(ClaimStatus.AGENT_ALLOCATED.name());
            statusList.add(ClaimStatus.IN_PROGRESS.name());
            statusList.add(ClaimStatus.VERIFIER_DISCREPENCY.name());
            statusList.add(ClaimStatus.ACTION_PENDING.name());
            statusList.add(ClaimStatus.UNDER_VERIFICATION.name());
            map.put(ClaimStatus.AGENT_ALLOCATED.name(), claimAllocatedRepository.countByClaimStatusByAgent(statusList, GenericUtils.getLoggedInUser().getId()));
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
            return claimAllocatedRepository.existsByUserIdAndClaimsDataId(GenericUtils.getLoggedInUser().getId(), claimId);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: checkAccess e{}", e);
            return false;
        }
    }

    @Override
    public ClaimsData getClaimData(Long claimId) {
        try {
            log.info("AgentServiceImpl :: getClaimData");
            Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(claimId);
            return optionalClaimsData.isPresent() ? optionalClaimsData.get() : null;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimData e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> uploadDocument(AgentUploadDocumentDTO documentDTO) {
        Map<String, Object> map = new HashMap<>();
        try{
            log.info("AgentServiceImpl :: uploadDocument documentDTO {]", documentDTO);
            List<ClaimDocuments> claimDocuments = new ArrayList<>();
            ClaimsData claimsData = documentDTO.getClaimsData();
            claimsData.setCauseOfDeath(documentDTO.getCauseOfDeath());
            claimsData.setIsMinor(documentDTO.isMinor());
            if(Objects.nonNull(documentDTO.getSignedForm())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.SIGNED_FORM, "SIGNED_FORM", claimsData, new MultipartFile[]{documentDTO.getSignedForm()}));
            }
            if(Objects.nonNull(documentDTO.getDeathCertificate())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.DEATH_CERTIFICATE, "DEATH_CERTIFICATE", claimsData, new MultipartFile[]{documentDTO.getDeathCertificate()}));
            }
            if(Objects.nonNull(documentDTO.getBorrowerIdDoc())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.BORROWER_ID_PROOF, documentDTO.getBorrowerIdDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getBorrowerIdDoc()}));
            }
            if(Objects.nonNull(documentDTO.getBorrowerAddressDoc())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.BORROWER_ADDRESS_PROOF, documentDTO.getBorrowerAddressDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getBorrowerAddressDoc()}));
            }
            if(Objects.nonNull(documentDTO.getNomineeIdDoc())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.NOMINEE_ID_PROOF, documentDTO.getNomineeIdDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getNomineeIdDoc()}));
            }
            if(Objects.nonNull(documentDTO.getNomineeAddressDoc())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.NOMINEE_ADDRESS_PROOF, documentDTO.getNomineeAddressDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getNomineeAddressDoc()}));
            }
            if(Objects.nonNull(documentDTO.getBankAccountDoc())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.BANK_ACCOUNT_PROOF, documentDTO.getBankAccountDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getBankAccountDoc()}));
            }
            if(Objects.nonNull(documentDTO.getFirOrPostmortemReport())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.FIR_POSTMORTEM_REPORT, "FIR_POSTMORTEM_REPORT", claimsData, new MultipartFile[]{documentDTO.getFirOrPostmortemReport()}));
            }
            if(Objects.nonNull(documentDTO.getAdditionalDoc())){
                claimDocuments.add(uploadDocumentOnS3(AgentDocType.ADDITIONAL, documentDTO.getAdditionalDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getAdditionalDoc()}));
            }
            claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
            claimsData.setAgentToVerifier(true);
            claimsData.setAgentToVerifierTime(System.currentTimeMillis());
            ClaimDataDTO claimDataDTO = mapperService.map(claimsDataRepository.save(claimsData), ClaimDataDTO.class);
            claimDataDTO.setClaimDocuments(claimDocuments);
            map.put("claimsData", claimDataDTO);
            map.put("status", true);
            map.put("message", MessageCode.success);
            return map;
        }catch (Exception e){
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
                if(claimDocuments.getIsVerified() && !claimDocuments.getIsApproved()){
                    rejectedDocList.add(claimDocuments.getDocType());
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
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setClaimsData(claimsDataRepository.findById(claimId).get());
            claimDocuments.setDocType(docType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(claimDocuments.getClaimsData().getPunchinClaimId(), multipartFile));
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
            ClaimDocuments claimDocuments = claimDocumentsRepository.findFirstByClaimsDataIdAndDocTypeAndUploadSideByAndIsVerifiedAndIsApprovedOrderByIdDesc(claimId, docType, "agent", true, false);
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
            return claimDocumentsRepository.existsByClaimsDataIdAndUploadSideByAndIsVerified(claimId,"agent", false);
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
            claimsData.setAgentToVerifier(true);
            claimsData.setAgentToVerifierTime(System.currentTimeMillis());
            claimsDataRepository.save(claimsData);
            return MessageCode.success;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: forwardToVerifier e{}", e);
            return e.getMessage();
        }
    }

    public ClaimDocuments uploadDocumentOnS3(AgentDocType agentDocType, String docType, ClaimsData claimsData, MultipartFile[] multipartFiles){
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
                urls.setDocUrl(amazonClient.uploadFile(claimsData.getPunchinClaimId(), multipartFile));
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
}
