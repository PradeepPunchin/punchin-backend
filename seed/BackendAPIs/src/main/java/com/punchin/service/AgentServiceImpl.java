package com.punchin.service;

import com.punchin.dto.PageDTO;
import com.punchin.entity.AgentClaimListDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.repository.ClaimAllocatedRepository;
import com.punchin.repository.ClaimsDataRepository;
import com.punchin.utility.GenericUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService{

    @Autowired
    private CommonUtilService commonService;

    @Autowired
    private ClaimsDataRepository claimsDataRepository;

    @Autowired
    private ClaimAllocatedRepository claimAllocatedRepository;

    @Override
    public PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit) {
        try{
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
            if(!page1.isEmpty()) {
                List<AgentClaimListDTO> agentClaimListDTOS = new ArrayList<>();
                List<ClaimsData> claimsDataList = page1.getContent();
                for (ClaimsData claimsData : claimsDataList) {
                    AgentClaimListDTO agentClaimListDTO = new AgentClaimListDTO();
                    agentClaimListDTO.setId(claimsData.getId());
                    agentClaimListDTO.setClaimDate(claimsData.getClaimInwardDate());
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
        }catch (Exception e){
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimsList e{}", e);
            return null;
        }
    }

    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> map = new HashMap<>();
        try{
            log.info("AgentServiceImpl :: getDashboardData");
            map.put(ClaimStatus.AGENT_ALLOCATED.name(), claimAllocatedRepository.countByAllocatedToAgent(GenericUtils.getLoggedInUser().getId()));
            map.put(ClaimStatus.IN_PROGRESS.name(), claimAllocatedRepository.countByClaimStatusByAgent(ClaimStatus.IN_PROGRESS.name(), GenericUtils.getLoggedInUser().getId()));
            map.put(ClaimStatus.ACTION_PENDING.name(), claimAllocatedRepository.countByClaimStatusByAgent(ClaimStatus.SETTLED.name(), GenericUtils.getLoggedInUser().getId()));
            return map;
        }catch (Exception e){
            log.error("EXCEPTION WHILE AgentServiceImpl :: getDashboardData e{}", e);
            map.put(ClaimStatus.ALL.name(), 0L);
            map.put(ClaimStatus.IN_PROGRESS.name(), 0L);
            map.put(ClaimStatus.SETTLED.name(), 0L);
            return map;
        }
    }

    @Override
    public boolean checkAccess(Long claimId) {
        try{
            log.info("AgentServiceImpl :: checkAccess");
            return claimAllocatedRepository.existsByUserIdAndClaimsDataId(GenericUtils.getLoggedInUser().getId(), claimId);
        }catch (Exception e){
            log.error("EXCEPTION WHILE AgentServiceImpl :: checkAccess e{}", e);
            return false;
        }
    }

    @Override
    public ClaimsData getClaimData(Long claimId) {
        try{
            log.info("AgentServiceImpl :: getClaimData");
            Optional<ClaimsData> optionalClaimsData = claimsDataRepository.findById(claimId);
            return optionalClaimsData.isPresent() ? optionalClaimsData.get() : null;
        }catch (Exception e){
            log.error("EXCEPTION WHILE AgentServiceImpl :: getClaimData e{}", e);
            return null;
        }
    }
}
