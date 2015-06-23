package com.fererlab.server;

import com.fererlab.app.ApplicationDescriptionHandler;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

import javax.servlet.http.HttpServletResponse;

/**
 * acm | 12/11/12
 */
public class Connection {

    private Request request;
    private Response response;
    private ApplicationDescriptionHandler applicationDescriptionHandler;
    private HttpServletResponse httpServletResponse;

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public void setApplicationDescriptionHandler(ApplicationDescriptionHandler applicationDescriptionHandler) {
        this.applicationDescriptionHandler = applicationDescriptionHandler;
    }

    public ApplicationDescriptionHandler getApplicationDescriptionHandler() {
        return applicationDescriptionHandler;
    }

    public HttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }
}
