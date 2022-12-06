package com.punchin.service;

import com.punchin.dto.*;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.utility.ObjectMapperUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
}

