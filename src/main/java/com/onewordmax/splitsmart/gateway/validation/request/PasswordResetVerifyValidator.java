package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.PasswordResetVerifyRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordResetVerifyValidator implements Validator<PasswordResetVerifyRequest> {
    private static final Pattern PASSWORD = Pattern.compile(RegexPattern.PASSWORD);

    @Override
    public List<ValidationError> validate(PasswordResetVerifyRequest req) {
        List<ValidationError> errors = new ArrayList<>();
        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        // 1. Token Check (UUID validation)
        if (req.token() == null || req.token().isBlank()) {
            errors.add(new ValidationError("token", ErrorCode.FIELD_OTP_EMPTY, ErrorCode.FIELD_OTP_EMPTY));
        }

        // 2. Password Check
        ValidatorUtil.checkPattern(req.newPassword(), PASSWORD, "newPassword",
                ErrorCode.FIELD_PASSWORD_INVALID, ErrorCode.FIELD_PASSWORD_INVALID, errors);

        return errors;
    }
}