package com.ndi.app.service;

import com.fererlab.ndi.Wire;

/**
 * acm
 */
public class FormService {

    @Wire
    private LDAPService ldapService;

    public boolean validateForm(Object form){
        return true;
    }

}
