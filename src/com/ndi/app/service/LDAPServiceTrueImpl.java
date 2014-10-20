package com.ndi.app.service;

/**
 * acm
 */
public class LDAPServiceTrueImpl implements LDAPService {
    @Override
    public boolean checkUsernamePassword(String Username, String password) {
        return true;
    }
}
