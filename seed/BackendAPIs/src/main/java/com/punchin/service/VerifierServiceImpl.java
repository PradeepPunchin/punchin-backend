package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimDocumentsStatus;
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
                    for (int i = 0; i <= claimDocuments.size(); i++) {
                        Map<String, Object> map = claimDocuments.get(i);
                        verifierClaimDataResponseDTO.setSingnedClaimDocument((String) map.get("SINGNED_CLAIM_FORM"));
                        verifierClaimDataResponseDTO.setDeathCertificate((String) map.get("DEATH_CERTIFICATE"));
                        verifierClaimDataResponseDTO.setBorrowerIdProof((String) map.get("BORROWER_ID_PROOF"));
                        verifierClaimDataResponseDTO.setBorrowerAddressProof((String) map.get("BORROWER_ADDRESS_PROOF"));
                        verifierClaimDataResponseDTO.setNomineeIdProof((String) map.get("NOMINEE_ID_PROOF"));
                        verifierClaimDataResponseDTO.setNomineeAddressProof((String) map.get("NOMINEE_ADDRESS_PROOF"));
                        verifierClaimDataResponseDTO.setBankAccountProof((String) map.get("BANK_ACCOUNT_PROOF"));
                        verifierClaimDataResponseDTO.setFIRPostmortemReport((String) map.get("FIR_POSTMORTEM_REPORT"));
                        verifierClaimDataResponseDTO.setAffidavit((String) map.get("AFFIDAVIT"));
                        verifierClaimDataResponseDTO.setDicrepancy((String) map.get("DISCREPANCY"));
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
            boolean present = isDocumentPresent(documentDetailsDTO.getDocumentName());
            if (present) {
                documentDetailsDTO.setDocumentUploaded(true);
            }
            List<DocumentUrls> documentUrlsList = documentUrlsRepository.findDocumentUrls(claimDocuments.getId());
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

    public static boolean isDocumentPresent(String documentName) {
        List<String> documentNameList = Arrays.asList("SINGNED_CLAIM_FORM", "DEATH_CERTIFICATE", "BORROWER_ID_PROOF", "BORROWER_ADDRESS_PROOF",
                "NOMINEE_ID_PROOF", "NOMINEE_ADDRESS_PROOF", "BANK_ACCOUNT_PROOF", "FIR_POSTMORTEM_REPORT", "AFFIDAVIT", "DISCREPANCY");
        return documentNameList.contains(documentName);
    }

    public String acceptAndRejectDocumentRequest(long claimDocumentId, String status) {
        log.info("Accept and Reject request received for claimDocumentId :: {}", claimDocumentId);
        Optional<ClaimDocuments> optionalClaimDocuments = claimDocumentsRepository.findById(claimDocumentId);
        if (!optionalClaimDocuments.isPresent()) {
            log.info("Claim document list not found for claimDocumentId :: {}", claimDocumentId);
            return null;
        }
        ClaimDocuments claimDocuments = optionalClaimDocuments.get();
        if (status.equalsIgnoreCase(Literals.APPROVE)) {
            claimDocuments.setClaimDocumentsStatus(ClaimDocumentsStatus.APPROVED);
        } else {
            claimDocuments.setClaimDocumentsStatus(ClaimDocumentsStatus.REJECTED);
            ClaimsData claimsData = claimDocuments.getClaimsData();
            claimsData.setClaimStatus(ClaimStatus.VERIFIER_DISCREPENCY);
            log.info("Claim status changed to VERIFIER_DISCREPANCY and saved successfully ");
            claimsDataRepository.save(claimsData);
        }
        log.info("Claim document saved successfully ");
        claimDocumentsRepository.save(claimDocuments);
        return ResponseMessgae.success;
    }
}

