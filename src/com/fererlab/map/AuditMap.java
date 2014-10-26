package com.fererlab.map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AuditMap extends HashMap<String, List<String>> {

    private static AuditMap instance;

    private AuditMap() {
    }

    public static AuditMap getInstance() {
        if (instance == null) {
            instance = new AuditMap();
        }
        return instance;
    }

    public void readAuditMap(String directoryName) {
        /*
        # Audit: all requests and filtered requests
        # /request/path     [HTTP,TYPE]
        # all requests for all HTTP request types for all user groups
        #/user/login         [GET,POST]
        #/admin              [*]

        # package.name.and.class.name               methodName
        # all classes under package, all methods for all all user groups
        #com.app.action.*                    *
        #com.app.action.GenericAction        *
        */
        URL url = getClass().getClassLoader().getResource(directoryName);
        if (url != null) {
            try {
                String currentLine;
                File file = new File(url.getPath() + "/AuditMap.properties");
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while ((currentLine = bufferedReader.readLine()) != null) {
                    if (currentLine.trim().isEmpty() || currentLine.startsWith("#")) {
                        continue;
                    }
                    /*
                    /admin         [GET,POST]
                     */
                    if (currentLine.startsWith("/")) {
                        //      /admin
                        String uri = currentLine.substring(0, currentLine.lastIndexOf('[')).trim();
                        //      POST,PUT,DELETE]
                        String startingFromHttpMethods = currentLine.substring(currentLine.lastIndexOf('[') + 1).trim();
                        if (startingFromHttpMethods.lastIndexOf(']') != -1) {
                            //      POST,PUT,DELETE
                            String httpMethodNames = startingFromHttpMethods.substring(0, startingFromHttpMethods.lastIndexOf(']')).trim();
                            //      {POST,   PUT,   DELETE}
                            String[] httpMethods = httpMethodNames.split(",");

                            // requestMethods are like GET, POST, DELETE, PUT etc.
                            for (String requestMethod : httpMethods) {
                                // trim the request method string, there may be some empty strings coming from properties entry
                                requestMethod = requestMethod.trim();
                                // if there is not entry until now, put an empty HashMap
                                if (!this.containsKey(requestMethod)) {
                                    this.put(requestMethod, new ArrayList<String>());
                                }
                                //       "POST" -> ["/admin", ...]
                                this.get(requestMethod).add(uri);
                            }
                        }
                    }
                    /*
                    com.app.action.SomeAction       login
                     */
                    else {
                        //      "com.app.action.SomeAction"
                        String packageClassName = currentLine.split(" ")[0].trim();
                        //      "login           admin,system"
                        String method = currentLine.substring(packageClassName.length()).trim();
                        //      "login"
                        String methodName = method.split(" ")[0].trim();
                        // if there is not entry until now, put an empty HashMap
                        if (!this.containsKey(packageClassName)) {
                            this.put(packageClassName, new ArrayList<String>());
                        }
                        // "com.app.action.SomeAction" -> ["login", ..]
                        this.get(packageClassName).add(methodName);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
