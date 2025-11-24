package com.tiqs.common;

public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code,String message){super(message); this.code=code;}
    public int getCode(){return code;}
    public static BusinessException of(int code,String msg){return new BusinessException(code,msg);}
}

