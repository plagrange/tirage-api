package com.lagrange.tirage.tirageapi.exceptions;

public class UserException extends Exception{

    private final ErrorCodesEnum errorCode;

    public UserException(ErrorCodesEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorCode(){
        return errorCode.getMessageCodeKey();
    }
}
