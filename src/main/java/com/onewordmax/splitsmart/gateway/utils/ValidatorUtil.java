package com.onewordmax.splitsmart.gateway.utils;

import com.onewordmax.splitsmart.gateway.constants.ErrorCode;
import com.onewordmax.splitsmart.gateway.dto.ValidationError;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;

@Slf4j
public class ValidatorUtil {

    private static final Set<String> ISO_COUNTRIES = Set.of(Locale.getISOCountries());
    private static final Set<String> ISO_LANGUAGES = Set.of(Locale.getISOLanguages());

    public static String normalize(String input, Normalizer.Form form) {
        return (input == null) ? null : Normalizer.normalize(input, form);
    }

    public static void checkPattern(String value, Pattern pattern, String field, String errorCode, String msg, List<ValidationError> errors) {
        if (value == null || value.isBlank()) {
            log.debug(field + " value is null or blank");
            errors.add(new ValidationError(field, errorCode, "Field is required"));
        } else if (!pattern.matcher(value).matches()) {
            log.debug(field + " does not matches the regex pattern " + pattern.pattern() + " value=" + value);
            errors.add(new ValidationError(field, errorCode, msg));
        }
    }

    public static void validateCountry(String code, List<ValidationError> errors) {
        if (code == null || !ISO_COUNTRIES.contains(code)) {
            log.debug(code + " is not a valid country code");
            errors.add(new ValidationError("country", ErrorCode.FIELD_COUNTRY_INVALID, ErrorCode.FIELD_COUNTRY_INVALID));
        }
    }

    public static void validateLanguage(String code, List<ValidationError> errors) {
        if (code == null || !ISO_LANGUAGES.contains(code.toLowerCase())) {
            log.debug(code + " is not a valid language");
            errors.add(new ValidationError("language", ErrorCode.FIELD_LANGUAGE_INVALID, ErrorCode.FIELD_LANGUAGE_INVALID));
        }
    }

    public static void validateCurrency(String code, List<ValidationError> errors) {
        try {
            if (code == null) {
                log.debug(code + " is not a valid currency code");
                throw new Exception();
            }
            Currency.getInstance(code);
        } catch (Exception e) {
            errors.add(new ValidationError("currency", ErrorCode.FIELD_CURRENCY_INVALID, ErrorCode.FIELD_CURRENCY_INVALID));
        }
    }

    public static void validateUserType(String type, List<ValidationError> errors) {
        if (type == null || type.isBlank()) {
            log.debug("User type is null or blank");
            errors.add(new ValidationError("user_type", ErrorCode.FIELD_USER_TYPE_EMPTY, ErrorCode.FIELD_USER_TYPE_EMPTY));
            return;
        }

        boolean isValid = Arrays.stream(com.onewordmax.splitsmart.gateway.constants.UserType.values())
                .anyMatch(enumType -> enumType.name().equalsIgnoreCase(type));

        if (!isValid) {
            log.debug(type + " is not a valid user type");
            errors.add(new ValidationError("user_type", ErrorCode.FIELD_CURRENCY_INVALID, ErrorCode.FIELD_USER_TYPE_INVALID));
        }
    }
}