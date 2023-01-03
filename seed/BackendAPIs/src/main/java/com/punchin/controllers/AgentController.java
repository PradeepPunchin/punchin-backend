package com.punchin.controllers;

import com.punchin.dto.*;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.*;
import com.punchin.repository.ClaimDocumentsRepository;
import com.punchin.service.AgentService;
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

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CrossOrigin
@RestController
@Slf4j
@RequestMapping(value = UrlMapping.AGENT, produces = MediaType.APPLICATION_JSON_VALUE)
public class AgentController {

    @Autowired
    private AgentService agentService;

    @Autowired
    private ClaimDocumentsRepository claimDocumentsRepository;

    @Secured({"AGENT"})
    @ApiOperation(value = "Dashboard Data", notes = "This can be used to Show count in dashboard tile.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("AgentController :: getDashboardData");
            Map<String, Object> map = agentService.getDashboardData();
            return ResponseHandler.response(map, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getDashboardData e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Claim List", notes = "This can be used to get not allocated claims list")
    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsList(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("AgentController :: getClaimsList dataFilter {}, page {}, limit {}", claimDataFilter, page, limit);
            //page = page > 0 ? page - 1 : page;
            PageDTO pageDTO = agentService.getClaimsList(claimDataFilter, page, limit);
            return ResponseHandler.response(pageDTO, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimsList e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Claim List", notes = "This can be used to get Allocated claim data")
    @GetMapping(value = UrlMapping.GET_CLAIM_DATA)
    public ResponseEntity<Object> getClaimData(@PathVariable Long id) {
        try {
            log.info("AgentController :: getClaimData claimId {}", id);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            Map<String, Object> claimsData = agentService.getClaimData(id);
            if (Objects.nonNull(claimsData)) {
                return ResponseHandler.response(claimsData, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimData e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @GetMapping(value = UrlMapping.GET_CLAIM_DOCUMENTS)
    public ResponseEntity<Object> getClaimDocuments(@PathVariable Long id) {
        try {
            log.info("AgentController :: getClaimDocuments claimId {}", id);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            Map<String, Object> claimDocumentsMAP = agentService.getClaimDocuments(id);
            if (claimDocumentsMAP.get("message").equals(MessageCode.success)) {
                return ResponseHandler.response(claimDocumentsMAP, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, claimDocumentsMAP.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getDocumentDetails :: e {} ", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Upload Discrepancy Document", notes = "This can be used to upload document regarding claim by agent")
    @PostMapping(value = UrlMapping.DISCREPANCY_DOCUMENT_UPLOAD)
    public ResponseEntity<Object> discrepancyDocumentUpload(@PathVariable Long id, @PathVariable AgentDocType docType, @RequestBody MultipartFile[] multipartFile, @RequestParam(defaultValue = "true") boolean isDiscrepancy) {
        try {
            log.info("BankerController :: discrepancyDocumentUpload claimId {}, multipartFile {}, docType {}", id, multipartFile, docType);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            if (!agentService.checkDocumentIsInDiscrepancy(id, docType) && !docType.equals(AgentDocType.OTHER) && isDiscrepancy) {
                return ResponseHandler.response(null, MessageCode.documentInUnderVerification, false, HttpStatus.BAD_REQUEST);
            }
            Map<String, Object> result = agentService.discrepancyDocumentUpload(id, multipartFile, docType);
            if (result.get("message").equals(MessageCode.success)) {
                if (!agentService.checkDocumentUploaded(id)) {
                    String result2 = agentService.forwardToVerifier(id);
                    if (result2.equals(MessageCode.success)) {
                        return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
                    }
                }
                return ResponseHandler.response(result, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getAllClaimsData e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Submit claim", notes = "This can be used to forward claim to verifier (Under verification)")
    @PostMapping(value = UrlMapping.FORWARD_TO_VERIFIER)
    public ResponseEntity<Object> forwardToVerifier(@PathVariable Long id) {
        try {
            log.info("AgentController :: forwardToVerifier claimId {}", id);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            if (agentService.checkDocumentUploaded(id)) {
                return ResponseHandler.response(null, MessageCode.documentNotUploaded, true, HttpStatus.OK);
            }
            String result = agentService.forwardToVerifier(id);
            if (result.equals(MessageCode.success)) {
                return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, result, false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: forwardToVerifier e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Upload claim document", notes = "This can be used to upload document regarding claim by verifier")
    @PutMapping(value = UrlMapping.AGENT_UPLOAD_DOCUMENT)
    public ResponseEntity<Object> uploadDocuments(@PathVariable Long id, @RequestParam(required = false)  CauseOfDeathEnum causeOfDeath, @RequestParam(required = false) boolean isMinor, @RequestParam(required = false) Map<String, MultipartFile> isMinorDoc, @RequestParam(required = false) String agentRemark) {
        try {
            log.info("AgentController :: uploadDocument claimId {}, multipartFiles {}", id);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            AgentUploadDocumentDTO documentDTO = new AgentUploadDocumentDTO();
            documentDTO.setClaimsData(agentService.getClaimsData(id));
            documentDTO.setCauseOfDeath(causeOfDeath);
            documentDTO.setMinor(isMinor);
            documentDTO.setIsMinorDoc(isMinorDoc);
            documentDTO.setAgentRemark(agentRemark);
            Map<String, Object> result = agentService.uploadDocument(documentDTO);
            if (Boolean.parseBoolean(result.get("status").toString())) {
                return ResponseHandler.response(null, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(result.get("claimsData"), result.get("message").toString(), false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getAllClaimsData e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Get searched data", notes = "This can be used to get by criteria loan account no or by claim id or by name")
    @GetMapping(value = UrlMapping.GET_CLAIM_SEARCHED_DATA)
    public ResponseEntity<Object> getClaimSearchedData(@RequestParam(value = "searchCaseEnum") SearchCaseEnum searchCaseEnum, @RequestParam(value = "searchedKeyword") String searchedKeyword,
                                                       @RequestParam ClaimDataFilter claimDataFilter, @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("Get Searched data request received for searchCaseEnum :{} , searchedKeyword :{} , pageNo :{} , limit :{} ", searchCaseEnum, searchedKeyword, pageNo, limit);
            PageDTO searchedClaimData = agentService.getClaimSearchedData(searchCaseEnum, searchedKeyword, pageNo, limit, claimDataFilter);
            if (searchedClaimData != null) {
                log.info("Searched claim data fetched successfully");
                return ResponseHandler.response(searchedClaimData, MessageCode.SEARCHED_CLAIM_DATA_FETCHED_SUCCESS, true, HttpStatus.OK);
            }
            log.info("No records found");
            return ResponseHandler.response(null, MessageCode.NO_RECORD_FOUND, false, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: Get searched data :: e {} ", e);
            return ResponseHandler.response(null, MessageCode.ERROR_SEARCHED_CLAIM_DATA_FETCHED, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Upload Document", notes = "This can be used to upload document regarding claim by agent")
    //@PutMapping(value = UrlMapping.UPLOAD_DOCUMENT_AGENT)
    public ResponseEntity<Object> uploadAgentDocument(@RequestParam Long id, @RequestParam AgentDocType docType, @ApiParam(name = "multipartFiles", value = "The multipart object as an array to upload multiple files.") @Valid @RequestBody MultipartFile multipartFiles) {
        try {
            log.info("BankerController :: uploadDocument claimId {}, multipartFiles {}, docType {}", id, multipartFiles, docType);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            boolean documentExists = claimDocumentsRepository.findExistingDocument(id, docType.toString());
            if (documentExists) {
                return ResponseHandler.response(null, MessageCode.DOCUMENT_ALREADY_EXISTS, true, HttpStatus.OK);
            }
            List<DocumentUrls> documentUrlsList = agentService.uploadAgentDocument(id, new MultipartFile[]{multipartFiles}, docType);
            if (!documentUrlsList.isEmpty()) {
                return ResponseHandler.response(documentUrlsList, MessageCode.DOCUMENT_UPLOADED_SUCCESS, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.NO_RECORD_FOUND, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: uploadAgentDocument e{}", e);
            return ResponseHandler.response(null, MessageCode.ERROR_UPLOAD_DOCUMENT, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Delete document", notes = "This can be used to delete document")
    @DeleteMapping(value = UrlMapping.AGENT_DELETE_DOCUMENT)
    public ResponseEntity<Object> deleteAgentDocument(@RequestParam Long documentId) {
        try {
            log.info("AgentController :: delete docId {}", documentId);
            String deleteClaimDocument = agentService.deleteClaimDocument(documentId);
            if (deleteClaimDocument.equals(MessageCode.DOCUMENT_DELETED)) {
                return ResponseHandler.response(null, MessageCode.DOCUMENT_DELETED, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.NO_RECORD_FOUND, false, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: deleteClaimDocument e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"AGENT"})
    @ApiOperation(value = "Claim history", notes = "This can be used to get Claim History")
    @GetMapping(value = UrlMapping.GET_CLAIM_HISTORY)
    public ResponseEntity<Object> getClaimHistory(@PathVariable Long id) {
        try {
            log.info("AgentController :: getClaimHistory claimId - {}", id);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, MessageCode.forbidden, false, HttpStatus.FORBIDDEN);
            }
            return ResponseHandler.response(agentService.getClaimHistory(id), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimHistory e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
