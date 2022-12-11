package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.*;
import com.punchin.enums.AgentDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.enums.KycOrAddressDocType;
import com.punchin.repository.ClaimAllocatedRepository;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ObjectMapperUtils;
import com.punchin.utility.constant.Literals;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
    private DocumentUrlsRepository documentUrlsRepository;

    @Override
    public PageDTO getAllClaimsData(ClaimDataFilter claimDataFilter, Integer pageNo, Integer pageSize) {
        Page<ClaimsData> page1 = Page.empty();
        try {
            log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimDataFilter, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize);
            if (claimDataFilter.ACTION_PENDING.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.ACTION_PENDING, true, pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.IN_PROGRESS, true, pageable);
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.UNDER_VERIFICATION, true, pageable);
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.SETTLED, true, pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.VERIFIER_DISCREPENCY, true, pageable);
            }
            return commonService.convertPageToDTO(page1.getContent(), page1);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getAllClaimsData", e);
            return commonService.convertPageToDTO(page1.getContent(), page1);
        }
    }

    public Map<String, Long> getDashboardData() {
        Map<String, Long> map = new HashMap<>();
        try {
            log.info("VerifierServiceImpl :: getDashboardData");
            map.put(ClaimStatus.IN_PROGRESS.name(), claimsDataRepository.countByClaimStatus(ClaimStatus.IN_PROGRESS));
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), claimsDataRepository.countByClaimStatus(ClaimStatus.UNDER_VERIFICATION));
            map.put(ClaimStatus.SETTLED.name(), claimsDataRepository.countByClaimStatus(ClaimStatus.SETTLED));
            return map;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.UNDER_VERIFICATION.name(), 0L);
            map.put(ClaimStatus.SETTLED.name(), 0L);
            return map;
        }
    }

    @Override
    public ClaimsData getClaimData(Long claimId) {
        try {
            log.info("VerifierServiceImpl :: getClaimData");
            return claimsDataRepository.findByIdAndIsForwardToVerifier(claimId, true);
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
            List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideBy(claimsData.getId(), "agent");
            List<ClaimDocumentsDTO> claimDocumentsDTOS = new ArrayList<>();
            for (ClaimDocuments claimDocuments : claimDocumentsList) {
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
                claimDocumentsDTOS.add(claimDocumentsDTO);
            }
            claimDetailForVerificationDTO.setClaimDocumentsDTOS(claimDocumentsDTOS);
            return claimDetailForVerificationDTO;
        } catch (Exception e){
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
            if (!approveRejectPayloadDTO.isApproved()) {
                claimsData.setClaimStatus(ClaimStatus.VERIFIER_DISCREPENCY);
                claimsDataRepository.save(claimsData);
            }
            return MessageCode.success;
        } catch (Exception e){
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
    public PageDTO getClaimDataWithDocumentStatus(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        Page<ClaimsData> page1 = Page.empty();
        try {
            log.info("BankerController :: getClaimDataWithDocumentStatus dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            Pageable pageable = PageRequest.of(page, limit, Sort.by("punchin_claim_id").descending());

            if (claimDataFilter.ACTION_PENDING.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.ACTION_PENDING, true, pageable);
            } else if (claimDataFilter.WIP.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.IN_PROGRESS, true, pageable);
            } else if (claimDataFilter.UNDER_VERIFICATION.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.UNDER_VERIFICATION, true, pageable);
            } else if (claimDataFilter.SETTLED.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.SETTLED, true, pageable);
            } else if (claimDataFilter.DISCREPENCY.equals(claimDataFilter)) {
                page1 = claimsDataRepository.findByClaimStatusAndIsForwardToVerifier(ClaimStatus.VERIFIER_DISCREPENCY, true, pageable);
            }
            List<ClaimsData> claimsData = page1.getContent();
            List<VerifierClaimDataResponseDTO> dtos = new ArrayList<>();
            for(ClaimsData claimData : claimsData){
                VerifierClaimDataResponseDTO dto = new VerifierClaimDataResponseDTO();
                dto.setId(claimData.getId());
                dto.setClaimDate(claimData.getClaimInwardDate());
                dto.setBorrowerName(claimData.getBorrowerName());
                dto.setNomineeAddress(claimData.getNomineeAddress());
                dto.setNomineeName(claimData.getNomineeName());
                dto.setNomineeContactNumber(claimData.getNomineeContactNumber());
                List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findByClaimsDataIdAndUploadSideByOrderById(claimData.getId(), "agent");
                for(ClaimDocuments claimDocuments : claimDocumentsList){
                    dto.setSingnedClaimDocument(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.SIGNED_FORM)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setSingnedClaimDocument(true);
                        }else {
                            dto.setSingnedClaimDocument(false);
                        }
                    }
                    dto.setDeathCertificate(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.DEATH_CERTIFICATE)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setDeathCertificate(true);
                        }else {
                            dto.setDeathCertificate(false);
                        }
                    }
                    dto.setBorrowerIdProof(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.BORROWER_ID_PROOF)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setBorrowerIdProof(true);
                        }else {
                            dto.setBorrowerIdProof(false);
                        }
                    }
                    dto.setBorrowerAddressProof(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.BORROWER_ADDRESS_PROOF)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setBorrowerAddressProof(true);
                        }else {
                            dto.setBorrowerAddressProof(false);
                        }
                    }
                    dto.setNomineeIdProof(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.NOMINEE_ID_PROOF)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setNomineeIdProof(true);
                        }else {
                            dto.setNomineeIdProof(false);
                        }
                    }
                    dto.setNomineeAddressProof(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.NOMINEE_ADDRESS_PROOF)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setNomineeAddressProof(true);
                        }else {
                            dto.setNomineeAddressProof(false);
                        }
                    }
                    dto.setBankAccountProof(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.BANK_ACCOUNT_PROOF)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setBankAccountProof(true);
                        }else {
                            dto.setBankAccountProof(false);
                        }
                    }
                    dto.setFirPostmortemReport(null);
                    if(claimDocuments.getAgentDocType().equals(AgentDocType.FIR_POSTMORTEM_REPORT)) {
                        if(claimDocuments.getIsVerified() && claimDocuments.getIsApproved()){
                            dto.setFirPostmortemReport(true);
                        }else {
                            dto.setFirPostmortemReport(false);
                        }
                    }
                }
                dtos.add(dto);
            }

            return commonService.convertPageToDTO(dtos, page1);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getClaimDataWithDocumentStatus", e);
            return commonService.convertPageToDTO(page1.getContent(), page1);
        }
    }

}

