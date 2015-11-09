package com.ndi.app.service;

import com.fererlab.ndi.Default;

/**
 * acm
 */
@Default(LDAPServiceTrueImpl.class)
public interface LDAPService {

    boolean checkUsernamePassword(String Username, String password);

}
