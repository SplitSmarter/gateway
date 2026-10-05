package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.VerifyOtpRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class VerifyOtpValidator implements Validator<VerifyOtpRequest> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(RegexPattern.EMAIL);

    // Pattern for exactly 6 digits
    private static final Pattern OTP_PATTERN = Pattern.compile("^[0-9]{6}$");

    @Override
    public List<ValidationError> validate(VerifyOtpRequest req) {
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

        // 2. OTP Code Validation
        ValidatorUtil.checkPattern(
                req.otpCode(),
                OTP_PATTERN,
                "otpCode",
                "otp_invalid",
                "OTP must be a 6-digit number",
                errors
        );

        return errors;
    }
}