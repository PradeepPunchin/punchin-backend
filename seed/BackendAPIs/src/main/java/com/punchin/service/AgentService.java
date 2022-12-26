package com.punchin.service;

import com.punchin.dto.AgentUploadDocumentDTO;
import com.punchin.dto.PageDTO;
import com.punchin.dto.UploadResponseUrl;
import com.punchin.entity.ClaimsData;
import com.punchin.entity.DocumentUrls;
import com.punchin.enums.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface AgentService {
    PageDTO getClaimsList(ClaimDataFilter claimDataFilter, Integer page, Integer limit);

    Map<String, Object> getDashboardData();

    boolean checkAccess(Long claimId);

    Map<String, Object> getClaimData(Long claimId);

    Map<String, Object> uploadDocument(AgentUploadDocumentDTO documentDTO);

    Map<String, Object> getClaimDocuments(Long id);

    Map<String, Object> discrepancyDocumentUpload(Long claimId, MultipartFile[] files, String docType);

    boolean checkDocumentIsInDiscrepancy(Long claimId, String docType);

    boolean checkDocumentUploaded(Long id);

    String forwardToVerifier(Long id);

    PageDTO getClaimSearchedData(SearchCaseEnum searchCaseEnum, String searchedKeyword, Integer pageNo, Integer limit, ClaimDataFilter claimDataFilter);

    String deleteClaimDocument(Long documentId);

    List<DocumentUrls> uploadAgentDocument(Long id, MultipartFile[] multipartFiles, AgentDocType docType);

    List<UploadResponseUrl> uploadAgentNewDocument(Long id,
                                                    CauseOfDeathEnum causeOfDeath,
                                                    AgentDocType deathCertificate,  MultipartFile[] deathCertificateMultipart,
                                                    String nomineeStatus,
                                                    AgentDocType signedClaim,  MultipartFile[] signedClaimMultipart,
                                                    AgentDocType relation_shipProof,  MultipartFile[] relation_shipProofMultipart,
                                                    AgentDocType gUARDIAN_ID_PROOF,  MultipartFile[] gUARDIAN_ID_PROOFMultipart,
                                                    AgentDocType gUARDIAN_ADD_PROOF,  MultipartFile[] gUARDIAN_ADD_PROOFMultipart,
                                                    AgentDocType borowerProof,  MultipartFile[] borowerProofMultipart);

    List<UploadResponseUrl> uploadAgentNewDocument2(Long id, KycOrAddressDocType nomineeProof,  MultipartFile[] nomineeMultiparts, AgentDocType bankerProof,  MultipartFile[] bankerPROOFMultipart, AgentDocType additionalDocs,  MultipartFile[] additionalMultipart);
}
