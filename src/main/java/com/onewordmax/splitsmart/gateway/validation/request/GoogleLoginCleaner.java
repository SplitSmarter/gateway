package com.onewordmax.splitsmart.gateway.validation.request;

import com.onewordmax.splitsmart.gateway.dto.auth.request.GoogleLoginRequest;
import com.onewordmax.splitsmart.gateway.validation.Cleaner;
import org.springframework.stereotype.Component;

@Component
public class GoogleLoginCleaner implements Cleaner<GoogleLoginRequest> {

    @Override
    public GoogleLoginRequest clean(GoogleLoginRequest req) {
        if (req == null) return null;

        return new GoogleLoginRequest(
                // Only trim surrounding whitespace.
                // DO NOT normalize or change case, as it breaks the JWT signature.
                req.idToken() != null ? req.idToken().trim() : null
        );
    }
}