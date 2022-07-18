package com.lagrange.tirage.tirageapi.exceptions;

public enum ErrorCodesEnum {

    TIRAGE_NOT_FOUND("TIRAGE_NOT_FOUND"),
    COMPANY_NOT_FOUND("COMPANY_NOT_FOUND"),
    NOTIFY_PARTICIPANT_FAILED("NOTIFY_PARTICIPANT_FAILED"),
    PARTICIPANT_NOT_FOUND("PARTICIPANT_NOT_FOUND");

    private String codeKey;
    ErrorCodesEnum(String codeKey){

        this.codeKey = codeKey;
    }

    String getMessageCodeKey(){
        return codeKey;
    }
}
