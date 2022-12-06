

package com.punchin.controllers;

import com.punchin.dto.PageDTO;
import com.punchin.dto.VerifierClaimDataResponseDTO;
import com.punchin.dto.VerifierDashboardCountDTO;
import com.punchin.enums.ClaimStatus;
import com.punchin.service.VerifierService;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.ResponseMessgae;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequestMapping(value = "/verifier", produces = MediaType.APPLICATION_JSON_VALUE)
public class VerifierController {

    @Autowired
    private VerifierService verifierService;

    @GetMapping(value = "/getVerifierClaimsData")
    public ResponseEntity<Object> getClaimsData(@RequestParam ClaimStatus claimStatus, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData dataFilter{}, page{}, limit{}", claimStatus, page, limit);
            PageDTO allClaimsData = verifierService.getAllClaimsData(claimStatus, page, limit);
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
    @GetMapping(value = UrlMapping.VERIFIER_GET_DASHBOARD_DATA_COUNT)
    public ResponseEntity<Object> getDashboardDataCount() {
        try {
            log.info("VerifierController :: getDashboardDataCount");
            VerifierDashboardCountDTO verifierDashboardCountDTO = verifierService.getDashboardDataCount();
            log.info("Verifier Dashboard count fetched Successfully");
            return ResponseHandler.response(verifierDashboardCountDTO, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE fetching Verifier Dashboard Count ::", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}

