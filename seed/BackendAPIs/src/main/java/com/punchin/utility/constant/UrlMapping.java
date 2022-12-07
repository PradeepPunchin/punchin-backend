package com.punchin.utility.constant;

public class UrlMapping {

    public static final String BASEURL = "/api/v1";
    public static final String PUBLIC_URL = "/api/v2";

    public static final String ADMIN = BASEURL + "/admin";
    public static final String BANKER = BASEURL + "/banker";
    public static final String VERIFIER = BASEURL + "/verifier";
    public static final String AGENT = BASEURL + "/agent";

    public static final String GET_DASHBOARD_DATA = "/getDashboardData";
    public static final String GET_CLAIMS_LIST = "/claim";
    public static final String GET_CLAIM_DATA = "/claim/{claimId}";




    //Authentication Endpoints
    public static final String LOGIN = BASEURL + "/auth" + "/login";
    public static final String LOGOUT = BASEURL + "/auth" + "/logout";

    //Banker Endpoints
    public static final String BANKER_UPLOAD_DOCUMENT = "/claim/{claimId}/uploadDocument/{docType}";
    public static final String BANKER_UPLOAD_CLAIM = "/claim/upload";
    public static final String BANKER_SUBMIT_CLAIMS = "/claim/submit";
    public static final String BANKER_DISCARD_CLAIMS = "/claim/discard";

    //Verifier Endpoints
    public static final String VERIFIER_GET_DASHBOARD_DATA_COUNT = "/getDashboardDataCount";
    public static final String VERIFIER_GET_DOCUMENT_DETAILS = "/getDocumentDetails";
    public static final String VERIFIER_ACCEPT_AND_REJECT_DOCUMENTS = "/acceptAndRejectDocuments";


    //Agent Endpoints
    public static final String AGENT_UPLOAD_DOCUMENT = "/claim/{claimId}/uploadDocument";


}
