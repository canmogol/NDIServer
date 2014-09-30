package com.fererlab.map;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acm | 3/3/13
 */
public class AuthenticationAuthorizationMap extends HashMap<String, Map<String, List<String>>> {

    private static AuthenticationAuthorizationMap instance;

    private AuthenticationAuthorizationMap() {
    }

    public static AuthenticationAuthorizationMap getInstance() {
        if (instance == null) {
            instance = new AuthenticationAuthorizationMap();
        }
        return instance;
    }

    public void readAuthenticationAuthorizationMap(URL file) {
        /*
        # URI               HTTP METHOD(s)              User
        /welcome            [*]                         *
        /admin              [POST,PUT,DELETE]           admin,system,root

        # PACKAGE.CLASS                             METHOD          User
        com.bugzter.app.action.SomeAction       login           admin,system
        com.bugzter.app.action.GenericAction        *               system
        */

        // read ExecutionMap.properties
        if (file == null) {
            file = getClass().getClassLoader().getResource("AuthenticationAuthorizationMap.properties");
        }
        if (file != null) {
            try {
                String currentLine;
                FileReader fileReader = new FileReader(file.getFile());
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while ((currentLine = bufferedReader.readLine()) != null) {
                    if (currentLine.trim().isEmpty() || currentLine.startsWith("#")){
                        continue;
                    }
                    /*
                    /admin              [POST,PUT,DELETE]           admin,system,root
                     */
                    if (currentLine.startsWith("/")) {
                        //      /admin
                        String uri = currentLine.substring(0, currentLine.lastIndexOf('[')).trim();
                        //      POST,PUT,DELETE]            admin,system,root
                        String startingFromHttpMethods = currentLine.substring(currentLine.lastIndexOf('[') + 1).trim();
                        if (startingFromHttpMethods.lastIndexOf(']') != -1) {
                            //      POST,PUT,DELETE
                            String httpMethodNames = startingFromHttpMethods.substring(0, startingFromHttpMethods.lastIndexOf(']')).trim();
                            //      {POST,   PUT,   DELETE}
                            String[] httpMethods = httpMethodNames.split(",");

                            //      admin,system,root
                            String startingFromGroupNames = startingFromHttpMethods.substring(startingFromHttpMethods.lastIndexOf(']') + 1).trim();
                            String[] groupsNames = startingFromGroupNames.split(",");

                            // list of group names
                            List<String> groupNamesList = new ArrayList<String>();

                            for (String groupName : groupsNames) {
                                if (groupName != null && !groupName.trim().isEmpty()) {
                                    groupNamesList.add(groupName.trim());
                                }
                            }

                            // requestMethods are like GET, POST, DELETE, PUT etc.
                            for (String requestMethod : httpMethods) {
                                // trim the request method string, there may be some empty strings coming from properties entry
                                requestMethod = requestMethod.trim();
                                // if there is not entry until now, put an empty HashMap
                                if (!this.containsKey(requestMethod)) {
                                    this.put(requestMethod, new HashMap<String, List<String>>());
                                }
                                // add this (uri -> className, methodName) to this request method's map
                                //       "POST" -> {"/admin" => [admin,system,root]}
                                this.get(requestMethod).put(uri, groupNamesList);
                            }
                        }
                    }
                    /*
                    com.bugzter.app.action.SomeAction       login           admin,system
                     */
                    else {

                        //      "com.bugzter.app.action.SomeAction"
                        String packageClassName = currentLine.split(" ")[0].trim();
                        //      "login           admin,system"
                        String methodAndGroups = currentLine.substring(packageClassName.length()).trim();
                        //      "login"
                        String methodName = methodAndGroups.split(" ")[0].trim();
                        //      "admin,system"
                        String groups = methodAndGroups.substring(methodName.length()).trim();
                        String[] groupsNames = groups.split(",");

                        // list of group names
                        List<String> groupNamesList = new ArrayList<String>();
                        for (String groupName : groupsNames) {
                            if (groupName != null && !groupName.trim().isEmpty()) {
                                groupNamesList.add(groupName.trim());
                            }
                        }
                        // "com.bugzter.app.action.SomeAction" -> {"login" => [admin,system]}
                        this.get(packageClassName).put(methodName, groupNamesList);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
