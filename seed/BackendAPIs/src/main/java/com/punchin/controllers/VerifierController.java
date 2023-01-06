package com.punchin.controllers;

import com.punchin.dto.*;
import com.punchin.entity.ClaimDocuments;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.User;
import com.punchin.enums.ClaimDataFilter;
import com.punchin.enums.SearchCaseEnum;
import com.punchin.repository.UserRepository;
import com.punchin.service.MISExportService;
import com.punchin.service.UserService;
import com.punchin.service.VerifierService;
import com.punchin.utility.GenericUtils;
import com.punchin.utility.ResponseHandler;
import com.punchin.utility.constant.MessageCode;
import com.punchin.utility.constant.UrlMapping;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private MISExportService misExportService;

    @Autowired
    private UserRepository userRepository;

    @Secured({"VERIFIER"})
    @GetMapping(value = UrlMapping.GET_CLAIMS_LIST)
    public ResponseEntity<Object> getClaimsData(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit, @RequestParam(value = "searchCaseEnum", required = false) SearchCaseEnum searchCaseEnum, @RequestParam(value = "searchedKeyword", required = false) String searchedKeyword) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            //page = page > 0 ? page - 1 : page;
            PageDTO allClaimsData = verifierService.getAllClaimsData(claimDataFilter, page, limit, searchCaseEnum, searchedKeyword);
            return ResponseHandler.response(allClaimsData, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @GetMapping(value = UrlMapping.VERIFIER_GET_CLAIM_DATA_WITH_DOCUMENT_STATUS)
    public ResponseEntity<Object> getClaimDataWithDocumentStatus(@RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getClaimDataWithDocumentStatus page {}, limit {}", page, limit);
            PageDTO pageDTO = verifierService.getClaimDataWithDocumentStatus(page, limit);
            if (Objects.nonNull(pageDTO)) {
                return ResponseHandler.response(pageDTO, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: getClaimDataWithDocumentStatus e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @GetMapping(value = UrlMapping.DOWNLOAD_VERIFIER_GET_CLAIM_DATA_WITH_DOCUMENT_STATUS)
    public ResponseEntity<Object> downloadClaimDataWithDocumentStatus(@RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: downloadClaimData page {}, limit {}", page, limit);
            String url = verifierService.downloadClaimDataWithDocumentStatus(page, limit);
            if (Objects.nonNull(url)) {
                return ResponseHandler.response(url, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: getClaimDataWithDocumentStatus e {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Dashboard Data Count", notes = "This can be used to Show Dashboard data count in Verifier dashboard tab.")
    @GetMapping(value = UrlMapping.GET_DASHBOARD_DATA)
    public ResponseEntity<Object> getDashboardData() {
        try {
            log.info("VerifierController :: getDashboardDataCount");
            Map<String, Long> map = verifierService.getDashboardData();
            log.info("Verifier Dashboard count fetched Successfully");
            return ResponseHandler.response(map, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE fetching Verifier Dashboard Count ::", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

   /*@PostMapping(value = UrlMapping.VERIFIER_ALLOCATE_CLAIM)
    public ResponseEntity<Object> allocateClaimToAgent(@PathVariable Long id, @PathVariable Long agentId) {
        try {
            log.info("VerifierController :: allocateClaimToAgent claimId {}, agentId {}", id, agentId);
            ClaimsData claimsData = verifierService.getClaimData(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            User user = userService.findAgentById(agentId);
            if (Objects.isNull(user)) {
                return ResponseHandler.response(null, MessageCode.invalidAgentId, false, HttpStatus.BAD_REQUEST);
            }
            boolean check = verifierService.allocateClaimToAgent(claimsData, user);
            if (check) {
                return ResponseHandler.response(null, MessageCode.claimAllocated, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: allocateClaimToAgent e{}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @Secured({"VERIFIER"})
    @GetMapping(value = UrlMapping.GET_CLAIM_DOCUMENTS)
    public ResponseEntity<Object> getClaimDocuments(@PathVariable Long id) {
        try {
            log.info("VerifierController :: getDocumentDetails claimId {}", id);
            ClaimsData claimsData = verifierService.getClaimData(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            ClaimDetailForVerificationDTO claimDetailForVerificationDTO = verifierService.getClaimDocuments(claimsData);
            if (Objects.nonNull(claimDetailForVerificationDTO)) {
                return ResponseHandler.response(claimDetailForVerificationDTO, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: getDocumentDetails :: e {} ", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //@GetMapping(value = UrlMapping.VERIFIER_CLAIMS_VERIFICATION_REQUEST)
/*    public ResponseEntity<Object> getClaimDocVerificationRequest(@RequestParam ClaimDataFilter claimDataFilter, @RequestParam Integer page, @RequestParam Integer limit) {
        try {
            log.info("VerifierController :: getAllVerifierClaimsData dataFilter{}, page{}, limit{}", claimDataFilter, page, limit);
            //page = page > 0 ? page - 1 : page;
            PageDTO allClaimsData = verifierService.getAllClaimsData(claimDataFilter, page, limit);
            return ResponseHandler.response(allClaimsData, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }*/

    @Secured({"VERIFIER"})
    @PostMapping(value = UrlMapping.VERIFIER_ACCEPT_AND_REJECT_DOCUMENTS)
    public ResponseEntity<Object> acceptAndRejectDocuments(@PathVariable Long id, @PathVariable Long docId, @RequestBody DocumentApproveRejectPayloadDTO approveRejectPayloadDTO) {
        try {
            log.info("VerifierController :: acceptAndRejectDocuments claimId {}, docId {}, approveRejectPayloadDTO {}", id, docId, approveRejectPayloadDTO);
            ClaimsData claimsData = verifierService.getClaimData(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            ClaimDocuments claimDocuments = verifierService.getClaimDocumentById(docId);
            if (Objects.isNull(claimDocuments)) {
                return ResponseHandler.response(null, MessageCode.invalidDocId, false, HttpStatus.BAD_REQUEST);
            }
            String message = verifierService.acceptAndRejectDocument(claimsData, claimDocuments, approveRejectPayloadDTO);
            if (message.equals(MessageCode.success)) {
                if (approveRejectPayloadDTO.isApproved()) {
                    return ResponseHandler.response(null, MessageCode.claimDocumentApproved, true, HttpStatus.OK);
                } else {
                    return ResponseHandler.response(null, MessageCode.claimDocumentRejected, true, HttpStatus.OK);
                }
            }
            return ResponseHandler.response(null, message, false, HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE for accepting and rejecting documents ::", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Download All file", notes = "This can be used to download all document on claim.")
    @GetMapping(value = UrlMapping.DOWNLOAD_CLAIM_DOCUMENT_DATA)
    public ResponseEntity<Object> downloadAllDocuments(@PathVariable Long id) {
        try {
            log.info("VerifierController :: downloadAllDocuments");
            String url = verifierService.downloadAllDocuments(id);
            log.info("Verifier Dashboard count fetched Successfully");
            return ResponseHandler.response(url, MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE downloading claim documents::", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Get searched data", notes = "This can be used to get by criteria loan account no or by claim id or by name")
    @GetMapping(value = UrlMapping.GET_CLAIM_SEARCHED_DATA_VERIFIER)
    public ResponseEntity<Object> getClaimSearchedData(@RequestParam(value = "searchCaseEnum") SearchCaseEnum searchCaseEnum, @RequestParam(value = "searchedKeyword") String searchedKeyword,
                                                       @RequestParam ClaimDataFilter claimDataFilter, @RequestParam(defaultValue = "0") Integer pageNo, @RequestParam(defaultValue = "10") Integer limit) {
        try {
            log.info("Get Searched data request received for searchCaseEnum :{} , searchedKeyword :{} , pageNo :{} , limit :{} ", searchCaseEnum, searchedKeyword, pageNo, limit);
            PageDTO searchedClaimData = verifierService.getVerifierClaimSearchedData(searchCaseEnum, searchedKeyword, claimDataFilter, pageNo, limit);
            if (searchedClaimData != null) {
                log.info("Searched claim data fetched successfully");
                return ResponseHandler.response(searchedClaimData, MessageCode.SEARCHED_CLAIM_DATA_FETCHED_SUCCESS, true, HttpStatus.OK);
            }
            log.info("No records found");
            return ResponseHandler.response(null, MessageCode.NO_RECORD_FOUND, false, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE VerifierController :: Get searched data ::  ", e);
        }
        return ResponseHandler.response(null, MessageCode.ERROR_SEARCHED_CLAIM_DATA_FETCHED, false, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Download MIS Report", notes = "This can be used to download MIS in excel sheet")
    @GetMapping(value = UrlMapping.DOWNLOAD_MIS_REPORT)
    public ResponseEntity<Object> downloadMISReport(@RequestParam ClaimDataFilter claimDataFilter) {
        try {
            log.info("BankerController :: downloadMISReport");
            return ResponseHandler.response(misExportService.downloadVerifierMISReport(claimDataFilter), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Error while fetching in pagination data");
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @GetMapping(value = UrlMapping.GET_ALL_AGENTS_VERIFIER)
    public ResponseEntity<Object> getAllAgentsForVerifier() {
        try {
            Long verifierId = GenericUtils.getLoggedInUser().getId();
            log.info("Request received for verifier's agent list {}", verifierId);
            User verifier = userRepository.verifierExistsByIdAndRole(verifierId);
            if (verifier == null) {
                log.info(MessageCode.INVALID_USERID);
                return ResponseHandler.response(null, MessageCode.INVALID_USERID, false, HttpStatus.NOT_FOUND);
            }
            List<AgentListResponseDTO> allAgentsList = verifierService.getAllAgentsForVerifier(verifier);
            if (!allAgentsList.isEmpty()) {
                log.info(MessageCode.ALL_AGENTS_LIST_FETCHED_SUCCESS);
                return ResponseHandler.response(allAgentsList, MessageCode.ALL_AGENTS_LIST_FETCHED_SUCCESS, true, HttpStatus.OK);
            }
            log.info(MessageCode.NO_RECORD_FOUND);
            return ResponseHandler.response(null, MessageCode.NO_RECORD_FOUND, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Error while fetching verifier's agents list", e);
            return ResponseHandler.response(null, MessageCode.ERROR_WHILE_FETCHING_VERIFIERS_AGENT_LIST, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @PutMapping(value = UrlMapping.VERIFIER_ALLOCATE_CLAIM)
    public ResponseEntity<Object> claimDataAgentAllocation(@PathVariable(value = "agentId") Long agentId, @PathVariable(value = "id") Long claimDataId) {
        try {
            log.info("Request received for claim data agent allocation {}, agentId {} ", claimDataId, agentId);
            String agentAllocation = verifierService.claimDataAgentAllocation(agentId, claimDataId);
            if (agentAllocation.equalsIgnoreCase(MessageCode.AGENT_ALLOCATED_SAVED_SUCCESS)) {
                log.info(MessageCode.AGENT_ALLOCATED_SAVED_SUCCESS);
                return ResponseHandler.response(agentAllocation, MessageCode.AGENT_ALLOCATED_SAVED_SUCCESS, true, HttpStatus.OK);
            } else if (agentAllocation.equalsIgnoreCase(MessageCode.invalidAgentId)) {
                log.info(MessageCode.invalidAgentId);
                return ResponseHandler.response(agentAllocation, MessageCode.invalidAgentId, false, HttpStatus.BAD_REQUEST);
            } else {
                log.info(MessageCode.invalidClaimId);
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            log.error("Error while submitting Agent allocation", e);
            return ResponseHandler.response(null, MessageCode.ERROR_WHILE_AGENT_ALLOCATED, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Claim history", notes = "This can be used to get Claim History")
    @GetMapping(value = UrlMapping.GET_CLAIM_HISTORY)
    public ResponseEntity<Object> getClaimHistory(@PathVariable Long id) {
        try {
            log.info("BankerController :: getClaimHistory claimId - {}", id);
            return ResponseHandler.response(verifierService.getClaimHistory(id), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getClaimHistory e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Claim history", notes = "This can be used to get remark History")
    @GetMapping(value = UrlMapping.GET_CLAIM_REMARK)
    public ResponseEntity<Object> getRemarkHistory(@PathVariable Long id) {
        try {
            log.info("BankerController :: getRemarkHistory claimId - {}", id);
            return ResponseHandler.response(verifierService.getRemarkHistory(id), MessageCode.success, true, HttpStatus.OK);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: getRemarkHistory e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Secured({"VERIFIER"})
    @ApiOperation(value = "Add remark", notes = "This can be used to add remark")
    @PostMapping(value = UrlMapping.ADD_CLAIM_REMARK)
    public ResponseEntity<Object> addClaimRemark(@PathVariable Long id, @RequestBody ClaimRemarkRequestDTO requestDTO) {
        try {
            log.info("BankerController :: addClaimRemark claimId - {}", id);
            ClaimsData claimsData = verifierService.getClaimData(id);
            if (Objects.isNull(claimsData)) {
                return ResponseHandler.response(null, MessageCode.invalidClaimId, false, HttpStatus.BAD_REQUEST);
            }
            ClaimsRemarksDTO claimsRemarksDTO = verifierService.addClaimRemark(claimsData, requestDTO);
            if(Objects.nonNull(claimsRemarksDTO)) {
                return ResponseHandler.response(claimsRemarksDTO, MessageCode.success, true, HttpStatus.OK);
            }
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("EXCEPTION WHILE BankerController :: addClaimRemark e - {}", e);
            return ResponseHandler.response(null, MessageCode.backText, false, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
