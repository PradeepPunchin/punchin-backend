package com.punchin.controllers;

import com.punchin.dto.AgentUploadDocumentDTO;
import com.punchin.dto.PageDTO;
import com.punchin.entity.ClaimsData;
import com.punchin.enums.*;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.service.AgentService;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.ResponseMessgae;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @GetMapping(value = "/getClaimsByAgentState")
    public ResponseEntity<Object> getDataClaimsData(@RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData  page{}, limit{}", page, limit);
            List<ClaimsData> claimsByAgentState = agentService.getClaimsByAgentState(page, limit);
            if (!claimsByAgentState.isEmpty())
                return ResponseHandler.response(claimsByAgentState, ResponseMessgae.success, true, HttpStatus.OK);
            return ResponseHandler.response("", ResponseMessgae.backText, false, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data ", e);
        }
        return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ApiOperation(value = "Dashboard Data", notes = "This can be used to Show count in dashboard tile.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("AgentController :: getDashboardData");
            Map<String, Object> map = agentService.getDashboardData();
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
            page = page > 0 ? page - 1 : page;
            PageDTO pageDTO = agentService.getClaimsList(claimDataFilter, page, limit);
            return ResponseHandler.response(pageDTO, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimsList e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Claim List", notes = "This can be used to get Allocated claim data")
    @GetMapping(value = UrlMapping.GET_CLAIM_DATA)
    public ResponseEntity<Object> getClaimData(@PathVariable Long id) {
        try {
            log.info("AgentController :: getClaimData claimId {}", id);
            if(!agentService.checkAccess(id)){
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            ClaimsData claimsData = agentService.getClaimData(id);
            if(Objects.nonNull(claimsData)) {
                return ResponseHandler.response(claimsData, ResponseMessgae.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, ResponseMessgae.invalidClaimId, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getClaimData e {}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Upload claim document", notes = "This can be used to upload document regarding claim by verifier")
    @PutMapping(value = UrlMapping.AGENT_UPLOAD_DOCUMENT)
    public ResponseEntity<Object> uploadDocument(@PathVariable Long id, @RequestParam CauseOfDeathEnum causeOfDeath, @RequestParam boolean isMinor,
                                                 @RequestBody(required = false) MultipartFile signedForm, @RequestBody(required = false) MultipartFile deathCertificate,
                                                 @RequestParam(required = false) KycOrAddressDocType borrowerIdDocType, @RequestBody(required = false) MultipartFile borrowerIdDoc,
                                                 @RequestParam(required = false) KycOrAddressDocType borrowerAddressDocType, @RequestBody(required = false) MultipartFile borrowerAddressDoc,
                                                 @RequestParam(required = false) KycOrAddressDocType nomineeIdDocType, @RequestBody(required = false) MultipartFile nomineeIdDoc,
                                                 @RequestParam(required = false) KycOrAddressDocType nomineeAddressDocType, @RequestBody(required = false) MultipartFile nomineeAddressDoc,
                                                 @RequestParam(required = false) BankAccountDocType bankAccountDocType, @RequestBody(required = false) MultipartFile bankAccountDoc,
                                                 @RequestBody(required = false) MultipartFile FirOrPostmortemReport, @RequestParam(required = false) AdditionalDocType additionalDocType,
                                                 @RequestBody(required = false) MultipartFile additionalDoc) {
        try {
            log.info("AgentController :: uploadDocument claimId {}, multipartFiles {}", id);
            if (!agentService.checkAccess(id)) {
                return ResponseHandler.response(null, ResponseMessgae.forbidden, false, HttpStatus.FORBIDDEN);
            }
            AgentUploadDocumentDTO documentDTO = new AgentUploadDocumentDTO();
            documentDTO.setClaimsData(agentService.getClaimData(id));
            documentDTO.setCauseOfDeath(causeOfDeath);
            documentDTO.setMinor(isMinor);
            documentDTO.setSignedForm(signedForm);
            documentDTO.setDeathCertificate(deathCertificate);
            documentDTO.setBorrowerIdDocType(borrowerIdDocType);
            documentDTO.setBorrowerIdDoc(borrowerIdDoc);
            documentDTO.setBorrowerAddressDocType(borrowerAddressDocType);
            documentDTO.setBorrowerAddressDoc(borrowerAddressDoc);
            documentDTO.setNomineeIdDocType(nomineeIdDocType);
            documentDTO.setNomineeIdDoc(nomineeIdDoc);
            documentDTO.setNomineeAddressDocType(nomineeAddressDocType);
            documentDTO.setNomineeAddressDoc(nomineeAddressDoc);
            documentDTO.setBankAccountDocType(bankAccountDocType);
            documentDTO.setBankAccountDoc(bankAccountDoc);
            documentDTO.setFirOrPostmortemReport(FirOrPostmortemReport);
            documentDTO.setAdditionalDocType(additionalDocType);
            documentDTO.setAdditionalDoc(additionalDoc);
            Map<String, Object> result = agentService.uploadDocument(documentDTO);
            return ResponseHandler.response(null, ResponseMessgae.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE AgentController :: getAllClaimsData e{}", e);
            return ResponseHandler.response(null, ResponseMessgae.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
