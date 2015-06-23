package com.fererlab.action;

import com.fererlab.dto.Model;

/**
 * acm
 */
public class ModelAction<T extends Model> extends BaseJpaCRUDAction<T> {

    public ModelAction(Class<T> modelClass) {
        super(modelClass);
    }

}