package com.onewordmax.splitsmart.gateway.constants;

public class ErrorCode {
    public static final String UNKNOWN_ERROR = "error.unknown_error";

    public static final String FILED_PASSWORD_EMPTY = "field.password.empty";
    public static final String FIELD_PASSWORD_INVALID = "field.password.invalid";
    public static final String FIELD_USERNAME_EMAIL_EMPTY = "field.username_email.empty";
    public static final String FIELD_USERNAME_EMAIL_INVALID = "field.username_email.invalid";
    public static final String FIELD_USERNAME_INVALID = "field.username.invalid";
    public static final String FIELD_USERNAME_EMPTY = "field.username.empty";
    public static final String FIELD_USERNAME_DUPLICATE = "field.username.duplicate";
    public static final String FIELD_GOOGLE_ID_TOKEN_INVALID = "field.google.id_token.invalid";

    public static final String FIELD_OTP_NOT_VERIFIED = "field.otp.not_verified";
    public static final String FIELD_OTP_ALREADY_SENT = "field.otp.already_sent";
    public static final String FIELD_OTP_VERIFIED = "field.otp.verified";
    public static final String FIELD_OTP_REVERIFICATION_REQ = "field.otp.reverification_req";
    public static final String FIELD_OTP_EMPTY = "field.otp.empty";
    public static final String FIELD_OTP_CONTEXT_CHANGED = "field.otp.context.changed";
    public static final String FIELD_OTP_INVALID = "field.otp.invalid";
    public static final String FIELD_OTP_EXPIRED = "field.otp.expired";
    public static final String FIELD_OTP_ATTEMPTS_REACHED = "field.otp.attempts_reached";
    public static final String FIELD_OTP_LOCKED = "field.otp.locked";
    public static final String FIELD_OTP_RESEND_LIMIT = "field.otp.resend.limit";

    public static final String FIELD_RESET_PASSWORD_NOT_VERIFIED = "field.otp.not_verified";
    public static final String FIELD_RESET_PASSWORD_ALREADY_SENT = "field.otp.already_sent";
    public static final String FIELD_RESET_PASSWORD_ALREADY_USED = "field.otp.already_sent";
    public static final String FIELD_RESET_PASSWORD_VERIFIED = "field.otp.verified";
    public static final String FIELD_RESET_PASSWORD_REVERIFICATION_REQ = "field.otp.reverification_req";
    public static final String FIELD_RESET_PASSWORD_EMPTY = "field.otp.empty";
    public static final String FIELD_RESET_PASSWORD_CONTEXT_CHANGED = "field.otp.context.changed";
    public static final String FIELD_RESET_PASSWORD_INVALID = "field.otp.invalid";
    public static final String FIELD_RESET_PASSWORD_EXPIRED = "field.otp.expired";
    public static final String FIELD_RESET_PASSWORD_ATTEMPTS_REACHED = "field.otp.attempts_reached";
    public static final String FIELD_RESET_PASSWORD_LOCKED = "field.otp.locked";
    public static final String FIELD_RESET_PASSWORD_RESEND_LIMIT = "field.otp.resend.limit";

    public static final String FIELD_EMAIL_EMPTY = "field.email.empty";
    public static final String FIELD_EMAIL_INVALID = "field.email.invalid";
    public static final String FIELD_NAME_EMPTY = "field.name.empty";
    public static final String FIELD_NAME_INVALID = "field.name.invalid";
    public static final String FIELD_COUNTRY_EMPTY = "field.country.empty";
    public static final String FIELD_COUNTRY_INVALID = "field.country.invalid";
    public static final String FIELD_CURRENCY_EMPTY = "field.currency.empty";
    public static final String FIELD_CURRENCY_INVALID = "field.currency.invalid";
    public static final String FIELD_LANGUAGE_EMPTY = "field.language.empty";
    public static final String FIELD_LANGUAGE_INVALID = "field.language.invalid";
    public static final String FIELD_USER_TYPE_EMPTY = "field.user_type.empty";
    public static final String FIELD_USER_TYPE_INVALID = "field.user_type.invalid";

    public static final String RESOURCE_USER_CONFLICT = "error.resource.user.conflict";
    public static final String RESOURCE_USER_BLOCKED = "error.resource.user.blocked";
    public static final String RESOURCE_USER_EMPTY = "error.resource.user.empty";
    public static final String RESOURCE_USER_DELETED = "error.resource.user.deleted";
    public static final String RESOURCE_USER_GOOGLE_UNLINKED = "error.resource.user.google.unlinked";

    public static final String RESOURCE_TOKEN_INVALID = "error.resource.token.invalid";
    public static final String RESOURCE_TOKEN_EXPIRED = "error.resource.token.expired";


    public static final String REQUEST_VALIDATION_ERROR = "error.request.validation_error";
    public static final String REQUEST_BAD_REQUEST = "error.request.bad_request";
    public static final String REQUEST_EMPTY = "request.empty";
    public static final String REQUEST_INVALID = "error.request.invalid_input";

    public static final String RESOURCE_ACCESS_DENIED = "error.resource.access_denied";
    public static final String RESOURCE_NOT_FOUND = "error.resource.not_found";
    public static final String RESOURCE_CONFLICT = "error.resource.conflict";
    public static final String RESOURCE_LOCKED = "error.resource.locked";
    public static final String RESOURCE_DELETED = "error.resource.deleted";
    public static final String RESOURCE_EXPIRED = "error.resource.expired";

    public static final String SERVICE_UNAUTHENTICATED = "error.user.unauthenticated";
    public static final String SERVICE_INTERNAL_SERVER_ERROR = "error.service.internal_server_error";
    public static final String SERVICE_SERVICE_UNAVAILABLE = "error.service.service_unavailable";
    public static final String SERVICE_TIMEOUT = "error.service.timeout";
    public static final String SERVICE_RATE_LIMIT_EXCEEDED = "error.service.rate_limit_exceeded";
    public static final String SERVICE_INSUFFICIENT_STORAGE = "error.service.insufficient_storage";
    public static final String SERVICE_INVALID_STATE = "error.service.invalid_state";
    public static final String SERVICE_DEPENDENCY_ERROR = "error.service.dependency_error";
}
