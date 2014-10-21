package com.fererlab.app;

import com.fererlab.action.ActionHandler;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.map.MessageProperties;
import com.fererlab.map.ProjectProperties;

/**
 * acm
 */
public abstract class BaseApplication implements Application {

    private EApplicationMode mode;
    private ActionHandler actionHandler = new ActionHandler(
            getClass().getClassLoader().getResource("ExecutionMap.properties"),
            getClass().getClassLoader().getResource("AuthenticationAuthorizationMap.properties"),
            getClass().getClassLoader().getResource("MimeTypesMap.properties"),
            getClass().getClassLoader().getResource("CacheMap.properties"),
            getClass().getClassLoader().getResource("ContextMap.properties")
    );

    @Override
    public void setMode(EApplicationMode mode) {
        this.mode = mode;
        String suffix = EApplicationMode.DEVELOPMENT.equals(mode) ? ".dev" : (EApplicationMode.TESTING.equals(mode) ? ".test" : "");
        ProjectProperties.getInstance().readProjectProperties(getClass().getSimpleName().toLowerCase(), suffix);
        MessageProperties.getInstance().readMessageProperties(getClass().getSimpleName().toLowerCase());
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
