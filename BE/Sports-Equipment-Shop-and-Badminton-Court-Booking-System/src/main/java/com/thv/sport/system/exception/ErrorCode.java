package com.thv.sport.system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    USER_EXISTED(1001,"User email is existed",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1002,"User Id is not existed",HttpStatus.BAD_REQUEST),
    ADDRESS_NOT_EXISTED(1003,"Address is not existed",HttpStatus.BAD_REQUEST),
    ADDRESS_EXISTED(1004,"Address is existed",HttpStatus.BAD_REQUEST),
    ADDRESS_DUPLICATED(1005,"Address is duplicated",HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1006,"Token expired",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(1007,"Token is not valid",HttpStatus.BAD_REQUEST),
    EMAIL_NOT_CONFIRMED(1008,"Email has not confirmed yet",HttpStatus.BAD_REQUEST),
    ACCOUNT_PASSWORD_NOT_CORRECT(1009,"Email or password is incorrect",HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(10010,"Incorrect password",HttpStatus.BAD_REQUEST),
    ACCOUNT_WAS_LOCKED(10010,"Incorrect password",HttpStatus.BAD_REQUEST),


    ;
    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
    ErrorCode(int code, String message, HttpStatusCode httpStatusCode) {
        this.code = code;
        this.message = message;
        this.httpStatusCode = httpStatusCode;
    }

}
