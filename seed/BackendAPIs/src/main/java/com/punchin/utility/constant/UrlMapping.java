package com.punchin.utility.constant;

public class UrlMapping {

    private UrlMapping(){}

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




    //Authentication Endpoints
    public static final String LOGIN = BASEURL + "/auth" + "/login";
    public static final String LOGOUT = BASEURL + "/auth" + "/logout";

    //Banker Endpoints
    public static final String BANKER_UPLOAD_DOCUMENT = "/claim/{id}/uploadDocument/{docType}";
    public static final String BANKER_UPLOAD_CLAIM = "/claim/upload";
    public static final String BANKER_SUBMIT_CLAIMS = "/claim/submit";
    public static final String BANKER_DISCARD_CLAIMS = "/claim/discard";
    public static final String FORWARD_TO_VERIFIER = "/claim/{id}/forward-to-verifier";


    //Verifier Endpoints
    public static final String VERIFIER_ALLOCATE_CLAIM = "/claim/{id}/allocate/{agentId}";
    public static final String VERIFIER_CLAIMS_VERIFICATION_REQUEST = "/claim/verification-requests";
    public static final String VERIFIER_ACCEPT_AND_REJECT_DOCUMENTS = "/claim/{id}/document/{docId}/doc-approve-reject";
    public static final String VERIFIER_GET_CLAIM_DATA_WITH_DOCUMENT_STATUS = "/claim/{id}/document/{docId}/doc-approve-reject";


    //Agent Endpoints
    public static final String AGENT_UPLOAD_DOCUMENT = "/claim/{id}/uploadDocument";
    public static final String AGENT_DISCREPANCY_DOCUMENT_UPLOAD = "/claim/{id}/discrepancy-document-upload/{docType}";


}
