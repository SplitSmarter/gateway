package com.onewordmax.splitsmart.gateway.validation;

import com.onewordmax.splitsmart.gateway.dto.ValidationError;

import java.util.List;

public interface Validator<T> {
    List<ValidationError> validate(T target);
}
