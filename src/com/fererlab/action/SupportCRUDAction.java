package com.fererlab.action;

import com.fererlab.dto.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acm | 1/21/13
 */
public class SupportCRUDAction<T extends Model> extends BaseAction {

    private CRUDAction<T> crudAction;

    public SupportCRUDAction(Class<T> type) {
        crudAction = new BaseJpaCRUDAction<T>(type);
    }

    public SupportCRUDAction(Class<T> type, Class<CRUDAction<T>> crudActionClass) {
        try {
            crudAction = crudActionClass.getConstructor(type).newInstance(type);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Response find(Request request) {
        try {
            T t = crudAction.find(request.get("id"));
            return Ok(request, "record found")
                    .add("data", t)
                    .toResponse();
        } catch (Exception e) {
            return Error(request, "Error: " + e.getMessage()).toResponse();
        }
    }

    public Response findAll(Request request) {
        try {
            List<T> list = crudAction.findAll(clearKeyValuePairs(request.getParams()));
            Long totalCount = crudAction.findCount();
            return Ok(request, "records found")
                    .add("data", list)
                    .add("totalCount", totalCount)
                    .toResponse();
        } catch (Exception e) {
            return Error(request, "Error: " + e.getMessage()).toResponse();
        }
    }

    public Response create(Request request) {
        try {
            T t = crudAction.create(clearKeyValuePairs(request.getParams()));
            return Created(request, "record created successfully")
                    .add("data", t)
                    .toResponse();
        } catch (Exception e) {
            return Error(request, "Error: " + e.getMessage()).exception(e).toResponse();
        }
    }

    public Response update(Request request) {
        try {
            ParamMap<String, Param<String, Object>> keyValuePairs = clearKeyValuePairs(request.getParams());
            T t = crudAction.update(keyValuePairs.remove("id").getValue(), keyValuePairs);
            return Ok(request, "record updated successfully")
                    .add("data", t)
                    .toResponse();
        } catch (Exception e) {
            return Error(request, "Error: " + e.getMessage()).toResponse();
        }
    }

    public Response delete(Request request) {
        try {
            ParamMap<String, Param<String, Object>> keyValuePairs = clearKeyValuePairs(request.getParams());
            Object id = keyValuePairs.remove("id").getValue();
            try {
                T t = crudAction.delete(id);
                return Ok(request, "record deleted successfully")
                        .add("data", t)
                        .toResponse();
            } catch (Exception e) {
                return Error(request, "could not delete object with id: " + id + " exception: " + e.getMessage()).toResponse();
            }
        } catch (Exception e) {
            return Error(request, "Error: " + e.getMessage()).toResponse();
        }
    }

    public Response deleteAll(Request request) {
        try {
            // prepare the ids for the records to be deleted
            List<Object> ids = new ArrayList<Object>();
            String idsValue = (String) request.get("ids");
            if (idsValue.lastIndexOf("-") != -1) {
                String[] fromToIds = idsValue.split("-");// 1-4
                int from = Integer.valueOf(fromToIds[0]);
                int to = Integer.valueOf(fromToIds[1]);
                for (int i = from; i <= to; i++) {
                    ids.add(new Integer(i)); // it turns out that, this unnecessary boxing is necessary
                }
            } else if (idsValue.lastIndexOf(",") != -1) {
                String[] stringIds = idsValue.split(",");// 1,2,3,4
                for (String id : stringIds) {
                    ids.add(Integer.valueOf(id));
                }
            }
            // now call deleteAll
            try {
                List<Object> deletedIds = crudAction.deleteAll(ids);
                return Ok(request, "records deleted successfully")
                        .add("data", deletedIds)
                        .toResponse();
            } catch (Exception e) {
                return Error(request, "could not delete objects, exception: " + e.getMessage()).toResponse();
            }
        } catch (Exception e) {
            return Error(request, "Error: " + e.getMessage()).toResponse();
        }
    }


    public final Map<Class<?>, Class<?>> primitives = new HashMap<Class<?>, Class<?>>() {{
        put(boolean.class, Boolean.class);
        put(byte.class, Byte.class);
        put(short.class, Short.class);
        put(char.class, Character.class);
        put(int.class, Integer.class);
        put(long.class, Long.class);
        put(float.class, Float.class);
        put(double.class, Double.class);
    }};

    private ParamMap<String, Param<String, Object>> clearKeyValuePairs(ParamMap<String, Param<String, Object>> params) {

        // put all params to key value pairs, but not the request keys
        ParamMap<String, Param<String, Object>> keyValuePairs = new ParamMap<String, Param<String, Object>>();
        keyValuePairs.putAll(params);
        for (RequestKeys requestKeys : RequestKeys.values()) {
            if (params.containsKey(requestKeys.getValue())) {
                keyValuePairs.remove(requestKeys.getValue());
            }
        }

        // for each value in the keyValuePair, set the correct type for value
        for (String key : keyValuePairs.keySet()) {
            Param<String, Object> param = keyValuePairs.get(key);
            try {
                Field field = crudAction.getType().getDeclaredField(param.getKey());
                Class<?> fieldClass = field.getType();
                if (primitives.containsKey(fieldClass)) {
                    fieldClass = primitives.get(fieldClass);
                }
                // then if the value's type is different from that, try to cast,
                if (!fieldClass.isInstance(param.getValue())) {
                    try {
                        param = new Param<String, Object>(
                                param.getKey(),
                                fieldClass.cast(param.getValue()),
                                param.getValueSecondary(),
                                param.getRelation()
                        );
                    } catch (Exception e) {
                        //              if cannot cast, try to create an instance of value's type using the
                        //                  value(probably the string value) as the constructor parameter of valueOf(...) parameter
                        try {
                            if (param.getRelation().equals(ParamRelation.BETWEEN)) {
                                param = new Param<String, Object>(
                                        param.getKey(),
                                        fieldClass.getDeclaredConstructor(String.class).newInstance(param.getValue().toString()),
                                        fieldClass.getDeclaredConstructor(String.class).newInstance(param.getValueSecondary().toString()),
                                        param.getRelation()
                                );
                            } else {
                                param = new Param<String, Object>(
                                        param.getKey(),
                                        fieldClass.getDeclaredConstructor(String.class).newInstance(param.getValue().toString()),
                                        param.getValueSecondary(),
                                        param.getRelation()
                                );
                            }
                        } catch (NoSuchMethodException e1) {
                            // no need for stack trace
                            continue;
                        } catch (Exception e1) {
                            // this value's class and the declared field's class are not compatible, will skip this key/value pair!
                            e1.printStackTrace();
                            continue;
                        }
                    }
                }
                // put the param over the old key's value
                keyValuePairs.put(key, param);
            } catch (Exception e) {
                // current param and its key/value pair is ok as they are, do nothing
            }
        }
        return keyValuePairs;
    }

}