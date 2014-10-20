package com.fererlab.action;

import com.fererlab.dto.Request;

/**
 * acm | 1/21/13
 */
public interface Action {

    public String toContent(Request request, Object... objects);

    public void log(String message);

}
