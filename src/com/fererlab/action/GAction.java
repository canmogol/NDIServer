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
import java.util.List;

/**
 * acm
 */
public class GAction extends BaseAction {

    @SuppressWarnings("unchecked")
    public Response runGroovy(Request request) {
        try {
            if (request.get("dynamicClassName") == null) {
                return Error(request, "missing dynamicClassName key").toResponse();
            }

            boolean development = false;
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            List<String> arguments = runtimeMxBean.getInputArguments();
            String env = "";
            for (String arg : arguments) {
                if (arg != null && arg.startsWith("-Dtarget.env=")) {
                    env = arg.substring("-Dtarget.env=".length());
                    break;
                }
            }
            if ("dev".equalsIgnoreCase(env) || (request.get("_dev") != null && "true".equalsIgnoreCase(request.get("_dev")))) {
                development = true;
            }

            String content;
            EM.getEntityManager().clear();
            ModelAction<GActionModel> modelAction = new ModelAction<GActionModel>(GActionModel.class);
            List<GActionModel> list = modelAction.findAll("name", request.get("dynamicClassName"));
            if (!development && list != null && list.size() > 0) {
                GActionModel gActionModel = list.get(0);
                content = gActionModel.getContent();
            } else {
                String classesPath = this.getClass().getResource("").toString().substring(0, this.getClass().getResource("").toString().length() - this.getClass().getPackage().getName().length() - 1);
                if (classesPath.startsWith("file:")) {
                    classesPath = classesPath.substring("file:".length());
                }
                File file = new File(classesPath + request.get("dynamicClassName") + "Action.groovy");
                if (!file.exists()) {
                    file = new File(classesPath + request.get("dynamicClassName") + "Action.java");
                    if (!file.exists()) {
                        throw new Exception("there is no file found with name: " + request.get("dynamicClassName") + "Action(.groovy/.java)");
                    }
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
                content = fileData.toString();

                if (!development) {
                    ParamMap<String, Param<String, Object>> keyValuePairs = new ParamMap<String, Param<String, Object>>();
                    keyValuePairs.addParam(new Param<String, Object>("name", request.get("dynamicClassName")));
                    keyValuePairs.addParam(new Param<String, Object>("content", content));
                    modelAction.create(keyValuePairs);
                }
            }

            if (content != null) {
                ClassLoader parent = getClass().getClassLoader();
                GroovyClassLoader loader = new GroovyClassLoader(parent);
                Class clazz = loader.parseClass(content);
                Object o = ContextMap.getInstance().getContext().getObject(clazz);
                GroovyObject gObject = (GroovyObject) o;
                if ("run".equals(request.get("dynamicMethodName"))) {
                    Object[] args = {};
                    Object response = gObject.invokeMethod(request.get("dynamicMethodName"), args);
                    return Ok(request).add("data", response).toResponse();
                } else {
                    Object[] args = {this, request};
                    return (Response) gObject.invokeMethod(request.get("dynamicMethodName"), args);
                }
            } else {
                return Error(request, "content is null, no class/content found").toResponse();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Error(request, e.getMessage()).toResponse();
        }
    }

}
