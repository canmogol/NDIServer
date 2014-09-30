package com.fererlab.server;

/**
 * acm | 12/11/12
 */
public enum PropertyKeys {

    LISTEN_PORTS("listen.ports"),
    MAXIMUM_THREAD_COUNT("maximum.thread.count"),
    LOGGER_LEVEL("logger.level"),
    CONFIG_FILE("config"),
    APP_DESC_FILE("application.description.file");

    private final String value;

    PropertyKeys(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
