package com.fererlab.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * acm
 */
@XStreamAlias("response")
public class ServerResponse {

    @XStreamAlias("header")
    private Pair<String, Object> header = new Pair<String, Object>();

    @XStreamAlias("content")
    private Pair<String, Object> content = new Pair<String, Object>();

    @XStreamAlias("exception")
    private Pair<String, Object> exception = new Pair<String, Object>();

    public ServerResponse() {
    }

    public ServerResponse(String status, Integer code, String message) {
        header.add("status", status);
        header.add("code", code);
        header.add("message", message);
    }

    public ServerResponse header(String key, Object value) {
        header.add(key, value);
        return this;
    }

    public ServerResponse add(String key, Object value) {
        content.add(key, value);
        return this;
    }

    public ServerResponse exception(String key, Object value) {
        exception.add(key, value);
        return this;
    }

    public Pair<String, Object> getHeader() {
        return header;
    }

    public Pair<String, Object> getContent() {
        return content;
    }

    public Pair<String, Object> getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "ServerResponse{" +
                "header=" + header +
                ", content=" + content +
                ", exception=" + exception +
                '}';
    }
}
