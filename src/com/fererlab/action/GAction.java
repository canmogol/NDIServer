package com.fererlab.action;

import com.fererlab.db.EM;
import com.fererlab.dto.*;
import com.fererlab.map.ContextMap;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.util.List;

/**
 * acm
 */
@SuppressWarnings("unchecked")
public class GAction extends BaseAction {

    String startParameterDev = null;
    boolean productionOrDevelopment = false;

    public Response runGroovy(Request request) {
        try {
            // reset content and clear entity manager
            String content = null;
            EM.getEntityManager().clear();

            // there should be at least class name
            if (request.get("dynamicClassName") == null) {
                return Error(request, "missing dynamicClassName key").toResponse();
            }

            // check the java execution params for dev or prod values
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            List<String> arguments = runtimeMxBean.getInputArguments();
            // it will be run only for the first request
            if (startParameterDev == null) {
                // this condition will met only once per lifecycle
                startParameterDev = "";
                for (String arg : arguments) {
                    if (arg != null && arg.startsWith("-Dtarget.env=")) {
                        startParameterDev = arg.substring("-Dtarget.env=".length());
                        break;
                    }
                }
                if (startParameterDev == null || "prod".equalsIgnoreCase(startParameterDev)) {
                    // either there is no parameter for environment or it is prod
                    productionOrDevelopment = true;
                } else if ("dev".equalsIgnoreCase(startParameterDev)) {
                    // parameter for environment exist and it is "dev"
                    productionOrDevelopment = false;
                }
            }

            // if the URL param _dev exists, it overrides the java execution params
            if ((request.get("_dev") != null && "true".equalsIgnoreCase(request.get("_dev")))) {
                productionOrDevelopment = false;
            }

            // check if this is a production environment
            if (productionOrDevelopment) {
                try {
                    // try to find the class
                    Class clazz = Class.forName(request.get("dynamicClassName") + "Action");
                    // and return response
                    return createObjectReturnResponse(clazz, request, true);
                } catch (Exception e) {
                    // could not find the class, will try to find the content from database
                    ModelAction<GActionModel> modelAction = new ModelAction<GActionModel>(GActionModel.class);
                    List<GActionModel> list = modelAction.findAll("name", request.get("dynamicClassName"));
                    // found entry at db
                    if (list != null && list.size() > 0) {
                        GActionModel gActionModel = list.get(0);
                        // got the content of the code
                        content = gActionModel.getContent();
                    }
                }
            }
            // it is either development environment which means it should read the content of the class from file
            // or it is production environment but could not find the compiled class or no entry at DB
            if (content == null) {
                // we will check if this class content is in the file
                content = readContentsOfFile(request);
                // now content should not be null, if it is null it means we don't have this action
                if (content == null) {
                    // this is really bad, because it means we don't have this action
                    return Error(request, "no action class or content at DB or content file exists, class name: " + request.get("dynamicClassName")).toResponse();
                } else if (productionOrDevelopment) {
                    // if this is production and it reached here,
                    // it means in production if found the content from file but content is not in DB,
                    // so it should put the content to DB,
                    // next request will not reach here, it will found the content at DB request
                    ParamMap<String, Param<String, Object>> keyValuePairs = new ParamMap<String, Param<String, Object>>();
                    keyValuePairs.addParam(new Param<String, Object>("name", request.get("dynamicClassName")));
                    keyValuePairs.addParam(new Param<String, Object>("content", content));
                    ModelAction<GActionModel> modelAction = new ModelAction<GActionModel>(GActionModel.class);
                    GActionModel gActionModel = modelAction.create(keyValuePairs);
                }
            }
            // regardless of is this a development or production environment,
            // now we have the content of the code so will execute it as if it is a groovy script and return response
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class clazz = loader.parseClass(content);
            return createGObjectReturnResponse(clazz, request, productionOrDevelopment);
        } catch (Exception e) {
            e.printStackTrace();
            return Error(request, e.getMessage()).toResponse();
        }
    }

    private String readContentsOfFile(Request request) throws Exception {
        String classesPath = this.getClass().getResource("").toString().substring(0, this.getClass().getResource("").toString().length() - this.getClass().getPackage().getName().length() - 1);
        if (classesPath.startsWith("file:")) {
            classesPath = classesPath.substring("file:".length());
        }
        File file = null;
        for (String extension : new String[]{"java", "groovy", "djava", "dj"}) {
            file = new File(classesPath + request.get("dynamicClassName") + "." + extension);
            if (file.exists()) {
                break;
            } else {
                file = new File(classesPath + request.get("dynamicClassName") + "Action." + extension);
                if (file.exists()) {
                    break;
                }
            }
        }
        if (file == null || !file.exists()) {
            throw new Exception("there is no file found with name: " + request.get("dynamicClassName"));
        }

        StringBuilder fileData = new StringBuilder();
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        char[] buf = new char[4096];
        int numRead;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[4096];
        }
        reader.close();
        return fileData.toString();
    }

    private Response createObjectReturnResponse(Class clazz, Request request, boolean singleton) {
        // try to create the object from class
        Object object = ContextMap.getInstance().getContext().getObject(clazz, singleton);
        // if it could not create the object, will return a proper error message
        if (object == null) {
            return Error(request, "No object created from this class: " + clazz.getName()).toResponse();
        } else {
            try {
                return runMethodReturnResponse(object, request);
            } catch (Exception e) {
                e.printStackTrace();
                return Error(request, "could not handle method, e: " + e.getMessage()).toResponse();
            }
        }
    }

    private Response runMethodReturnResponse(Object object, Request request) throws Exception {
        Class[] methodArguments = new Class[1];
        methodArguments[0] = Request.class;
        Method method = object.getClass().getDeclaredMethod(request.get("dynamicMethodName"), methodArguments);
        Object[] args = {request};
        Object responseObject = method.invoke(object, args);
        return (Response) responseObject;
    }

    private Response createGObjectReturnResponse(Class clazz, Request request, boolean singleton) {
        // try to create the object from class
        Object o = ContextMap.getInstance().getContext().getObject(clazz, singleton);
        // if it could not create the object, will return a proper error message
        if (o == null) {
            return Error(request, "No object created from this class: " + clazz.getName()).toResponse();
        } else {
            GroovyObject gObject = (GroovyObject) o;
            try {
                if ("run".equals(request.get("dynamicMethodName"))) {
                    Object[] args = {};
                    Object response = gObject.invokeMethod(request.get("dynamicMethodName"), args);
                    return Ok(request).add("data", response).toResponse();
                } else {
                    Object[] args = {this, request};
                    return (Response) gObject.invokeMethod(request.get("dynamicMethodName"), args);
                }
            } catch (Exception e) {
                try {
                    return runMethodReturnResponse(gObject, request);
                } catch (Exception em) {
                    e.printStackTrace();
                    em.printStackTrace();
                    return Error(request, "could not run method neither dynamic nor compiled, e: " + em.getMessage()).toResponse();
                }
            }
        }
    }

}
