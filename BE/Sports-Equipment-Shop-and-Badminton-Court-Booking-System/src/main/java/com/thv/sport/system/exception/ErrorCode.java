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
    ACCOUNT_WAS_LOCKED(10011,"Account was locked",HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_AVAILABLE(1012,"Product not available",HttpStatus.BAD_REQUEST),
    ADD_TO_CART_SUCCESS(1013,"Add to cart successfully",HttpStatus.OK),
    INVALID_DOB(3008,"Your age is at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_CART_ITEM_NAME(3009,"Cart item name is at least {min}",HttpStatus.BAD_REQUEST),
    INVALID_QUANTITY(3010,"The minimum quantity is 1",HttpStatus.BAD_REQUEST),
    INVALID_PRICE(3011,"The price must be > 0",HttpStatus.BAD_REQUEST),
    VARIANT_NOT_FOUND(3014,"Variant not found",HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(3015,"Product not found",HttpStatus.BAD_REQUEST),

    INTERNAL_SERVER_ERROR(9999,"Internal server error",HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_REQUEST(2001,"Invalid request",HttpStatus.BAD_REQUEST),
    RUNTIME_ERROR(2002,"Runtime error",HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(2003,"Unauthorized",HttpStatus.FORBIDDEN),




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
