package com.fererlab.dto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * acm 10/16/12
 */
public class ParamMap<K extends String, V extends Param<K, Object>> extends LinkedHashMap<K, V> {


    public void addParam(V param) {
        put(param.getKey(), param);
    }

    public List<Param<K, Object>> getParamList() {
        return new ArrayList<Param<K, Object>>(values()) ;
    }

    public Object getValue(K k) {
        return get(k).getValue();
    }

}

