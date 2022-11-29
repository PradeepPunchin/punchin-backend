package com.punchin.utility.constant;

public class Headers {
    /*
     * CORS configuration Headers
     */
    public static final String ACCESS_CONTROL_ALLOW = "Access-Control-Allow-";
    public static final String ACCESS_CONTROL_ALLOW_METHODS = ACCESS_CONTROL_ALLOW + "Methods";
    public static final String ACCESS_CONTROL_ALLOW_ORIGIN = ACCESS_CONTROL_ALLOW + "Origin";
    public static final String ACCESS_CONTROL_ALLOW_HEADERS = ACCESS_CONTROL_ALLOW + "Headers";
    public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS = ACCESS_CONTROL_ALLOW + "Credentials";

    /*
     * Request Headers for API's
     */
    public static final String AUTH_TOKEN = "X-Xsrf-Token";
    public static final String PREFFERED_LANGUAGE = "X-preferedLanguage";

    private Headers() {
        /*
         *A private constructor to restrict useless referrence call of class members
         */
    }
}
