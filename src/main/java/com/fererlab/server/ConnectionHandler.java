package com.fererlab.server;

import com.fererlab.app.ApplicationHandler;
import com.fererlab.dto.*;

import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * acm | 12/11/12
 */
public class ConnectionHandler implements Runnable {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final Connection connection;
    private ApplicationHandler applicationHandler = new ApplicationHandler();

    public ConnectionHandler(final Connection connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        try {
            log("ConnectionHandler start");

            // run the application
            runApplication();

            // add default response headers
            addResponseHeaders();

            // send the response back
            sendResponseBack();

        } catch (Throwable e) {
            log("exception occurred: " + e);
            e.printStackTrace();
        }
        log("ConnectionHandler shutdown");
    }

    private String[] trim(String[] strings) {
        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].trim();
            }
        }
        return strings;
    }

    private void runApplication() {
        long start = System.currentTimeMillis();
        Response response = applicationHandler.runApplication(connection.getRequest(), connection.getApplicationDescriptionHandler());
        log("application run in " + (System.currentTimeMillis() - start) + " milliseconds");
        connection.setResponse(response);
    }

    private void addResponseHeaders() {
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.PROTOCOL.getValue(), connection.getRequest().getParams().get(RequestKeys.PROTOCOL.getValue()).getValue()));
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.STATUS.getValue(), "" + Status.STATUS_OK.getStatus()));
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.MESSAGE.getValue(), Status.STATUS_OK.getMessage()));
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.EXPIRES.getValue(),
                !connection.getResponse().getHeaders().containsKey(ResponseKeys.EXPIRES.getValue()) ?
                        "-1" :
                        connection.getResponse().getHeaders().getValue(ResponseKeys.EXPIRES.getValue()))
        );
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.CACHE_CONTROL.getValue(),
                !connection.getResponse().getHeaders().containsKey(ResponseKeys.CACHE_CONTROL.getValue()) ?
                        "Cache-Control: no-store, no-cache, must-revalidate, post-check=0, pre-check=0, private, max-age=0" :
                        connection.getResponse().getHeaders().getValue(ResponseKeys.CACHE_CONTROL.getValue()))
        );
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.SERVER.getValue(), "bucket"));


        // default response type is text/html
        String returnType = "text/html";
        // if response headers contains RESPONSE_TYPE, which indicates that it is added by the application
        if (connection.getResponse().getHeaders().containsKey(ResponseKeys.RESPONSE_TYPE.getValue())) {
            // "json" and "xml" are shortcuts for application development, just for convenience
            if ("json".equalsIgnoreCase(String.valueOf(connection.getResponse().getHeaders().getValue(ResponseKeys.RESPONSE_TYPE.getValue())))) {
                returnType = "application/json";
            } else if ("xml".equalsIgnoreCase(String.valueOf(connection.getResponse().getHeaders().getValue(ResponseKeys.RESPONSE_TYPE.getValue())))) {
                returnType = "text/xml";
            } else {
                returnType = String.valueOf(connection.getResponse().getHeaders().getValue(ResponseKeys.RESPONSE_TYPE.getValue()));
            }
        }
        // if the request headers contains the RESPONSE_TYPE which indicates that the client ask for it
        else if (connection.getRequest().getHeaders().containsKey(RequestKeys.RESPONSE_TYPE.getValue())) {
            if ("json".equalsIgnoreCase(String.valueOf(connection.getRequest().getHeaders().getValue(ResponseKeys.RESPONSE_TYPE.getValue())))) {
                returnType = "application/json";
            } else if ("xml".equalsIgnoreCase(String.valueOf(connection.getRequest().getHeaders().getValue(ResponseKeys.RESPONSE_TYPE.getValue())))) {
                returnType = "text/xml";
            } else {
                returnType = String.valueOf(connection.getRequest().getHeaders().getValue(RequestKeys.RESPONSE_TYPE.getValue()));
            }

        }
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.CONTENT_TYPE.getValue(), returnType + "; charset=UTF-8"));
        connection.getResponse().getHeaders().addParam(new Param<String, Object>(ResponseKeys.CONTENT_LENGTH.getValue(), connection.getResponse().getContent().length));
    }

    private void sendResponseBack() throws IOException {
        // set status
        connection.getHttpServletResponse().setStatus(connection.getResponse().getStatus().getStatus());

        // set cookie(s)
        Map<String, String> keyValueMap = connection.getResponse().getSession().getKeyValueMap();
        for (String key : keyValueMap.keySet()) {
            if (key != null && !key.isEmpty()) {
                Cookie cookie = new Cookie(key, keyValueMap.get(key));
                cookie.setPath("/");
                cookie.setMaxAge(10 * 365 * 24 * 60 * 60);
                connection.getHttpServletResponse().addCookie(cookie);
            }
        }

        // set headers
        ParamMap<String, Param<String, Object>> param = connection.getResponse().getHeaders();
        for (String key : param.keySet()) {
            connection.getHttpServletResponse().setHeader(key, String.valueOf(param.get(key).getValue()));
        }

        // write content
        connection.getHttpServletResponse().getOutputStream().write(connection.getResponse().getContent());
    }

    private void log(String log) {
        logger.info("[" + Thread.currentThread().getId() + "] " + log);
    }

}
