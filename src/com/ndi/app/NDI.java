package com.ndi.app;

import com.fererlab.app.BaseApplication;
import com.fererlab.app.EApplicationMode;
import com.fererlab.db.EM;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;


/**
 * acm
 */
public class NDI extends BaseApplication {

    @Override
    public void start() {
        if (EApplicationMode.DEVELOPMENT.equals(getMode())) {
            try {
                EM.start(property("persistence-unit"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Response runApplication(final Request request) {
        // read the cookie to Session object
        request.getSession().fromCookie(this.getClass().getName(), property("sign"));
        // run application
        return super.runApplication(request);
    }

    @Override
    public void stop() {
        EM.stop();
    }

}