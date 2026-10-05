package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.CredentialLoginRequest;
import com.onewordmax.splitsmart.gateway.utils.ValidatorUtil;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.Normalizer;

@Component
public class CredentialLoginCleaner implements Cleaner<CredentialLoginRequest> {

    @Override
    public CredentialLoginRequest clean(CredentialLoginRequest req) {
        if (req == null) return null;

        String cleanId = req.emailOrUsername() != null ? req.emailOrUsername().trim().toLowerCase() : null;

        return new CredentialLoginRequest(
                // Trim identifier (email/username) and use NFC
                ValidatorUtil.normalize(cleanId, Normalizer.Form.NFC),
                // Normalize Password to NFKC (DO NOT TRIM passwords, spaces can be intentional)
                ValidatorUtil.normalize(req.password(), Normalizer.Form.NFKC)
        );
    }
}