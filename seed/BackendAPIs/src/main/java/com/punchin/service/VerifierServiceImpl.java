package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.*;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimAllocatedRepository;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
import com.punchin.utility.ObjectMapperUtils;
import com.punchin.utility.constant.Literals;
import com.punchin.utility.constant.ResponseMessgae;
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

    @Override
    public List<VerifierClaimDataResponseDTO> getDataClaimsData(Integer pageNo, Integer pageSize) {
        try {
            log.info("BankerController :: getAllClaimsData  page{}, limit{}", pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("punchin_claim_id"));
            List<ClaimDataResponse> allClaimData = claimsDataRepository.findClaimsDataVerifier(pageable);
            List<VerifierClaimDataResponseDTO> verifierClaimDataResponseDTOS = ObjectMapperUtils.mapAll(allClaimData, VerifierClaimDataResponseDTO.class);
            for (VerifierClaimDataResponseDTO verifierClaimDataResponseDTO : verifierClaimDataResponseDTOS) {
                List<Map<String, Object>> claimDocuments = claimDocumentsRepository.getAllClaimDocument(verifierClaimDataResponseDTO.getId());
                if (!claimDocuments.isEmpty())
                    for (int i = 0; i < claimDocuments.size(); i++) {
                        Map<String, Object> map = claimDocuments.get(i);
                        String docType = (String) map.get("doc_type");
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getSingnedClaimDocument()) || Objects.isNull(verifierClaimDataResponseDTO.getSingnedClaimDocument()))
                            verifierClaimDataResponseDTO.setSingnedClaimDocument(docType.equalsIgnoreCase("SINGNED_CLAIM_FORM"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getDeathCertificate()) || Objects.isNull(verifierClaimDataResponseDTO.getDeathCertificate()))
                            verifierClaimDataResponseDTO.setDeathCertificate(docType.equalsIgnoreCase("DEATH_CERTIFICATE"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getBorrowerIdProof()) || Objects.isNull(verifierClaimDataResponseDTO.getBorrowerIdProof()))
                            verifierClaimDataResponseDTO.setBorrowerIdProof(docType.equalsIgnoreCase("BORROWER_ID_PROOF"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getBorrowerAddressProof()) || Objects.isNull(verifierClaimDataResponseDTO.getBorrowerAddressProof()))
                            verifierClaimDataResponseDTO.setBorrowerAddressProof(docType.equalsIgnoreCase("BORROWER_ADDRESS_PROOF"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getNomineeIdProof()) || Objects.isNull(verifierClaimDataResponseDTO.getNomineeIdProof()))
                            verifierClaimDataResponseDTO.setNomineeIdProof(docType.equalsIgnoreCase("NOMINEE_ID_PROOF"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getNomineeAddressProof()) || Objects.isNull(verifierClaimDataResponseDTO.getNomineeAddressProof()))
                            verifierClaimDataResponseDTO.setNomineeAddressProof(docType.equalsIgnoreCase("NOMINEE_ADDRESS_PROOF"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getBankAccountProof()) || Objects.isNull(verifierClaimDataResponseDTO.getBankAccountProof()))
                            verifierClaimDataResponseDTO.setBankAccountProof(docType.equalsIgnoreCase("BANK_ACCOUNT_PROOF"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getFIRPostmortemReport()) || Objects.isNull(verifierClaimDataResponseDTO.getFIRPostmortemReport()))
                            verifierClaimDataResponseDTO.setFIRPostmortemReport(docType.equalsIgnoreCase("FIR_POSTMORTEM_REPORT"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getAffidavit()) || Objects.isNull(verifierClaimDataResponseDTO.getAffidavit()))
                            verifierClaimDataResponseDTO.setAffidavit(docType.equalsIgnoreCase("AFFIDAVIT"));
                        if (Boolean.FALSE.equals(verifierClaimDataResponseDTO.getDicrepancy()) || Objects.isNull(verifierClaimDataResponseDTO.getDicrepancy()))
                            verifierClaimDataResponseDTO.setDicrepancy(docType.equalsIgnoreCase("DISCREPANCY"));
                    }
                else {
                    verifierClaimDataResponseDTO.setSingnedClaimDocument(false);
                    verifierClaimDataResponseDTO.setDeathCertificate(false);
                    verifierClaimDataResponseDTO.setBorrowerIdProof(false);
                    verifierClaimDataResponseDTO.setBorrowerAddressProof(false);
                    verifierClaimDataResponseDTO.setNomineeIdProof(false);
                    verifierClaimDataResponseDTO.setNomineeAddressProof(false);
                    verifierClaimDataResponseDTO.setBankAccountProof(false);
                    verifierClaimDataResponseDTO.setFIRPostmortemReport(false);
                    verifierClaimDataResponseDTO.setAffidavit(false);
                    verifierClaimDataResponseDTO.setDicrepancy(false);
                }
            }
            return verifierClaimDataResponseDTOS;
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getAllClaimsData", e);
            return Collections.emptyList();
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

    public VerifierDocDetailsResponseDTO getDocumentDetails(long claimDataId) {
        log.info("VerifierController :: getDocumentDetails");
        ClaimsData claimsData = claimsDataRepository.findClaimDataForVerifier(claimDataId);
        if (claimsData == null) {
            log.info("Claim data not found for claimId :: {}", claimDataId);
            return null;
        }
        VerifierDocDetailsResponseDTO verifierDocDetailsResponseDTO = new VerifierDocDetailsResponseDTO();
        verifierDocDetailsResponseDTO.setBorrowerName(claimsData.getBorrowerName());
        verifierDocDetailsResponseDTO.setBorrowerAddress(claimsData.getBorrowerAddress());
        verifierDocDetailsResponseDTO.setLoanAccountNumber(claimsData.getLoanAccountNumber());
        verifierDocDetailsResponseDTO.setInsurerName(claimsData.getInsurerName());
        verifierDocDetailsResponseDTO.setNomineeName(claimsData.getNomineeName());
        verifierDocDetailsResponseDTO.setNomineeAddress(claimsData.getNomineeAddress());
        verifierDocDetailsResponseDTO.setNomineeRelationShip(claimsData.getNomineeRelationShip());
        List<ClaimDocuments> claimDocumentsList = claimDocumentsRepository.findClaimDocumentsByClaimDataId(claimsData.getId());
        if (claimDocumentsList.isEmpty()) {
            log.info("Claim document list not found for claimId :: {}", claimDataId);
            return null;
        }
        List<DocumentDetailsDTO> documentDetailsDTOList = new ArrayList<>();
        for (ClaimDocuments claimDocuments : claimDocumentsList) {
            DocumentDetailsDTO documentDetailsDTO = new DocumentDetailsDTO();
            documentDetailsDTO.setDocumentId(claimDocuments.getId());
            documentDetailsDTO.setDocumentName(claimDocuments.getDocType());
            documentDetailsDTO.setDocumentUploaded(true);
            List<DocumentUrls> documentUrlsList = documentUrlsRepository.findDocumentUrlsByClaimDocumentId(claimDocuments.getId());
            if (documentUrlsList.isEmpty()) {
                log.info("Claim document url list not found for claimDocuments :: {}", claimDocuments.getId());
                return null;
            }
            List<DocumentUrlListDTO> documentUrlListDTOList = new ArrayList<>();
            for (DocumentUrls documentUrls : documentUrlsList) {
                DocumentUrlListDTO documentUrlListDTO = new DocumentUrlListDTO();
                documentUrlListDTO.setDocumentUrlId(documentUrls.getId());
                documentUrlListDTO.setDocumentUrl(documentUrls.getDocUrl());
                String fileFormat = FilenameUtils.getExtension(documentUrlListDTO.getDocumentUrl());
                documentUrlListDTO.setDocFormat(fileFormat);
                documentUrlListDTOList.add(documentUrlListDTO);
            }
            documentDetailsDTO.setDocumentUrlListDTOList(documentUrlListDTOList);
            documentDetailsDTOList.add(documentDetailsDTO);
        }
        verifierDocDetailsResponseDTO.setDocumentDetailsDTOList(documentDetailsDTOList);
        log.info("Claim document details fetched successfully");
        return verifierDocDetailsResponseDTO;
    }

    public String acceptAndRejectDocumentRequest(long claimDocumentId, String status, String reason, String remark) {
        log.info("Accept and Reject request received for claimDocumentId :: {}", claimDocumentId);
        Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(claimDocumentId);
        if (!optionalClaimDocuments.isPresent()) {
            log.info("Claim document list not found for claimDocumentId :: {}", claimDocumentId);
            return null;
        }
        ClaimDocuments claimDocuments = optionalClaimDocuments.get();
        if (claimDocuments.getIsApproved() != null || claimDocuments.getIsApproved().equals(true)) {
            log.info("Claim document already approved for :: {}", claimDocumentId);
            return null;
        }
        ClaimsData claimsData = claimDocuments.getClaimsData();
        if (status.equalsIgnoreCase(Literals.APPROVE) && StringUtils.isBlank(reason) && StringUtils.isBlank(remark)) {
            claimDocuments.setIsApproved(true);
            Long documentCounts = claimDocumentsRepository.findApprovedClaimDocumentsByClaimDataId(claimsData.getId());
            if (documentCounts == 10L) {
                claimsData.setClaimStatus(ClaimStatus.SUBMITTED_TO_INSURER);
            }
        } else {
            claimDocuments.setIsApproved(false);
            claimDocuments.setReason(reason);
            claimDocuments.setRejectRemark(remark);
            claimsData.setClaimStatus(ClaimStatus.VERIFIER_DISCREPENCY);
        }
        log.info("Claim status changed and saved successfully ");
        claimsDataRepository.save(claimsData);
        log.info("Claim document saved successfully ");
        claimDocumentsRepository.save(claimDocuments);
        return ResponseMessgae.success;
    }

}

