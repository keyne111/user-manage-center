package com.xiaofan.usercenter.common;

import lombok.Data;


public enum ErrorCode {
    SUCCESS(0,"ok",""),
    NOT_ALOGIN(40100,"未登录",""),
    NO_AUTH(40101,"无权限",""),
    PARAM_ERROR(40001,"请求参数错误",""),
    NULL_ERROR(40002,"数据为空",""),
    SAVE_ERROR(40003,"插入异常",""),

    SYSTEM_ERROR(50000,"系统内部错误","");

    private final Integer code;

    private final String message;

    private final String description;


    ErrorCode(Integer code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getDescription() {
        return description;
    }
}
