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

    @ExceptionHandler(com.tiqs.common.BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBiz(com.tiqs.common.BusinessException ex){
        log.info("业务处理失败: {}", ex.getMessage());
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex){
        log.warn("参数错误: {}", ex.getMessage());
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<Void> handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException ex){
        log.warn("文件大小超出限制: {}", ex.getMessage());
        return ApiResponse.error(413, "文件大小超出限制，请选择小于100MB的文件");
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handle(Exception ex){
        log.error("系统未处理异常", ex);
        return ApiResponse.error(500, "服务器内部错误，请稍后重试");
    }
}
