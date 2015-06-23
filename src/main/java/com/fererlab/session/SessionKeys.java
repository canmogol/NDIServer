package com.fererlab.session;

/**
 * acm 10/15/12
 */
public enum SessionKeys {

    // user related
    USER("S_USER_COOKIE"),
//    IS_LOGGED("S_USER_IS_LOGGED"),
//    USERNAME("S_USER_USERNAME"),
//    GROUP_NAMES("S_USER_GROUP_NAMES"),
//    SESSION_ID("S_USER_SESSION_ID"),

    // cookie related
    COOKIE_SIGN_KEY("fr_ck_sn_ky"),
    COOKIE("cookie");


    private final String value;

    SessionKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
