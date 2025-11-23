package com.tiqs.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Map<String,Object> handle(Exception ex){
        log.error("Unhandled exception", ex);
        Map<String,Object> m = new HashMap<>();
        m.put("code",500);
        m.put("message",ex.getMessage());
        return m;
    }
}
