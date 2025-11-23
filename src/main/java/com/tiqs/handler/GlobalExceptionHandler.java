package com.tiqs.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Map<String,Object> handle(Exception ex){
        Map<String,Object> m = new HashMap<>();
        m.put("code",500);
        m.put("message",ex.getMessage());
        return m;
    }
}
