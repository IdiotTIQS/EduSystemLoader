/**
 * @author TIQS
 * @date Created in 2025-11-28 14:50:16
 * @description BusinessException
 */
package com.tiqs.common;

public class BusinessException extends RuntimeException {
    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException of(int code, String msg) {
        return new BusinessException(code, msg);
    }

    public int getCode() {
        return code;
    }
}

