/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description GlobalExceptionHandler
 */
package com.tiqs.handler;

import com.tiqs.auth.AuthException;
import com.tiqs.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResponse<Void> handleAuth(AuthException ex) {
        log.warn("鉴权失败: {}", ex.getMessage());
        return ApiResponse.error(401, ex.getMessage());
    }

    @ExceptionHandler(com.tiqs.common.BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> handleBiz(com.tiqs.common.BusinessException ex) {
        log.info("业务处理失败: {}", ex.getMessage());
        return ApiResponse.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("参数错误: {}", ex.getMessage());
        return ApiResponse.error(400, ex.getMessage());
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    public ApiResponse<Void> handleMaxUploadSizeExceeded(org.springframework.web.multipart.MaxUploadSizeExceededException ex) {
        log.warn("文件大小超出限制: {}", ex.getMessage());
        return ApiResponse.error(413, "文件大小超出限制，请选择小于100MB的文件");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public String handleNoResourceFound(NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("静态资源未找到: {}", request.getRequestURI());
        return "redirect:/";
    }

    @ExceptionHandler(org.apache.catalina.connector.ClientAbortException.class)
    public String handleClientAbortException(org.apache.catalina.connector.ClientAbortException ex) {
        log.debug("客户端中断连接: {}", ex.getMessage());
        return null;
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("数据重复冲突: {}", ex.getMessage());
        String message = "数据已存在，请检查重复项";
        if (ex.getMessage().contains("username")) {
            message = "用户名已存在";
        } else if (ex.getMessage().contains("email")) {
            message = "邮箱已被注册";
        }
        return ApiResponse.error(409, message);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleDataAccess(DataAccessException ex) {
        log.error("数据库访问异常", ex);
        return ApiResponse.error(500, "数据操作失败，请稍后重试");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数验证失败: {}", message);
        return ApiResponse.error(400, "参数验证失败: " + message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleBindException(BindException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("参数绑定失败: {}", message);
        return ApiResponse.error(400, "参数绑定失败: " + message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        log.warn("约束验证失败: {}", message);
        return ApiResponse.error(400, "参数验证失败: " + message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMissingParameter(MissingServletRequestParameterException ex) {
        log.warn("缺少必需参数: {}", ex.getParameterName());
        return ApiResponse.error(400, "缺少必需参数: " + ex.getParameterName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("请求消息不可读: {}", ex.getMessage());
        return ApiResponse.error(400, "请求数据格式错误，请检查JSON格式");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ApiResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("不支持的HTTP方法: {}", ex.getMethod());
        return ApiResponse.error(405, "不支持的请求方法: " + ex.getMethod());
    }

    @ExceptionHandler(MultipartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleMultipartException(MultipartException ex) {
        log.warn("文件上传异常: {}", ex.getMessage());
        return ApiResponse.error(400, "文件上传失败: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handle(Exception ex, HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/uploads/") || uri.startsWith("/static/") || uri.startsWith("/css/") || uri.startsWith("/js/")) {
            log.warn("静态资源访问异常: {} - {}", uri, ex.getMessage());
            return null;
        }

        log.error("系统未处理异常: {}", uri, ex);
        return ApiResponse.error(500, "服务器内部错误，请稍后重试");
    }
}
