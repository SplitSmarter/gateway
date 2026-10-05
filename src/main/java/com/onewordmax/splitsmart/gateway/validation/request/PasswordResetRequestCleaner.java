package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.PasswordResetRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;
import java.text.Normalizer;

@Component
public class PasswordResetRequestCleaner implements Cleaner<PasswordResetRequest> {
    @Override
    public PasswordResetRequest clean(PasswordResetRequest req) {
        if (req == null) return null;

        String cleanEmail = req.email() != null ? req.email().trim().toLowerCase() : null;
        return new PasswordResetRequest(ValidatorUtil.normalize(cleanEmail, Normalizer.Form.NFC));
    }
}