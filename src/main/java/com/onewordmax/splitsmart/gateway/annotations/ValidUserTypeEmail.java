package com.onewordmax.splitsmart.gateway.annotations;

import com.onewordmax.splitsmart.gateway.validation.UserTypeEmailValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UserTypeEmailValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserTypeEmail {
    String message() default "Email is required when user_type is 'user'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
