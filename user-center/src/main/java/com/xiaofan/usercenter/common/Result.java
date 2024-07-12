package com.xiaofan.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 *
 * @author xiaofan
 * @param <T>
 */

@Data
public class Result<T> implements Serializable {

    private Integer code; //编码：0成功，1和其它数字为失败
    private String msg; //信息
    private T data; //数据
    private String description;

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 0;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 0;
        result.msg="ok";
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode){
        Result<T> result = new Result<T>();
        result.code=errorCode.getCode();
        result.msg=errorCode.getMessage();
        result.description=errorCode.getDescription();
        return result;

    }

    public static <T> Result<T> error(Integer code,String message,String description){
        Result<T> result = new Result<T>();
        result.code=code;
        result.msg=message;
        result.description=description;
        return result;

    }

    public static <T> Result<T> error(ErrorCode errorCode,String message,String description){
        Result<T> result = new Result<T>();
        result.code= errorCode.getCode();
        result.msg=message;
        result.description=description;
        return result;

    }
}
