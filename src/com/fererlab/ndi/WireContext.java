package com.fererlab.ndi;

import com.sun.xml.internal.ws.util.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * acm
 */
public class WireContext {

    private Map<String, String> beans = new HashMap<String, String>();
    private Map<String, Object> context = new HashMap<String, Object>();

    public WireContext(Map<String, String> beans) {
        this.beans.putAll(beans);
    }

    @SuppressWarnings("unchecked")
    public <T extends Object> T getObject(Class<T> t) {
        try {
            String key = t.getPackage() + "." + t.getName();
            if (context.containsKey(key)) {
                return (T) context.get(key);
            } else if (!t.isInterface()) {
                T instance = t.newInstance();
                context.put(key, instance);
                fillWired(instance);
                return instance;
            } else {
                // <T> t is an interface, implementation should be set at the properties file
                if (beans.containsKey(t.getName())) {
                    Class<T> implementation = (Class<T>) Class.forName(beans.get(t.getName()));
                    return getObject(implementation);
                } else {
                    Default defaultAnnotation = t.getAnnotation(Default.class);
                    if (defaultAnnotation != null) {
                        Class<T> defaultImplementation = (Class<T>) defaultAnnotation.value();
                        return getObject(defaultImplementation);
                    } else {
                        // could not find the implementation, it will be NULL
                    }
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private <T extends Object> void fillWired(T instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            Wire wired = field.getAnnotation(Wire.class);
            if (wired != null) {
                try {
                    field.setAccessible(true);
                    field.set(instance, getObject(field.getType()));
                } catch (Exception e) {
                    try {
                        String methodName = "set" + StringUtils.capitalize(field.getName());
                        instance.getClass().getMethod(methodName, field.getType()).invoke(instance, getObject(field.getType()));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

}
