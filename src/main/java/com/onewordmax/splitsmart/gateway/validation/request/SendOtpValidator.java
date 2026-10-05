package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.SendOtpRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class SendOtpValidator implements Validator<SendOtpRequest> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexPattern.EMAIL);
    private static final Pattern NAME_PATTERN = Pattern.compile(RegexPattern.NAME);

    @Override
    public List<ValidationError> validate(SendOtpRequest req) {
        List<ValidationError> errors = new ArrayList<>();

        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        // 1. Email Validation
        ValidatorUtil.checkPattern(
                req.email(),
                EMAIL_PATTERN,
                "email",
                "user.input.email.invalid",
                "Invalid email format",
                errors
        );

        return errors;
    }
}