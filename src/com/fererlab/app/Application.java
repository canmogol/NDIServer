package com.fererlab.app;

import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

/**
 * acm | 12/11/12
 */
public interface Application {

    void setDevelopmentMode(boolean isDevelopment);

    boolean isDevelopmentModeOn();

    void start();

    Response runApplication(final Request request);

    void stop();

}
