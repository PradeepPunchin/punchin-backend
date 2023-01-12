package com.punchin.controllers;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.*;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.service.BankerService;
import com.punchin.service.MISExportService;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.MessageCode;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
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
    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;
    @Autowired
    private MISExportService misExportService;

    @Secured({"BANKER"})
    @ApiOperation(value = "Dashboard Data", notes = "This can be used to Show count in dashboard tile.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("BankerController :: getDashboardData");
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            Map<String, Long> map = bankerService.getDashboardData();
            return ResponseHandler.response(map, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getDashboardData e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Upload Claims", notes = "This can be used to Upload spreadsheet for claims data")
    @PostMapping(value = UrlMapping.BANKER_UPLOAD_CLAIM)
    public ResponseEntity<Object> uploadClaimData(@ApiParam(name = "multipartFile", value = "The multipart object to upload multiple files.") @Valid @RequestBody MultipartFile multipartFile) {
        try {
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            bankerService.discardClaims();
            MultipartFile[] files = {multipartFile};
            log.info("BankerController :: uploadClaimData files{}", files.length);
            Map<String, Object> map = new HashMap<>();
            if (files.length > 0) {
                for (MultipartFile file : files) {
                    if (!GenericUtils.checkExcelFormat(file)) {
                        return ResponseHandler.response(null, MessageCode.invalidFormat, false, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                    }
                }
                String fileFormat = GenericUtils.checkCSV(multipartFile);
                if (fileFormat == null) {
                    return ResponseHandler.response(null, MessageCode.invalidFormat, false, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                }
               else if (fileFormat.equalsIgnoreCase("csv")){
                  return  bankerService.saveUploadCSVData(multipartFile);
                }
                map = bankerService.saveUploadExcelData(files);
                if (Boolean.parseBoolean(map.get("status").toString())) {
                    return ResponseHandler.response(map.get("data"), MessageCode.success, true, HttpStatus.CREATED);
                }
            }
            return ResponseHandler.response(null, map.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: uploadClaimData e{}", e);
            return ResponseHandler.response(null, e.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Claim List", notes = "This can be used to get not submitted claims list")
    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsList(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit, @RequestParam(value = "searchCaseEnum", required = false) SearchCaseEnum searchCaseEnum, @RequestParam(value = "searchedKeyword", required = false) String searchedKeyword) {
        try {
            log.info("BankerController :: getClaimsList dataFilter {}, page {}, limit {}", claimDataFilter, page, limit);
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            //page = page > 0 ? page - 1 : page;
            PageDTO pageDTO = bankerService.getClaimsList(claimDataFilter, page, limit, searchedKeyword, searchCaseEnum);
            if(Objects.nonNull(pageDTO.getMessage()))
                return ResponseHandler.response(pageDTO, pageDTO.getMessage(), true, HttpStatus.OK);
            return ResponseHandler.response(pageDTO, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimsList e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Claim List", notes = "This can be used to get submitted claims list")
    @GetMapping(value = UrlMapping.GET_CLAIM_DATA)
    public ResponseEntity<Object> getClaimData(@PathVariable Long id) {
        try {
            log.info("BankerController :: getClaimData claimId {}", id);
            ClaimsData claimsData = bankerService.isClaimByBanker(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            BankerClaimDocumentationDTO bankerClaimDocumentationDTO = bankerService.getClaimDataForBankerAction(id);
            if (Objects.nonNull(bankerClaimDocumentationDTO)) {
                return ResponseHandler.response(bankerClaimDocumentationDTO, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimData e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Submit Claim", notes = "This can be used to submit claims")
    @PutMapping(value = UrlMapping.BANKER_SUBMIT_CLAIMS)
    public ResponseEntity<Object> submitClaims() {
        try {
            log.info("BankerController :: submitClaims");
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            String result = bankerService.submitClaims();
            if (result.equals(MessageCode.success)) {
                return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result, false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Discard Claim", notes = "This can be used to discard claims")
    @DeleteMapping(value = UrlMapping.BANKER_DISCARD_CLAIMS)
    public ResponseEntity<Object> discardClaims() {
        try {
            log.info("BankerController :: discardClaims");
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            String result = bankerService.discardClaims();
            if (result.equals(MessageCode.success)) {
                return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Upload Document", notes = "This can be used to upload document regarding claim by banker")
    @PutMapping(value = UrlMapping.BANKER_UPLOAD_DOCUMENT)
    public ResponseEntity<Object> uploadDocument(@PathVariable Long id, @PathVariable BankerDocType docType, @ApiParam(name = "multipartFiles", value = "The multipart object as an array to upload multiple files.") @Valid @RequestBody MultipartFile[] multipartFiles) {
        try {
            log.info("BankerController :: uploadDocument claimId {}, multipartFiles {}, docType {}", id, multipartFiles, docType);
            ClaimsData claimsData = bankerService.isClaimByBanker(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            if (bankerService.checkDocumentAlreadyExist(id, docType)) {
                return ResponseHandler.response(null, MessageCode.DOCUMENT_ALREADY_EXISTS, false, HttpStatus.FORBIDDEN);
            }
            Map<String, Object> result = bankerService.uploadDocument(claimsData, multipartFiles, docType);
            if (result.get("message").equals(MessageCode.success)) {
                return ResponseHandler.response(result, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getAllClaimsData ", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Forward Claim", notes = "This can be used to forward claim to verifier")
    @PutMapping(value = UrlMapping.FORWARD_TO_VERIFIER)
    public ResponseEntity<Object> forwardToVerifier(@PathVariable Long id) {
        try {
            log.info("BankerController :: forwardToVerifier claimId {}", id);
            ClaimsData claimsData = bankerService.isClaimByBanker(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            String result = bankerService.forwardToVerifier(claimsData);
            if (result.equals(MessageCode.success)) {
                return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result, false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: forwardToVerifier e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Download Excel Sheet", notes = "This can be used to download excel sheet format for upload claim data")
    @GetMapping(value = UrlMapping.BANKER_STANDARIZED_FORMAT)
    public ResponseEntity<Object> downloadStandardFormat() {
        try {
            log.info("BankerController :: discardClaims");
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.response("https://punchin-dev.s3.amazonaws.com/Claim_Data_Format.xlsx", MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Delete document", notes = "This can be used to delete document")
    @DeleteMapping(value = UrlMapping.DELETE_CLAIM_DOCUMENT)
    public ResponseEntity<Object> deleteBankerClaimDocument(@PathVariable Long docId) {
        try {
            log.info("BankerController :: deleteBankerClaimDocument docId {}", docId);
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            ClaimDocuments claimDocuments = bankerService.getClaimDocuments(docId);
            if (Objects.nonNull(claimDocuments)) {
                String result = bankerService.deleteBankDocument(claimDocuments);
                if (result.equals(MessageCode.success)) {
                    return ResponseHandler.response(null, MessageCode.DOCUMENT_DELETED, true, HttpStatus.OK);
                }
                return ResponseHandler.response(null, result, false, HttpStatus.BAD_REQUEST);
            }
            return ResponseHandler.response(null, MessageCode.INVALID_DOCUMENT_ID, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: deleteBankerClaimDocument e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Claim List", notes = "This can be used to get submitted claims list")
    @PostMapping(value = UrlMapping.BANKER_SAVEAS_DRAFT_DOCUMENT)
    public ResponseEntity<Object> saveASDraftDocument(@PathVariable Long claimId) {
        try {
            log.info("BankerController :: getClaimData claimId {}", claimId);
            ClaimsData claimsData = bankerService.isClaimByBanker(claimId);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            String result = bankerService.saveASDraftDocument(claimsData);
            if (result.equals(MessageCode.success)) {
                return ResponseHandler.response(null, MessageCode.DOCUMENT_SAVEAS_DRAFT, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimData e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Download MIS Report", notes = "This can be used to download MIS in excel sheet")
    @GetMapping(value = UrlMapping.DOWNLOAD_MIS_REPORT)
    public ResponseEntity<Object> downloadMISReport(@RequestParam ClaimDataFilter claimDataFilter) {
        try {
            log.info("BankerController :: downloadMISReport");
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.response(misExportService.downloadBankerExcelFile(claimDataFilter), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Upload Claims", notes = "This can be used to Upload spreadsheet for claims data")
    @PostMapping(value = UrlMapping.BANKER_CSV_UPLOAD_CLAIM)
    public ResponseEntity<Object> uploadCSVFileClaimData(@ApiParam(name = "multipartFile", value = "The multipart object to upload multiple files.") @Valid @RequestBody MultipartFile multipartFile) {
        try {
            return bankerService.saveUploadCSVData(multipartFile);
        } catch (
                Exception e) {
            log.error("EXCEPTION WHILE BankerController :: uploadClaimData ", e);
            return ResponseHandler.response(null, e.getMessage(), false, HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Get searched data", notes = "This can be used to get by criteria loan account no or by claim id or by name")
    @GetMapping(value = UrlMapping.GET_CLAIM_SEARCHED_DATA_BANKER)
    public ResponseEntity<Object> getClaimSearchedData(@RequestParam(value = "searchCaseEnum") SearchCaseEnum
                                                               searchCaseEnum, @RequestParam(value = "searchedKeyword") String searchedKeyword,
                                                       @RequestParam ClaimDataFilter claimDataFilter, @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            log.info("Get Searched data request received for searchCaseEnum :{} , searchedKeyword :{} , pageNo :{} , limit :{} ", searchCaseEnum, searchedKeyword, pageNo, pageSize);
            PageDTO searchedClaimData = bankerService.getBankerClaimSearchedData(searchCaseEnum, searchedKeyword, claimDataFilter, pageNo, pageSize);
            if (searchedClaimData != null) {
                log.info("Searched claim data fetched successfully");
                return ResponseHandler.response(searchedClaimData, MessageCode.SEARCHED_CLAIM_DATA_FETCHED_SUCCESS, true, HttpStatus.OK);
            }
            log.info("No records found");
            return ResponseHandler.response(null, MessageCode.NO_RECORD_FOUND, false, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: Get searched data :: ", e);
            return ResponseHandler.response(null, MessageCode.ERROR_SEARCHED_CLAIM_DATA_FETCHED, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @GetMapping(value = UrlMapping.GET_CLAIM_DOCUMENTS)
    public ResponseEntity<Object> getClaimBankerDocuments(@PathVariable Long id) {
        try {
            log.info("BankerController :: getClaimDocuments claimId {}", id);
            Map<String, Object> claimDocumentsMAP = bankerService.getClaimBankerDocuments(id);
            if (claimDocumentsMAP.get("message").equals(MessageCode.success)) {
                return ResponseHandler.response(claimDocumentsMAP, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, claimDocumentsMAP.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getDocumentDetails :: e {} ", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Upload Discrepancy Document", notes = "This can be used to upload document regarding claim by agent")
    @PutMapping(value = UrlMapping.DISCREPANCY_DOCUMENT_UPLOAD)
    public ResponseEntity<Object> discrepancyDocumentUpload(@PathVariable Long id, @PathVariable String docType, @RequestBody MultipartFile multipartFile) {
        try {
            log.info("BankerController :: discrepancyDocumentUpload claimId {}, multipartFile {}, docType {}", id, multipartFile, docType);
            if (!bankerService.checkDocumentIsInDiscrepancy(id, docType) && !docType.equals(AgentDocType.OTHER.name())) {
                return ResponseHandler.response(null, MessageCode.documentInUnderVerification, false, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> result = bankerService.discrepancyDocumentUpload(id, new MultipartFile[]{multipartFile}, docType);
            if (result.get("message").equals(MessageCode.success)) {
                return ResponseHandler.response(result, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: discrepancyDocumentUpload e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Secured({"BANKER"})
    @PostMapping(value = UrlMapping.REQUEST_ADDITIONAL_DOCUMENT)
    public ResponseEntity<Object> requestForAdditionalDocument(@RequestBody AdditionalDocumentRequestDTO additionalDocumentRequestDTO) {
        try {
            log.info("BankerController :: requestForAdditionalDocument claimsId - {}, docTypes - {}, remark - {}", additionalDocumentRequestDTO.getClaimId(), additionalDocumentRequestDTO.getDocTypes(), additionalDocumentRequestDTO.getRemark());
            ClaimsData claimsData = bankerService.isClaimByBanker(additionalDocumentRequestDTO.getClaimId());
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            if(claimsData.getBankerId() < 1){
                return ResponseHandler.response(null, MessageCode.AGENT_NOT_ALLOCATED, false, HttpStatus.FORBIDDEN);
            }
            boolean claimDocumentsMAP = bankerService.requestForAdditionalDocument(claimsData, additionalDocumentRequestDTO.getDocTypes(), additionalDocumentRequestDTO.getRemark());
            if (claimDocumentsMAP) {
                return ResponseHandler.response(claimDocumentsMAP, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: requestForAdditionalDocument :: e {} ", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Download All file", notes = "This can be used to donwload all document on claim.")
    @GetMapping(value = UrlMapping.DOWNLOAD_CLAIM_DOCUMENT_DATA)
    public ResponseEntity<Object> downloadAllDocuments(@PathVariable Long id) {
        try {
            log.info("BankerController :: downloadAllDocuments");
            String url = bankerService.downloadAllDocuments(id);
            log.info("Banker Dashboard count fetched Successfully");
            return ResponseHandler.response(url, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController downloading claim documents::", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Claim history", notes = "This can be used to get Claim History")
    @GetMapping(value = UrlMapping.GET_CLAIM_HISTORY)
    public ResponseEntity<Object> getClaimHistory(@PathVariable Long id) {
        try {
            log.info("BankerController :: getClaimHistory claimId - {}", id);
            if (Objects.isNull(bankerService.isClaimByBanker(id))) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.response(bankerService.getClaimHistory(id), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimHistory e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Claim Remark", notes = "This can be used to get remark History")
    @GetMapping(value = UrlMapping.GET_CLAIM_REMARK)
    public ResponseEntity<Object> getRemarkHistory(@PathVariable Long id) {
        try {
            log.info("BankerController :: getRemarkHistory claimId - {}", id);
            return ResponseHandler.response(bankerService.getRemarkHistory(id), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getRemarkHistory e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Add remark", notes = "This can be used to add remark")
    @PostMapping(value = UrlMapping.ADD_CLAIM_REMARK)
    public ResponseEntity<Object> addClaimRemark(@PathVariable Long id, @RequestBody ClaimRemarkRequestDTO requestDTO) {
        try {
            log.info("VerifierController :: addClaimRemark claimId - {}", id);
            ClaimsData claimsData = bankerService.getClaimData(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            ClaimsRemarksDTO claimsRemarksDTO = bankerService.addClaimRemark(claimsData, requestDTO);
            if(Objects.nonNull(claimsRemarksDTO)) {
                return ResponseHandler.response(claimsRemarksDTO, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: addClaimRemark e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Update claim data", notes = "This can be used to update claim data")
    @PutMapping(value = UrlMapping.UPDATE_CLAIM_DATA)
    public ResponseEntity<Object> updateClaimData(@PathVariable Long id, @RequestBody ClaimUpdateRequestDTO requestDTO) {
        try {
            log.info("VerifierController :: addClaimRemark claimId - {}", id);
            ClaimsData claimsData = bankerService.getClaimData(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            if(!bankerService.checkPinCode(requestDTO.getBorrowerPinCode())){
                return ResponseHandler.response(null, MessageCode.INVALID_PINCODE, false, HttpStatus.BAD_REQUEST);
            }
            /*if(!bankerService.checkAvailableVerifier(requestDTO.getBorrowerPinCode())){
                return ResponseHandler.response(null, MessageCode.INVALID_PINCODE, false, HttpStatus.BAD_REQUEST);
            }*/
            ClaimDataDTO claimDataDTO = bankerService.updateClaimData(claimsData, requestDTO);
            if(Objects.nonNull(claimDataDTO)) {
                return ResponseHandler.response(claimDataDTO, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: addClaimRemark e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"BANKER"})
    @ApiOperation(value = "Download Reject MIS Report", notes = "This can be used to download rejected MIS in excel sheet")
    @GetMapping(value = UrlMapping.DOWNLOAD_REJECTED_MIS_REPORT)
    public ResponseEntity<Object> downloadRejectedMISReport() {
        try {
            String bankerId = GenericUtils.getLoggedInUser().getUserId();
            log.info("BankerController :: downloadRejectMISReport");
            if (!bankerService.isBanker()) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.response(misExportService.exportRejectedClaimsData(bankerId), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while download reject claim data list");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
