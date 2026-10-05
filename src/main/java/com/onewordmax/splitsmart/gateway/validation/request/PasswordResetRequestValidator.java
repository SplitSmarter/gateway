package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.PasswordResetRequest;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PasswordResetRequestValidator implements Validator<PasswordResetRequest> {
    private static final Pattern EMAIL = Pattern.compile(RegexPattern.EMAIL);

    @Override
    public List<ValidationError> validate(PasswordResetRequest req) {
        List<ValidationError> errors = new ArrayList<>();
        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        if (req.email() == null || req.email().isBlank()) {
            errors.add(new ValidationError("email", ErrorCode.FIELD_EMAIL_EMPTY, ErrorCode.FIELD_EMAIL_EMPTY));
        } else if (!EMAIL.matcher(req.email()).matches()) {
            errors.add(new ValidationError("email", ErrorCode.FIELD_EMAIL_INVALID, ErrorCode.FIELD_EMAIL_INVALID));
        }
        return errors;
    }
}