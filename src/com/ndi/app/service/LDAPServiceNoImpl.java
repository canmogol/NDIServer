package com.ndi.app.service;

/**
 * acm
 */
public class LDAPServiceNoImpl implements LDAPService {

    @Override
    public boolean checkUsernamePassword(String Username, String password) {
        return false;
    }

}
