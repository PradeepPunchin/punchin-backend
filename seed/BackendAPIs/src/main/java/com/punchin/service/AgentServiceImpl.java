package com.punchin.service;

import com.punchin.dto.AgentUploadDocumentDTO;
import com.punchin.dto.ClaimDataDTO;
import com.punchin.dto.PageDTO;
import com.punchin.dto.AgentClaimListDTO;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimAllocatedRepository;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
import com.punchin.security.AmazonClient;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.constant.ResponseMessgae;
import lombok.extern.slf4j.Slf4j;
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
    public List<ClaimsData> getClaimsByAgentState(Integer page, Integer limit) {
        try {
            Pageable pageable = PageRequest.of(page, limit);
            return claimsDataRepository.getClaimsByAgentState(pageable);
        } catch (Exception e) {
            log.error("Error in getClaimsByAgentState ", e);
        }
        return Collections.emptyList();
    }

    @Override
    public PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        try {
            log.info("AgentServiceImpl :: getClaimsList dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit);
            Page<ClaimsData> page1 = Page.empty();
            if(claimDataFilter.ALLOCATED.equals(claimDataFilter)){
                page1 = claimsDataRepository.findAllByAgentAllocated(GenericUtils.getLoggedInUser().getId(), pageable);
            } else if(claimDataFilter.ACTION_PENDING.equals(claimDataFilter)){
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), ClaimStatus.ACTION_PENDING, pageable);
            } else if(claimDataFilter.WIP.equals(claimDataFilter)){
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), ClaimStatus.IN_PROGRESS, pageable);
            } else if(claimDataFilter.DISCREPENCY.equals(claimDataFilter)){
                page1 = claimsDataRepository.findAllByAgentAllocatedAndClaimStatus(GenericUtils.getLoggedInUser().getId(), ClaimStatus.VERIFIER_DISCREPENCY, pageable);
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
            map.put(ClaimStatus.AGENT_ALLOCATED.name(), claimAllocatedRepository.countByClaimStatusByAgent(ClaimStatus.AGENT_ALLOCATED.name(), GenericUtils.getLoggedInUser().getId()));
            map.put(ClaimStatus.IN_PROGRESS.name(), claimAllocatedRepository.countByClaimStatusByAgent(ClaimStatus.IN_PROGRESS.name(), GenericUtils.getLoggedInUser().getId()));
            map.put(ClaimStatus.ACTION_PENDING.name(), claimAllocatedRepository.countByClaimStatusByAgent(ClaimStatus.ACTION_PENDING.name(), GenericUtils.getLoggedInUser().getId()));
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.AGENT_ALLOCATED.name(), 0L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.ACTION_PENDING.name(), 0L);
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
                claimDocuments.add(uploadDocumentOnS3("SIGNED_FORM", claimsData, new MultipartFile[]{documentDTO.getSignedForm()}));
            }
            if(Objects.nonNull(documentDTO.getDeathCertificate())){
                claimDocuments.add(uploadDocumentOnS3("DEATH_CERTIFICATE", claimsData, new MultipartFile[]{documentDTO.getDeathCertificate()}));
            }
            if(Objects.nonNull(documentDTO.getBorrowerIdDoc())){
                claimDocuments.add(uploadDocumentOnS3(documentDTO.getBorrowerIdDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getBorrowerIdDoc()}));
            }
            if(Objects.nonNull(documentDTO.getBorrowerAddressDoc())){
                claimDocuments.add(uploadDocumentOnS3(documentDTO.getBorrowerAddressDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getBorrowerAddressDoc()}));
            }
            if(Objects.nonNull(documentDTO.getNomineeIdDoc())){
                claimDocuments.add(uploadDocumentOnS3(documentDTO.getNomineeIdDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getNomineeIdDoc()}));
            }
            if(Objects.nonNull(documentDTO.getNomineeAddressDoc())){
                claimDocuments.add(uploadDocumentOnS3(documentDTO.getNomineeAddressDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getNomineeAddressDoc()}));
            }
            if(Objects.nonNull(documentDTO.getBankAccountDoc())){
                claimDocuments.add(uploadDocumentOnS3(documentDTO.getBankAccountDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getBankAccountDoc()}));
            }
            if(Objects.nonNull(documentDTO.getFirOrPostmortemReport())){
                claimDocuments.add(uploadDocumentOnS3("FIR_POSTMORTEM_REPORT", claimsData, new MultipartFile[]{documentDTO.getFirOrPostmortemReport()}));
            }
            if(Objects.nonNull(documentDTO.getAdditionalDoc())){
                claimDocuments.add(uploadDocumentOnS3(documentDTO.getAdditionalDocType().getValue(), claimsData, new MultipartFile[]{documentDTO.getAdditionalDoc()}));
            }
            claimsData.setClaimStatus(ClaimStatus.UNDER_VERIFICATION);
            claimsData.setAgentToVerifier(true);
            claimsData.setAgentToVerifierTime(System.currentTimeMillis());
            ClaimDataDTO claimDataDTO = mapperService.map(claimsDataRepository.save(claimsData), ClaimDataDTO.class);
            claimDataDTO.setClaimDocuments(claimDocuments);
            map.put("claimsData", claimDataDTO);
            map.put("status", true);
            map.put("message", ResponseMessgae.success);
            return map;
        }catch (Exception e){
            log.error("EXCEPTION WHILE AgentServiceImpl :: uploadDocument e{}", e);
            map.put("claimsData", null);
            map.put("status", false);
            map.put("message", e.getMessage());
            return Collections.EMPTY_MAP;
        }
    }

    public ClaimDocuments uploadDocumentOnS3(String docType, ClaimsData claimsData, MultipartFile[] multipartFiles){
        try {
            log.info("AgentServiceImpl :: uploadFiles claimsData {}, multipartFiles {}, docType {}", claimsData, multipartFiles, docType);
            ClaimDocuments claimDocuments = new ClaimDocuments();
            claimDocuments.setClaimsData(claimsData);
            claimDocuments.setDocType(docType);
            claimDocuments.setUploadBy(GenericUtils.getLoggedInUser().getUserId());
            claimDocuments.setUploadSideBy("agent");
            List<DocumentUrls> documentUrls = new ArrayList<>();
            for (MultipartFile multipartFile : multipartFiles) {
                DocumentUrls urls = new DocumentUrls();
                urls.setDocUrl(amazonClient.uploadFile(multipartFile));
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
