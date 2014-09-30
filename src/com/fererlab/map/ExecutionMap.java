package com.fererlab.map;

import com.fererlab.dto.Param;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 * acm | 3/3/13
 */
public class ExecutionMap extends TreeMap<String, Map<String, Param<String, String>>> {

    private static ExecutionMap instance;

    private ExecutionMap() {
    }

    public static ExecutionMap getInstance() {
        if (instance == null) {
            instance = new ExecutionMap();
        }
        return instance;
    }

    public void readUriExecutionMap(URL file) {
         /*
        request method      ->    uri                ->   className, method
        GET                 ->    /welcome           ->   com.sample.app.action.MainAction, welcome       welcomeTemplate
        POST                ->    /product/details   ->   com.sample.app.action.ProductCRUDAction, details
         */

        // comparator for the keys
        Comparator<String> stringComparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                if (o1.length() < o2.length()) {
                    return 1;
                } else if (o1.length() > o2.length()) {
                    return -1;
                }
                return o1.compareTo(o2);
            }
        };

        // read ExecutionMap.properties
        if (file == null) {
            file = getClass().getClassLoader().getResource("ExecutionMap.properties");
        }
        if (file != null) {
            try {
                String currentLine;
                FileReader fileReader = new FileReader(file.getFile());
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                while ((currentLine = bufferedReader.readLine()) != null) {
                    /*
                    #URI         HTTP METHOD(s)          Handle Class                       Handle Method   XSL-Template
                    /           [*]                     com.sample.app.action.MainAction   main
                    /welcome    [GET,DELETE]            com.sample.app.action.MainAction   welcome         welcome
                     */
                    if (!currentLine.startsWith("#") && currentLine.lastIndexOf('[') != -1) {
                        //      /welcome
                        String uri = currentLine.substring(0, currentLine.lastIndexOf('[')).trim();
                        //      GET,DELETE]            com.sample.app.action.MainAction   welcome         welcome
                        String startingFromHttpMethods = currentLine.substring(currentLine.lastIndexOf('[') + 1).trim();
                        if (startingFromHttpMethods.lastIndexOf(']') != -1) {
                            //      GET,DELETE
                            String httpMethodNames = startingFromHttpMethods.substring(0, startingFromHttpMethods.lastIndexOf(']')).trim();
                            //      {GET,   DELETE}
                            String[] httpMethods = httpMethodNames.split(",");

                            //      com.sample.app.action.MainAction   welcome         welcome
                            String startingFromClassName = startingFromHttpMethods.substring(startingFromHttpMethods.lastIndexOf(']') + 1).trim();
                            //      com.sample.app.action.MainAction   welcome         welcome
                            //      OR
                            //      com.sample.app.action.MainAction   welcome
                            String[] classNameMethodNameMaybeTemplateName = startingFromClassName.split(" ");
                            String className = null;
                            String methodName = null;
                            String templateName = null;
                            for (String name : classNameMethodNameMaybeTemplateName) {
                                if (name != null && !name.trim().isEmpty()) {
                                    if (className == null) {
                                        className = name.trim();
                                    } else if (methodName == null) {
                                        methodName = name.trim();
                                    } else {
                                        templateName = name.trim();
                                        break;
                                    }
                                }
                            }

                            // at least there should be class and method names
                            if (className != null && methodName != null) {
                                // requestMethods are like GET, POST, DELETE, PUT etc.
                                for (String requestMethod : httpMethods) {
                                    // trim the request method string, there may be some empty strings coming from properties entry
                                    requestMethod = requestMethod.trim();
                                    // if there is not entry until now, put an empty HashMap
                                    if (!this.containsKey(requestMethod)) {
                                        this.put(requestMethod, new TreeMap<String, Param<String, String>>(stringComparator));
                                    }
                                    // add this (uri -> className, methodName) to this request method's map
                                    this.get(requestMethod).put(uri, new Param<String, String>(className, methodName, templateName));
                                }
                            }
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public Map<String, Param<String, String>> get(Object key) {
        Map<String, Param<String, String>> map = super.get(key);
        if (map == null) {
            return new TreeMap<String, Param<String, String>>();
        }
        return map;
    }
}
