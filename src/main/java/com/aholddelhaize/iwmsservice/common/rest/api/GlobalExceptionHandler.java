package com.aholddelhaize.iwmsservice.common.rest.api;

import com.aholddelhaize.iwmsservice.common.dto.ErrorResponseData;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.util.WebUtils;

import static com.aholddelhaize.iwmsservice.constants.ResponseErrors.*;

@Log4j2
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.isSameCodeAs(statusCode)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, RequestAttributes.SCOPE_REQUEST);
        }
        String error = body != null ? body.toString() : INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(new ErrorResponseData(error, ex.getMessage()), headers, statusCode);
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(final NoHandlerFoundException ex, final HttpHeaders headers,
                                                                   final HttpStatusCode statusCode, final WebRequest request) {
        return handleExceptionInternal(ex, RESOURCE_NOT_FOUND_ERROR, headers, statusCode, request);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(final MissingServletRequestParameterException ex, final HttpHeaders headers,
                                                                          final HttpStatusCode statusCode, final WebRequest request) {
        return handleExceptionInternal(ex, REQUEST_VALIDATION_ERROR, headers, statusCode, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers,
                                                                  final HttpStatusCode statusCode, final WebRequest request) {
        return handleExceptionInternal(ex, REQUEST_VALIDATION_ERROR, headers, statusCode, request);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseData handleException(Exception e) {
        log.error("Unknown internal error happens", e);
        return new ErrorResponseData(INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler({ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponseData handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Handle constraint violation exception: {}", ex.getMessage());
        return new ErrorResponseData(REQUEST_VALIDATION_ERROR, ex.getMessage());
    }

    @ExceptionHandler(value = {InvalidTokenException.class, AuthenticationException.class, InsufficientAuthenticationException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponseData handleUnauthorizedException(Exception ex) {
        log.warn("Handle unauthorized exception: {}", ex.getMessage());
        return new ErrorResponseData(AUTHORIZATION_REQUIRED_ERROR, ex.getMessage());
    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponseData handleAccessDeniedException(Exception ex) {
        log.warn("Handle access denied exception: {}", ex.getMessage());
        return new ErrorResponseData(ACCESS_DENIED_ERROR, ex.getMessage());
    }
}
