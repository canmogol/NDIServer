package com.fererlab.action;

import com.fererlab.dto.Model;
import com.fererlab.dto.Param;
import com.fererlab.dto.ParamMap;

import java.util.List;

/**
 * acm 11/12/12
 */
public interface CRUDAction<T extends Model> extends Action {

    public Class<T> getType();

    public T find(Object id);

    public List<T> findAll();

    public List<T> findAll(Object... keyValueList);

    public List<T> findAll(ParamMap<String, Param<String, Object>> keyValuePairs);

    public Long findCount();

    public T create(ParamMap<String, Param<String, Object>> keyValuePairs) throws Exception;

    public T update(Object id, ParamMap<String, Param<String, Object>> keyValuePairs) throws Exception;

    public T delete(Object id) throws Exception;

    public List<Object> deleteAll(List<Object> ids) throws Exception;
}