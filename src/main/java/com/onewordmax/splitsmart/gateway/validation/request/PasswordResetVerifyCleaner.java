package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.PasswordResetVerifyRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;
import java.text.Normalizer;

@Component
public class PasswordResetVerifyCleaner implements Cleaner<PasswordResetVerifyRequest> {
    @Override
    public PasswordResetVerifyRequest clean(PasswordResetVerifyRequest req) {
        if (req == null) return null;

        return new PasswordResetVerifyRequest(
                // Token is a UUID, trim it but keep NFC
                ValidatorUtil.normalize(req.token() != null ? req.token().trim() : null, Normalizer.Form.NFC),
                // Password normalization (no trim)
                ValidatorUtil.normalize(req.newPassword(), Normalizer.Form.NFKC)
        );
    }
}