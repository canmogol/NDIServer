package com.ndi.app.action;

import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.P;
import com.ndi.app.model.Department;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * acm
 */
public class LoginAction extends BaseAction {

    public Response show(Request request) {
        for (Method m : this.getClass().getDeclaredMethods()) {
            if (m.getName().equals("other")) {
                List<String> parameterNames = new ArrayList<>();
                Annotation[][] annMatrix = m.getParameterAnnotations();
                for (Annotation[] anns : annMatrix) {
                    for (Annotation annotation : anns) {
                        if (annotation instanceof P) {
                            P p = (P) annotation;
                            String parameterName = p.value();
                            parameterNames.add(parameterName);
                        }
                    }
                }
                List<String> parameterValues = new ArrayList<>();
                for (String parameterName : parameterNames) {
                    String parameterValue = request.get(parameterName);
                    parameterValues.add(parameterValue);
                }
                try {
                    Object response = m.invoke(this, parameterValues.toArray());
                    return Ok(request).add(m.getReturnType().getSimpleName(), response).toResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return Ok(request).add("data", "testing").toResponse();
    }


    public Department other(@P("service") String service,
                          @P("name") String name,
                          @P("args") String args) {
        Department department = new Department();
        department.setEmail("q@q.com");
        department.setId(1L);
        department.setName("name");
        return department;
    }

    public Response doLogin(Request request) {
        return Ok(request).add("data", "").toResponse();
    }

    public Response content(final Request request) {
        return RequestHttp(
                "http://www.google.co.uk",
                new HashMap<String, String>(),
                (Response response) -> {
                    return handleResponse(request, response);
                }
        ).toResponse();
    }

    private Response handleResponse(Request request, Response response) {
        return Ok(request).add("content", response.getContent()).toResponse();
    }

}
