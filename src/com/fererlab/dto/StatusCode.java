package com.fererlab.dto;

/**
 * acm
 */
public enum StatusCode {
    SUCCESS("success", 1),
    FAIL("fail", 0);

    private StatusCode(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    private String name;
    private Integer code;

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }
}
