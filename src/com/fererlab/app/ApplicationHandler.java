package com.fererlab.app;

import com.fererlab.dto.*;
import com.fererlab.session.Session;

import java.util.logging.Logger;

/**
 * acm | 12/12/12
 */
public class ApplicationHandler {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    public Response runApplication(Request request, ApplicationDescriptionHandler adh) {
        try {
            String projectName = null;
            String applicationName = "";
            String[] uriParts = (String.valueOf(request.getParams().get(RequestKeys.URI.getValue()).getValue())).split("/");
            if (uriParts.length > 2) {
                projectName = uriParts[1].trim();
                applicationName = uriParts[2].trim();
            } else if (uriParts.length > 1) {
                applicationName = uriParts[1].trim();
            }
            applicationName = applicationName.startsWith("/") ? applicationName : "/" + applicationName;
            // change the request URI for application to handle request correctly
            String currentRequestURI = request.getParams().getValue(RequestKeys.URI.getValue()).toString();
            String uriStartsWith = projectName == null ? "/" + applicationName : "/" + projectName + applicationName;
            // run the application if exists otherwise try to run the default application if available
            if (!adh.applicationExists(applicationName)) {
                if (adh.getDefaultApplication() != null) {
                    applicationName = adh.getDefaultApplication();
                    uriStartsWith = projectName == null ? "/" : "/" + projectName;
                } else {
                    log("will return not found message");
                    return new Response(new ParamMap<String, Param<String, Object>>(), new Session(""), Status.STATUS_NOT_FOUND);
                }
            }

            if (currentRequestURI.startsWith(uriStartsWith)) {
                currentRequestURI = currentRequestURI.substring((uriStartsWith).length());
                if (currentRequestURI.lastIndexOf("?") != -1) {
                    currentRequestURI = currentRequestURI.substring(0, currentRequestURI.lastIndexOf("?"));
                }
                Param<String, Object> param = new Param<String, Object>(
                        RequestKeys.URI.getValue(),
                        currentRequestURI
                );
                request.getParams().put(RequestKeys.URI.getValue(), param);
                log("request URI for application: " + applicationName + " changed to: \"" + request.getParams().get(RequestKeys.URI.getValue()).getValue() + "\"");
            }

            log("will run the application: " + applicationName);
            return adh.getApplication(applicationName).runApplication(request);
        } catch (Exception e) {
            e.printStackTrace();
            log("Exception occurred while running the application, will return service unavailable message, e: " + e.getMessage());
            return new Response(new ParamMap<String, Param<String, Object>>(), new Session(""), Status.STATUS_SERVICE_UNAVAILABLE);
        }
    }

    private void log(String log) {
        logger.info(log);
    }

}
