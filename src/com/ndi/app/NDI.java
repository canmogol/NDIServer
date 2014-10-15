package com.ndi.app;

import com.fererlab.app.BaseApplication;
import com.fererlab.db.EM;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;


/**
 * acm
 */
public class NDI extends BaseApplication {

    @Override
    public void start() {
        if (!isDevelopmentModeOn()) {
            try {
                EM.start("db");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Response runApplication(final Request request) {
        // read the cookie to Session object
        request.getSession().fromCookie(this.getClass().getPackage().getName() + "." + this.getClass().getName(),
                "23746s2s8ad723423jh2323746s2s8ad723423jh-989asc2213543687sad12311234t");
        // run application
        return super.runApplication(request);
    }

    @Override
    public void stop() {
        EM.stop();
    }

}