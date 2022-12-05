package com.punchin.utility.constant;

public class UrlMapping {

    public static final String BASEURL = "/api/v1";
    public static final String PUBLIC_URL = "/api/v2";

    public static final String ADMIN = BASEURL + "/admin";
    public static final String BANKER = BASEURL + "/banker";
    public static final String VERIFIER = BASEURL + "/verifier";
    public static final String AGENT = BASEURL + "/agent";


    //Authentication Endpoints
    public static final String LOGIN = BASEURL + "/auth" + "/login";
    public static final String LOGOUT = BASEURL + "/auth" + "/logout";

    //Banker Endpoints
    public static final String BANKER_GET_DASHBOARD_DATA = "/getDashboardData";
    public static final String BANKER_GET_CLAIMS_LIST = "/claim";
    public static final String BANKER_GET_CLAIM_DATA = "/claim/{claimId}";
    public static final String BANKER_UPLOAD_CLAIM = "/claim/upload";
    public static final String BANKER_SUBMIT_CLAIMS = "/claim/submit";
    public static final String BANKER_DISCARD_CLAIMS = "/claim/discard";
    public static final String BANKER_UPLOAD_DOCUMENT = "/claim/{claimId}/uploadDocument/{docType}";

}
