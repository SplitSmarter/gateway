package com.onewordmax.splitsmart.gateway.validation;

import com.onewordmax.splitsmart.gateway.annotations.ValidUserTypeEmail;
import com.onewordmax.splitsmart.gateway.constants.UserType;
import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialSignupRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UserTypeEmailValidator implements ConstraintValidator<ValidUserTypeEmail, CredentialSignupRequest> {

    @Override
    public boolean isValid(CredentialSignupRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true; // handled elsewhere
        }

        if (!UserType.GUEST.toString().equalsIgnoreCase(request.user_type())) {
            return request.email() != null && !request.email().trim().isEmpty();
        }

        return true;
    }
}
