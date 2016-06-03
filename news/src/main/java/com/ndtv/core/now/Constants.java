package com.ndtv.core.now;


class Constants {

    public static final int ALARM_SERVICE_REQUEST_CODE = 0;
    static final String SERVER_CLIENT_ID =
            "72129834489-uk4o358ag2ievntn375ab6a4spltimn3.apps.googleusercontent.com";
    static final String CHECK_CREDENTIALS_URL =
            "http://social.ndtv.com/ajax/login-google-now.php?action=getRefreshToken";
    static final String ADD_CREDENTIALS_URL =
            "http://social.ndtv.com/ajax/login-google-now.php";
    static final String REVOKE_CREDENTIALS_URL = "https://accounts.google.com/o/oauth2/revoke";
    static final String VALID_CREDENTIALS_SERVER_RESPONSE = "valid";
    static final String INVALID_CREDENTIALS_SERVER_RESPONSE = "invalid";
    static final String USER_PARAM = "email";
    static final String AUTH_CODE_PARAM = "authCode";
    static final String BROADCAST_ACTION =
            "com.google.api.services.samples.now.nowsampleclient.BROADCAST";

    static final String GET_AUTH_CODE_METHOD = "GetAuthCode";

    static final String AUTH_CODE_EXTRA = "authcode";

    static final String ACCESS_TOKEN_EXTRA = "accessToken";

    static final String METHOD_EXTRA = "method";
    static final String DATA_STATUS_EXTRA = "statusCode";
    static final String DATA_RESPONSE_EXTRA = "responseText";
    static final String PARAMS_EXTRA = "params";
    static String USER_ID = "";
}
