package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialSignupRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class CredentialSignupValidator implements Validator<CredentialSignupRequest> {

    private static final Pattern NAME = Pattern.compile(RegexPattern.NAME);
    private static final Pattern EMAIL = Pattern.compile(RegexPattern.EMAIL);
    private static final Pattern PASSWORD = Pattern.compile(RegexPattern.PASSWORD);

    @Override
    public List<ValidationError> validate(CredentialSignupRequest req) {
        List<ValidationError> errors = new ArrayList<>();

        if (req == null) {
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        ValidatorUtil.checkPattern(ValidatorUtil.normalize(req.name(), Normalizer.Form.NFC),
                NAME, "name", "user.input.name.invalid", ErrorCode.FIELD_NAME_INVALID, errors);

        if (req.email() != null && !req.email().isBlank()) {
            ValidatorUtil.checkPattern(req.email(), EMAIL, "email", ErrorCode.FIELD_EMAIL_INVALID, ErrorCode.FIELD_EMAIL_INVALID, errors);
        }

        String normPassword = ValidatorUtil.normalize(req.password(), Normalizer.Form.NFKC);
        ValidatorUtil.checkPattern(normPassword, PASSWORD, "password", ErrorCode.FIELD_PASSWORD_INVALID, ErrorCode.FIELD_PASSWORD_INVALID, errors);

        ValidatorUtil.validateCountry(req.country(), errors);
        // TODO: Add city validation
        ValidatorUtil.validateLanguage(req.language(), errors);
        ValidatorUtil.validateCurrency(req.currency(), errors);

        ValidatorUtil.validateUserType(req.user_type(), errors);

        return errors;
    }
}