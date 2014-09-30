package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.action.ModelAction;
import com.fererlab.dto.*;
import com.ndi.app.model.Department;

import java.util.List;

/**
 * acm
 */
public class DepartmentAction extends BaseAction {

    public Response specialSearch(Request request) {
        ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
        List<Department> list = modelAction.findAll("email", "a@a.com", "name", "a");

        List<Department> userList = modelAction.findAll(
                new ParamMap<String, Param<String, Object>>() {{
                    addParam(new Param<String, Object>("email", "a@a.com", ParamRelation.LIKE));
                    addParam(new Param<String, Object>("name", "a"));
                }}
        );

        return Ok(request)
                .add("data", userList)
                .toResponse();
    }
}
