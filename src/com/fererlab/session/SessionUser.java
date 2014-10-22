package com.fererlab.session;

import java.io.Serializable;
import java.util.*;

/**
 * acm
 */
public class SessionUser implements Serializable, Cloneable {

    private boolean isLogged = false;
    private String username;
    private String sessionId;
    private Set<String> groups = new HashSet<String>();
    private Map<String, Object> properties = new TreeMap<String, Object>();

    public SessionUser() {
    }

    public boolean isLogged() {
        return isLogged;
    }

    public void setLogged(boolean logged) {
        isLogged = logged;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void logout() {
        setLogged(false);
        setUsername(null);
        setSessionId(null);
        getGroups().clear();
        getProperties().clear();
    }
}
