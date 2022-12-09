package com.punchin.controllers;

import com.punchin.dto.BankerClaimDocumentationDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.BankerDocType;
import com.punchin.enums.ClaimStatus;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.service.BankerService;
import com.punchin.service.MISExport;
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

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(value = UrlMapping.BANKER, produces = MediaType.APPLICATION_JSON_VALUE)
public class BankerController {

    @Autowired
    private BankerService bankerService;
    @Autowired
    private HttpServletResponse httpServletResponse;

    @ApiOperation(value = "Dashboard Data", notes = "This can be used to Show count in dashboard tile.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("BankerController :: getDashboardData");
            if(!bankerService.isBanker()){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            Map<String, Long> map = bankerService.getDashboardData();
            return ResponseHandler.response(map, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getDashboardData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Upload Claims", notes = "This can be used to Upload spreadsheet for claims data")
    @PostMapping(value = UrlMapping.BANKER_UPLOAD_CLAIM)
    public ResponseEntity<Object> uploadClaimData(@ApiParam(name = "multipartFile", value = "The multipart object to upload multiple files.") @Valid @RequestBody MultipartFile multipartFile) {
        try {
            if(!bankerService.isBanker()){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            MultipartFile[] files = {multipartFile};
            log.info("BankerController :: uploadClaimData files{}", files.length);
            Map<String, Object> map = new HashMap<>();
            if (files.length > 0) {
                for (MultipartFile file : files) {
                    if (!GenericUtils.checkExcelFormat(file)) {
                        return ResponseHandler.response(null, ResponseMessgae.invalidFormat, false, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                    }
                }
                map = bankerService.saveUploadExcelData(files);
                if (Boolean.parseBoolean(map.get("status").toString())) {
                    return ResponseHandler.response(map.get("data"), ResponseMessgae.success, true, HttpStatus.CREATED);
                }
            }
            return ResponseHandler.response(null, map.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: uploadClaimData e{}", e);
            return ResponseHandler.response(null, e.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Claim List", notes = "This can be used to get not submitted claims list")
    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsList(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("BankerController :: getClaimsList dataFilter {}, page {}, limit {}", claimDataFilter, page, limit);
            if(!bankerService.isBanker()){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            page = page > 0 ? page - 1 : page;
            Page pageDTO = bankerService.getClaimsList(claimDataFilter, page, limit);
            return ResponseHandler.response(pageDTO, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimsList e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Claim List", notes = "This can be used to get submitted claims list")
    @GetMapping(value = UrlMapping.GET_CLAIM_DATA)
    public ResponseEntity<Object> getClaimData(@PathVariable Long id) {
        try {
            log.info("BankerController :: getClaimData claimId {}", id);
            ClaimsData claimsData = bankerService.isClaimByBanker(id);
            if(Objects.isNull(claimsData)){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            BankerClaimDocumentationDTO bankerClaimDocumentationDTO = bankerService.getClaimDataForBankerAction(id);
            if (Objects.nonNull(bankerClaimDocumentationDTO)) {
                return ResponseHandler.response(bankerClaimDocumentationDTO, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimData e {}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Submit Claim", notes = "This can be used to submit claims")
    @PutMapping(value = UrlMapping.BANKER_SUBMIT_CLAIMS)
    public ResponseEntity<Object> submitClaims() {
        try {
            log.info("BankerController :: submitClaims");
            if(!bankerService.isBanker()){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
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

    @ApiOperation(value = "Discard Claim", notes = "This can be used to discard claims")
    @DeleteMapping(value = UrlMapping.BANKER_DISCARD_CLAIMS)
    public ResponseEntity<Object> discardClaims() {
        try {
            log.info("BankerController :: discardClaims");
            if(!bankerService.isBanker()){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
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

    @ApiOperation(value = "Upload Document", notes = "This can be used to upload document regarding claim by banker")
    @PutMapping(value = UrlMapping.BANKER_UPLOAD_DOCUMENT)
    public ResponseEntity<Object> uploadDocument(@PathVariable Long id, @PathVariable BankerDocType docType, @ApiParam(name = "multipartFiles", value = "The multipart object as an array to upload multiple files.") @Valid @RequestBody MultipartFile multipartFiles) {
        try {
            log.info("BankerController :: uploadDocument claimId {}, multipartFiles {}, docType {}", id, multipartFiles, docType);
            ClaimsData claimsData = bankerService.isClaimByBanker(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            Map<String, Object> result = bankerService.uploadDocument(claimsData, new MultipartFile[] {multipartFiles}, docType);
            if (result.get("message").equals(ResponseMessgae.success)) {
                return ResponseHandler.response(result, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getAllClaimsData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Forward Claim", notes = "This can be used to forward claim to verifier")
    @PutMapping(value = UrlMapping.FORWARD_TO_VERIFIER)
    public ResponseEntity<Object> forwardToVerifier(@PathVariable Long id) {
        try {
            log.info("BankerController :: forwardToVerifier claimId {}", id);
            ClaimsData claimsData = bankerService.isClaimByBanker(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            String result = bankerService.forwardToVerifier(claimsData);
            if (result.equals(ResponseMessgae.success)) {
                return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result, false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: forwardToVerifier e {}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(value = "/downloadMISFile")
    public ResponseEntity<Object> downloadMISFile(@RequestParam ClaimStatus claimStatus) {
        try {
            log.info("BankerController :: downloadMISFile dataFilter{}", claimStatus);
            List<ClaimsData> claimsDataList = bankerService.downloadMISFile(claimStatus);
            MISExport misExport = new MISExport(claimsDataList);
            misExport.export(httpServletResponse);
            return ResponseHandler.response(claimsDataList, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
