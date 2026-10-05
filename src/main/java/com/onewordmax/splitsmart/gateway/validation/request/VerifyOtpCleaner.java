package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.VerifyOtpRequest;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;

@Component
public class VerifyOtpCleaner implements Cleaner<VerifyOtpRequest> {

    @Override
    public VerifyOtpRequest clean(VerifyOtpRequest req) {
        if (req == null) return null;

        return new VerifyOtpRequest(
                req.email() != null ? req.email().trim().toLowerCase() : null,
                req.otpCode()
        );
    }
}
