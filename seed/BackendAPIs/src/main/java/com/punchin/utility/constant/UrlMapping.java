package com.punchin.utility.constant;

public class UrlMapping {

    public static final String BASEURL = "/api/v1";
    public static final String PUBLIC_URL = "/api/v2";


    //Authentication Endpoints
    public static final String LOGIN = BASEURL + "/auth" + "/login";
    public static final String LOGOUT = BASEURL + "/auth" + "/logout";

    //Banker Endpoints
    public static final String GET_DASHBOARD_DATA = BASEURL + "/banker" + "/getDashboardData";
    public static final String GET_CLAIMS_LIST = BASEURL + "/banker" + "/claim";
    public static final String GET_CLAIM_DATA = BASEURL + "/banker" + "/claim/{claimId}";
    public static final String UPLOAD_CLAIM = BASEURL + "/banker" + "/claim/upload";
    public static final String SUBMIT_CLAIMS = BASEURL + "/banker" + "/claim/submit";
    public static final String DISCARD_CLAIMS = BASEURL + "/banker" + "/claim/discard";
    public static final String UPLOAD_DOCUMENT = BASEURL + "/banker" + "/claim/{claimId}/uploadDocument/{docType}";
}
