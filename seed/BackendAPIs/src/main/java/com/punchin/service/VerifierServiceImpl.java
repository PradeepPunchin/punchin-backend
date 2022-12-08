package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.repository.DocumentUrlsRepository;
import com.punchin.utility.ObjectMapperUtils;
import com.punchin.utility.constant.Literals;
import com.punchin.utility.constant.ResponseMessgae;
import lombok.extern.slf4j.Slf4j;
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
    private CommonUtilService commonUtilService;
    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;

    @Autowired
    private DocumentUrlsRepository documentUrlsRepository;

    @Override
    public PageDTO getAllClaimsData(ClaimStatus claimStatus, Integer pageNo, Integer pageSize) {
        try {
            log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimStatus, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("punchin_claim_id"));
            Page<ClaimsData> allClaimData;
            if (ClaimDataFilter.ALL.toString().equals(claimStatus.toString()))
                allClaimData = claimsDataRepository.findAllClaimData(pageable);
            else
                allClaimData = claimsDataRepository.findClaimDataByStatus(claimStatus.toString(), pageable);
            List<ClaimDataDTO> claimDataDTOList = new ArrayList<>();
            for (ClaimsData claimData : allClaimData) {
                ClaimDataDTO map = modelMapperService.map(claimData, ClaimDataDTO.class);
                claimDataDTOList.add(map);
            }
            return commonUtilService.getDetailsPage(claimDataDTOList, allClaimData.getContent().size(), allClaimData.getTotalPages(), allClaimData.getTotalElements());
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierServiceImpl :: getAllClaimsData", e);
            return null;
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

    public VerifierDashboardCountDTO getDashboardDataCount() {
        log.info("VerifierController :: getDashboardDataCount");
        VerifierDashboardCountDTO verifierDashboardCountDTO = new VerifierDashboardCountDTO();
        verifierDashboardCountDTO.setAllocatedCount(0L);
        verifierDashboardCountDTO.setInProgressCount(claimsDataRepository.countByClaimStatus(ClaimStatus.IN_PROGRESS));
        verifierDashboardCountDTO.setActionPendingCount(claimsDataRepository.countByClaimStatus(ClaimStatus.ACTION_PENDING));
        return verifierDashboardCountDTO;
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
                documentUrlListDTOList.add(documentUrlListDTO);
            }
            documentDetailsDTO.setDocumentUrlListDTOList(documentUrlListDTOList);
            documentDetailsDTOList.add(documentDetailsDTO);
        }
        verifierDocDetailsResponseDTO.setDocumentDetailsDTOList(documentDetailsDTOList);
        log.info("Claim document details fetched successfully");
        return verifierDocDetailsResponseDTO;
    }

    public String acceptAndRejectDocumentRequest(long claimDocumentId, String status, long claimDataId) {
        log.info("Accept and Reject request received for claimDocumentId :: {}", claimDocumentId);
        Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(claimDocumentId);
        if (!optionalClaimDocuments.isPresent()) {
            log.info("Claim document list not found for claimDocumentId :: {}", claimDocumentId);
            return null;
        }
        ClaimDocuments claimDocuments = optionalClaimDocuments.get();
        ClaimsData claimsData = claimDocuments.getClaimsData();
        if (status.equalsIgnoreCase(Literals.APPROVE)) {
            claimDocuments.setIsApproved(true);
            Long documentCounts = claimDocumentsRepository.findApprovedClaimDocumentsByClaimDataId(claimDataId);
            if (documentCounts == 10L) {
                claimsData.setClaimStatus(ClaimStatus.SUBMITTED_TO_INSURER);
            }
        } else {
            claimDocuments.setIsApproved(false);
            claimsData.setClaimStatus(ClaimStatus.VERIFIER_DISCREPENCY);
        }
        log.info("Claim status changed and saved successfully ");
        claimsDataRepository.save(claimsData);
        log.info("Claim document saved successfully ");
        claimDocumentsRepository.save(claimDocuments);
        return ResponseMessgae.success;
    }
}

