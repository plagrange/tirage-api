package com.lagrange.tirage.tirageapi.exceptions;

public class UserException extends Exception{

    private ErrorCodesEnum errorCode;

    public UserException(ErrorCodesEnum errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorCode(ErrorCodesEnum errorCode){
        this.errorCode = errorCode;
    }
}
