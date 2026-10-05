package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.ResendOtpRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ResendOtpValidator implements Validator<ResendOtpRequest> {

    private static final Pattern NAME_PATTERN = Pattern.compile(RegexPattern.NAME);
    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexPattern.EMAIL);

    @Override
    public List<ValidationError> validate(ResendOtpRequest req) {
        List<ValidationError> errors = new ArrayList<>();

        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }


        // 2. Validate Email
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