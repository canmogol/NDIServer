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
        // check "ContextMap.properties" file for bean definitions
        this.beans.putAll(beans);
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Class<T> t) {
        return getObject(t, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T getObject(Class<T> t, Class value) {
        /*
        if value not null, create and return object for value
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
            // singleton annotated classes should not be created twice in same context
            if (t.getAnnotation(Singleton.class) != null) {
                String key = t.getName();
                if (singletons.containsKey(key)) {
                    instance = (T) singletons.get(key);
                } else {
                    instance = t.newInstance();
                    fillWired(instance);
                    singletons.put(key, instance);
                }
            }
            // other than singleton annotated classes, they should be created for every call
            else {
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
            // value to be set for this field
            Object fieldValue = null;
            // check the field has annotation of wire
            Wire wired = field.getAnnotation(Wire.class);
            if (wired != null) {
                Class type = null;
                // wire annotation with class type
                if (!Wire.class.equals(wired.type())) {
                    type = wired.type();
                }
                // wire annotation with package.class name
                else if (!"".equals(wired.name())) {
                    try {
                        type = Class.forName(wired.name());
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                // just wire annotation for concrete type
                else if (!field.getType().isInterface()) {
                    type = field.getType();
                }
                // this is obviously an interface, search classpath for implementations
                else {
                    // Here we can search all packages/classes that implements this interface
                    // which contradicts everything about this project, since this is implemented
                    // by all the DI containers like spring and CDI(i.e. weld)
                    // traversing all the classes is beyond the scope of this project, if you need
                    // something that reads and knows all the classes in the classpath,
                    // use a dependency injection container.
                    // This is just a wiring tool
                }
                // if there is no type found, do not set the type
                if (type != null) {
                    fieldValue = getObject(field.getType(), type);
                }
            }
            // if field value is null and beans contains this fields package.class as key
            if (fieldValue == null && beans.containsKey(field.getType().getTypeName())) {
                try {
                    // find the class and get the object as this field's value
                    String defaultClassName = beans.get(field.getType().getTypeName());
                    Class defaultClass = Class.forName(defaultClassName);
                    fieldValue = getObject(field.getType(), defaultClass);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            // if there is no field value, this field should not be wired
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
