package com.fererlab.app;

import com.fererlab.action.ActionHandler;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.map.*;

/**
 * acm
 */
public abstract class BaseApplication implements Application {

    private EApplicationMode mode;
    private ActionHandler actionHandler = new ActionHandler();

    @Override
    public void setMode(EApplicationMode mode) {
        this.mode = mode;
        String suffix = EApplicationMode.DEVELOPMENT.equals(mode) ? ".dev" : (EApplicationMode.TESTING.equals(mode) ? ".test" : "");
        String directoryName = getClass().getSimpleName().toLowerCase();
        ProjectProperties.getInstance().readProjectProperties(directoryName, suffix);
        MessageProperties.getInstance().readMessageProperties(directoryName);
        ExecutionMap.getInstance().readUriExecutionMap(directoryName);
        AuthenticationAuthorizationMap.getInstance().readAuthenticationAuthorizationMap(directoryName);
        MimeTypeMap.getInstance().readMimeTypeMap(directoryName);
        CacheMap.getInstance().readCacheMap(directoryName);
        ContextMap.getInstance().readContextMap(directoryName);
        AuditMap.getInstance().readAuditMap(directoryName);
    }

    public EApplicationMode getMode() {
        return mode;
    }

    public String property(String key) {
        return ProjectProperties.getInstance().get(key);
    }

    @Override
    public Response runApplication(Request request) {
        // run action and return response
        return actionHandler.runAction(request);
    }
}
