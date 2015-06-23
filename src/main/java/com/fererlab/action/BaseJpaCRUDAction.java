package com.fererlab.action;

import com.fererlab.db.EM;
import com.fererlab.dto.Model;
import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;
import org.hibernate.TypeMismatchException;

import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * acm 11/12/12
 */
public class BaseJpaCRUDAction<T extends Model> extends BaseAction implements CRUDAction<T> {

    private Class<T> type;
    private List<String> fieldNames = new ArrayList<String>();

    public BaseJpaCRUDAction(Class<T> type) {
        super();
        this.type = type;

        for (Method m : type.getMethods()) {
            if (m.getName().startsWith("set")) {
                String fieldName = m.getName().substring(3);
                fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
                fieldNames.add(fieldName);
            }
        }

        getXStream().autodetectAnnotations(true);
        getXStream().alias(type.getSimpleName(), type);

        getXStreamJSON().autodetectAnnotations(true);
        getXStreamJSON().alias(type.getSimpleName(), type);
    }

    public Class<T> getType() {
        return type;
    }

    @Override
    public T find(Object id) {
        return EM.find(type, id);
    }

    @Override
    public List<T> findAll() {
        return findAll(new ParamMap<String, Param<String, Object>>());
    }

    @Override
    public List<T> findAll(Object... keyValueList) {
        ParamMap<String, Param<String, Object>> paramMap = new ParamMap<String, Param<String, Object>>();
        for (int i = 0; i < keyValueList.length; i = i + 2) {
            String key = String.valueOf(keyValueList[i]);
            paramMap.put(key, new Param<String, Object>(key, keyValueList[i + 1]));
        }
        return findAll(paramMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<T> findAll(ParamMap<String, Param<String, Object>> keyValuePairs) {

        // will use criteria builder
        CriteriaBuilder criteriaBuilder = EM.getEntityManager().getCriteriaBuilder();

        // select from entity
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(type);
        Root<T> from = criteriaQuery.from(type);
        criteriaQuery.select(from);

        // will store all predicates
        List<Predicate> predicates = new ArrayList<Predicate>();
        List<Boolean> andOrList = new ArrayList<Boolean>();

        // parameter list
        List<ParameterExpression<Object>> parameterExpressionList = new ArrayList<ParameterExpression<Object>>();
        List<Object> parameterList = new ArrayList<Object>();

        // orderBy may be null
        String orderBy = null;
        Integer start = null;
        Integer limit = null;
        if (keyValuePairs != null) {
            // set offset, limit and orderBy any if exists
            if (keyValuePairs.containsKey("_start") && keyValuePairs.getValue("_start") != null) {
                start = Integer.valueOf(keyValuePairs.remove("_start").getValue().toString());
            }
            if (keyValuePairs.containsKey("_limit") && keyValuePairs.getValue("_limit") != null) {
                limit = Integer.valueOf(keyValuePairs.remove("_limit").getValue().toString());
            }
            if (keyValuePairs.containsKey("_order") && keyValuePairs.getValue("_order") != null) {
                orderBy = keyValuePairs.remove("_order").getValue().toString().trim().replace("%20", " ");
            }
            // for rest of the param list
            for (final Param<String, Object> param : keyValuePairs.getParamList()) {
                // if model does not have this field than do not add the parameter as a criteria
                if (!fieldNames.contains(param.getKey())) {
                    continue;
                }
                // add and or for query builder
                if (param.getValueSecondary() != null && param.getValueSecondary().equals(false)) {
                    andOrList.add(Boolean.FALSE);
                } else {
                    andOrList.add(Boolean.TRUE);
                }
                // create a parameter expression with value's type
                ParameterExpression parameterExpression = null;
                switch (param.getRelation()) {
                    case EQ:
                        parameterExpression = criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.equal(from.get(param.getKey()), parameterExpression));
                        break;
                    case LIKE:
                        parameterExpression = criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.like(from.<String>get(param.getKey()), parameterExpression));
                        break;
                    case NE:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.notEqual(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case BETWEEN:
                        parameterExpression = (ParameterExpression<Comparable>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(
                                criteriaBuilder.between(from.<Comparable>get(param.getKey()), (Comparable) param.getValue(), (Comparable) param.getValueSecondary())
                        );
                        continue;
                    case GT:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.gt(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case GE:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.ge(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case LT:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.lt(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                    case LE:
                        parameterExpression = (ParameterExpression<? extends Number>) criteriaBuilder.parameter(param.getValue().getClass());
                        predicates.add(criteriaBuilder.le(from.<Number>get(param.getKey()), parameterExpression));
                        break;
                }
                // then add the value(with its new type) to parameterList and the parameter expression to its list
                parameterExpressionList.add(parameterExpression);
                parameterList.add(param.getValue());
            }

            // where    and|x=1     and|y=2     or|z=3
            // set predicates if any
            if (predicates.size() > 0) {
                List<Predicate> predicateList = new ArrayList<Predicate>();
                Predicate predicate;
                for (int i = 0; i < predicates.size(); i = i + 2) {
                    if (i + 1 < predicates.size()) {
                        Boolean andOr = andOrList.get(i + 1);
                        if (andOr) {
                            predicate = criteriaBuilder.and(predicates.get(i), predicates.get(i + 1));
                        } else {
                            predicate = criteriaBuilder.or(predicates.get(i), predicates.get(i + 1));
                        }
                        predicateList.add(predicate);
                    } else {
                        Boolean andOr = andOrList.get(i);
                        if (andOr) {
                            predicate = criteriaBuilder.and(predicates.get(i));
                        } else {
                            predicate = criteriaBuilder.or(predicates.get(i));
                        }
                        predicateList.add(predicate);
                    }
                }
                if (predicateList.size() > 0) {
                    Predicate[] predicateArray = new Predicate[predicateList.size()];
                    for (int i = 0; i < predicateList.size(); i++) {
                        predicateArray[i] = predicateList.get(i);
                    }
                    criteriaQuery.where(predicateArray);
                }
            }

        }

        // set if orderBy is not null
        if (orderBy != null) {
            String[] orderByAndOrderDirection = orderBy.split(" ");
            if (orderByAndOrderDirection.length == 2) { // "id asc" or "id desc"
                if (orderByAndOrderDirection[1].equalsIgnoreCase("asc")) {
                    criteriaQuery.orderBy(criteriaBuilder.asc(from.get(orderByAndOrderDirection[0])));
                } else {
                    criteriaQuery.orderBy(criteriaBuilder.desc(from.get(orderByAndOrderDirection[0])));
                }
            } else {
                criteriaQuery.orderBy(criteriaBuilder.desc(from.get(orderBy)));
            }
        }

        // create query
        TypedQuery<T> query = EM.getEntityManager().createQuery(criteriaQuery);

        /*
         * 1	0	25	0	25	
         * 2	25	25	25	25
         * 3	50	25	50	25
         * 
         */
        // set offset if available
        if (start != null) {
            query = query.setFirstResult(start);
        }

        // set limit if available
        if (limit != null) {
            query = query.setMaxResults(limit);
        }

        // set parameter values
        if (parameterList.size() > 0) {
            for (int i = 0; i < parameterList.size(); i++) {
                query.setParameter(parameterExpressionList.get(i), parameterList.get(i));
            }
        }

        // return the result list
        return query.getResultList();

    }

    @Override
    public Long findCount() {
        String queryString = "select count(model) from " + type.getSimpleName() + " model";
        Query query = EM.getEntityManager().createQuery(queryString);
        return (Long) query.getSingleResult();
    }

    public T create(Object... keyValueList) throws Exception {
        ParamMap<String, Param<String, Object>> paramMap = new ParamMap<String, Param<String, Object>>();
        for (int i = 0; i < keyValueList.length; i = i + 2) {
            String key = String.valueOf(keyValueList[i]);
            paramMap.put(key, new Param<String, Object>(key, keyValueList[i + 1]));
        }
        return create(paramMap);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T create(ParamMap<String, Param<String, Object>> keyValuePairs) throws Exception {
        T t = null;
        try {
            t = type.newInstance();

            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().startsWith("set")) {
                    String fieldName = method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH) + method.getName().substring(4);
                    if (keyValuePairs.containsKey(fieldName)) {
                        try {
                            Class<?>[] parameterClasses = method.getParameterTypes();
                            method = t.getClass().getDeclaredMethod(method.getName(), parameterClasses);
                            Object value;
                            if (parameterClasses.length > 0 && type.getPackage().equals(parameterClasses[0].getPackage())) {
                                Object id;
                                try {
                                    id = Long.valueOf(keyValuePairs.get(fieldName).getValue().toString());
                                } catch (Exception e) {
                                    id = keyValuePairs.get(fieldName).getValue();
                                }
                                Class<T> relationalT = (Class<T>) parameterClasses[0];
                                BaseJpaCRUDAction<?> relationalTBaseJpaCRUDAction = new BaseJpaCRUDAction(relationalT);
                                value = relationalTBaseJpaCRUDAction.find(id);
                            } else {
                                value = keyValuePairs.get(fieldName).getValue();
                            }
                            try {
                                method.invoke(t, value);
                            } catch (IllegalArgumentException iae) {
                                if (parameterClasses.length > 0) {
                                    try {
                                        // method.invoke(...) should work, this should not be called
                                        Constructor constructor = Class.forName(parameterClasses[0].getName()).getConstructor(String.class);
                                        value = constructor.newInstance(keyValuePairs.get(fieldName).getValue());
                                        method.invoke(t, value);
                                    } catch (NoSuchMethodException e) {
                                        try {
                                            Class c = Class.forName(parameterClasses[0].getName());
                                            Method cm = c.getMethod("valueOf", String.class);
                                            Object obj = cm.invoke(null, keyValuePairs.get(fieldName).getValue().toString().replace("%20", " "));
                                            method.invoke(t, obj);
                                        } catch (Exception ce) {
                                            ce.printStackTrace();
                                        }
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            EM.persist(t);

        } catch (Exception e) {
            EM.getEntityManager().clear();
            if (EM.getEntityManager().getTransaction() != null && EM.getEntityManager().getTransaction().isActive()) {
                EM.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
        return t;
    }

    public T update(Object id, Object... keyValueList) throws Exception {
        ParamMap<String, Param<String, Object>> paramMap = new ParamMap<String, Param<String, Object>>();
        for (int i = 0; i < keyValueList.length; i = i + 2) {
            String key = String.valueOf(keyValueList[i]);
            paramMap.put(key, new Param<String, Object>(key, keyValueList[i + 1]));
        }
        return update(id, paramMap);
    }

    @Override
    public T update(Object id, ParamMap<String, Param<String, Object>> keyValuePairs) throws Exception {

        T t = null;

        try {

            t = EM.find(type, id);

            if (t == null) {
                throw new Exception("There is no object found with id: " + id + " of type: " + type);
            }

            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().startsWith("set")) {
                    String fieldName = method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH) + method.getName().substring(4);
                    if (keyValuePairs.containsKey(fieldName)) {
                        try {
                            Class<?>[] parameterClasses = method.getParameterTypes();
                            method = t.getClass().getDeclaredMethod(method.getName(), parameterClasses);
                            Object value = keyValuePairs.get(fieldName).getValue();
                            try {
                                method.invoke(t, value);
                            } catch (IllegalArgumentException iae) {
                                if (parameterClasses.length > 0) {
                                    try {
                                        // method.invoke(...) should work, this should not be called
                                        Constructor constructor = Class.forName(parameterClasses[0].getName()).getConstructor(String.class);
                                        value = constructor.newInstance(keyValuePairs.get(fieldName).getValue());
                                        method.invoke(t, value);
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    } catch (InstantiationException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            t = EM.merge(t);


        } catch (Exception e) {
            EM.getEntityManager().clear();
            if (EM.getEntityManager().getTransaction() != null && EM.getEntityManager().getTransaction().isActive()) {
                EM.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
        return t;
    }

    @Override
    public T delete(Object id) throws Exception {
        try {
            T t = EM.find(type, id);
            EM.remove(t);
            return t;
        } catch (Exception e) {
            EM.getEntityManager().clear();
            if (EM.getEntityManager().getTransaction() != null && EM.getEntityManager().getTransaction().isActive()) {
                EM.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }

    @Override
    public List<Object> deleteAll(List<Object> ids) throws Exception {
        try {
            List<Object> deletedIds = new ArrayList<Object>();
            for (Object id : ids) {
                T t = null;
                try {
                    t = EM.find(type, id);
                } catch (IllegalArgumentException iae) {
                    if (iae.getCause() instanceof TypeMismatchException) {
                        id = Long.valueOf(id.toString());
                        t = EM.find(type, id);
                    }
                }
                if (t == null) {
                    throw new Exception("no object found for this id: " + id);
                }
                EM.remove(t);
                deletedIds.add(id);
            }
            return deletedIds;
        } catch (Exception e) {
            EM.getEntityManager().clear();
            if (EM.getEntityManager().getTransaction() != null && EM.getEntityManager().getTransaction().isActive()) {
                EM.getEntityManager().getTransaction().rollback();
            }
            throw e;
        }
    }
}
