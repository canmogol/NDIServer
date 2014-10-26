package com.fererlab.action;

import com.fererlab.cache.Cache;
import com.fererlab.db.EM;
import com.fererlab.db.Transactional;
import com.fererlab.dto.*;
import com.fererlab.map.*;
import com.fererlab.session.Session;
import com.fererlab.session.SessionUser;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * acm | 1/16/13
 */
public class ActionHandler {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private ExecutionMap executionMap = ExecutionMap.getInstance();
    private AuthenticationAuthorizationMap authenticationAuthorizationMap = AuthenticationAuthorizationMap.getInstance();
    private MimeTypeMap mimeTypeMap = MimeTypeMap.getInstance();
    private CacheMap cacheMap = CacheMap.getInstance();
    private ContextMap contextMap = ContextMap.getInstance();
    private AuditMap auditMap = AuditMap.getInstance();

    private Map<String, List<String>> uriGroupNames = new HashMap<String, List<String>>();
    private AuditLogModel auditLog;

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
                            Status.STATUS_NOT_FOUND
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
                methodName = GAction.GROOVY_METHOD_NAME;
                templateName = null;
                request.getParams().addParam(new Param<String, Object>(GAction.DYNAMIC_CLASS_NAME, dynamicClassName));
                request.getParams().addParam(new Param<String, Object>(GAction.DYNAMIC_METHOD_NAME, dynamicMethodName));
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

        // authorization flag for user's group
        boolean userAuthorized = false;

        // find the user's group names
        Session session = request.getSession();
        SessionUser user = session.getUser();
        // user at least should have GUEST group
        if (user.getGroups().isEmpty()) {
            String everybody = "*";
            user.getGroups().add(everybody);
        }

        // check the AuthenticationAuthorizationMap contains requestMethod
        if (authenticationAuthorizationMap.containsKey(requestMethod)
                || authenticationAuthorizationMap.containsKey("*")) {

            if (uriGroupNames.isEmpty()) {
                // for this http request method, like GET, POST or PUT
                if (authenticationAuthorizationMap.get(requestMethod) != null) {
                    uriGroupNames.putAll(authenticationAuthorizationMap.get(requestMethod));
                }
                if (authenticationAuthorizationMap.get("*") != null) {
                    uriGroupNames.putAll(authenticationAuthorizationMap.get("*"));
                }
            }

            // check if the uri is wildcard
            if (!uriGroupNames.keySet().contains(requestURI)) {
                boolean uriFound = false;
                Set<String> uriSet = uriGroupNames.keySet();
                LinkedList<String> uriList = new LinkedList<String>(uriSet);
                Collections.sort(uriList);
                Iterator<String> iterator = uriList.descendingIterator();
                while (iterator.hasNext()) {
                    String uri = iterator.next();
                    String searchUri = uri;
                    if (searchUri.endsWith("/**")) {
                        searchUri = searchUri.substring(0, searchUri.length() - 3);
                    }
                    if (requestURI.startsWith(searchUri) && uri.length() > 3) {
                        String restOfUri = uri.substring(uri.length() - 3);
                        if (restOfUri.equals("/**")) {
                            uriGroupNames.put(requestURI, uriGroupNames.get(uri));
                            uriFound = true;
                            break;
                        }
                    }
                }
                if (!uriFound) {
                    uriGroupNames.put(requestURI, null);
                }
            }

            // check this requested uri has any authentication/authorization
            if (uriGroupNames.get(requestURI) != null) {

                // user has at least one group
                List<String> authorizedGroups = uriGroupNames.get(requestURI);

                // if the authorizedGroups contains (*) it means any authenticated client may request this uri
                if (authorizedGroups.contains("*")) {
                    userAuthorized = true;
                }

                // find the required group names
                else {
                    for (String userGroupName : user.getGroups()) {
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
                            Status.STATUS_UNAUTHORIZED
                    );
                }
            }
        }

        // if the user is not authorized return STATUS_UNAUTHORIZED
        if (!userAuthorized) {
            return new Response(
                    new ParamMap<String, Param<String, Object>>(),
                    request.getSession(),
                    Status.STATUS_UNAUTHORIZED
            );
        } else if (className != null) {
            // set Class and Method
            try {
                actionClass = Class.forName(className);
                method = actionClass.getMethod(methodName, Request.class);
                // add template to the request if template exists
                if (templateName != null) {
                    request.getParams().addParam(new Param<String, Object>(RequestKeys.RESPONSE_TEMPLATE.getValue(), templateName));
                }

                // check the AuthenticationAuthorizationMap contains requestMethod
                // com.app.action.*                    *               *
                // com.app.action.SomeAction       login           admin,system
                Map<String, List<String>> methodGroupsMap = null;
                if (authenticationAuthorizationMap.containsKey(actionClass.getName())) {
                    methodGroupsMap = authenticationAuthorizationMap.get(actionClass.getName());
                } else if (authenticationAuthorizationMap.containsKey(actionClass.getPackage().getName() + ".*")) {
                    methodGroupsMap = authenticationAuthorizationMap.get(actionClass.getPackage().getName() + ".*");
                }

                // check this methodName has any authentication/authorization
                if (methodGroupsMap != null &&
                        (methodGroupsMap.containsKey(methodName) || methodGroupsMap.containsKey("*"))) {

                    // authorized groups for this uri
                    List<String> authorizedGroups = methodGroupsMap.get(methodName);

                    // user has at least one group
                    // if the authorizedGroups contains (*) it means any authenticated client may request this uri
                    if (authorizedGroups.contains("*")) {
                        userAuthorized = true;
                    }

                    // find the required group names
                    else {
                        userAuthorized = false;
                        for (String userGroupName : user.getGroups()) {
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
                                Status.STATUS_UNAUTHORIZED
                        );
                    }
                }

                // audit the log with request
                audit(request, null, requestURI, actionClass, method, requestMethod);

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

                // audit the log with response
                audit(request, response, requestURI, actionClass, method, requestMethod);

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
                        Status.STATUS_SERVICE_UNAVAILABLE
                );
            }
        }

        // something went wrong, return an error message
        return new Response(
                new ParamMap<String, Param<String, Object>>(),
                request.getSession(),
                Status.STATUS_SERVICE_UNAVAILABLE
        );

    }

    private void audit(Request request, Response response, String requestURI, Class<?> actionClass, Method method, String requestMethod) {
        // project.properties should have an auditing entry with "logger,db,none" option
        Boolean auditing = false;
        String auditClass = actionClass.getName();
        String auditMethod = method.getName();
        //      /-/Department/listModelAll
        if (GAction.class.getName().equals(auditClass)
                && GAction.GROOVY_METHOD_NAME.equals(auditMethod)) {
            auditClass = String.valueOf(request.getParam(GAction.DYNAMIC_CLASS_NAME).getValue());
            auditMethod = String.valueOf(request.getParam(GAction.DYNAMIC_METHOD_NAME).getValue());
        }
        //      #/user/login         [GET,POST]
        if (auditMap.containsKey("*") || auditMap.containsKey(requestMethod)) {
            List<String> uris = new ArrayList<String>();
            if (auditMap.get("*") != null) {
                uris.addAll(auditMap.get("*"));
            }
            if (auditMap.get(requestMethod) != null) {
                uris.addAll(auditMap.get(requestMethod));
            }
            if (uris.contains(requestURI) || auditMap.containsKey(auditClass)) {
                auditing = true;
            }
        }
        // auditing is enabled for this URI
        if (auditing) {
            if ("logger".equals(property("auditing"))) {
                if (response == null) {
                    auditLog = new AuditLogModel();
                    logger.log(Level.INFO, "AUDIT: " + auditLog.getCreateDate() + ", class: " + auditClass + ", method: " + auditMethod + ", user: " + request.getSession().getUser() + ", request: " + request);
                } else {
                    logger.log(Level.INFO, "AUDIT: " + auditLog.getCreateDate() + ", class: " + auditClass + ", method: " + auditMethod + ", user: " + request.getSession().getUser() + ", response: " + response);
                }
            } else if ("db".equals(property("auditing"))) {
                if (response == null) {
                    ModelAction<AuditLogModel> auditLogModelAction = new ModelAction<AuditLogModel>(AuditLogModel.class);
                    auditLog = new AuditLogModel();
                    auditLog.setClassName(auditClass);
                    auditLog.setMethodName(auditMethod);
                    auditLog.setUsername(request.getSession().getUser().getUsername());
                    auditLog.setRemoteIp(request.getHeader(RequestKeys.REMOTE_IP.getValue()));
                    auditLog.setGroups(request.getSession().getUser().getGroups().toString());
                    auditLog.setRequest(request.toString());
                    auditLog.setRequestUri(request.get(RequestKeys.URI.getValue()));
                    EM.persist(auditLog);
                    logger.log(Level.INFO, "AUDIT: done request, " + auditLog.getId());
                } else {
                    auditLog.setResponse(response.toString());
                    auditLog.setResponseContent(new String(response.getContent()));
                    auditLog.setUpdatedDate(new Date());
                    auditLog = EM.merge(auditLog);
                    logger.log(Level.INFO, "AUDIT: done response, " + auditLog.getId());
                    auditLog = null;
                }
            }
        }
    }

    public ExecutionMap getExecutionMap() {
        return executionMap;
    }

    public AuthenticationAuthorizationMap getAuthenticationAuthorizationMap() {
        return authenticationAuthorizationMap;
    }

    public MimeTypeMap getMimeTypeMap() {
        return mimeTypeMap;
    }

    public CacheMap getCacheMap() {
        return cacheMap;
    }

    public ContextMap getContextMap() {
        return contextMap;
    }

    public AuditMap getAuditMap() {
        return auditMap;
    }

    public String property(String key) {
        return ProjectProperties.getInstance().get(key);
    }

}
