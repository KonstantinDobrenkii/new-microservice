package com.aholddelhaize.iwmsservice.constants;

public final class ResponseErrors {

    private ResponseErrors() {
    }

    public static final String INTERNAL_SERVER_ERROR = "InternalError";
    public static final String AUTHORIZATION_REQUIRED_ERROR = "AuthorizationRequiredError";
    public static final String ACCESS_DENIED_ERROR = "AccessDeniedError";
    public static final String RESOURCE_NOT_FOUND_ERROR = "ResourceNotFoundError";

    public static final String REQUEST_VALIDATION_ERROR = "RequestValidationError";


}
