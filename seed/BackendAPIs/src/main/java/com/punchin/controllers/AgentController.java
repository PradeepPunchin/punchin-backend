package com.punchin.controllers;

import com.punchin.entity.ClaimsData;
import com.punchin.service.AgentService;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.ResponseMessgae;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AgentController {

    @Autowired
    private AgentService agentService;

    @GetMapping(value = "/getVerifierDataDocumentClaimsData")
    public ResponseEntity<Object> getDataClaimsData(@RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData  page{}, limit{}", page, limit);
            List<ClaimsData> claimsByAgentState = agentService.getClaimsByAgentState(page, limit);
            if (!claimsByAgentState.isEmpty())
                return ResponseHandler.response(claimsByAgentState, ResponseMessgae.success, true, HttpStatus.OK);
            return ResponseHandler.response("", ResponseMessgae.backText, false, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
