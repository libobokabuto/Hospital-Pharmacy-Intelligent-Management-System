package com.hpims.exception;

/**
 * 认证异常
 */
public class AuthenticationException extends BusinessException {
    
    public AuthenticationException(String message) {
        super("AUTHENTICATION_FAILED", message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super("AUTHENTICATION_FAILED", message, cause);
    }
}

