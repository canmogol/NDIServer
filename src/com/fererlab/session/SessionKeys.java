package com.fererlab.session;

/**
 * acm 10/15/12
 */
public enum SessionKeys {

    USERNAME("USERNAME"),
    PASSWORD("PASSWORD"),
    AUTHENTICATION_TYPE("AUTHENTICATION_TYPE"),
    SESSION_STORED_AT("SESSION_STORED_AT"),
    SESSION_ID("SESSION_ID"),
    IS_LOGGED("IS_LOGGED"),
    COOKIE_SIGN_KEY("fr_ck_sn_ky"),
    GROUP_NAMES("GROUP_NAMES"),
    COOKIE("cookie");

    private final String value;

    SessionKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
