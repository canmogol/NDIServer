package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.dto.Status;

public class MainAction extends BaseAction {

    public Response main(final Request request) {
        return Response.create(request, "This is the default action, check your URL, most probably you made a mistake!", Status.STATUS_OK);
    }

}