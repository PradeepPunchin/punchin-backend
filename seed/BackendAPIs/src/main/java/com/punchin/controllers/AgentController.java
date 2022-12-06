package com.punchin.controllers;

import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.DocType;
import com.punchin.service.AgentService;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.ResponseMessgae;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(value = UrlMapping.AGENT, produces = MediaType.APPLICATION_JSON_VALUE)
public class AgentController {

    @Autowired
    private AgentService agentService;

    @ApiOperation(value = "Dashboard Data", notes = "This can be used to Show count in dashboard tile.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("AgentController :: getDashboardData");
            Map<String, Long> map = agentService.getDashboardData();
            return ResponseHandler.response(map, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getDashboardData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Claim List", notes = "This can be used to get not allocated claims list")
    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsList(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("AgentController :: getClaimsList dataFilter {}, page {}, limit {}", claimDataFilter, page, limit);
            PageDTO pageDTO = agentService.getClaimsList(claimDataFilter, page, limit);
            return ResponseHandler.response(pageDTO, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimsList e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Claim List", notes = "This can be used to get Allocated claim data")
    @GetMapping(value = UrlMapping.GET_CLAIM_DATA)
    public ResponseEntity<Object> getClaimData(@PathVariable Long claimId) {
        try {
            log.info("AgentController :: getClaimData claimId {}", claimId);
            if(agentService.checkAccess(claimId)){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            ClaimsData claimsData = agentService.getClaimData(claimId);
            if(Objects.nonNull(claimsData)) {
                return ResponseHandler.response(claimsData, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimData e {}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /*@ApiOperation(value = "Upload claim document", notes = "This can be used to upload document regarding claim by verifier")
    @PutMapping(value = UrlMapping.UPLOAD_DOCUMENT)
    public ResponseEntity<Object> uploadDocument(@PathVariable Long claimId, @PathVariable DocType docType, @ApiParam(name = "multipartFiles", value = "The multipart object as an array to upload multiple files.") @Valid @RequestBody MultipartFile[] multipartFiles) {
        try {
            log.info("AgentController :: uploadDocument claimId {}, multipartFiles {}, docType {}", claimId, multipartFiles, docType);
            if(agentService.checkAccess(claimId)){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            ClaimsData claimsData = bankerService.getClaimData(claimId);
            if(Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> result = bankerService.uploadDocument(claimsData, multipartFiles, docType);
            if(result.get("message").equals(ResponseMessgae.success)) {
                return ResponseHandler.response(result, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getAllClaimsData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/
}
