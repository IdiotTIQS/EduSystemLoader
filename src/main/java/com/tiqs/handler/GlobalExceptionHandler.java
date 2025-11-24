package com.tiqs.handler;

import com.tiqs.auth.AuthException;
import com.tiqs.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthException ex){
        log.warn("鉴权失败: {}", ex.getMessage());
        return ApiResponse.error(401, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handle(Exception ex){
        log.error("系统未处理异常", ex);
        return ApiResponse.error(500, ex.getMessage());
    }
}
