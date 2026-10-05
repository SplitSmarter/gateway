package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.GoogleLoginRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class GoogleLoginValidator implements Validator<GoogleLoginRequest> {

    private static final Pattern JWT_PATTERN = Pattern.compile(RegexPattern.JWT);

    @Override
    public List<ValidationError> validate(GoogleLoginRequest req) {
        List<ValidationError> errors = new ArrayList<>();

        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        // Check if token exists and matches JWT structure
        ValidatorUtil.checkPattern(
                req.idToken(),
                JWT_PATTERN,
                "idToken",
                "id_token_invalid",
                "Invalid Google ID Token format",
                errors
        );

        return errors;
    }
}