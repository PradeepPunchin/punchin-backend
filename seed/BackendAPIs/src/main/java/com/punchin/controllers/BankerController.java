package com.punchin.controllers;

import com.punchin.enums.ClaimStatus;
import com.punchin.service.BankerService;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.ResponseMessgae;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/banker", produces = MediaType.APPLICATION_JSON_VALUE)
public class BankerController {

    @Autowired
    private BankerService bankerService;

    @GetMapping(value = "/getDashboardData")
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("BankerController :: getDashboardData");
            Map<String, Long> map = bankerService.getDashboardData();
            return ResponseHandler.response(map, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getDashboardData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured("BANKER")
    @ApiOperation(value = "User Login", notes = "This can be used to Upload spreadsheet for claims data")
    @PostMapping(value = "/upload")
    public ResponseEntity<Object> uploadClaimData(@ApiParam(name = "files", value = "The multipart object as an array to upload multiple files.") @Valid @RequestParam("files") MultipartFile[] files) {
        try {
            log.info("BankerController :: uploadClaimData files{}", files.length);
            Map<String, Object> map = new HashMap<>();
            if (files.length > 0) {
                for (MultipartFile file : files) {
                    if (!GenericUtils.checkExcelFormat(file)) {
                        return ResponseHandler.response(null, ResponseMessgae.invalidFormat, false, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                    }
                }
                map = bankerService.saveUploadExcelData(files);
                if (Boolean.getBoolean(map.get("status").toString())) {
                    return ResponseHandler.response(map.get("data"), ResponseMessgae.success, true, HttpStatus.CREATED);
                }
            }
            return ResponseHandler.response(null, map.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: uploadClaimData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/getClaimsData")
    public ResponseEntity<Object> getClaimsData(@RequestParam ClaimStatus claimStatus, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimStatus, page, limit);
            Page pageDTO = bankerService.getAllClaimsData(claimStatus, page, limit);
            return ResponseHandler.response(pageDTO, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/submitClaims")
    public ResponseEntity<Object> submitClaims() {
        try {
            //log.info("BankerController :: submitClaims dataFilter{}, page{}, limit{}", claimStatus, page, limit);
            boolean result = bankerService.submitClaims();
            return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/discardClaims")
    public ResponseEntity<Object> discardClaims() {
        try {
            //log.info("BankerController :: discardClaims");
            //Page pageDTO = bankerService.getAllClaimsData(claimStatus, page, limit);
            return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/uploadDocument/{claimId}/{docType}")
    public ResponseEntity<Object> uploadDocument(@PathVariable Long claimId, @PathVariable String docType) {
        try {
            //log.info("BankerController :: getAllClaimsData dataFilter{}, page{}, limit{}", claimStatus, page, limit);
            //Page pageDTO = bankerService.getAllClaimsData(claimStatus, page, limit);
            return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
