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
    private Map<String, Object> singletons = new HashMap<String, Object>();

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
            String key = t.getPackage() + "." + t.getName();
            if (t.getAnnotation(Singleton.class) != null) {
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
            if (wired != null) {
                try {
                    field.setAccessible(true);
                    field.set(instance, getObject(field.getType(), wired.value()));
                } catch (Exception e) {
                    try {
                        String methodName = "set" + StringUtils.capitalize(field.getName());
                        instance.getClass().getMethod(methodName, field.getType()).invoke(instance, getObject(field.getType(), wired.value()));
                    } catch (Exception ex) {
                        e.printStackTrace();
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

}
