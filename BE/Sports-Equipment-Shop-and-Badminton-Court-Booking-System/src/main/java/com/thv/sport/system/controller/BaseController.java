package com.thv.sport.system.controller;

import com.thv.sport.system.dto.response.BaseResponse;
import com.thv.sport.system.common.MessageUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;

/**
 * Base controller with common response methods
 * All controllers should extend this class to ensure consistent response format
 */
public abstract class BaseController {

    /**
     * Build a successful response with data
     * @param data the response data
     * @param messageCode the message code key
     * @param headers optional HTTP headers
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> successResponse(T data, String messageCode, HttpHeaders headers) {
        BaseResponse<T> response = BaseResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .error(null)
                .timestamp(LocalDateTime.now())
                .message(MessageUtils.getMessage(messageCode))
                .data(data)
                .build();

        if (ObjectUtils.isEmpty(headers)) {
            headers = new HttpHeaders();
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    /**
     * Build a successful response with data (no custom headers)
     * @param data the response data
     * @param messageCode the message code key
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> successResponse(T data, String messageCode) {
        return successResponse(data, messageCode, null);
    }

    /**
     * Build a successful response with data (default message)
     * @param data the response data
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> successResponse(T data) {
        return successResponse(data, "SUCCESS", null);
    }

    /**
     * Build a successful response without data (void response)
     * @param messageCode the message code key
     * @param headers optional HTTP headers
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> successResponse(String messageCode, HttpHeaders headers) {
        BaseResponse<T> response = BaseResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .error(null)
                .timestamp(LocalDateTime.now())
                .message(MessageUtils.getMessage(messageCode))
                .build();

        if (ObjectUtils.isEmpty(headers)) {
            headers = new HttpHeaders();
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(response);
    }

    /**
     * Build a successful response without data (no custom headers)
     * @param messageCode the message code key
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> successResponse(String messageCode) {
        return successResponse(messageCode, new HttpHeaders());
    }

    /**
     * Build a created response (HTTP 201) with data
     * @param data the response data
     * @param messageCode the message code key
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> createdResponse(T data, String messageCode) {
        BaseResponse<T> response = BaseResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .error(null)
                .timestamp(LocalDateTime.now())
                .message(MessageUtils.getMessage(messageCode))
                .data(data)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
    }

    /**
     * Build a created response (HTTP 201) with data (default message)
     * @param data the response data
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> createdResponse(T data) {
        return createdResponse(data, "SUCCESS.CREATE");
    }

    /**
     * Build an error response
     * @param status HTTP status code
     * @param errorMessage the error message
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> errorResponse(HttpStatus status, String errorMessage) {
        BaseResponse<T> response = BaseResponse.<T>builder()
                .status(status.value())
                .error(errorMessage)
                .timestamp(LocalDateTime.now())
                .message("Operation failed")
                .build();

        return ResponseEntity.status(status)
                .body(response);
    }

    /**
     * Build a bad request error response
     * @param errorMessage the error message
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> badRequestResponse(String errorMessage) {
        return errorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    /**
     * Build a not found error response
     * @param errorMessage the error message
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> notFoundResponse(String errorMessage) {
        return errorResponse(HttpStatus.NOT_FOUND, errorMessage);
    }

    /**
     * Build an internal server error response
     * @param errorMessage the error message
     * @return ResponseEntity with BaseResponse
     */
    protected <T> ResponseEntity<BaseResponse<T>> internalErrorResponse(String errorMessage) {
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, errorMessage);
    }
}

