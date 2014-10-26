package com.fererlab.dto;

import com.fererlab.session.Session;

import java.io.Serializable;

/**
 * acm 10/15/12
 */
public class Request implements Serializable {

    private ParamMap<String, Param<String, Object>> headers;
    private ParamMap<String, Param<String, Object>> params;
    private Session session;

    public Request(ParamMap<String, Param<String, Object>> params, ParamMap<String, Param<String, Object>> headers, Session session) {
        this.session = session;
        this.params = params;
        this.headers = headers;
    }

    public Session getSession() {
        return session;
    }

    public ParamMap<String, Param<String, Object>> getHeaders() {
        return headers;
    }

    public ParamMap<String, Param<String, Object>> getParams() {
        return params;
    }

    public Param<String, Object> getParam(String key) {
        return params.get(key);
    }

    public String get(String key) {
        if (getParams() != null && getParams().containsKey(key)) {
            return String.valueOf(getParams().getValue(key));
        } else {
            return null;
        }
    }

    public String getHeader(String key) {
        if (getHeaders() != null && getHeaders().containsKey(key)) {
            return String.valueOf(getHeaders().get(key).getValue());
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Request{" +
                "headers=" + headers +
                ", params=" + params +
                '}';
    }
}
