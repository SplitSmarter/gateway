package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.ResendOtpRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class ResendOtpCleaner implements Cleaner<ResendOtpRequest> {

    @Override
    public ResendOtpRequest clean(ResendOtpRequest req) {
        if (req == null) return null;

        return new ResendOtpRequest(
                // Email: Trim and Lowercase for consistency
                req.email() != null ? req.email().trim().toLowerCase() : null
        );
    }
}