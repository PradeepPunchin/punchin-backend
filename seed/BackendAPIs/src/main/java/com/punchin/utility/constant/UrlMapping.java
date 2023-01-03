package com.punchin.utility.constant;

public class UrlMapping {



    private UrlMapping() {
    }

    public static final String BASEURL = "/api/v1";
    public static final String PUBLIC_URL = "/api/v2";

    public static final String ADMIN = BASEURL + "/admin";
    public static final String BANKER = BASEURL + "/banker";
    public static final String VERIFIER = BASEURL + "/verifier";
    public static final String AGENT = BASEURL + "/agent";

    public static final String GET_DASHBOARD_DATA = "/getDashboardData";
    public static final String GET_CLAIMS_LIST = "/claim";
    public static final String GET_CLAIM_DATA = "/claim/{id}";
    public static final String GET_CLAIM_DOCUMENTS = "/claim/{id}/documents";
    public static final String DOWNLOAD_CLAIM_DOCUMENT_DATA = "/claim/{id}/download-all-documents";
    public static final String GET_CLAIM_SEARCHED_DATA_VERIFIER = "/claim/searchVerifier";
    public static final String DOWNLOAD_MIS_REPORT = "/claim/download-mis-report";
    public static final String DELETE_CLAIM_DOCUMENT = "/claim/document/delete/{docId}";
    public static final String REQUEST_ADDITIONAL_DOCUMENT = "/claim/document/additional-request";
    public static final String GET_CLAIM_HISTORY = "/claim/{id}/history";


    //Authentication Endpoints
    public static final String LOGIN = BASEURL + "/auth" + "/login";
    public static final String LOGOUT = BASEURL + "/auth" + "/logout";

    //Banker Endpoints
    public static final String BANKER_UPLOAD_DOCUMENT = "/claim/{id}/uploadDocument/{docType}";
    public static final String BANKER_UPLOAD_CLAIM = "/claim/upload";
    public static final String BANKER_CSV_UPLOAD_CLAIM = "/csv/claim/upload";
    public static final String BANKER_SUBMIT_CLAIMS = "/claim/submit";
    public static final String BANKER_DISCARD_CLAIMS = "/claim/discard";
    public static final String BANKER_STANDARIZED_FORMAT = "/download-excel-format";
    public static final String FORWARD_TO_VERIFIER = "/claim/{id}/forward-to-verifier";
    public static final String BANKER_SAVEAS_DRAFT_DOCUMENT = "/claim/{claimId}/documents/save-draft";
    public static final String GET_CLAIM_SEARCHED_DATA_BANKER = "/claim/searchBanker";
    public static final String DISCREPANCY_DOCUMENT_UPLOAD = "/claim/{id}/discrepancy-document-upload/{docType}";


    //Verifier Endpoints
    public static final String VERIFIER_ALLOCATE_CLAIM = "/claim/{id}/allocate/{agentId}";
    public static final String VERIFIER_CLAIMS_VERIFICATION_REQUEST = "/claim/verification-requests";
    public static final String VERIFIER_ACCEPT_AND_REJECT_DOCUMENTS = "/claim/{id}/document/{docId}/doc-approve-reject";
    public static final String VERIFIER_GET_CLAIM_DATA_WITH_DOCUMENT_STATUS = "/claim/data-with-document-status";
    public static final String DOWNLOAD_VERIFIER_GET_CLAIM_DATA_WITH_DOCUMENT_STATUS = "/downloadMIS/claim/data-with-document-status";


    //Agent Endpoints
    public static final String AGENT_UPLOAD_DOCUMENT = "/claim/{id}/uploadDocument";
    public static final String GET_CLAIM_SEARCHED_DATA = "/claim/search";

    public static final String UPLOAD_DOCUMENT_AGENT = "/claim/uploadDocument";

    public static final String AGENT_DELETE_DOCUMENT = "/claim/document/delete";
    public static final String GET_ALL_AGENTS_VERIFIER = "/agents";

    public static final String UPLOAD_DOCUMENT_NEW_AGENT = "/claim/uploadDocumentNew1";
    public static final String UPLOAD_DOCUMENT_NEW_AGENT2 = "/claim/uploadDocumentNew2";

    public static final String CLAIM_DATA_AGENT_ALLOCATION = "/claim/agentAllocation";

}
