package com.fererlab.action;

import com.fererlab.cache.Cache;
import com.fererlab.db.EM;
import com.fererlab.db.Transactional;
import com.fererlab.dto.*;
import com.fererlab.map.*;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * acm | 1/16/13
 */
public class ActionHandler {

    private ExecutionMap executionMap = ExecutionMap.getInstance();
    private AuthenticationAuthorizationMap authenticationAuthorizationMap = AuthenticationAuthorizationMap.getInstance();
    private MimeTypeMap mimeTypeMap = MimeTypeMap.getInstance();
    private CacheMap cacheMap = CacheMap.getInstance();
    private ContextMap contextMap = ContextMap.getInstance();

    public ActionHandler(URL executionMapFile, URL authenticationAuthorizationMapFile, URL mimeTypeMapFile, URL cacheMapFile, URL contextFile) {
        executionMap.readUriExecutionMap(executionMapFile);
        authenticationAuthorizationMap.readAuthenticationAuthorizationMap(authenticationAuthorizationMapFile);
        mimeTypeMap.readMimeTypeMap(mimeTypeMapFile);
        cacheMap.readCacheMap(cacheMapFile);
        contextMap.readContextMap(contextFile);
    }

    public Response runAction(final Request request) {

        // prepare method and action class
        Method method;
        Class<?> actionClass;

        // define the className and methodName here
        String className = null;
        String methodName = null;
        String templateName = null;

        // get the request Method(GET, POST etc.) and URI
        String requestMethod = request.getParams().get(RequestKeys.REQUEST_METHOD.getValue()).getValue().toString();
        String requestURI = request.getParams().get(RequestKeys.URI.getValue()).getValue().toString();
        if (requestURI == null || requestURI.trim().isEmpty()) {
            requestURI = "/";
        }

        // URI starting with /_/ indicates it is a resource but not an action
        if (requestURI.startsWith("/_/") && requestURI.lastIndexOf("..") == -1) {

            Map.Entry<byte[], String> entry = Cache.getContentIfCached(requestURI);
            if (entry == null) {
                // request URI is either one of these; xsl, css, js, image, file,
                FileContentHandler fileContentHandler = new FileContentHandler();
                byte[] content = new byte[0];
                try {
                    content = fileContentHandler.getContent(fileContentHandler.getContentPath(), requestURI);
                } catch (FileNotFoundException e) {
                    return new Response(
                            new ParamMap<String, Param<String, Object>>(),
                            request.getSession(),
                            Status.STATUS_NOT_FOUND,
                            ""
                    );
                }
                String extension = fileContentHandler.getFileExtension();
                Map<byte[], String> contentAndExtension = new HashMap<byte[], String>();
                contentAndExtension.put(content, extension);
                entry = contentAndExtension.entrySet().iterator().next();
                Cache.put(requestURI, entry);
            }
            Response response = new Response(
                    new ParamMap<String, Param<String, Object>>(),
                    request.getSession(),
                    Status.STATUS_OK,
                    entry.getKey()
            );
            response.getHeaders().put(
                    ResponseKeys.RESPONSE_TYPE.getValue(),
                    new Param<String, Object>(
                            ResponseKeys.RESPONSE_TYPE.getValue(),
                            mimeTypeMap.get(entry.getValue())
                    )
            );
            return response;
        } else if (requestURI.startsWith("/-/") && requestURI.lastIndexOf("..") == -1) {
            // run groovy actions
            String[] uriParts = requestURI.substring("/-/".length()).split("/");
            if (uriParts.length > 0) {
                String dynamicClassName = uriParts[0];
                String dynamicMethodName = "run";
                if (uriParts.length > 1) {
                    dynamicMethodName = uriParts[1];
                }
                //   GAction action
                className = GAction.class.getName();
                methodName = "runGroovy";
                templateName = null;
                request.getParams().addParam(new Param<String, Object>("dynamicClassName",dynamicClassName));
                request.getParams().addParam(new Param<String, Object>("dynamicMethodName",dynamicMethodName));
            }
        }

        // remove the forward slash if there is any
        if (requestURI.length() > 1 && requestURI.endsWith("/")) {
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }

        // first check for the exact match
        // requestMethod    ->      GET
        if (className == null && executionMap.containsKey(requestMethod)) {

            // uriExecutionMap contains all the URI -> execution mapping for this request method
            Map<String, Param<String, String>> uriExecutionMap = executionMap.get(requestMethod);

            // requestURI           /welcome        or       /welcome/
            if (uriExecutionMap.containsKey(requestURI) || uriExecutionMap.containsKey(requestURI + "/")) {
                //   com.sample.app.action.MainAction, welcome
                Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                //   com.sample.app.action.MainAction
                className = executionParam.getKey();
                //   welcome
                methodName = executionParam.getValue();
                //   welcome
                templateName = executionParam.getValueSecondary();
            } else if (requestURI.startsWith("/*/")) {
                for (String uri : uriExecutionMap.keySet()) {
                    //  requestURI      /*/all/Product
                    //  uri             /*/all
                    if (requestURI.startsWith(uri)) {
                        //   com.sample.app.action.MainAction, welcome
                        Param<String, String> executionParam = uriExecutionMap.get(uri);
                        //   com.sample.app.action.MainAction
                        className = executionParam.getKey();
                        //   welcome
                        methodName = executionParam.getValue();
                        //   welcome
                        templateName = executionParam.getValueSecondary();
                        // found the class/method
                        break;
                    }
                }
            }
        }

        // if className not found, check the '*' method
        if (className == null && executionMap.containsKey("*")) {

            // uriExecutionMap contains all the URI -> execution mapping for '*' request method
            Map<String, Param<String, String>> uriExecutionMap = executionMap.get("*");

            if (uriExecutionMap.containsKey(requestURI) || uriExecutionMap.containsKey(requestURI + "/")) {
                //   com.sample.app.action.MainAction, welcome
                Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                //   com.sample.app.action.MainAction
                className = executionParam.getKey();
                //   welcome
                methodName = executionParam.getValue();
                //   welcome
                templateName = executionParam.getValueSecondary();
            } else if (requestURI.startsWith("/*/")) {
                for (String uri : uriExecutionMap.keySet()) {
                    //  requestURI      /*/all/Product
                    //  uri             /*/all
                    if (requestURI.startsWith(uri)) {
                        //   com.sample.app.action.MainAction, welcome
                        Param<String, String> executionParam = uriExecutionMap.get(uri);
                        //   com.sample.app.action.MainAction
                        className = executionParam.getKey();
                        //   welcome
                        methodName = executionParam.getValue();
                        //   welcome
                        templateName = executionParam.getValueSecondary();
                    }
                }
            }

            // if still className not found set it to default which is [GET] '/'
            if (className == null) {
                requestURI = "/";
                // get the default
                Param<String, String> executionParam = uriExecutionMap.get(requestURI);
                //   com.sample.app.action.MainAction
                className = executionParam.getKey();
                //   welcome
                methodName = executionParam.getValue();
                //   welcome
                templateName = executionParam.getValueSecondary();
            }
        }

        /*
        // find the user's group names
        String[] groupNamesCommaSeparated = null;
        if (request.getSession().containsKey(SessionKeys.GROUP_NAMES.getValue())) {
            groupNamesCommaSeparated = ((String) request.getSession().get(SessionKeys.GROUP_NAMES.getValue())).split(",");
        }

        // check the AuthenticationAuthorizationMap contains requestMethod
        if (authenticationAuthorizationMap.containsKey(requestMethod)
                || authenticationAuthorizationMap.containsKey("*")) {

            // authorization flag for user's group
            boolean userAuthorized = false;

            // for this http request method, like GET, POST or PUT
            Map<String, List<String>> uriGroupNames = authenticationAuthorizationMap.get(requestMethod);

            // may be request method defined as *
            if (uriGroupNames == null) {
                uriGroupNames = authenticationAuthorizationMap.get("*");
            }

            // check this requested uri has any authentication/authorization
            if (uriGroupNames.containsKey(requestURI)) {

                // the user does not have any groups but this uri needs at least one group
                // return STATUS_UNAUTHORIZED
                if (groupNamesCommaSeparated == null) {
                    return new Response(
                            new ParamMap<String, Param<String, Object>>(),
                            request.getSession(),
                            Status.STATUS_UNAUTHORIZED,
                            ""
                    );
                }

                // authorized groups for this uri
                List<String> authorizedGroups = uriGroupNames.get(requestURI);

                // user has at least one group
                // if the authorizedGroups contains (*) it means any authenticated client may request this uri
                if (authorizedGroups.contains("*")) {
                    userAuthorized = true;
                }

                // find the required group names
                else {
                    for (String userGroupName : groupNamesCommaSeparated) {
                        if (authorizedGroups.contains(userGroupName)) {
                            userAuthorized = true;
                            break;
                        }
                    }
                }


                // if the user is not authorized return STATUS_UNAUTHORIZED
                if (!userAuthorized) {
                    return new Response(
                            new ParamMap<String, Param<String, Object>>(),
                            request.getSession(),
                            Status.STATUS_UNAUTHORIZED,
                            ""
                    );
                }
            }


            // check for the [*] all http method request map
            if (!userAuthorized) {

                // [*] http request method
                uriGroupNames = authenticationAuthorizationMap.get("*");

                // check this requested uri has any authentication/authorization
                if (uriGroupNames.containsKey(requestURI)) {

                    // the user does not have any groups but this uri needs some
                    // return STATUS_UNAUTHORIZED
                    if (groupNamesCommaSeparated == null) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>(),
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }

                    // find the required group names
                    List<String> authorizedGroups = uriGroupNames.get(requestURI);
                    for (String userGroupName : groupNamesCommaSeparated) {
                        if (authorizedGroups.contains(userGroupName)) {
                            userAuthorized = true;
                            break;
                        }
                    }

                    // if the user is not authorized return STATUS_UNAUTHORIZED
                    if (!userAuthorized) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>(),
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }
                }
            }
        }
        */

        if (className != null) {
            // set Class and Method
            try {
                actionClass = Class.forName(className);
                method = actionClass.getMethod(methodName, Request.class);
                // add template to the request if template exists
                if (templateName != null) {
                    request.getParams().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TEMPLATE.getValue(), templateName));
                }

                /*
                // check the AuthenticationAuthorizationMap contains requestMethod
                // com.bugzter.app.action.*                    *               *
                // com.bugzter.app.action.SomeAction       login           admin,system
                Map<String, List<String>> methodGroupsMap = null;
                if (authenticationAuthorizationMap.containsKey(actionClass.getPackage().getName() + "." + actionClass.getName())) {
                    methodGroupsMap = authenticationAuthorizationMap.get(actionClass.getPackage().getName() + "." + actionClass.getName());
                } else if (authenticationAuthorizationMap.containsKey(actionClass.getPackage().getName() + ".*")) {
                    methodGroupsMap = authenticationAuthorizationMap.get(actionClass.getPackage().getName() + ".*");
                }

                // check this methodName has any authentication/authorization
                if (methodGroupsMap.containsKey(methodName) || methodGroupsMap.containsKey("*")) {

                    // authorization flag for user's group
                    boolean userAuthorized = false;

                    // the user does not have any groups but this method execution needs at least one group
                    // return STATUS_UNAUTHORIZED
                    if (groupNamesCommaSeparated == null) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>(),
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }

                    // authorized groups for this uri
                    List<String> authorizedGroups = methodGroupsMap.get(methodName);

                    // user has at least one group
                    // if the authorizedGroups contains (*) it means any authenticated client may request this uri
                    if (authorizedGroups.contains("*")) {
                        userAuthorized = true;
                    }

                    // find the required group names
                    else {
                        for (String userGroupName : groupNamesCommaSeparated) {
                            if (authorizedGroups.contains(userGroupName)) {
                                userAuthorized = true;
                                break;
                            }
                        }
                    }

                    // if the user is not authorized return STATUS_UNAUTHORIZED
                    if (!userAuthorized) {
                        return new Response(
                                new ParamMap<String, Param<String, Object>>(),
                                request.getSession(),
                                Status.STATUS_UNAUTHORIZED,
                                ""
                        );
                    }
                }
                */

                // check if the method is transactional
                if (method.getAnnotation(Transactional.class) != null) {
                    EM.getEntityManager().getTransaction().begin();
                }

                // get the response
                Response response = (Response) method.invoke(contextMap.getContext().getObject(actionClass), request);

                // check if the method is transactional
                if (method.getAnnotation(Transactional.class) != null) {
                    EM.getEntityManager().getTransaction().commit();
                }

                // return response
                return response;

            } catch (Exception e) {
                try {
                    EM.getEntityManager().clear();
                    if (EM.getEntityManager().getTransaction() != null && EM.getEntityManager().getTransaction().isActive()) {
                        EM.getEntityManager().getTransaction().rollback();
                    }
                } catch (Exception te) {
                    te.printStackTrace();
                }
                e.printStackTrace();
                new Response(
                        new ParamMap<String, Param<String, Object>>(),
                        request.getSession(),
                        Status.STATUS_SERVICE_UNAVAILABLE,
                        e.getMessage()
                );
            }
        }

        // something went wrong, return an error message
        return new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                Status.STATUS_SERVICE_UNAVAILABLE,
                ""
        );

    }

}
