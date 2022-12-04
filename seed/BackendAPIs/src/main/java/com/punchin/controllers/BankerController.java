package com.punchin.controllers;

import com.punchin.entity.ClaimsData;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.service.BankerService;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.ResponseMessgae;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class BankerController {

    @Autowired
    private BankerService bankerService;

    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
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

    @ApiOperation(value = "User Login", notes = "This can be used to Upload spreadsheet for claims data")
    @PostMapping(value = UrlMapping.UPLOAD_CLAIM)
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

    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsList(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("BankerController :: getClaimsList dataFilter {}, page {}, limit {}", claimDataFilter, page, limit);
            Page pageDTO = bankerService.getClaimsList(claimDataFilter, page, limit);
            return ResponseHandler.response(pageDTO, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimsList e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = UrlMapping.GET_CLAIM_DATA)
    public ResponseEntity<Object> getClaimData(@PathVariable Long claimId, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("BankerController :: getClaimData claimId {}", claimId);
            ClaimsData claimsData = bankerService.getClaimData(claimId);
            if(Objects.nonNull(claimsData)) {
                return ResponseHandler.response(claimsData, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = UrlMapping.SUBMIT_CLAIMS)
    public ResponseEntity<Object> submitClaims() {
        try {
            log.info("BankerController :: submitClaims");
            String result = bankerService.submitClaims();
            if (result.equals(ResponseMessgae.success)) {
                return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result, false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping(value = UrlMapping.DISCARD_CLAIMS)
    public ResponseEntity<Object> discardClaims() {
        try {
            log.info("BankerController :: discardClaims");
            String result = bankerService.discardClaims();
            if (result.equals(ResponseMessgae.success)) {
                return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = UrlMapping.UPLOAD_DOCUMENT)
    public ResponseEntity<Object> uploadDocument(@PathVariable Long claimId, @RequestParam MultipartFile[] multipartFiles) {
        try {
            log.info("BankerController :: uploadDocument claimId {}, multipartFiles {}", claimId, multipartFiles.length);
            ClaimsData claimsData = bankerService.getClaimData(claimId);
            if(Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> result = bankerService.uploadDocument(claimsData, multipartFiles);
            return ResponseHandler.response(result, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getAllClaimsData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
