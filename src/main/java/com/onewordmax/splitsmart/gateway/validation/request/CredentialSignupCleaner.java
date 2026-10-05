package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialSignupRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class CredentialSignupCleaner implements Cleaner<CredentialSignupRequest> {

    @Override
    public CredentialSignupRequest clean(CredentialSignupRequest req) {
        if (req == null) return null;
        // Create a new record with cleaned values
        return new CredentialSignupRequest(
                trimAndNormalize(req.name(), Normalizer.Form.NFC),      // Names: NFC
                req.email().trim().toLowerCase(),                   // Email: Just trim
                ValidatorUtil.normalize(req.password(), Normalizer.Form.NFKC), // Password: NFKC (Don't trim password!)
                req.city() != null ? req.city().toLowerCase().trim() : null,
                req.country() != null ? req.country().toUpperCase().trim() : null,
                req.currency() != null ? req.currency().toUpperCase().trim() : null,
                req.language() != null ? req.language().toLowerCase().trim() : null,
                req.user_type().toUpperCase()
        );
    }

    private String trimAndNormalize(String input, Normalizer.Form form) {
        if (input == null) return null;
        String trimmed = input.trim();
        return (form != null) ? ValidatorUtil.normalize(trimmed, form) : trimmed;
    }
}