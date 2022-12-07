package com.punchin.service;

import com.punchin.entity.ClaimsData;

import java.util.List;

public interface AgentService {
    List<ClaimsData> getClaimsByAgentState(Integer page, Integer limit);
}
