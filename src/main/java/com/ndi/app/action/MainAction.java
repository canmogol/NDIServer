package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

public class MainAction extends BaseAction {

    public Response main(final Request request) {
        return Error(request, "This is the default action, check your URL, most probably you made a mistake!").toResponse();
    }

}