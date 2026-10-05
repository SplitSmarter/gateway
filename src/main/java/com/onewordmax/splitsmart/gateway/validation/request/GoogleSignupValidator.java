package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.GoogleSignupRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


@Component
public class GoogleSignupValidator implements Validator<GoogleSignupRequest> {

    private static final Pattern NAME = Pattern.compile(RegexPattern.NAME);
    private static final Pattern JWT_PATTERN = Pattern.compile(RegexPattern.JWT);

    @Override
    public List<ValidationError> validate(GoogleSignupRequest req) {
        List<ValidationError> errors = new ArrayList<>();

        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        ValidatorUtil.checkPattern(ValidatorUtil.normalize(req.name(), Normalizer.Form.NFC),
                NAME, "name", "user.input.name.invalid", ErrorCode.FIELD_NAME_INVALID, errors);


        // 2. Validate Google ID Token (Structural check)
        ValidatorUtil.checkPattern(
                req.idToken(),
                JWT_PATTERN,
                "idToken",
                ErrorCode.FIELD_GOOGLE_ID_TOKEN_INVALID,
                ErrorCode.FIELD_GOOGLE_ID_TOKEN_INVALID,
                errors
        );

        ValidatorUtil.validateCountry(req.country(), errors);
        // TODO: Add city validation
        ValidatorUtil.validateLanguage(req.language(), errors);
        ValidatorUtil.validateCurrency(req.currency(), errors);

        return errors;
    }
}