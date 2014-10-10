package com.fererlab.dto;

/**
 * acm
 */
public class ParamMapBuilder<K extends String, V extends Param<K, Object>> {

    ParamMap<K, Param<K, Object>> paramMap = new ParamMap<K, Param<K, Object>>();

    public void add(K field, Object value) {
        add(field, value, ParamRelation.EQ);
    }

    public void add(K field, Object value, ParamRelation relation) {
        paramMap.addParam(new Param<K, Object>(field, value, relation));
    }

    public ParamMap<K, Param<K, Object>> getParamMap() {
        return paramMap;
    }
}
