package com.punchin.service;

import com.punchin.entity.ClaimsData;
import com.punchin.repository.ClaimsDataRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class AgentServiceImpl implements AgentService {

    @Autowired
    private ClaimsDataRepository claimsDataRepository;

    @Override
    public List<ClaimsData> getClaimsByAgentState(Integer page, Integer limit) {
        try {
            Pageable pageable= PageRequest.of(page,limit);
            return claimsDataRepository.getClaimsByAgentState(pageable);
        } catch (Exception e) {
            log.error("Error in getClaimsByAgentState ", e);
        }
        return Collections.emptyList();
    }
}
