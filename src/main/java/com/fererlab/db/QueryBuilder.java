package com.fererlab.db;

import com.fererlab.action.ModelAction;
import com.fererlab.dto.Model;
import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;
import com.fererlab.dto.ParamRelation;

import java.util.List;

/**
 * acm
 */
public class QueryBuilder<T extends Model> {

    private ModelAction<T> modelAction;
    ParamMap<String, Param<String, Object>> paramMap = new ParamMap<String, Param<String, Object>>();

    public QueryBuilder(Class<T> clazz) {
        modelAction = new ModelAction<T>(clazz);
    }

    public QueryBuilder<T> where(String field, Object value) {
        return where(field, ParamRelation.EQ, value);
    }

    public QueryBuilder<T> where(String field, ParamRelation relation, Object value) {
        // this parameter's and/or will be ignored so it is set to true
        return addParam(field, relation, true, value);
    }

    public QueryBuilder<T> and(String field, Object value) {
        return and(field, ParamRelation.EQ, value);
    }

    public QueryBuilder<T> and(String field, ParamRelation relation, Object value) {
        return addParam(field, relation, true, value);
    }

    public QueryBuilder<T> or(String field, Object value) {
        return or(field, ParamRelation.EQ, value);
    }

    public QueryBuilder<T> or(String field, ParamRelation relation, Object value) {
        return addParam(field, relation, false, value);
    }

    public List<T> findAll() {
        return modelAction.findAll(paramMap);
    }

    private QueryBuilder<T> addParam(String field, ParamRelation relation, boolean andOr, Object value) {
        paramMap.addParam(new Param<String, Object>(field, value, andOr, relation));
        return this;
    }

}
