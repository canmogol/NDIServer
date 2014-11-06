package com.fererlab.dto;

/**
 * acm 10/15/12
 */
public enum RequestKeys {

    URL("URL"),
    PROTOCOL("PROTOCOL"),
    IP("IP"),
    PORT("PORT"),
    URI("URI"),
    HOST("Host"),
    HOST_NAME("HOST_NAME"),
    HOST_PORT("HOST_PORT"),
    REMOTE_IP("REMOTE_IP"),
    QUERY_STRING("QUERY_STRING"),
    REQUEST_METHOD("REQUEST_METHOD"),
    REQUEST_POST_DATA("REQUEST_POST_DATA"),
    APPLICATION_REQUEST("APPLICATION_REQUEST"),
    APPLICATION_URI("APPLICATION_URI"),
    APPLICATION_PATH("APPLICATION_PATH"),
    RESPONSE_TEMPLATE("RESPONSE_TEMPLATE"),
    RESPONSE_TYPE("RESPONSE_TYPE"),
    HEADER_NAMES("HEADER_NAMES");

    private final String value;

    RequestKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
