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
import java.util.Date;
import java.util.List;


/**
 * acm
 */
@SuppressWarnings("unchecked")
public class GAction extends BaseAction {

    public static final String GROOVY_METHOD_NAME = "runGroovy";
    public static final String DYNAMIC_CLASS_NAME = "dynamicClassName";
    public static final String DYNAMIC_METHOD_NAME = "dynamicMethodName";

    String startParameterDev = null;
    String startParameterSource = null;

    public Response runGroovy(Request request) {
        try {
            // reset content and clear entity manager
            String content = null;

            // there should be at least class name
            if (request.get("dynamicClassName") == null) {
                return Error(request, "missing dynamicClassName key").toResponse();
            }

            //      check if this is development or production environment
            String environment = findEnvironment(request);
            //      find the source to execute
            String source = findSource(request);
            log("environment: " + environment + ", source: " + source);

            // if env is class then create an instance and return response
            if ("class".equalsIgnoreCase(source)) {
                try {
                    // try to find the class
                    Class clazz = Class.forName(request.get("dynamicClassName") + "Action");
                    // and return response
                    return createObjectReturnResponse(clazz, request);
                } catch (Exception e) {
                    return Error(request, "no action class exists, class name: " + request.get("dynamicClassName")).exception(e).toResponse();
                }
            }

            // do according to source
            if ("db".equalsIgnoreCase(source)) {
                // could not find the class, will try to find the content from database
                EM.getEntityManager().clear();
                ModelAction<GActionModel> modelAction = new ModelAction<GActionModel>(GActionModel.class);
                List<GActionModel> list = modelAction.findAll("name", request.get("dynamicClassName"));
                // found entry at db
                if (list != null && list.size() > 0) {
                    GActionModel gActionModel = list.get(0);
                    // got the content of the code
                    content = gActionModel.getContent();
                }
            } else if ("db-update-from-file".equalsIgnoreCase(source)) {
                // get content from file, may be this is a development source and db needs to be reloaded from file
                EM.getEntityManager().clear();
                content = readContentsOfFile(request);
                List<GActionModel> gActionModels = query(GActionModel.class).and("name", request.get("dynamicClassName")).findAll();
                if (gActionModels != null && gActionModels.size() > 0) {
                    GActionModel gActionModel = gActionModels.get(0);
                    gActionModel.setContent(content);
                    gActionModel = EM.merge(gActionModel);
                } else {
                    GActionModel gActionModel = new GActionModel();
                    gActionModel.setName(request.get("dynamicClassName"));
                    gActionModel.setContent(content);
                    gActionModel.setUpdateDate(new Date());
                    EM.persist(gActionModel);
                }
            } else if ("file".equalsIgnoreCase(source)) {
                // read the content from file, this may be the default for development source
                content = readContentsOfFile(request);
            }
            // now we have the content of the code so will execute it as if it is a groovy script and return response
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class clazz = loader.parseClass(content);
            // only for production, objects are singleton, others will reload for every request
            return createGObjectReturnResponse(clazz, request);
        } catch (Exception e) {
            e.printStackTrace();
            return Error(request, e.getMessage()).exception(e).toResponse();
        }
    }

    private String findSource(Request request) {
        // set the predefined value as 'class'
        String current = "class";
        String source = current;
        // check the java execution params for dev or prod values
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        // it will be run only for the first request
        if (startParameterSource == null) {
            // this condition will met only once per lifecycle
            startParameterSource = "";
            for (String arg : arguments) {
                if (arg != null && arg.startsWith("-Dtarget.source=")) {
                    startParameterSource = arg.substring("-Dtarget.source=".length());
                    break;
                }
            }
        }
        // first level is jvm parameter
        if (startParameterSource != null) {
            current = startParameterSource;
        }

        // second is the session
        if (request.getSession().containsKey("Dtarget.source") && request.getSession().get("Dtarget.source") != null) {
            current = String.valueOf(request.getSession().get("Dtarget.source"));
        }

        // third is the header
        if (request.getHeader("Dtarget.source") != null) {
            current = request.getHeader("Dtarget.source");
        }

        // last is the request
        if (request.get("Dtarget.source") != null) {
            current = request.get("Dtarget.source");
        }

        if ("db".equalsIgnoreCase(current)) {
            source = "db";
        } else if ("db-update-from-file".equalsIgnoreCase(current)) {
            source = "db-update-from-file";
        } else if ("file".equalsIgnoreCase(current)) {
            source = "file";
        } else if ("class".equalsIgnoreCase(current)) {
            source = "class";
        }

        // return current execution source
        return source;
    }

    private String findEnvironment(Request request) {
        String current = "production";
        String environment;
        // check the java execution params for dev or prod values
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        List<String> arguments = runtimeMxBean.getInputArguments();
        // it will be run only for the first request
        if (startParameterDev == null) {
            // this condition will met only once per lifecycle
            for (String arg : arguments) {
                if (arg != null && arg.startsWith("-Dtarget.env=")) {
                    startParameterDev = arg.substring("-Dtarget.env=".length());
                    break;
                }
            }
        }
        // first level is jvm parameter
        if (startParameterDev != null) {
            current = startParameterDev;
        }

        // second is the session
        if (request.getSession().containsKey("Dtarget.env") && request.getSession().get("Dtarget.env") != null) {
            current = String.valueOf(request.getSession().get("Dtarget.env"));
        }

        // third is the header
        if (request.getHeader("Dtarget.env") != null) {
            current = request.getHeader("Dtarget.env");
        }

        // last is the request
        if (request.get("Dtarget.env") != null) {
            current = request.get("Dtarget.env");
        }

        if ("development".equalsIgnoreCase(current) || "dev".equalsIgnoreCase(current)) {
            environment = "development";
        } else if ("test".equalsIgnoreCase(current)) {
            environment = "test";
        } else {
            environment = "production";
        }

        // return current execution environment
        return environment;
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

    private Response createObjectReturnResponse(Class clazz, Request request) {
        // try to create the object from class
        Object object = ContextMap.getInstance().getContext().getObject(clazz);
        // if it could not create the object, will return a proper error message
        if (object == null) {
            return Error(request, "No object created from this class: " + clazz.getName()).toResponse();
        } else {
            try {
                return runMethodReturnResponse(object, request);
            } catch (Exception e) {
                e.printStackTrace();
                return Error(request, "could not handle method").exception(e).toResponse();
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

    private Response createGObjectReturnResponse(Class clazz, Request request) {
        // try to create the object from class
        Object o = ContextMap.getInstance().getContext().getObject(clazz);
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
                    return Error(request, "could not run method neither dynamic nor compiled,").exception(em).toResponse();
                }
            }
        }
    }

}
