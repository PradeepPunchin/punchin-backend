package com.punchin.controllers;

import com.punchin.dto.PageDTO;
import com.punchin.dto.VerifierClaimDataResponseDTO;
import com.punchin.dto.VerifierDashboardCountDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.ClaimStatus;
import com.punchin.service.UserService;
import com.punchin.service.VerifierService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@Slf4j
@RequestMapping(value = UrlMapping.VERIFIER, produces = MediaType.APPLICATION_JSON_VALUE)
public class VerifierController {

    @Autowired
    private UserService userService;

    @Autowired
    private VerifierService verifierService;

    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsData(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            page = page > 0 ? page - 1 : page;
            PageDTO allClaimsData = verifierService.getAllClaimsData(claimDataFilter, page, limit);
            return ResponseHandler.response(allClaimsData, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getVerifierDataDocumentClaimsData")
    public ResponseEntity<Object> getDataClaimsData(@RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData  page{}, limit{}", page, limit);
            List<VerifierClaimDataResponseDTO> allClaimsData = verifierService.getDataClaimsData(page, limit);
            if (!allClaimsData.isEmpty())
                return ResponseHandler.response(allClaimsData, ResponseMessgae.success, true, HttpStatus.OK);
            return ResponseHandler.response("", ResponseMessgae.backText, false, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Dashboard Data Count", notes = "This can be used to Show Dashboard data count in Verifier dashboard tab.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardDataCount() {
        try {
            log.info("VerifierController :: getDashboardDataCount");
            Map<String, Long> map = verifierService.getDashboardData();
            log.info("Verifier Dashboard count fetched Successfully");
            return ResponseHandler.response(map, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE fetching Verifier Dashboard Count ::", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = UrlMapping.VERIFIER_ALLOCATE_CLAIM)
    public ResponseEntity<Object> allocateClaimToAgent(@PathVariable Long claimId, @PathVariable Long agentId) {
        try {
            log.info("VerifierController :: allocateClaimToAgent claimId {}, agentId {}", claimId, agentId);
            ClaimsData claimsData = verifierService.getClaimData(claimId);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            User user = userService.findAgentById(agentId);
            if (Objects.isNull(user)) {
                return ResponseHandler.response(null, ResponseMessgae.invalidAgentId, false, HttpStatus.BAD_REQUEST);
            }
            boolean check = verifierService.allocateClaimToAgent(claimsData, user);
            if(check){
                return ResponseHandler.response(null, ResponseMessgae.claimAllocated, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: allocateClaimToAgent e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

