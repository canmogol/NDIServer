package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

/**
 * acm
 */
public class DevAction extends BaseAction {

    public Response show(Request request) {
        return Ok(request).add("data", "dev testing").toResponse();
    }

    public Response login(Request request) {
        return Ok(request).add("data", "logged").toResponse();
    }

}
