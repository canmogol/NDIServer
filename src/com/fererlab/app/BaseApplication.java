package com.fererlab.app;

import com.fererlab.ndi.*;
import com.fererlab.action.ActionHandler;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

/**
 * acm
 */
public abstract class BaseApplication implements Application {

    private ActionHandler actionHandler = new ActionHandler(
            getClass().getClassLoader().getResource("ExecutionMap.properties"),
            getClass().getClassLoader().getResource("AuthenticationAuthorizationMap.properties"),
            getClass().getClassLoader().getResource("MimeTypesMap.properties"),
            getClass().getClassLoader().getResource("CacheMap.properties"),
            getClass().getClassLoader().getResource("ContextMap.properties")
    );

    private boolean isDevelopment = false;

    private WireContext context;

    @Override
    public void setDevelopmentMode(boolean isDevelopment) {
        this.isDevelopment = isDevelopment;
    }

    @Override
    public boolean isDevelopmentModeOn() {
        return isDevelopment;
    }

    @Override
    public Response runApplication(Request request) {
        // run action and return response
        return actionHandler.runAction(request);
    }
}
