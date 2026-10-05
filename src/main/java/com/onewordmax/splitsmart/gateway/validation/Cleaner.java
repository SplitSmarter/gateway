package com.onewordmax.splitsmart.gateway.validation;

public interface Cleaner<T> {
    T clean(T request);
}