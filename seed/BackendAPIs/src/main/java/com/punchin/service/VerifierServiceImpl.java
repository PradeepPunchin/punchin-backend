package com.punchin.service;

import com.punchin.dto.ClaimDataDTO;
import com.punchin.dto.PageDTO;
import com.punchin.dto.VerifierDashboardCountDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimsDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class VerifierServiceImpl implements VerifierService {
    @Autowired
    private ClaimsDataRepository claimsDataRepository;
    @Autowired
    private ModelMapperService modelMapperService;
    @Autowired
    private CommonUtilService commonUtilService;

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
    public PageDTO getDataClaimsData(ClaimStatus claimStatus, Integer pageNo, Integer pageSize) {
        try {
            log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimStatus, pageNo, pageSize);
            Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("punchin_claim_id"));
            Page<ClaimsData> allClaimData = claimsDataRepository.findClaimDataByStatus(claimStatus.toString(), pageable);
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

    public VerifierDashboardCountDTO getDashboardDataCount(){
        log.info("VerifierController :: getDashboardDataCount");
        VerifierDashboardCountDTO verifierDashboardCountDTO= new VerifierDashboardCountDTO();
        verifierDashboardCountDTO.setAllocatedCount(0L);
        verifierDashboardCountDTO.setInProgressCount(claimsDataRepository.countByClaimStatus(ClaimStatus.IN_PROGRESS));
        verifierDashboardCountDTO.setActionPendingCount(claimsDataRepository.countByClaimStatus(ClaimStatus.ACTION_PENDING));
        return verifierDashboardCountDTO;
    }
}

