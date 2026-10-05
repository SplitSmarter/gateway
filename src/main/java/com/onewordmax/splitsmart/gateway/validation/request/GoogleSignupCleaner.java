package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.GoogleSignupRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class GoogleSignupCleaner implements Cleaner<GoogleSignupRequest> {

    @Override
    public GoogleSignupRequest clean(GoogleSignupRequest req) {
        if (req == null) return null;

        return new GoogleSignupRequest(
                // Trim idToken
                req.idToken() != null ? req.idToken().trim() : null,
                trimAndNormalize(req.name(), Normalizer.Form.NFC),      // Names: NFC
                ValidatorUtil.normalize(req.password(), Normalizer.Form.NFKC), // Password: NFKC (Don't trim password!)
                req.city() != null ? req.city().toLowerCase().trim() : null,
                req.country() != null ? req.country().toUpperCase().trim() : null,
                req.currency() != null ? req.currency().toUpperCase().trim() : null,
                req.language() != null ? req.language().toLowerCase().trim() : null
        );
    }
    
    private String trimAndNormalize(String input, Normalizer.Form form) {
        if (input == null) return null;
        String trimmed = input.trim();
        return (form != null) ? ValidatorUtil.normalize(trimmed, form) : trimmed;
    }
}