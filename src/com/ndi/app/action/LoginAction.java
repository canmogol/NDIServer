package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Param;
import com.fererlab.dto.Request;
import com.fererlab.dto.RequestKeys;
import com.fererlab.dto.Response;

/**
 * acm
 */
public class LoginAction extends BaseAction {

    public Response show(Request request) {
        return Ok(request).add("data", "testing").toResponse();
    }

    public Response login(Request request) {
        return Ok(request).add("data", "").toResponse();
    }

}
