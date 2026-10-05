package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.SendOtpRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class SendOtpCleaner implements Cleaner<SendOtpRequest> {

    @Override
    public SendOtpRequest clean(SendOtpRequest req) {
        if (req == null) return null;

        return new SendOtpRequest(
                // Email: Trimmed and standardized to lowercase
                req.email() != null ? req.email().trim().toLowerCase() : null
        );
    }
}