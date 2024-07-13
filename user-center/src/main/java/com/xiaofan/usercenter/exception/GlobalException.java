package com.xiaofan.usercenter.exception;

import com.xiaofan.usercenter.common.ErrorCode;
import com.xiaofan.usercenter.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器类
 *
 * @author xiaofan
 */
@RestControllerAdvice
@Slf4j
public class GlobalException {

    @ExceptionHandler(BusinessException.class)
    public Result businessException(BusinessException e){
        log.error("businessException:"+e.getMessage(),e);
        return Result.error(e.getCode(),e.getMessage(),e.getDescription());

    }

    @ExceptionHandler(RuntimeException.class)
    public Result runtimeException(RuntimeException e){
        log.error("runtimeException:"+e);
        return Result.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"出现未知异常");
    }
}
