package com.fererlab.servlet;

import com.fererlab.app.ApplicationDescriptionHandler;
import com.fererlab.dto.*;
import com.fererlab.server.Connection;
import com.fererlab.server.ConnectionHandler;
import com.fererlab.session.Session;
import com.fererlab.session.SessionKeys;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * acm
 */
public class ServerServlet extends HttpServlet {

    private ApplicationDescriptionHandler applicationDescriptionHandler = null;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) {
        try {

            log("new client connection accepted, will create connection object and start ConnectionHandler");

            // create connection object
            Connection connection = new Connection();
            connection.setHttpServletResponse(resp);
            connection.setApplicationDescriptionHandler(applicationDescriptionHandler);

            // for servlet implementation parse request has to be done at the servlet
            parseRequest(req, connection);

            // pass the connection to handler and run
            ConnectionHandler connectionHandler = new ConnectionHandler(connection);

            // run this connection
            connectionHandler.run();

        } catch (Exception e) {
            log("serverServlet got an exception, will not listen anymore, needs restart, exception: " + e);
            e.printStackTrace();
        }
    }

    private void parseRequest(HttpServletRequest request, Connection connection) {

        // create headers and params maps
        ParamMap<String, Param<String, Object>> headers = new ParamMap<String, Param<String, Object>>();
        ParamMap<String, Param<String, Object>> params = new ParamMap<String, Param<String, Object>>();

        // add defaults request method
        params.addParam(new Param<String, Object>(RequestKeys.REQUEST_METHOD.getValue(), request.getMethod()));
        params.addParam(new Param<String, Object>(RequestKeys.URI.getValue(), request.getRequestURI()));
        params.addParam(new Param<String, Object>(RequestKeys.PROTOCOL.getValue(), request.getProtocol()));

        //  http://localhost:6000/js/loader/
        //  http
        String protocol = request.getRequestURL().toString().split(":")[0];
        //  localhost:6000/js/loader/
        String urlPart = request.getRequestURL().toString().substring(protocol.length() + "://".length());
        //  localhost:6000
        String domainAndPortPart = urlPart.split("/")[0];
        //  localhost:6000
        String[] domainAndPort = domainAndPortPart.split(":");
        String domain = domainAndPort[0];
        String port = "http".equalsIgnoreCase(protocol) ? "80" : "https".equalsIgnoreCase(protocol) ? "443" : null;
        if (domainAndPort.length > 0) {
            port = domainAndPort[1];
        }

        // add default headers
        headers.addParam(new Param<String, Object>(RequestKeys.HOST.getValue(), domain));
        headers.addParam(new Param<String, Object>(RequestKeys.HOST_NAME.getValue(), domain));
        headers.addParam(new Param<String, Object>(RequestKeys.HOST_PORT.getValue(), port));
        headers.addParam(new Param<String, Object>(RequestKeys.REMOTE_IP.getValue(), getRemoteIpAddress(request)));

        Enumeration headerKeys = request.getHeaderNames();
        while (headerKeys.hasMoreElements()) {
            String headerKey = String.valueOf(headerKeys.nextElement());
            headers.addParam(new Param<String, Object>(headerKey, request.getHeader(headerKey)));
        }

        String[] requestParams = request.getQueryString() != null ? request.getQueryString().split("&") : new String[]{};
        // requestParams    a=1&b<2&c>3&d=4&e!=5&5<f<7&g<=7&h>=8
        for (String paramKeyValue : requestParams) {
            String[] paramArr;

            paramArr = paramKeyValue.split("(<=)|(%3C=)", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.LE));
                continue;
            }

            paramArr = paramKeyValue.split("(>=)|(%3E=)", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.GE));
                continue;
            }

            paramArr = paramKeyValue.split("!=", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.NE));
                continue;
            }

            paramArr = paramKeyValue.split("=", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.EQ));
                continue;
            }

            paramArr = paramKeyValue.split("-like-", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.LIKE));
                continue;
            }

            paramArr = paramKeyValue.split("((<)|(%3C))*((<)|(%3C))");
            if (paramArr.length == 3) {
                params.addParam(new Param<String, Object>(paramArr[1], paramArr[0], paramArr[2], ParamRelation.BETWEEN));
                continue;
            }

            paramArr = paramKeyValue.split("(<)|(%3C)", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.LT));
                continue;
            }

            paramArr = paramKeyValue.split("(>)|(%3E)", 2);
            if (paramArr.length == 2) {
                params.addParam(new Param<String, Object>(paramArr[0], paramArr[1], ParamRelation.GT));
            }

        }

        // create, prepare and set the session to request
        Session session;
        if (headers.containsKey(SessionKeys.COOKIE.getValue())) {
            session = new Session(String.valueOf(headers.get(SessionKeys.COOKIE.getValue()).getValue()));
        } else {
            // request does not have any param with key "Cookie"
            // create an empty session object
            session = new Session("");
        }

        // check if the header contains HOST
        if (headers.containsKey(RequestKeys.HOST.getValue())) {
            Param<String, Object> hostParam = headers.get(RequestKeys.HOST.getValue());
            String host = hostParam.getValue().toString();
            String[] hostNamePort = trim(host.split(":"));
            if (hostNamePort.length == 2) {
                headers.addParam(new Param<String, Object>(RequestKeys.HOST_NAME.getValue(), hostNamePort[0]));
                headers.addParam(new Param<String, Object>(RequestKeys.HOST_PORT.getValue(), hostNamePort[1]));
            } else if (hostNamePort.length == 1) {
                headers.addParam(new Param<String, Object>(RequestKeys.HOST_NAME.getValue(), hostNamePort[0]));
            }
        }

        // set request to connection object
        connection.setRequest(new Request(params, headers, session));

    }

    private String[] trim(String[] strings) {
        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].trim();
            }
        }
        return strings;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        applicationDescriptionHandler = ApplicationDescriptionHandler.getInstance();
        // Prepare the application descriptions
        String applicationDescriptionFile = getClass().getResource("/").getPath() + "applications.properties";
        try {
            applicationDescriptionHandler.reloadApplicationDescriptions(applicationDescriptionFile);
        } catch (IOException e) {
            log("could not load the application description file, e: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        applicationDescriptionHandler.stopApplications();
        super.destroy();
    }

    private String getRemoteIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip : "";
    }
}
