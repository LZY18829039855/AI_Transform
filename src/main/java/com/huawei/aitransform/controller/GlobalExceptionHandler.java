package com.huawei.aitransform.controller;

import com.huawei.aitransform.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Result<Object> handleException(Exception e) {
        logger.error("系统异常：", e);
        return Result.error("系统异常：" + e.getMessage());
    }
}








