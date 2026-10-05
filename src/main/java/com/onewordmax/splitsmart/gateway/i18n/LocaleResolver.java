package com.onewordmax.splitsmart.gateway.i18n;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Locale;

@Slf4j
@Component
public class LocaleResolver {

    public Locale resolve(ServerWebExchange exchange) {
        String acceptLanguage = exchange.getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.ACCEPT_LANGUAGE);

        log.debug("Provided language is {}", acceptLanguage);

        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return Locale.ENGLISH;
        }

        final List<Locale.LanguageRange> ranges;
        try {
            ranges = Locale.LanguageRange.parse(acceptLanguage);
        } catch (IllegalArgumentException ex) {
            return Locale.ENGLISH;
        }

        Locale resolved = Locale.lookup(
                ranges,
                List.of(
                        Locale.ENGLISH,
                        Locale.forLanguageTag("hi"),
                        Locale.SIMPLIFIED_CHINESE
                )
        );
        return resolved != null ? resolved : Locale.ENGLISH;
    }
}
