package com.fererlab.ndi;


import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * acm
 */
public class WireContext {

    private Map<String, String> beans = new HashMap<>();
    private Map<String, Object> singletons = new HashMap<>();

    public WireContext(Map<String, String> beans) {
        this.beans.putAll(beans);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> t) {
        return getObject(t, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T getObject(Class<T> t, Class value) {
        /*
        if value is not null, create and return object for value
        if t is an interface and has default, create and return object for default
        create and return object for t
         */
        if (value != null && value != Wire.class) {
            return (T) createObject(value);
        }
        if (t.isInterface()) {
            if (beans.containsKey(t.getName())) {
                try {
                    return (T) createObject(Class.forName(beans.get(t.getName())));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if (t.getAnnotation(Default.class) != null) {
                return (T) createObject(t.getAnnotation(Default.class).value());
            }
        }
        return createObject(t);
    }

    @SuppressWarnings("unchecked")
    private <T> T createObject(Class<T> t) {
        T instance = null;
        try {
            if (t.getAnnotation(Singleton.class) != null) {
                String key = t.getName();
                if (singletons.containsKey(key)) {
                    instance = (T) singletons.get(key);
                } else {
                    instance = t.newInstance();
                    fillWired(instance);
                    singletons.put(key, instance);
                }
            } else {
                instance = t.newInstance();
                fillWired(instance);
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return instance;
    }

    private <T> void fillWired(T instance) {
        for (Field field : instance.getClass().getDeclaredFields()) {
            Wire wired = field.getAnnotation(Wire.class);
            Object fieldValue = null;
            if (wired != null) {
                Class type = null;
                if (!Wire.class.equals(wired.type())) {
                    type = wired.type();
                } else if (!"".equals(wired.name())) {
                    try {
                        type = Class.forName(wired.name());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                } else if (!field.getType().isInterface()) {
                    type = field.getType();
                }
                if (type != null) {
                    fieldValue = getObject(field.getType(), type);
                }
            } else if (beans.containsKey(field.getType().getTypeName())) {
                try {
                    String defaultClassName = beans.get(field.getType().getTypeName());
                    Class defaultClass = Class.forName(defaultClassName);
                    fieldValue = getObject(field.getType(), defaultClass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (fieldValue != null) {
                try {
                    field.setAccessible(true);
                    field.set(instance, fieldValue);
                } catch (Exception e) {
                    try {
                        String methodName = "set" + StringUtils.capitalize(field.getName());
                        instance.getClass().getMethod(methodName, field.getType()).invoke(instance, fieldValue);
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                }
            }

        }
    }

}
