package com.punchin.utility.constant;

public class UrlMapping {

    public static final String BASEURL = "/api/v1";
    public static final String PUBLIC_URL = "/api/v2";


    //Authentication Endpoints
    public static final String LOGIN = BASEURL + "/auth" + "/login";
    public static final String LOGOUT = BASEURL + "/auth" + "/logout";

    //Banker Endpoints
    public static final String GET_DASHBOARD_DATA = BASEURL + "/banker" + "/getDashboardData";
    public static final String GET_CLAIMS_DATA = BASEURL + "/banker" + "/getClaimsData";
    public static final String UPLOAD_CLAIM = BASEURL + "/banker" + "/upload";
    public static final String SUBMIT_CLAIMS = BASEURL + "/banker" + "/submitClaims";
    public static final String DISCARD_CLAIMS = BASEURL + "/banker" + "/discardClaims";
}
