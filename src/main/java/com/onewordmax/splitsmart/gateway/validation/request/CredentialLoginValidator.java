package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.constants.RegexPattern;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialLoginRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CredentialLoginValidator implements Validator<CredentialLoginRequest> {

    private static final Pattern EMAIL = Pattern.compile(RegexPattern.EMAIL);
    private static final Pattern USERNAME = Pattern.compile(RegexPattern.USERNAME);
    private static final Pattern PASSWORD = Pattern.compile(RegexPattern.PASSWORD);

    @Override
    public List<ValidationError> validate(CredentialLoginRequest req) {
        List<ValidationError> errors = new ArrayList<>();

        if (req == null) {
            log.debug("Request is null");
            errors.add(new ValidationError("request", ErrorCode.REQUEST_EMPTY, ErrorCode.REQUEST_EMPTY));
            return errors;
        }

        // 1. Email or Username (NFC)
        String identifier = ValidatorUtil.normalize(req.emailOrUsername(), Normalizer.Form.NFC);
        if (identifier == null || identifier.isBlank()) {
            log.debug("Email or username is null or blank" + identifier);
            errors.add(new ValidationError("emailOrUsername", ErrorCode.FIELD_USERNAME_EMAIL_EMPTY, ErrorCode.FIELD_USERNAME_EMAIL_EMPTY));
        } else if (!EMAIL.matcher(identifier).matches() && !USERNAME.matcher(identifier).matches()) {
            log.debug("Email or username is invalid");
            errors.add(new ValidationError("emailOrUsername", ErrorCode.FIELD_USERNAME_EMAIL_INVALID, ErrorCode.FIELD_USERNAME_EMAIL_INVALID));
        }

        // 2. Password (NFKC)
        String normPassword = ValidatorUtil.normalize(req.password(), Normalizer.Form.NFKC);
        ValidatorUtil.checkPattern(normPassword, PASSWORD, "password", ErrorCode.FIELD_PASSWORD_INVALID, ErrorCode.FIELD_PASSWORD_INVALID, errors);

        return errors;
    }
}